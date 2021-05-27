package org.geoserver.featurestemplating.configuration;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;

public class TemplateInfo implements Serializable, Comparable<TemplateInfo> {

    private String identifier;

    private String templateName;

    private String description;

    private String workspace;

    private String featureType;

    private String templateLocation;

    private String extension;

    private String rawTemplate;

    public TemplateInfo() {
        this.identifier = UUID.randomUUID().toString();
    }

    public TemplateInfo(String templateName, String workspace, String featureType) {
        this.templateName = templateName;
        this.workspace = workspace;
        this.featureType = featureType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public File getTemplateLocation() {
        Resource resource = null;
        Catalog catalog = (Catalog) GeoServerExtensions.bean("catalog");
        GeoServerDataDirectory dd = GeoServerExtensions.bean(GeoServerDataDirectory.class);
        if (featureType != null) {
            FeatureTypeInfo fti = catalog.getFeatureTypeByName(featureType);
            resource = dd.get(fti);
        } else if (workspace != null) {
            WorkspaceInfo ws = catalog.getWorkspaceByName(workspace);
            resource = dd.get(workspace);
        } else {
            resource = dd.get(TemplateInfoDaoImpl.TEMPLATE_DIR);
        }
        File destDir = resource.dir();
        if (!destDir.exists() || !destDir.isDirectory()) {
            destDir.mkdir();
        }
        return destDir;
    }

    public Path getTemplateParentDir() {
        Path path;
        if (workspace == null) {
            path = Paths.get(TemplateInfoDaoImpl.TEMPLATE_DIR);
        } else {
            path = Paths.get("workspace", workspace);
            if (featureType != null) {
                Catalog catalog = (Catalog) GeoServerExtensions.bean("catalog");
                FeatureTypeInfo fti = catalog.getFeatureTypeByName(featureType);
                DataStoreInfo store = fti.getStore();
                path = path.resolve(store.getName()).resolve(fti.getQualifiedName().getLocalPart());
            }
        }

        return path;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setTemplateLocation(String templateLocation) {
        this.templateLocation = templateLocation;
    }

    @Override
    public int compareTo(TemplateInfo o) {
        return this.templateName.compareTo(o.getTemplateName());
    }

    public Resource getTemplateResource() {
        Resource resource = null;
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

    public String getFullName() {
        String fullName = "";
        if (workspace != null) fullName += workspace + ":";
        if (featureType != null) fullName += featureType + ":";
        fullName += templateName;
        return fullName;
    }
}
