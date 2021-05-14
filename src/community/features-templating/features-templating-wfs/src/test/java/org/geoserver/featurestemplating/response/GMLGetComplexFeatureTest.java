package org.geoserver.featurestemplating.response;

import java.io.IOException;
import org.geoserver.featurestemplating.configuration.TemplateIdentifier;
import org.junit.Test;
import org.w3c.dom.Document;

public class GMLGetComplexFeatureTest extends TemplateComplexTestSupport {

    @Test
    public void getMappedFeature() throws IOException {
        setUpComplex("MappedFeatureGML31.xml", mappedFeature);
        Document doc =
                getAsDOM(
                        "wfs?request=GetFeature&version=1.1.0&typename=gsml:MappedFeature&outputFormat=gml3");
        assertXpathCount(5, "//gsml:MappedFeature", doc);
        assertXpathCount(5, "//gsml:MappedFeature/gsml:geometry/gml:Polygon", doc);
        assertXpathEvaluatesTo("GUNTHORPE FORMATION", "//gsml:MappedFeature/gml:name", doc);
        assertXpathCount(2, "//gsml:GeologicUnit", doc);
        assertXpathCount(4, "//gsml:lithology", doc);
    }

    @Test
    public void getMappedFeatureBackwardsMapping() throws IOException {
        setUpComplex("MappedFeatureGML31.xml", mappedFeature);
        Document doc =
                getAsDOM(
                        "wfs?request=GetFeature&version=1.1.0&typename=gsml:MappedFeature&outputFormat=gml3"
                                + "&cql_filter=wfs:FeatureCollection.gml:featureMember"
                                + ".gsml:MappedFeature.gsml:specification.gsml:GeologicUnit"
                                + ".gsml:composition.gsml:CompositionPart.gsml:lithology.gsml:ControlledConcept.gsml:name = 'name_cc_3'");
        assertXpathCount(2, "//gsml:MappedFeature", doc);
    }

    @Override
    protected String getTemplateFileName() {
        return TemplateIdentifier.GML31.getFilename();
    }
}
