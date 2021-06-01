package org.geoserver.featurestemplating.configuration;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;

public class TemplateInfo extends AbstractFeatureTemplateInfo {

    private String identifier;

    private String description;

    public TemplateInfo() {
        super();
        this.identifier = UUID.randomUUID().toString();
    }

    public TemplateInfo(String templateName, String workspace, String featureType, String extension) {
        super(templateName,workspace,featureType,extension);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    public String getFullName() {
        String fullName = "";
        if (workspace != null) fullName += workspace + ":";
        if (featureType != null) fullName += featureType + ":";
        fullName += templateName;
        return fullName;
    }

    public boolean lenientEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TemplateInfo that = (TemplateInfo) o;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), identifier, description);
    }

    @Override
    public boolean equals(Object info){
        if (!lenientEquals(info)) return false;
        TemplateInfo templateInfo=(TemplateInfo) info;
        return Objects.equals(identifier,templateInfo.identifier);
    }
}
