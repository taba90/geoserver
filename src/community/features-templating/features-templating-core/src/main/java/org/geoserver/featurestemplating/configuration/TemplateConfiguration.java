package org.geoserver.featurestemplating.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateConfiguration implements Serializable {

    private Map<String,TemplateEntry> templateEntries;

    public static String METADATA_KEY = "FEATURE_TEMPLATING_CONFIGURATION";

    public TemplateConfiguration(Map<String,TemplateEntry> templateEntries) {
        this.templateEntries = templateEntries;
    }

    public TemplateConfiguration(){
        this.templateEntries=new HashMap<>();
    }

    public Map<String,TemplateEntry> getTemplateEntries() {
        return templateEntries;
    }

    public List<TemplateEntry> getEntriesAsList(){
        return new ArrayList<>(this.templateEntries.values());
    }

    public void addOrUpdateEntry(TemplateEntry entry){
        templateEntries.put(getEntryKey(entry), entry);
    }

    public boolean hasEntry(TemplateEntry entry){
        return templateEntries.get(getEntryKey(entry)) !=null;
    }

    private String getEntryKey(TemplateEntry entry){
        return entry.getMimeType() + "|"+entry.getTemplateName();
    }
}
