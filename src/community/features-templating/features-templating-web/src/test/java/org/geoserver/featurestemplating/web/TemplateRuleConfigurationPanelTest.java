package org.geoserver.featurestemplating.web;

import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.data.test.MockData;
import org.geoserver.featurestemplating.configuration.TemplateFileManager;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfoDao;
import org.geoserver.featurestemplating.configuration.TemplateLayerConfig;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.web.ComponentBuilder;
import org.geoserver.web.FormTestPage;
import org.geoserver.web.GeoServerWicketTestSupport;
import org.junit.Before;
import org.junit.Test;

public class TemplateRuleConfigurationPanelTest extends GeoServerWicketTestSupport {

    @Before
    public void setUpInternal() {
        TemplateInfo tinfo = new TemplateInfo();
        tinfo.setExtension("xml");
        tinfo.setTemplateName("test_template_xml");
        TemplateFileManager.get().saveTemplateFile(tinfo, "<ft:FeatureCollection/>");
        TemplateInfoDao.get().saveOrUpdate(tinfo);
        tinfo = new TemplateInfo();
        tinfo.setExtension("json");
        tinfo.setTemplateName("test_template_json");
        TemplateFileManager.get().saveTemplateFile(tinfo, "features:[]");
        TemplateInfoDao.get().saveOrUpdate(tinfo);
    }

    @Test
    public void testLayerRuleConfiguration() {
        LayerInfo layerInfo = getCatalog().getLayerByName(getLayerId(MockData.BUILDINGS));
        Model<LayerInfo> layerModel = new Model<>(layerInfo);
        tester.startPage(
                new FormTestPage(
                        new ComponentBuilder() {
                            private static final long serialVersionUID = -5907648151984337786L;

                            @Override
                            public Component buildComponent(final String id) {
                                return new TemplateRulesTabPanel(id, layerModel);
                            }
                        }));
        FormTester form = tester.newFormTester("form:panel:ruleConfiguration:theForm");
        form.select("templateIdentifier", 0);
        form.select("outputFormats", 1);
        form.setValue("cqlFilter", "requestParam('myParam')='use this template'");
        form.submit("save");
        // add a second rule
        form = tester.newFormTester("form:panel:ruleConfiguration:theForm");
        form.select("templateIdentifier", 1);
        form.select("outputFormats", 0);
        form.setValue("priority", "1");
        form.setValue("cqlFilter", "requestParam('myParam2')='use this template'");
        form.submit("save");
        tester.assertNoErrorMessage();
    }

    @Test
    public void testLayerRuleEdit() {
        LayerInfo layerInfo = getCatalog().getLayerByName(getLayerId(MockData.FIFTEEN));
        TemplateLayerConfig layerConfig = new TemplateLayerConfig();
        TemplateRule templateRule = new TemplateRule();
        List<TemplateInfo> infos = TemplateInfoDao.get().findAll();
        templateRule.setPriority(0);
        templateRule.setTemplateName(infos.get(1).getFullName());
        templateRule.setTemplateIdentifier(infos.get(1).getIdentifier());
        templateRule.setCqlFilter("mimeType() = 'application/geo+json'");
        layerConfig.addTemplateRule(templateRule);
        layerInfo.getResource().getMetadata().put(TemplateLayerConfig.METADATA_KEY, layerConfig);
        Model<LayerInfo> layerModel = new Model<>(layerInfo);
        tester.startPage(
                new FormTestPage(
                        new ComponentBuilder() {
                            private static final long serialVersionUID = -5907648151984337786L;

                            @Override
                            public Component buildComponent(final String id) {
                                return new TemplateRulesTabPanel(id, layerModel);
                            }
                        }));
        tester.executeAjaxEvent(
                "form:panel:rulesTable:table:listContainer:items:1:itemProperties:1:component:link",
                "click");
        tester.assertModelValue(
                "form:panel:ruleConfiguration:theForm:priority", templateRule.getPriority());
        tester.assertModelValue(
                "form:panel:ruleConfiguration:theForm:templateIdentifier", infos.get(1));
        tester.assertModelValue(
                "form:panel:ruleConfiguration:theForm:cqlFilter", templateRule.getCqlFilter());
        FormTester form = tester.newFormTester("form:panel:ruleConfiguration:theForm");
        form.select("outputFormats", 0);
        form.setValue("cqlFilter", "requestParam('myParam')='use this template'");
        form.submit("save");
        tester.assertNoErrorMessage();
    }
}
