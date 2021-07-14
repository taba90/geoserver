package org.geoserver.featurestemplating.configuration;

public class FileTemplateDAOListener implements TemplateDAOListener {
    @Override
    public void handleDeleteEvent(TemplateInfoEvent deleteEvent) {
        TemplateFileManager.get().delete(deleteEvent.getSource());
    }

    @Override
    public void handleUpdateEvent(TemplateInfoEvent updateEvent) {
        // do nothing
    }
}
