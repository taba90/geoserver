/* (c) 2014 - 2016 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2007 OpenPlans
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wms.capabilities;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;

import java.util.Locale;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.Keyword;
import org.geoserver.catalog.KeywordInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.data.test.MockData;
import org.geoserver.wms.WMSInfo;
import org.geoserver.wms.WMSTestSupport;
import org.geotools.util.GrowableInternationalString;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;

/**
 * Unit test suite for {@link GetCapabilitiesResponse}
 *
 * @author Simone Giannecchini - GeoSolutions
 * @version $Id$
 */
public class GetCapabilitiesReponseTest extends WMSTestSupport {

    /** Tests ContentDisposition */
    @Test
    public void testSimple() throws Exception {
        String request = "wms?version=1.1.1&request=GetCapabilities&service=WMS";
        MockHttpServletResponse result = getAsServletResponse(request);
        Assert.assertTrue(result.containsHeader("content-disposition"));
        Assert.assertEquals(
                "inline; filename=getcapabilities_1.1.1.xml",
                result.getHeader("content-disposition"));

        request = "wms?version=1.3.0&request=GetCapabilities&service=WMS";
        result = getAsServletResponse(request);
        Assert.assertTrue(result.containsHeader("content-disposition"));
        Assert.assertEquals(
                "inline; filename=getcapabilities_1.3.0.xml",
                result.getHeader("content-disposition"));
    }

    @Test
    public void testInternationalContent() throws Exception {
        Catalog catalog = getCatalog();
        GeoServer geoServer = getGeoServer();

        LayerGroupInfo groupInfo = catalog.getLayerGroupByName("nature");
        GrowableInternationalString title = new GrowableInternationalString();
        title.add(Locale.ENGLISH, "a i18n title for group nature");
        title.add(Locale.ITALIAN, "titolo italiano");
        GrowableInternationalString _abstract = new GrowableInternationalString();
        _abstract.add(Locale.ENGLISH, "a i18n abstract for group nature");
        _abstract.add(Locale.ITALIAN, "abstract italiano");

        groupInfo.setInternationalTitle(title);
        groupInfo.setInternationalAbstract(_abstract);
        KeywordInfo keywordInfo = new Keyword("english keyword");
        keywordInfo.setLanguage(Locale.ENGLISH.getLanguage());

        KeywordInfo keywordInfo2 = new Keyword("parola chiave");
        keywordInfo2.setLanguage(Locale.ITALIAN.getLanguage());
        groupInfo.getKeywords().add(keywordInfo);
        groupInfo.getKeywords().add(keywordInfo2);
        catalog.save(groupInfo);

        LayerInfo li = catalog.getLayerByName(getLayerId(MockData.FIFTEEN));
        title = new GrowableInternationalString();
        title.add(Locale.ENGLISH, "a i18n title for layer fifteen");
        _abstract = new GrowableInternationalString();
        _abstract.add(Locale.ENGLISH, "a i18n abstract for layer fifteen");
        li.setInternationalTitle(title);
        li.setInternationalAbstract(_abstract);

        catalog.save(li);

        WMSInfo info = geoServer.getService(WMSInfo.class);
        title = new GrowableInternationalString();
        title.add(Locale.ENGLISH, "a i18n title for WMS service");
        _abstract = new GrowableInternationalString();
        _abstract.add(Locale.ENGLISH, "a i18n abstract for WMS service");
        info.setInternationalTitle(title);
        info.setInternationalAbstract(_abstract);
        geoServer.save(info);

        String request =
                "wms?version=1.1.1&request=GetCapabilities&service=WMS&"
                        + "AcceptLanguages="
                        + Locale.ENGLISH.getLanguage();
        Document result = getAsDOM(request);

        String service = "/WMT_MS_Capabilities/Service";
        assertXpathEvaluatesTo("a i18n title for WMS service", service + "/Title", result);
        assertXpathEvaluatesTo("a i18n abstract for WMS service", service + "/Abstract", result);

        String fifteenLayer = "/WMT_MS_Capabilities/Capability/Layer/Layer[Name = 'cdf:Fifteen']";
        assertXpathEvaluatesTo("a i18n title for layer fifteen", fifteenLayer + "/Title", result);
        assertXpathEvaluatesTo(
                "a i18n abstract for layer fifteen", fifteenLayer + "/Abstract", result);

        String natureGroup = "/WMT_MS_Capabilities/Capability/Layer/Layer/Layer[Name = 'nature']";
        assertXpathEvaluatesTo("a i18n title for group nature", natureGroup + "/Title", result);
        assertXpathEvaluatesTo(
                "a i18n abstract for group nature", natureGroup + "/Abstract", result);

        assertXpathEvaluatesTo("english keyword", natureGroup + "/KeywordList/Keyword", result);
    }

    @Test
    public void testInternationalContentAnyLanguage() throws Exception {
        // tests that if a * value is provided international content in
        // the available language is provided
        Catalog catalog = getCatalog();
        GeoServer geoServer = getGeoServer();

        LayerGroupInfo groupInfo = catalog.getLayerGroupByName("nature");
        GrowableInternationalString title = new GrowableInternationalString();
        title.add(Locale.ITALIAN, "titolo italiano");
        GrowableInternationalString _abstract = new GrowableInternationalString();
        _abstract.add(Locale.ITALIAN, "abstract italiano");

        groupInfo.setInternationalTitle(title);
        groupInfo.setInternationalAbstract(_abstract);

        KeywordInfo keywordInfo = new Keyword("parola chiave");
        keywordInfo.setLanguage(Locale.ITALIAN.getLanguage());
        groupInfo.getKeywords().add(keywordInfo);
        catalog.save(groupInfo);

        LayerInfo li = catalog.getLayerByName(getLayerId(MockData.FIFTEEN));
        title = new GrowableInternationalString();
        title.add(Locale.ITALIAN, "titolo per layer fifteen");
        _abstract = new GrowableInternationalString();
        _abstract.add(Locale.ITALIAN, "abstract per layer fifteen");
        li.setInternationalTitle(title);
        li.setInternationalAbstract(_abstract);

        catalog.save(li);

        WMSInfo info = geoServer.getService(WMSInfo.class);
        title = new GrowableInternationalString();
        title.add(Locale.ITALIAN, "Servizio WMS");
        _abstract = new GrowableInternationalString();
        _abstract.add(Locale.ITALIAN, "Abstract del servizio WMS");
        info.setInternationalTitle(title);
        info.setInternationalAbstract(_abstract);
        geoServer.save(info);

        // we put both fr and *. Content for fr is not available but since a * is present the
        // available one in it
        // will be returned in capabilities
        String request =
                "wms?version=1.1.1&request=GetCapabilities&service=WMS&"
                        + "AcceptLanguages="
                        + Locale.FRENCH.getLanguage()
                        + " *";
        Document result = getAsDOM(request);

        String service = "/WMT_MS_Capabilities/Service";
        assertXpathEvaluatesTo("Servizio WMS", service + "/Title", result);
        assertXpathEvaluatesTo("Abstract del servizio WMS", service + "/Abstract", result);

        String fifteenLayer = "/WMT_MS_Capabilities/Capability/Layer/Layer[Name = 'cdf:Fifteen']";
        assertXpathEvaluatesTo("titolo per layer fifteen", fifteenLayer + "/Title", result);
        assertXpathEvaluatesTo("abstract per layer fifteen", fifteenLayer + "/Abstract", result);

        String natureGroup = "/WMT_MS_Capabilities/Capability/Layer/Layer/Layer[Name = 'nature']";
        assertXpathEvaluatesTo("titolo italiano", natureGroup + "/Title", result);
        assertXpathEvaluatesTo("abstract italiano", natureGroup + "/Abstract", result);

        assertXpathEvaluatesTo("parola chiave", natureGroup + "/KeywordList/Keyword", result);
    }
}
