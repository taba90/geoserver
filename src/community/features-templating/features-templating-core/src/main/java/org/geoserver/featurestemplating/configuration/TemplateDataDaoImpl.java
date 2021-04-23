package org.geoserver.featurestemplating.configuration;

import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.Resource;
import org.geoserver.security.PropertyFileWatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

public class TemplateDataDaoImpl implements TemplateDataDao {


    private TreeSet<TemplateData> templateDataSet = new TreeSet<>();

    private PropertyFileWatcher fileWatcher;

    private Resource templateDir;

    private GeoServerDataDirectory dd;

    private static final String PROPERTY_FILE_NAME="features-templates-data.properties";

    public static TemplateDataDao get(){
        return GeoServerExtensions.bean(TemplateDataDaoImpl.class);
    }
    public TemplateDataDaoImpl (GeoServerDataDirectory dd){
        this.dd=dd;
        GeoServerResourceLoader rl=dd.getResourceLoader();
        Resource templateDir=rl.get(TEMPLATE_DIR);
        if (templateDir==null) {
            try {
                rl.createDirectory(TEMPLATE_DIR);
            } catch (IOException e) {
                e.printStackTrace();
            }
            templateDir=rl.get(TEMPLATE_DIR);
        }
        this.templateDir=templateDir;
        Resource prop=rl.get(PROPERTY_FILE_NAME);
        if (prop==null){
            try {
                rl.createFile(PROPERTY_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
            prop=rl.get(PROPERTY_FILE_NAME);
            this.fileWatcher=new PropertyFileWatcher(prop);
        }
        this.templateDataSet=new TreeSet<>();
    }

    @Override
    public List<TemplateData> findAll() {
        if (isModified()) {
            loadTemplateData();
        }
        return  new ArrayList<>(templateDataSet);
    }

    @Override
    public TemplateData findByName(String templateName) {
        return null;
    }

    @Override
    public TemplateData saveOrUpdate(TemplateData templateData) {
        templateDataSet.removeIf(td->td.getId().equals(templateData.getId()));
        templateDataSet.add(templateData);
        return templateData;
    }

    @Override
    public void delete(TemplateData templateData) {
        templateDataSet.remove(templateData);
    }

    @Override
    public void delete(String templateName) {
        templateDataSet.removeIf(td->td.getTemplateName().equals(templateName));
    }

    @Override
    public boolean templateDataExists(String templateName) {
        return templateDataSet.stream().anyMatch(td->td.getTemplateName().equals(templateName));
    }

    private TemplateData parseProperty(String key, String value){
        TemplateData templateData=new TemplateData();
        String [] values=value.split(";");
        for (String v:values){
            String[] attribute=v.split("=");
            String attrName=attribute[0];
            String attrValue=attribute[1];
            if (attrName.equals("templateName"))
                templateData.setTemplateName(attrValue);
            else if (attrName.equals("templateFileName"))
                templateData.setTemplateFileName(attrValue);
            else if (attrName.equals("workspace"))
                templateData.setWorkspace(attrValue);
            else if (attrName.equals("featureTypeInfo"))
                templateData.setFeatureTypeInfo(attrValue);
        }
        templateData.setId(key);
        return templateData;
    }

    private Properties toProperties(){
        Properties properties=new Properties();
        for (TemplateData td: templateDataSet){
            StringBuilder sb=new StringBuilder();
            sb.append("templateName=").append(td.getTemplateName())
                    .append(";templateFileName=").append(td.getTemplateFileName());
            String ws=td.getWorkspace();
            if (ws!=null)
                sb.append(";workspace=").append(td.getWorkspace());
            String fti=td.getFeatureTypeInfo();
            if (fti!=null)
                sb.append(";featureTypeInfo=").append(td.getFeatureTypeInfo());
            properties.put(td.getId(),sb.toString());
        }
        return properties;
    }

    private void storeProperties() throws IOException {
        OutputStream os = null;
        try {
            // turn back the users into a users map
            Properties p = toProperties();
            // write out to the data dir
            Resource propFile = templateDir.get(PROPERTY_FILE_NAME);
            os = propFile.out();
            p.store(os, null);
        } catch (Exception e) {
            if (e instanceof IOException) throw (IOException) e;
            else
                throw (IOException)
                        new IOException("Could not write rules to " + PROPERTY_FILE_NAME)
                                .initCause(e);
        } finally {
            if (os != null) os.close();
        }
    }

    private boolean isModified(){
        return fileWatcher!=null && fileWatcher.isStale();
    }

    private void loadTemplateData(){
        try {
            Properties properties=fileWatcher.getProperties();
            this.templateDataSet=new TreeSet<>();
            for (Object k:properties.keySet()){
                TemplateData td=parseProperty(k.toString(),properties.getProperty(k.toString()));
                this.templateDataSet.add(td);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
