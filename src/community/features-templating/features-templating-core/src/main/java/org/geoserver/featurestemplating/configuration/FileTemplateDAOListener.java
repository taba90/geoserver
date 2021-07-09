package org.geoserver.featurestemplating.configuration;

import org.geoserver.platform.resource.Resource;
import org.geoserver.util.IOUtils;

import java.io.IOException;

public class FileTemplateDAOListener implements TemplateDAOListener {
    @Override
    public void handleDeleteEvent(TemplateInfoEvent deleteEvent) {
        TemplateFileManager.get().delete(deleteEvent.getSource());
    }

    @Override
    public void handleUpdateEvent(TemplateInfoEvent updateEvent) {
        TemplateInfo info=updateEvent.getSource();
        TemplateFileManager fileManager=TemplateFileManager.get();
        TemplateInfo old=TemplateInfoDao.get().findById(info.getIdentifier());
        Resource resource=fileManager.getTemplateResource(old);
        if (!resource.getType().equals(Resource.Type.UNDEFINED)) {
            try {
                String rawTemplate= IOUtils.toString(resource.in());
                fileManager.saveTemplateFile(info,rawTemplate);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }
}
