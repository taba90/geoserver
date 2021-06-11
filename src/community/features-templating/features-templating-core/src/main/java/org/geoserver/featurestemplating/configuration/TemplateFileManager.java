package org.geoserver.featurestemplating.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;

public class TemplateFileManager {

    private Catalog catalog;
    private GeoServerDataDirectory dd;

    public TemplateFileManager(Catalog catalog, GeoServerDataDirectory dd) {
        this.catalog = catalog;
        this.dd = dd;
    }

    public static TemplateFileManager get() {
        return GeoServerExtensions.bean(TemplateFileManager.class);
    }

    public Resource getTemplateResource(AbstractFeatureTemplateInfo templateInfo) {
        String featureType = templateInfo.getFeatureType();
        String workspace = templateInfo.getWorkspace();
        String templateName = templateInfo.getTemplateName();
        String extension = templateInfo.getExtension();
        Resource resource;
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

    public boolean delete(AbstractFeatureTemplateInfo templateInfo) {
        return getTemplateResource(templateInfo).delete();
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

    public void saveTemplateFile(AbstractFeatureTemplateInfo templateInfo, String rawTemplate) {
        File destDir = getTemplateLocation(templateInfo);
        try {
            File file =
                    new File(
                            destDir,
                            templateInfo.getTemplateName() + "." + templateInfo.getExtension());
            if (!file.exists()) file.createNewFile();
            synchronized (this) {
                try (FileOutputStream fos = new FileOutputStream(file, false)) {
                    fos.write(rawTemplate.getBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
