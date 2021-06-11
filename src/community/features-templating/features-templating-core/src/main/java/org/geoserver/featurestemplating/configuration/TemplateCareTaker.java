package org.geoserver.featurestemplating.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.platform.GeoServerExtensions;
import org.geotools.util.logging.Logging;

public class TemplateCareTaker {

    static final Logger LOGGER = Logging.getLogger(TemplateInfoMemento.class);

    private Map<String, TemplateInfoMemento> mementoMap;
    private TemplateFileManager fileManager;

    public TemplateCareTaker() {
        this.mementoMap = new HashMap<>();
        this.fileManager = GeoServerExtensions.bean(TemplateFileManager.class);
    }

    public void deleteOldTemplateFile(TemplateInfo info) {
        String identifier = info.getIdentifier();
        TemplateInfoMemento memento = mementoMap.get(identifier);
        if (memento == null) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(
                        Level.WARNING,
                        "Cannot delete old template file, "
                                + "something went wrong when saving the previous info state");
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(
                        Level.FINE,
                        "Deleting template file for template with name "
                                + memento.getTemplateName());
            }
            if (!memento.lenientEquals(info)) {
                boolean result = fileManager.delete(memento);
                if (!result) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(
                                Level.WARNING,
                                "Cannot delete old template file, something went wrong during the delete process");
                }
            }
        }
    }

    public void undo(TemplateInfo info) {
        String identifier = info.getIdentifier();
        TemplateInfoMemento memento = mementoMap.get(identifier);
        if (memento == null) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(
                        Level.WARNING,
                        "Cannot undo operation performed on template with name "
                                + info.getTemplateName()
                                + " something went wrong when saving the previous state");
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(
                        Level.FINE,
                        "Undoing modifications for templateInfo " + memento.getTemplateName());
            }
            TemplateInfo restored = new TemplateInfo(memento);
            TemplateInfoDao.get().saveOrUpdate(restored);
            fileManager.delete(info);
            fileManager.saveTemplateFile(memento, memento.getRawTemplate());
        }
    }

    public void addMemento(TemplateInfo templateInfo, String rawTemplate) {
        TemplateInfoMemento memento = new TemplateInfoMemento(templateInfo,rawTemplate);
        mementoMap.put(templateInfo.getIdentifier(), memento);
    }
}
