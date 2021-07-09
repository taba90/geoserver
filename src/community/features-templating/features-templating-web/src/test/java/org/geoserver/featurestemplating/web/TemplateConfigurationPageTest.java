package org.geoserver.featurestemplating.web;

import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.featurestemplating.configuration.TemplateFileManager;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfoDao;
import org.geoserver.web.GeoServerWicketTestSupport;
import org.geoserver.web.wicket.CodeMirrorEditor;
import org.junit.Test;

public class TemplateConfigurationPageTest extends GeoServerWicketTestSupport {

    private static final String NAMED_PLACES_TEMPLATE =
            "{"
                    + "  \"@context\": {"
                    + "    \"gsp\": \"http://www.opengis.net/ont/geosparql#\","
                    + "    \"sf\": \"http://www.opengis.net/ont/sf#\","
                    + "    \"schema\": \"https://schema.org/\","
                    + "    \"dc\": \"http://purl.org/dc/terms/\","
                    + "    \"Feature\": \"gsp:Feature\","
                    + "    \"FeatureCollection\": \"schema:Collection\","
                    + "    \"Point\": \"sf:Point\","
                    + "    \"wkt\": \"gsp:asWKT\","
                    + "    \"features\": {"
                    + "      \"@container\": \"@set\","
                    + "      \"@id\": \"schema:hasPart\""
                    + "    },"
                    + "    \"geometry\": \"sf:geometry\","
                    + "    \"description\": \"dc:description\","
                    + "    \"title\": \"dc:title\","
                    + "    \"name\": \"schema:name\""
                    + "  },"
                    + "  \"type\": \"FeatureCollection\","
                    + "  \"features\": ["
                    + "    {"
                    + "      \"$source\": \"cite:NamedPlaces\""
                    + "    },"
                    + "    {"
                    + "      \"id\": \"${cite:FID}\","
                    + "      \"@type\": ["
                    + "        \"Feature\","
                    + "        \"cite:NamedPlaces\","
                    + "        \"http://vocabulary.odm2.org/samplingfeaturetype/namedplaces\""
                    + "      ],"
                    + "      \"name\": \"${cite:NAME}\","
                    + "      \"geometry\": {"
                    + "        \"@type\": \"MultiPolygon\","
                    + "        \"wkt\": \"$${toWKT(xpath('cite:the_geom'))}\""
                    + "      }"
                    + "    }"
                    + "  ]"
                    + "}";

    @Test
    public void testNew() {
        try {
            login();
            tester.startPage(new TemplateConfigurationPage(new Model<>(new TemplateInfo()), true));
            FormTester form = tester.newFormTester("theForm");
            form.select("tabbedPanel:panel:extension", 2);
            tester.executeAjaxEvent("theForm:tabbedPanel:panel:extension", "change");
            form.select("tabbedPanel:panel:workspace", 2);
            tester.executeAjaxEvent("theForm:tabbedPanel:panel:workspace", "change");
            form.select("tabbedPanel:panel:featureTypeInfo", 8);
            tester.executeAjaxEvent("theForm:tabbedPanel:panel:featureTypeInfo", "change");
            form.setValue(
                    "templateEditor:editorContainer:editorParent:editor", NAMED_PLACES_TEMPLATE);
            form.setValue("tabbedPanel:panel:templateName", "testJsonLDTemplate");
            tester.clickLink("theForm:tabbedPanel:tabs-container:tabs:1:link");
            FormTester form2 = tester.newFormTester("theForm:tabbedPanel:panel:previewForm");
            form2.select("outputFormats", 0);
            tester.assertComponent(
                    "theForm:tabbedPanel:panel:previewForm:workspaces", DropDownChoice.class);
            tester.assertComponent(
                    "theForm:tabbedPanel:panel:previewForm:featureTypes", DropDownChoice.class);
            tester.assertComponent(
                    "theForm:tabbedPanel:panel:previewForm:previewArea", CodeMirrorEditor.class);
            tester.assertComponent("theForm:tabbedPanel:panel:preview", AjaxSubmitLink.class);
            tester.assertComponent("theForm:tabbedPanel:panel:validate", AjaxSubmitLink.class);
            form.submit("save");
            tester.assertNoErrorMessage();
            tester.assertRenderedPage(TemplateInfoPage.class);
        } finally {
            TemplateInfoDao.get().deleteAll();
        }
    }

    @Test
    public void testEdit() {
        TemplateInfo info = new TemplateInfo();
        info.setWorkspace("cite");
        info.setFeatureType("NamedPlaces");
        info.setTemplateName("testJsonLDTemplate");
        info.setExtension("json");
        info = TemplateInfoDao.get().saveOrUpdate(info);
        TemplateFileManager.get().saveTemplateFile(info, NAMED_PLACES_TEMPLATE);
        try {
            login();
            tester.startPage(new TemplateConfigurationPage(new Model<>(info), false));
            tester.assertModelValue("theForm:tabbedPanel:panel:templateName", "testJsonLDTemplate");
            tester.assertModelValue("theForm:tabbedPanel:panel:extension", "json");
            tester.assertModelValue("theForm:tabbedPanel:panel:workspace", "cite");
            tester.assertModelValue("theForm:tabbedPanel:panel:featureTypeInfo", "NamedPlaces");
            tester.clickLink("theForm:tabbedPanel:tabs-container:tabs:1:link");
            FormTester form2 = tester.newFormTester("theForm:tabbedPanel:panel:previewForm");
            form2.select("outputFormats", 0);
            tester.assertComponent(
                    "theForm:tabbedPanel:panel:previewForm:workspaces", DropDownChoice.class);
            tester.assertComponent(
                    "theForm:tabbedPanel:panel:previewForm:featureTypes", DropDownChoice.class);
            tester.assertComponent(
                    "theForm:tabbedPanel:panel:previewForm:previewArea", CodeMirrorEditor.class);
            tester.assertComponent("theForm:tabbedPanel:panel:previewForm:preview", AjaxSubmitLink.class);
            tester.assertComponent("theForm:tabbedPanel:panel:previewForm:validate", AjaxSubmitLink.class);
            tester.newFormTester("theForm").submit("save");
            tester.assertNoErrorMessage();
            tester.assertRenderedPage(TemplateInfoPage.class);
        } finally {
            TemplateInfoDao.get().deleteAll();
        }
    }
}
