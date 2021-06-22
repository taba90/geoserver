package org.geoserver.inspire.wms;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.Keyword;
import org.geoserver.catalog.KeywordInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.data.test.MockData;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.geoserver.wms.WMSInfo;
import org.geoserver.wms.WMSTestSupport;
import org.geotools.util.GrowableInternationalString;
import org.junit.Test;
import org.w3c.dom.Document;

import java.util.Locale;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;

public class WMSLanguageIntegrationTests extends WMSTestSupport {

    @Test
    public void testLanguageParameter() throws Exception {
        Catalog catalog=getCatalog();
        GeoServer geoServer=getGeoServer();

        LayerGroupInfo groupInfo=catalog.getLayerGroupByName("nature");
        GrowableInternationalString title=new GrowableInternationalString();
        title.add(Locale.ENGLISH,"a i18n title for group nature");
        title.add(Locale.ITALIAN,"titolo italiano");
        GrowableInternationalString _abstract= new GrowableInternationalString();
        _abstract.add(Locale.ENGLISH,"a i18n abstract for group nature");
        _abstract.add(Locale.ITALIAN,"abstract italiano");

        groupInfo.setInternationalTitle(title);
        groupInfo.setInternationalAbstract(_abstract);
        KeywordInfo keywordInfo=new Keyword("english keyword");
        keywordInfo.setLanguage(Locale.ENGLISH.getLanguage());

        KeywordInfo keywordInfo2=new Keyword("parola chiave");
        keywordInfo2.setLanguage(Locale.ITALIAN.getLanguage());
        groupInfo.getKeywords().add(keywordInfo);
        groupInfo.getKeywords().add(keywordInfo2);
        catalog.save(groupInfo);

        LayerInfo li=catalog.getLayerByName(getLayerId(MockData.FIFTEEN));
        title=new GrowableInternationalString();
        title.add(Locale.ENGLISH,"a i18n title for layer fifteen");
        _abstract= new GrowableInternationalString();
        _abstract.add(Locale.ENGLISH,"a i18n abstract for layer fifteen");
        li.setInternationalTitle(title);
        li.setInternationalAbstract(_abstract);

        catalog.save(li);

        WMSInfo info= geoServer.getService(WMSInfo.class);
        title=new GrowableInternationalString();
        title.add(Locale.ENGLISH,"a i18n title for WMS service");
        _abstract= new GrowableInternationalString();
        _abstract.add(Locale.ENGLISH,"a i18n abstract for WMS service");
        info.setInternationalTitle(title);
        info.setInternationalAbstract(_abstract);
        geoServer.save(info);

        String request = "wms?version=1.1.1&request=GetCapabilities&service=WMS&"+"Language=eng";
        Document result = getAsDOM(request);

        String service = "/WMT_MS_Capabilities/Service";
        assertXpathEvaluatesTo("a i18n title for WMS service",service+"/Title",result);
        assertXpathEvaluatesTo("a i18n abstract for WMS service",service+"/Abstract",result);

        String fifteenLayer="/WMT_MS_Capabilities/Capability/Layer/Layer[Name = 'cdf:Fifteen']";
        assertXpathEvaluatesTo("a i18n title for layer fifteen",fifteenLayer+"/Title",result);
        assertXpathEvaluatesTo("a i18n abstract for layer fifteen",fifteenLayer+"/Abstract",result);

        String natureGroup="/WMT_MS_Capabilities/Capability/Layer/Layer/Layer[Name = 'nature']";
        assertXpathEvaluatesTo("a i18n title for group nature",natureGroup+"/Title",result);
        assertXpathEvaluatesTo("a i18n abstract for group nature",natureGroup+"/Abstract",result);

        assertXpathEvaluatesTo("english keyword",natureGroup+"/KeywordList/Keyword",result);
    }
}
