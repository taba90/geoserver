package org.geoserver.featurestemplating.configuration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.resource.Resource;
import org.geoserver.security.PropertyFileWatcher;

public class TemplateInfoDaoImpl implements TemplateInfoDao {

    private TreeSet<TemplateInfo> templateDataSet = new TreeSet<>();

    private PropertyFileWatcher fileWatcher;

    private GeoServerDataDirectory dd;

    private Set<TemplateListener> listeners;

    private static final String PROPERTY_FILE_NAME = "features-templates-data.properties";

    public TemplateInfoDaoImpl(GeoServerDataDirectory dd) {
        this.dd = dd;
        Resource templateDir = dd.get(TEMPLATE_DIR);
        File dir = templateDir.dir();
        if (!dir.exists()) dir.mkdir();
        Resource prop = dd.get(TEMPLATE_DIR, PROPERTY_FILE_NAME);
        prop.file();
        this.fileWatcher = new PropertyFileWatcher(prop);
        this.templateDataSet = new TreeSet<>();
        this.listeners = new HashSet<>();
    }

    @Override
    public List<TemplateInfo> findAll() {
        reloadIfNeeded();
        return new ArrayList<>(templateDataSet);
    }

    @Override
    public TemplateInfo saveOrUpdate(TemplateInfo templateData) {
        reloadIfNeeded();
        boolean isUpdate=templateDataSet.removeIf(ti->ti.getIdentifier().equals(templateData.getIdentifier()));
        templateDataSet.add(templateData);
        storeProperties();
        if (isUpdate)
            fireTemplateUpdateEvent(templateData);
        return templateData;
    }

    @Override
    public void delete(TemplateInfo templateData) {
        reloadIfNeeded();
        templateDataSet.remove(templateData);
        fireTemplateInfoRemoveEvent(templateData);
        storeProperties();
    }

    @Override
    public void deleteAll(List<TemplateInfo> templateInfos) {
        reloadIfNeeded();
        templateDataSet.removeAll(templateInfos);
        storeProperties();
        for (TemplateInfo ti : templateInfos) fireTemplateInfoRemoveEvent(ti);
    }

    @Override
    public TemplateInfo findById(String id) {
        reloadIfNeeded();
        Optional<TemplateInfo> optional =
                templateDataSet.stream().filter(ti -> ti.getIdentifier().equals(id)).findFirst();
        if (optional.isPresent()) return optional.get();
        else return null;
    }

    @Override
    public List<TemplateInfo> findByFeatureTypeInfo(
            FeatureTypeInfo featureTypeInfo) {
        reloadIfNeeded();
        String workspace =featureTypeInfo.getStore().getWorkspace().getName();
        String name=featureTypeInfo.getNativeName();
        return templateDataSet
                .stream()
                .filter(
                        ti ->
                                (ti.getWorkspace() == null && ti.getFeatureType() == null)
                                        || ti.getFeatureType() == null
                                                && ti.getWorkspace().equals(workspace)
                                        || (ti.getWorkspace().equals(workspace)
                                                && ti.getFeatureType().equals(name)))
                .collect(Collectors.toList());
    }

    @Override
    public void fireTemplateUpdateEvent(TemplateInfo templateInfo) {
        for (TemplateListener listener:listeners){
            listener.handleUpdateEvent(new TemplateInfoEvent(templateInfo));
        }
    }

    @Override
    public void fireTemplateInfoRemoveEvent(TemplateInfo templateInfo) {
        for (TemplateListener listener : listeners) {
            listener.handleDeleteEvent(new TemplateInfoEvent(templateInfo));
        }
    }

    @Override
    public void addTemplateListener(TemplateListener listener) {
        this.listeners.add(listener);
    }


    private TemplateInfo parseProperty(String key, String value) {
        TemplateInfo templateData = new TemplateInfo();
        templateData.setIdentifier(key);
        String[] values = value.split(";");
        for (String v : values) {
            String[] attribute = v.split("=");
            String attrName = attribute[0];
            String attrValue = attribute[1];
            if (attrName.equals("templateName")) templateData.setTemplateName(attrValue);
            else if (attrName.equals("extension")) templateData.setExtension(attrValue);
            else if (attrName.equals("workspace")) templateData.setWorkspace(attrValue);
            else if (attrName.equals("featureTypeInfo")) templateData.setFeatureType(attrValue);
        }
        templateData.setIdentifier(key);
        return templateData;
    }

    private Properties toProperties() {
        Properties properties = new Properties();
        for (TemplateInfo td : templateDataSet) {
            StringBuilder sb = new StringBuilder();
            sb.append("templateName=")
                    .append(td.getTemplateName())
                    .append(";extension=")
                    .append(td.getExtension());
            String ws = td.getWorkspace();
            if (ws != null) sb.append(";workspace=").append(td.getWorkspace());
            String fti = td.getFeatureType();
            if (fti != null) sb.append(";featureTypeInfo=").append(td.getFeatureType());
            properties.put(td.getIdentifier(), sb.toString());
        }
        return properties;
    }

    private void storeProperties() {
        synchronized (this) {
            Properties p = toProperties();
            Resource propFile = dd.get(TEMPLATE_DIR, PROPERTY_FILE_NAME);
            try(OutputStream os = propFile.out()){
                p.store(os, null);
            } catch (Exception e) {
                throw new RuntimeException("Could not write rules to " + PROPERTY_FILE_NAME);
            }
        }
    }

    private boolean isModified() {
        return fileWatcher != null && fileWatcher.isStale();
    }

    private void loadTemplateInfo() {
        try {
            Properties properties = fileWatcher.getProperties();
            this.templateDataSet = new TreeSet<>();
            for (Object k : properties.keySet()) {
                TemplateInfo td = parseProperty(k.toString(), properties.getProperty(k.toString()));
                this.templateDataSet.add(td);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadIfNeeded(){
        if (isModified() || templateDataSet.isEmpty()) loadTemplateInfo();
    }

}


