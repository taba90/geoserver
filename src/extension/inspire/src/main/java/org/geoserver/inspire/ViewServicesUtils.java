/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.inspire;

import static org.geoserver.inspire.InspireSchema.COMMON_NAMESPACE;
import static org.geoserver.inspire.InspireSchema.VS_NAMESPACE;

import org.geoserver.ExtendedCapabilitiesProvider;
import org.geoserver.ExtendedCapabilitiesProvider.Translator;
import org.geoserver.ows.Dispatcher;
import org.xml.sax.helpers.NamespaceSupport;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ViewServicesUtils {

    public static final String DEFAULT_SUFFIX="(default)";

    private ViewServicesUtils() {}

    public static void registerNameSpaces(NamespaceSupport namespaces) {
        namespaces.declarePrefix("inspire_vs", VS_NAMESPACE);
        namespaces.declarePrefix("inspire_common", COMMON_NAMESPACE);
    }

    public static void addScenario1Elements(
            Translator translator, String metadataUrl, String mediaType, String languages) {

        translator.start("inspire_vs:ExtendedCapabilities");
        translator.start("inspire_common:MetadataUrl");
        translator.start("inspire_common:URL");
        translator.chars(metadataUrl);
        translator.end("inspire_common:URL");
        if (mediaType != null) {
            translator.start("inspire_common:MediaType");
            translator.chars(mediaType);
            translator.end("inspire_common:MediaType");
        }
        translator.end("inspire_common:MetadataUrl");
        encodeSupportedLanguages(languages, translator);
        translator.end("inspire_vs:ExtendedCapabilities");
    }

    private static String getDefaultLang(String languages, List<String> langList){
        String defaultLang;
        if (languages.contains(DEFAULT_SUFFIX))
            defaultLang=langList.stream().filter(l->l.contains(DEFAULT_SUFFIX)).map(l->l.replace(DEFAULT_SUFFIX,"")).findFirst().get();
        else
            defaultLang="eng";
        return defaultLang;
    }

    public static void encodeSupportedLanguages(String languages, ExtendedCapabilitiesProvider.Translator translator){
        languages = languages != null ? languages : "eng";
        List<String> langList= Arrays.asList(languages.split(","));
        String defaultLanguage= getDefaultLang(languages,langList);
        // encode the default language
        translator.start("inspire_common:SupportedLanguages");
        translator.start("inspire_common:DefaultLanguage");
        translator.start("inspire_common:Language");
        translator.chars(defaultLanguage);
        translator.end("inspire_common:Language");
        translator.end("inspire_common:DefaultLanguage");
        for (String lang:langList) {
            if (lang.contains(DEFAULT_SUFFIX)) lang=lang.replace(DEFAULT_SUFFIX,"");
            if (lang.equals(defaultLanguage))
                continue;
            translator.start("inspire_common:SupportedLanguage");
            translator.chars(lang);
            translator.end("inspire_common:SupportedLanguage");
        }
        translator.end("inspire_common:SupportedLanguages");
        // encode supported languages

        translator.start("inspire_common:ResponseLanguage");
        translator.start("inspire_common:Language");
        translator.chars(retrieveLanguageParameter(defaultLanguage,langList));
        translator.end("inspire_common:Language");
        translator.end("inspire_common:ResponseLanguage");
    }

    private static String retrieveLanguageParameter(String defaultLanguage, List<String> languages){
        Map<String,Object> kvpDispatcher=Dispatcher.REQUEST.get().getRawKvp();
        String value=null;
        if(kvpDispatcher!=null){
            if (kvpDispatcher.containsKey("LANGUAGE")){
                Object reqLang=kvpDispatcher.get("LANGIAGE");
                if (reqLang!=null && languages.contains(reqLang.toString()))
                    value= reqLang.toString();
            }
        }
        if (value==null) value=defaultLanguage;
        return value;
    }
}
