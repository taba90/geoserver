package org.geoserver.featurestemplating.configuration;

public interface TemplateListener {

    void handleDeleteEvent(TemplateInfoEvent removeEvent);

    void handleUpdateEvent(TemplateInfoEvent updateEvent);
}
