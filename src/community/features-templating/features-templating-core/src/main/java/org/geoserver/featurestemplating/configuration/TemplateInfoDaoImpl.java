package org.geoserver.featurestemplating.configuration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geoserver.security.PropertyFileWatcher;

public class TemplateInfoDaoImpl implements TemplateInfoDao {

    private TreeSet<TemplateInfo> templateDataSet = new TreeSet<>();

    private PropertyFileWatcher fileWatcher;

    private GeoServerDataDirectory dd;

    private Set<TemplateListener> listeners;

    private static final String PROPERTY_FILE_NAME = "features-templates-data.properties";

    public static TemplateInfoDaoImpl get() {
        return GeoServerExtensions.bean(TemplateInfoDaoImpl.class);
    }

    public TemplateInfoDaoImpl(GeoServerDataDirectory dd) {
        this.dd = dd;
        Resource templateDir = dd.get(TEMPLATE_DIR);
        File dir = templateDir.dir();
        if (!dir.exists()) dir.mkdir();
        Resource prop = dd.get(TEMPLATE_DIR, PROPERTY_FILE_NAME);
        prop.file();
        this.fileWatcher = new PropertyFileWatcher(prop);
        this.templateDataSet = new TreeSet<>();
        this.listeners=new HashSet<>();
    }

    @Override
    public List<TemplateInfo> findAll() {
        if (isModified() || templateDataSet.isEmpty()) {
            loadTemplateInfo();
        }
        return new ArrayList<>(templateDataSet);
    }

    @Override
    public TemplateInfo findByName(String templateName) {
        if (isModified()) {
            loadTemplateInfo();
        }
        Optional<TemplateInfo> info =
                templateDataSet
                        .stream()
                        .filter(ti -> ti.getTemplateName().equals(templateName))
                        .findFirst();
        return info.get();
    }

    @Override
    public TemplateInfo saveOrUpdate(TemplateInfo templateData) {
        templateDataSet.add(templateData);
        storeProperties();
        return templateData;
    }

    @Override
    public void delete(TemplateInfo templateData) {
        templateDataSet.remove(templateData);
        fireTemplateInfoRemoveEvent(templateData);
        storeProperties();
    }

    @Override
    public void deleteAll(List<TemplateInfo> templateInfos) {
        templateDataSet.removeAll(templateInfos);
        storeProperties();
        for (TemplateInfo ti:templateInfos)
            fireTemplateInfoRemoveEvent(ti);
    }

    @Override
    public void delete(String templateName) {
        templateDataSet.removeIf(td -> td.getTemplateName().equals(templateName));
        storeProperties();
    }

    @Override
    public boolean templateDataExists(TemplateInfo templateInfo) {
        if (isModified()) loadTemplateInfo();
        return templateDataSet.stream().anyMatch(td -> td.lenientEquals(templateInfo));
    }

    @Override
    public void fireTemplateInfoRemoveEvent(TemplateInfo templateInfo) {
        for (TemplateListener listener: listeners){
            listener.handleDeleteEvent(new TemplateInfoRemoveEvent(templateInfo));
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
            OutputStream os = null;
            try {
                // turn back the users into a users map
                Properties p = toProperties();
                // write out to the data dir
                Resource propFile = dd.get(TEMPLATE_DIR, PROPERTY_FILE_NAME);
                os = propFile.out();
                p.store(os, null);
            } catch (Exception e) {
                throw new RuntimeException("Could not write rules to " + PROPERTY_FILE_NAME);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
            e.printStackTrace();
        }
    }



    @Override
    public TemplateInfo findById(String id) {
        if (isModified() || templateDataSet.isEmpty())
            loadTemplateInfo();
        Optional<TemplateInfo> optional=templateDataSet.stream().filter(ti->ti.getIdentifier().equals(id)).findFirst();
        if (optional.isPresent())
            return optional.get();
        else
            return null;
    }

    @Override
    public List<TemplateInfo> findByWorkspaceAndFeatureTypeInfo(String workspace, String featureTypeInfo) {
        return templateDataSet.stream().filter(ti->(ti.getWorkspace()==null && ti.getFeatureType()==null) ||
                ti.getFeatureType()==null && ti.getWorkspace().equals(workspace)
                || (ti.getWorkspace().equals(workspace) && ti.getFeatureType().equals(featureTypeInfo))
        ).collect(Collectors.toList());
    }
}
