/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wcs.web;

import static org.junit.Assert.assertNotNull;

import org.apache.wicket.util.tester.FormTester;
import org.geoserver.wcs.WCSInfo;
import org.geoserver.web.wicket.KeywordsEditor;
import org.junit.Test;

public class WCSAdminPageTest extends GeoServerWicketCoverageTestSupport {

    @Test
    public void test() throws Exception {
        login();
        WCSInfo wcs = getGeoServerApplication().getGeoServer().getService(WCSInfo.class);

        // start the page
        tester.startPage(new WCSAdminPage());

        tester.assertRenderedPage(WCSAdminPage.class);

        // test that components have been filled as expected
        tester.assertComponent("form:keywords", KeywordsEditor.class);
        tester.assertModelValue("form:keywords", wcs.getKeywords());
    }

    @Test
    public void testInternationalContent() {
        login();

        // start the page
        tester.startPage(new WCSAdminPage());

        FormTester form = tester.newFormTester("form");
        // enable i18n for title
        form.setValue("internationalContent:titleLabel:titleLabel_i18nCheckbox", true);
        tester.executeAjaxEvent("form:internationalContent:titleLabel:titleLabel_i18nCheckbox", "click");
        tester.executeAjaxEvent(
                "form:internationalContent:internationalTitle:container:addNew", "click");

        form.select(
                "internationalContent:internationalTitle:container:tablePanel:listContainer:items:1:itemProperties:0:component:border:border_body:select",
                10);
        form.setValue(
                "internationalContent:internationalTitle:container:tablePanel:listContainer:items:1:itemProperties:1:component:border:border_body:txt",
                "an international title");
        tester.executeAjaxEvent(
                "form:internationalContent:internationalTitle:container:addNew", "click");
        form=tester.newFormTester("form");
        form.select(
                "internationalContent:internationalTitle:container:tablePanel:listContainer:items:2:itemProperties:0:component:border:border_body:select",
                20);
        form.setValue(
                "internationalContent:internationalTitle:container:tablePanel:listContainer:items:2:itemProperties:1:component:border:border_body:txt",
                "another international title");
        tester.executeAjaxEvent(
                "form:internationalContent:internationalTitle:container:tablePanel:listContainer:items:2:itemProperties:2:component:remove",
                "click");

        // enable i18n for abstract
        form.setValue("internationalContent:abstractLabel:abstractLabel_i18nCheckbox", true);
        tester.executeAjaxEvent(
                "form:internationalContent:abstractLabel:abstractLabel_i18nCheckbox", "click");
        form=tester.newFormTester("form");
        tester.executeAjaxEvent(
                "form:internationalContent:internationalAbstract:container:addNew", "click");
        form.select(
                "internationalContent:internationalAbstract:container:tablePanel:listContainer:items:1:itemProperties:0:component:border:border_body:select",
                10);
        form.setValue(
                "internationalContent:internationalAbstract:container:tablePanel:listContainer:items:1:itemProperties:1:component:border:border_body:txt",
                "an international title");
        tester.executeAjaxEvent(
                "form:internationalContent:internationalAbstract:container:addNew", "click");
        form.select(
                "internationalContent:internationalAbstract:container:tablePanel:listContainer:items:2:itemProperties:0:component:border:border_body:select",
                20);
        form.setValue(
                "internationalContent:internationalAbstract:container:tablePanel:listContainer:items:2:itemProperties:1:component:border:border_body:txt",
                "another international title");
        tester.executeAjaxEvent(
                "form:internationalContent:internationalAbstract:container:tablePanel:listContainer:items:2:itemProperties:2:component:remove",
                "click");
        form = tester.newFormTester("form");
        form.submit("submit");
        tester.hasNoErrorMessage();
    }

    @Test
    public void testDefaultLocale() {
        login();

        // start the page
        tester.startPage(new WCSAdminPage());

        tester.assertRenderedPage(WCSAdminPage.class);

        FormTester form = tester.newFormTester("form");

        form.select("defaultLocale", 14);
        form.submit("submit");
        WCSInfo info = getGeoServer().getService(WCSInfo.class);
        assertNotNull(info.getDefaultLocale());
    }
}
