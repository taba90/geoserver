/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.opengis.ows20.AcceptLanguagesType;
import org.eclipse.emf.common.util.EList;
import org.geoserver.catalog.KeywordInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.config.ServiceInfo;
import org.geotools.styling.Description;
import org.geotools.styling.Style;
import org.geotools.util.GrowableInternationalString;
import org.opengis.util.InternationalString;

/**
 * Helper class that allows to retrieve a String value out of a GrowableInternationalString
 * according to the list of requestedLocales. It is meant to help handle internationalization in
 * GetCapabilities response.
 */
public class InternationalContentHelper {

    private Set<Locale> requestedLocales = new HashSet<>();

    // field that map the AcceptLanguages value *
    protected boolean anyMatch = false;

    // supported locales are those used at least in one field of
    // an element that support i18n content
    private Set<Locale> supportedLocales;

    private static final String ERROR_MESSAGE = "DID NOT FIND i18n CONTENT FOR THIS ELEMENT";

    public static final String ACCEPTLANGUAGES_PARAM = "ACCEPTLANGUAGES";

    public InternationalContentHelper(
            String[] acceptLanguages,
            ServiceInfo serviceInfo,
            List<LayerInfo> layers,
            List<LayerGroupInfo> groups) {
        setSupportedLocales(serviceInfo, layers, groups);
        setRequestedLocales(acceptLanguages);
        verify();
    }

    public InternationalContentHelper(
            String[] acceptLanguages, ServiceInfo serviceInfo, List<ResourceInfo> resources) {
        setSupportedLocales(serviceInfo, resources);
        setRequestedLocales(acceptLanguages);
        verify();
    }

    public InternationalContentHelper(
            AcceptLanguagesType acceptLanguagesType,
            ServiceInfo serviceInfo,
            List<ResourceInfo> resources) {
        setSupportedLocales(serviceInfo, resources);
        setRequestedLocales(acceptLanguagesType);
        verify();
    }

    private void setRequestedLocales(AcceptLanguagesType acceptLanguagesType) {
        if (acceptLanguagesType != null) {
            EList<String> acceptLanguages = acceptLanguagesType.getLanguage();
            String[] languagesAr = new String[acceptLanguages.size()];
            acceptLanguages.toArray(languagesAr);
            setRequestedLocales(languagesAr);
        }
    }

    private void setRequestedLocales(String[] preferredLanguages) {
        List<String> withoutVariant = new ArrayList<>();
        if (preferredLanguages != null && preferredLanguages.length > 0) {
            for (String language : preferredLanguages) {
                Locale locale = null;
                if (language.equals("*")) {
                    this.anyMatch = true;
                    locale = Locale.getDefault();
                } else if (language.contains("-")) locale = Locale.forLanguageTag(language);
                else withoutVariant.add(language);
                if (locale != null) requestedLocales.add(locale);
            }
        }
        for (String language : withoutVariant) {
            requestedLocales.addAll(
                    supportedLocales
                            .stream()
                            .filter(l -> l != null && l.getLanguage().equals(language))
                            .collect(Collectors.toSet()));
            requestedLocales.add(Locale.forLanguageTag(language));
        }
    }

    /**
     * @param info the ResourceInfo from which retrieve the internationalTitle value
     * @return the international title found according to the list of requested locales
     */
    public String getTitle(ResourceInfo info) {
        InternationalString internationalString = info.getInternationalTitle();
        return getString(internationalString, false);
    }

    /**
     * @param info the ResourceInfo from which retrieve the internationalAbstract value
     * @return the international abstract found according to the list of requested locales
     */
    public String getAbstract(ResourceInfo info) {
        InternationalString internationalString = info.getInternationalAbstract();
        return getString(internationalString, true);
    }

    /**
     * @param info the PublishedInfo from which retrieve the internationalTitle value
     * @return the international title found according to the list of requested locales
     */
    public String getTitle(PublishedInfo info) {
        InternationalString internationalString = info.getInternationalTitle();
        return getString(internationalString, false);
    }

    /**
     * @param info the PublishedInfo from which retrieve the internationalAbstract value
     * @return the international abstract found according to the list of requested locales
     */
    public String getAbstract(PublishedInfo info) {
        InternationalString internationalString = info.getInternationalAbstract();
        return getString(internationalString, true);
    }

    /**
     * @param serviceInfo the ServiceInfo from which retrieve the internationalTitle value
     * @return the international title found according to the list of requested locales
     */
    public String getTitle(ServiceInfo serviceInfo) {
        InternationalString internationalString = serviceInfo.getInternationalTitle();
        return getString(internationalString, false);
    }

    /**
     * @param serviceInfo the ServiceInfo from which retrieve the internationalAbstract value
     * @return the international abstract found according to the list of requested locales
     */
    public String getAbstract(ServiceInfo serviceInfo) {
        InternationalString internationalString = serviceInfo.getInternationalAbstract();
        return getString(internationalString, true);
    }

    /**
     * @param style the Style from which retrieve the internationalTitle value
     * @return the international title found according to the list of requested locales
     */
    public String getTitle(Style style) {
        Description description = style.getDescription();
        if (description != null) {
            InternationalString internationalString = description.getTitle();
            return getString(internationalString, true);
        }
        return null;
    }

    /**
     * @param style the Style from which retrieve the internationalAbstract value
     * @return the international abstract found according to the list of requested locales
     */
    public String getAbstract(Style style) {
        Description description = style.getDescription();
        if (description != null) {
            InternationalString internationalString = description.getAbstract();
            return getString(internationalString, true);
        }
        return null;
    }

    /**
     * Filter a list of KeywordInfo object according to the requested locales
     *
     * @param original the list of KeywordInfo
     * @return a list of KeywordInfo containing only the elements that matches the requested locales
     */
    public List<KeywordInfo> filterKeywords(List<KeywordInfo> original) {
        List<KeywordInfo> filtered =
                original.stream().filter(new KeywordMatch()).collect(Collectors.toList());
        if (filtered.isEmpty() && anyMatch) return original;
        return filtered;
    }

    private String getString(InternationalString internationalString, boolean nullable) {
        String result = null;
        if (internationalString instanceof GrowableInternationalString) {
            GrowableInternationalString growable =
                    (GrowableInternationalString) internationalString;
            result = getFirstMatchingInternationalValue(growable, requestedLocales);
            // if client specified * try with the supported locales
            if (result == null && anyMatch) {
                result = getFirstMatchingInternationalValue(growable, supportedLocales);
            }
        }
        if (result == null && !nullable) result = ERROR_MESSAGE;
        return result;
    }

    private String getFirstMatchingInternationalValue(
            GrowableInternationalString growable, Set<Locale> locales) {
        String result = null;
        if (locales != null && !locales.isEmpty()) {
            Set<Locale> growableLocales = growable.getLocales();
            for (Locale l : locales) {
                if (!growableLocales.contains(l)) continue;
                result = growable.toString(l);
                if (result != null) break;
            }
        }
        return result;
    }

    private void setSupportedLocales(ServiceInfo info, List<ResourceInfo> resourceInfos) {
        Set<Locale> candidates = new HashSet<>();
        candidates.addAll(getLocales(info.getInternationalTitle()));
        candidates.addAll(getLocales(info.getInternationalAbstract()));
        for (ResourceInfo resourceInfo : resourceInfos) {
            candidates.addAll(getLocales(resourceInfo.getInternationalTitle()));
            candidates.addAll(getLocales(resourceInfo.getInternationalAbstract()));
        }
        this.supportedLocales = candidates;
    }

    private void setSupportedLocales(
            ServiceInfo info, List<LayerInfo> layerInfos, List<LayerGroupInfo> groups) {
        Set<Locale> candidates = new HashSet<>();
        candidates.addAll(getLocales(info.getInternationalTitle()));
        candidates.addAll(getLocales(info.getInternationalAbstract()));
        setSupportedLocalesFromGroups(candidates, groups);
        setSupportedLocalesFromLayers(candidates, layerInfos);
        this.supportedLocales = candidates;
    }

    private void setSupportedLocalesFromGroups(
            Set<Locale> candidates, List<LayerGroupInfo> groups) {
        for (LayerGroupInfo groupInfo : groups) {
            candidates.addAll(getLocales(groupInfo.getInternationalTitle()));
            candidates.addAll(getLocales(groupInfo.getInternationalAbstract()));
            List<StyleInfo> styles = new ArrayList<>(groupInfo.getStyles());
            setSupportedLocalesFromStyles(candidates, styles);
        }
    }

    private void setSupportedLocalesFromLayers(Set<Locale> candidates, List<LayerInfo> layers) {
        for (LayerInfo layerInfo : layers) {
            candidates.addAll(getLocales(layerInfo.getInternationalTitle()));
            candidates.addAll(getLocales(layerInfo.getInternationalAbstract()));
            List<StyleInfo> styles = new ArrayList<>(layerInfo.getStyles());
            styles.add(layerInfo.getDefaultStyle());
            setSupportedLocalesFromStyles(candidates, styles);
        }
    }

    private void setSupportedLocalesFromStyles(Set<Locale> candidates, List<StyleInfo> styles) {
        for (StyleInfo styleInfo : styles) {
            if (styleInfo != null) {
                try {
                    Description description = styleInfo.getStyle().getDescription();
                    if (description != null) {
                        candidates.addAll(getLocales(description.getTitle()));
                        candidates.addAll(getLocales(description.getAbstract()));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Set<Locale> getLocales(InternationalString internationalString) {
        Set<Locale> found;
        if (internationalString instanceof GrowableInternationalString) {
            GrowableInternationalString growable =
                    (GrowableInternationalString) internationalString;
            found =
                    growable.getLocales()
                            .stream()
                            .filter(l -> l != null)
                            .collect(Collectors.toSet());
        } else {
            found = Collections.emptySet();
        }
        return found;
    }

    public void verify() {
        if (requestedLocales != null && !requestedLocales.isEmpty()) {
            String requested =
                    requestedLocales
                            .stream()
                            .map(l -> l.toLanguageTag())
                            .collect(Collectors.joining(","));
            if (supportedLocales == null || supportedLocales.isEmpty()) {
                throw new UnsupportedOperationException(
                        "Content has been requested in one of the following languages: "
                                + requested
                                + ". But there is no international content defined");
            } else {
                if (anyMatch) return;
                String supported =
                        supportedLocales
                                .stream()
                                .filter(l -> l != null)
                                .map(l -> l.toLanguageTag())
                                .collect(Collectors.joining(","));
                for (Locale locale : requestedLocales) {
                    if (supportedLocales.contains(locale)) return;
                }
                throw new UnsupportedOperationException(
                        "Content has been requested in one of the following languages: "
                                + requested
                                + ". But supported languages are: "
                                + supported);
            }
        }
    }

    public Set<Locale> getSupportedLocales() {
        return this.supportedLocales;
    }

    private class KeywordMatch implements Predicate<KeywordInfo> {

        @Override
        public boolean test(KeywordInfo keywordInfo) {
            String language = keywordInfo.getLanguage();
            for (Locale l : requestedLocales) {
                if (l != null && l.getLanguage().equals(language)) return true;
            }
            return false;
        }
    }
}
