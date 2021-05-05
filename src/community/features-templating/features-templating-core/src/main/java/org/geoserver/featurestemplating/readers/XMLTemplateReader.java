package org.geoserver.featurestemplating.readers;

import static org.geoserver.featurestemplating.builders.EncodingHints.ENCODE_AS_ATTRIBUTE;
import static org.geoserver.featurestemplating.builders.EncodingHints.REPEAT;
import static org.geoserver.featurestemplating.builders.EncodingHints.ROOT_ELEMENT_ATTRIBUTES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.geoserver.featurestemplating.builders.TemplateBuilder;
import org.geoserver.featurestemplating.builders.TemplateBuilderMaker;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.xml.sax.helpers.NamespaceSupport;

public class XMLTemplateReader implements TemplateReader {

    private XMLEventReader reader;
    private TemplateBuilderMaker maker;
    private Stack<StartElement> elementsStack;
    private NamespaceSupport namespaceSupport;
    private List<StartElement> parsedElements;

    private static final String FEATURES_TEMPLATING_NS_URI =
            "http://localhost:8080/geoserver/features-templating";
    private static final String FEATURES_TEMPLATING_NS_PREFIX = "gft";

    public XMLTemplateReader(XMLEventReader reader, NamespaceSupport namespaceSupport) {
        this.reader = reader;
        this.elementsStack = new Stack<>();
        this.maker = new TemplateBuilderMaker();
        this.namespaceSupport = namespaceSupport;
        this.parsedElements = new ArrayList<>();
    }

    @Override
    public RootBuilder getRootBuilder() {
        RootBuilder rootBuilder = new RootBuilder();

        try {
            iterateReader(rootBuilder);
        } finally {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        return rootBuilder;
    }

    private void iterateReader(TemplateBuilder builder) {
        try {
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement())
                    handleStartElementEvent(event.asStartElement(), builder);
                else if (event.isCharacters()) {
                    Characters characters = event.asCharacters();
                    if (!characters.isIgnorableWhiteSpace()
                            && !characters.isWhiteSpace()
                            && !characters.isEntityReference())
                        handleCharacterEvent(event.asCharacters(), builder);
                } else if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    boolean alreadyParsed = alreadyParsed(endElement);
                    if (!alreadyParsed && !elementsStack.isEmpty()) {
                        buildTemplateBuilderFromElement(elementsStack.pop(), builder);
                    } else {
                        while (!elementsStack.isEmpty()) {
                            buildTemplateBuilderFromElement(elementsStack.pop(), builder);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCharacterEvent(Characters characters, TemplateBuilder currentParent) {
        String data = characters.getData();
        StartElement element = elementsStack.pop();
        TemplateBuilder leafBuilder;
        if (getAttributeValueIfPresent(element, "isCollection") != null)
            leafBuilder = createLeaf(data, null);
        else leafBuilder = createLeaf(data, element);
        parsedElements.add(element);
        currentParent.addChild(leafBuilder);
        addAttributeAsChildrenBuilder(element.getAttributes(), leafBuilder);
        iterateReader(currentParent);
    }

    private void handleStartElementEvent(StartElement startElement, TemplateBuilder currentParent) {
        if (startElement.getName().toString().equals("wfs:FeatureCollection"))
            handleFeatureCollectionElement(startElement, (RootBuilder) currentParent);
        else if (!elementsStack.isEmpty()) {
            StartElement previous = !elementsStack.isEmpty() ? elementsStack.pop() : null;
            currentParent = buildTemplateBuilderFromElement(previous, currentParent);
            parsedElements.add(previous);
            elementsStack.add(startElement);
            iterateReader(currentParent);
        } else {
            elementsStack.add(startElement);
        }
    }

    private TemplateBuilder buildTemplateBuilderFromElement(
            StartElement startElement, TemplateBuilder currentParent) {
        if (startElement != null && !parsedElements.contains(startElement)) {
            Attribute attribute = startElement.getAttributeByName(new QName("gft:isCollection"));
            String qName = startElement.getName().toString();
            boolean isRootCollection = qName.equals("gml:featureMembers");
            boolean collection =
                    isRootCollection
                            || (attribute != null
                                    && Boolean.valueOf(attribute.getValue()).booleanValue());
            maker.collection(collection)
                    .name(qName)
                    .namespaces(namespaceSupport)
                    .filter(getAttributeValueIfPresent(startElement, "gft:filter"))
                    .source(getAttributeValueIfPresent(startElement, "gft:source"))
                    .root(isRootCollection);
            if (collection) {
                maker.encodingOption(REPEAT, "true");
            }
            TemplateBuilder parentBuilder = maker.build();
            Iterator<Attribute> attributeIterator = startElement.getAttributes();
            addAttributeAsChildrenBuilder(attributeIterator, parentBuilder);
            currentParent.addChild(parentBuilder);
            currentParent = parentBuilder;
        }
        return currentParent;
    }

    private TemplateBuilder createLeaf(String data, StartElement startElement) {
        maker.namespaces(namespaceSupport).textContent(data);
        if (startElement != null) {
            maker.name(startElement.getName().toString());
            String filter = getAttributeValueIfPresent(startElement, "gft:filter");
            maker.filter(filter);
        }
        TemplateBuilder builder = maker.build();

        return builder;
    }

    private void addAttributeAsChildrenBuilder(
            Iterator<Attribute> attributes, TemplateBuilder parentBuilder) {
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            if (!attribute.isNamespace()) {
                String localPart = attribute.getName().getLocalPart();
                if (!localPart.equals("gft:filter")
                        && !localPart.equals("gft:source")
                        && !localPart.equals("gft:isCollection")) {
                    maker.namespaces(namespaceSupport)
                            .name(attribute.getName().toString())
                            .textContent(attribute.getValue())
                            .encodingOption(ENCODE_AS_ATTRIBUTE, true);
                    parentBuilder.addChild(maker.build());
                }
            }
        }
    }

    private String getAttributeValueIfPresent(StartElement startElement, String attributeName) {
        Attribute filter = startElement.getAttributeByName(new QName(attributeName));
        if (filter == null) return null;
        return filter.getValue();
    }

    private void handleFeatureCollectionElement(
            StartElement startElementEvent, RootBuilder rootBuilder) {
        Iterator<Attribute> attributeIterator = startElementEvent.getAttributes();
        RootElementAttributes rootElementAttributes = new RootElementAttributes();
        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();
            String prefix = attribute.getName().getLocalPart();
            if (prefix.startsWith("xmlns")) {
                String localPart = prefix.split(":")[1];
                rootElementAttributes.addNamespace(localPart, attribute.getValue());
            } else if (prefix.startsWith("xsi")) {
                rootElementAttributes.addSchemaLocations(
                        strName(attribute.getName()), attribute.getValue());
            }
        }
        rootBuilder.addEncodingHint(ROOT_ELEMENT_ATTRIBUTES, rootElementAttributes);
    }

    String strName(QName qName) {
        return qName.getLocalPart();
    }

    private boolean alreadyParsed(EndElement endElement) {
        return parsedElements.stream().anyMatch(se -> se.getName().equals(endElement.getName()));
    }

    public static class RootElementAttributes {
        private Map<String, String> namespaces;
        private Map<String, String> schemaLocations;

        public RootElementAttributes() {
            this.namespaces = new HashMap<>();
            this.schemaLocations = new HashMap<>();
        }

        public Map<String, String> getNamespaces() {
            return namespaces;
        }

        public Map<String, String> getSchemaLocations() {
            return schemaLocations;
        }

        public void addNamespace(String prefix, String value) {
            namespaces.put(prefix, value);
        }

        public void addSchemaLocations(String prefix, String value) {
            schemaLocations.put(prefix, value);
        }
    }
}
