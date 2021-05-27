package org.geoserver.featurestemplating.configuration;

public interface TemplateListener {

    void handleDeleteEvent(TemplateInfoRemoveEvent removeEvent);
}
