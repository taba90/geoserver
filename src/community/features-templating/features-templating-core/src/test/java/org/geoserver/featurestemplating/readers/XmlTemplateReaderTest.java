package org.geoserver.featurestemplating.readers;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import org.geoserver.featurestemplating.builders.AbstractTemplateBuilder;
import org.geoserver.featurestemplating.builders.TemplateBuilder;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.featurestemplating.builders.impl.StaticBuilder;
import org.geoserver.platform.resource.FileSystemResourceStore;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.helpers.NamespaceSupport;

public class XmlTemplateReaderTest {

    FileSystemResourceStore store;

    @Before
    public void setupStore() {
        store = new FileSystemResourceStore(new File("src/test/resources/xmlincludes"));
    }

    @Test
    public void testParentInclusion() throws IOException {
        XMLTemplateReader reader =
                new XMLTemplateReader(
                        store.get("MappedFeatureIncludeParent.xml"), new NamespaceSupport());
        RootBuilder rootBuilder = reader.getRootBuilder();
        AbstractTemplateBuilder builder =
                (AbstractTemplateBuilder) rootBuilder.getChildren().get(0);
        assertEquals("wfs:FeatureCollection", builder.getKey());
        AbstractTemplateBuilder memberBuilder =
                (AbstractTemplateBuilder) builder.getChildren().get(0);
        assertEquals("wfs:member", memberBuilder.getKey());
        AbstractTemplateBuilder featureBuilder =
                (AbstractTemplateBuilder) memberBuilder.getChildren().get(0);
        assertEquals("gsml:MappedFeature", featureBuilder.getKey());
        assertEquals(5, featureBuilder.getChildren().size());
    }

    @Test
    public void testFlatInclusion() throws IOException {
        XMLTemplateReader reader =
                new XMLTemplateReader(
                        store.get("MappedFeatureIncludeFlat.xml"), new NamespaceSupport());
        RootBuilder rootBuilder = reader.getRootBuilder();
        TemplateBuilder mappedFeatureBuilder =
                rootBuilder.getChildren().get(0).getChildren().get(0).getChildren().get(0);
        TemplateBuilder specification = null;
        for (TemplateBuilder b : mappedFeatureBuilder.getChildren()) {
            if (((AbstractTemplateBuilder) b).getKey().equals("gsml:specification")) {
                specification = b;
                break;
            }
        }
        assertNotNull(specification);
        assertEquals(2, specification.getChildren().size());
        assertTrue(specification.getChildren().get(1) instanceof StaticBuilder);
        AbstractTemplateBuilder geologicUnit =
                (AbstractTemplateBuilder) specification.getChildren().get(0);
        assertEquals("gsml:GeologicUnit", geologicUnit.getKey());
        assertEquals(4, geologicUnit.getChildren().size());
    }

    @Test
    public void testInlineInclusion() throws IOException {
        XMLTemplateReader reader =
                new XMLTemplateReader(
                        store.get("MappedFeatureInclude.xml"), new NamespaceSupport());
        RootBuilder rootBuilder = reader.getRootBuilder();
        TemplateBuilder mappedFeatureBuilder =
                rootBuilder.getChildren().get(0).getChildren().get(0).getChildren().get(0);
        TemplateBuilder specification = null;
        for (TemplateBuilder b : mappedFeatureBuilder.getChildren()) {
            if (((AbstractTemplateBuilder) b).getKey().equals("gsml:specification")) {
                specification = b;
                break;
            }
        }
        assertNotNull(specification);
        AbstractTemplateBuilder geologicUnit =
                (AbstractTemplateBuilder) specification.getChildren().get(0);
        assertEquals("gsml:GeologicUnit", geologicUnit.getKey());
        assertTrue(geologicUnit.getChildren().size() > 0);
    }

    @Test
    public void testNotExistingInclusion() {
        checkThrowingTemplate("MappedFeatureIncludeNotExisting.xml");
    }

    @Test
    public void testRecursiveInclusion() {
        RuntimeException ex = checkThrowingTemplate("ping.xml");
        assertThat(
                ex.getMessage(),
                containsString(
                        "Went beyond maximum nested inclusion depth (51), inclusion chain is: [ping.xml"));
    }

    private RuntimeException checkThrowingTemplate(String s) {
        return assertThrows(
                RuntimeException.class,
                () -> new XMLTemplateReader(store.get(s), new NamespaceSupport()).getRootBuilder());
    }
}
