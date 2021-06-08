/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.configuration;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.eclipse.emf.common.util.URI;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.featurestemplating.builders.visitors.SimplifiedPropertyReplacer;
import org.geoserver.featurestemplating.readers.TemplateReaderConfiguration;
import org.geoserver.featurestemplating.validation.TemplateValidator;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.Request;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geotools.data.complex.AppSchemaDataAccessRegistry;
import org.geotools.data.complex.DataAccessRegistry;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.feature.type.ComplexFeatureTypeImpl;
import org.geotools.data.complex.feature.type.Types;
import org.opengis.feature.type.FeatureType;
import org.xml.sax.helpers.NamespaceSupport;

/** Manage the cache and the retrieving for all templates files */
public class TemplateLoader {

    private final LoadingCache<CacheKey, Template> templateCache;
    private GeoServerDataDirectory dataDirectory;

    public TemplateLoader(GeoServerDataDirectory dd) {
        this.dataDirectory = dd;
        templateCache =
                CacheBuilder.newBuilder()
                        .maximumSize(100)
                        .initialCapacity(1)
                        .expireAfterAccess(120, TimeUnit.MINUTES)
                        .build(
                                new CacheLoader<CacheKey, Template>() {
                                    @Override
                                    public Template load(CacheKey key) {
                                        NamespaceSupport namespaces = null;
                                        try {
                                            FeatureType type = key.getResource().getFeatureType();
                                            namespaces = declareNamespaces(type);
                                        } catch (IOException e) {
                                            throw new RuntimeException(
                                                    "Error retrieving FeatureType "
                                                            + key.getResource().getName()
                                                            + "Exception is: "
                                                            + e.getMessage());
                                        }
                                        TemplateInfo templateInfo =
                                                TemplateInfoDao.get().findById(key.getPath());
                                        Resource resource;
                                        if (templateInfo != null)
                                            resource =
                                                    getTemplateFileManager()
                                                            .getTemplateResource(templateInfo);
                                        else
                                            resource =
                                                    getDataDirectory()
                                                            .get(key.getResource(), key.getPath());
                                        Template template =
                                                new Template(
                                                        resource,
                                                        new TemplateReaderConfiguration(
                                                                namespaces));
                                        RootBuilder builder = template.getRootBuilder();
                                        if (builder != null) {
                                            replaceSimplifiedPropertiesIfNeeded(
                                                    key.getResource(), builder);
                                        }
                                        return template;
                                    }
                                });
    }

    /**
     * Get the template related to the featureType. If template has been modified updates the cache
     * with the new Template
     */
    public RootBuilder getTemplate(FeatureTypeInfo typeInfo, String outputFormat)
            throws ExecutionException {
        String templateIdentifier = getTemplateIdentifierByLayerRuleEvaluation(typeInfo);
        if (templateIdentifier == null)
            templateIdentifier =
                    TemplateIdentifier.getTemplateIdentifierFromOutputFormat(outputFormat)
                            .getFilename();
        CacheKey key = new CacheKey(typeInfo, templateIdentifier);
        Template template = templateCache.get(key);
        boolean updateCache = false;
        if (template.checkTemplate()) updateCache = true;

        RootBuilder root = template.getRootBuilder();
        // check if reload is needed anyway and eventually reload the template
        if (root != null && root.needsReload()) {
            template.reloadTemplate();
            updateCache = true;
            root = template.getRootBuilder();
        }

        if (updateCache) {
            replaceSimplifiedPropertiesIfNeeded(key.getResource(), template.getRootBuilder());
            templateCache.put(key, template);
        }

        if (root != null) {
            TemplateValidator validator = new TemplateValidator(typeInfo);
            boolean isValid = validator.validateTemplate(root);
            if (!isValid) {
                throw new RuntimeException(
                        "Failed to validate template for feature type "
                                + typeInfo.getName()
                                + ". Failing attribute is "
                                + URI.decode(validator.getFailingAttribute()));
            }
        }
        return root;
    }

    /**
     * Extract Namespaces from given FeatureType
     *
     * @return Namespaces if found for the given FeatureType
     */
    private NamespaceSupport declareNamespaces(FeatureType type) {
        NamespaceSupport namespaceSupport = null;
        if (type instanceof ComplexFeatureTypeImpl) {
            Map namespaces = (Map) type.getUserData().get(Types.DECLARED_NAMESPACES_MAP);
            if (namespaces != null) {
                namespaceSupport = new NamespaceSupport();
                for (Iterator it = namespaces.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String prefix = (String) entry.getKey();
                    String namespace = (String) entry.getValue();
                    namespaceSupport.declarePrefix(prefix, namespace);
                }
            }
        }
        return namespaceSupport;
    }

    GeoServerDataDirectory getDataDirectory() {
        return dataDirectory;
    }

    private class CacheKey {
        private FeatureTypeInfo resource;
        private String path;

        public CacheKey(FeatureTypeInfo resource, String path) {
            this.resource = resource;
            this.path = path;
        }

        public FeatureTypeInfo getResource() {
            return resource;
        }

        public String getPath() {
            return path;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CacheKey)) return false;
            CacheKey other = (CacheKey) o;
            if (!other.getPath().equals(path)) return false;
            else if (!(other.getResource().getName().equals(resource.getName()))) return false;
            else if (!(other.getResource().getNamespace().equals(resource.getNamespace())))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(resource, path);
        }
    }

    private void replaceSimplifiedPropertiesIfNeeded(
            FeatureTypeInfo featureTypeInfo, RootBuilder rootBuilder) {
        try {
            if (featureTypeInfo.getFeatureType() instanceof ComplexFeatureTypeImpl
                    && rootBuilder != null) {

                DataAccessRegistry registry = AppSchemaDataAccessRegistry.getInstance();
                FeatureTypeMapping featureTypeMapping =
                        registry.mappingByElement(featureTypeInfo.getQualifiedName());
                if (featureTypeMapping != null) {
                    SimplifiedPropertyReplacer visitor =
                            new SimplifiedPropertyReplacer(featureTypeMapping);
                    rootBuilder.accept(visitor, null);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getTemplateIdentifierByLayerRuleEvaluation(FeatureTypeInfo featureTypeInfo) {
        List<TemplateRule> matching = new ArrayList<>();
        TemplateLayerConfig config =
                featureTypeInfo
                        .getMetadata()
                        .get(TemplateLayerConfig.METADATA_KEY, TemplateLayerConfig.class);
        if (config == null || config.getTemplateRules().isEmpty()) return null;
        else {
            Set<TemplateRule> rules = config.getTemplateRules();
            Request request = Dispatcher.REQUEST.get();
            for (TemplateRule r : rules) {
                if (r.applyRule(request)) matching.add(r);
            }
        }
        int size = matching.size();
        if (size > 0) {
            if (size > 1) {
                TemplateRule.TemplateRuleComparator comparator=new TemplateRule.TemplateRuleComparator();
                matching.sort(comparator);
            }
            return matching.get(0).getTemplateIdentifier();
        }

        return null;
    }

    public void cleanCache(FeatureTypeInfo fti, String templateIdentifier) {
        CacheKey key = new CacheKey(fti, templateIdentifier);
        if (templateCache.getIfPresent(key) != null) this.templateCache.invalidate(key);
    }

    private TemplateFileManager getTemplateFileManager() {
        return GeoServerExtensions.bean(TemplateFileManager.class);
    }
}
