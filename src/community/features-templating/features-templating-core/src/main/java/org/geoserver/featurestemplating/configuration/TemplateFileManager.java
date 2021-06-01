package org.geoserver.featurestemplating.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geotools.util.logging.Logging;

public class TemplateFileManager {

    static final Logger LOGGER = Logging.getLogger(TemplateInfoMemento.class);

    private Catalog catalog;
    private GeoServerDataDirectory dd;
    private Map<String, TemplateInfoMemento> mementoMap;

    public TemplateFileManager(Catalog catalog, GeoServerDataDirectory dd) {
        this.catalog = catalog;
        this.dd = dd;
        this.mementoMap = new HashMap<>();
    }

    public Resource getTemplateResource(AbstractFeatureTemplateInfo templateInfo) {
        String featureType = templateInfo.getFeatureType();
        String workspace = templateInfo.getWorkspace();
        String templateName = templateInfo.getTemplateName();
        String extension = templateInfo.getExtension();
        Resource resource;
        Catalog catalog = (Catalog) GeoServerExtensions.bean("catalog");
        GeoServerDataDirectory dd = GeoServerExtensions.bean(GeoServerDataDirectory.class);
        if (featureType != null) {
            FeatureTypeInfo fti = catalog.getFeatureTypeByName(featureType);
            resource = dd.get(fti, templateName + "." + extension);
        } else if (workspace != null) {
            WorkspaceInfo ws = catalog.getWorkspaceByName(workspace);
            resource = dd.get(ws, templateName + "." + extension);
        } else {
            resource = dd.get(TemplateInfoDaoImpl.TEMPLATE_DIR, templateName + "." + extension);
        }
        return resource;
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
            TemplateInfoMemento infoMemento = mementoMap.get(identifier);
            if (!infoMemento.equals(info)) {
                boolean result = getTemplateResource(memento).delete();
                if (!result) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(
                                Level.WARNING,
                                "Cannot delete old template file, something went wrong during the delete process");
                }
            }
            mementoMap.remove(identifier);
        }
    }

    public File getTemplateLocation(AbstractFeatureTemplateInfo templateInfo) {
        String featureType = templateInfo.getFeatureType();
        String workspace = templateInfo.getWorkspace();
        Resource resource = null;
        if (featureType != null) {
            FeatureTypeInfo fti = catalog.getFeatureTypeByName(featureType);
            resource = dd.get(fti);
        } else if (workspace != null) {
            WorkspaceInfo ws = catalog.getWorkspaceByName(workspace);
            resource = dd.get(ws);
        } else {
            resource = dd.get(TemplateInfoDaoImpl.TEMPLATE_DIR);
        }
        File destDir = resource.dir();
        if (!destDir.exists() || !destDir.isDirectory()) {
            destDir.mkdir();
        }
        return destDir;
    }

    public void addMemento(TemplateInfo templateInfo) {
        TemplateInfoMemento memento = new TemplateInfoMemento(templateInfo);
        mementoMap.put(templateInfo.getIdentifier(), memento);
    }
}
