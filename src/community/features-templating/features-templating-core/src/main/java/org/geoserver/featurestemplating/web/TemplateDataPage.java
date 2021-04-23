package org.geoserver.featurestemplating.web;

import org.apache.wicket.model.Model;
import org.geoserver.catalog.Catalog;
import org.geoserver.featurestemplating.configuration.TemplateData;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.web.GeoServerSecuredPage;

public class TemplateDataPage extends GeoServerSecuredPage {

    Catalog catalog;
    public TemplateDataPage (){
        this.catalog= (Catalog) GeoServerExtensions.bean("catalog");
        add(new TemplateDataTablePanel("tablePanel"));
        add(new TemplateDataConfigurationPanel("configurationPanel",this.catalog,new Model<>(new TemplateData())));
    }

}
