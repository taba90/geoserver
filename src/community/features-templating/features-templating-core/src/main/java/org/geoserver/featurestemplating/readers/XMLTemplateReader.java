package org.geoserver.featurestemplating.readers;

import static org.geoserver.featurestemplating.builders.EncodingHints.ENCODE_AS_ATTRIBUTE;
import static org.geoserver.featurestemplating.builders.EncodingHints.ROOT_ELEMENT_ATTRIBUTES;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.geoserver.featurestemplating.builders.TemplateBuilder;
import org.geoserver.featurestemplating.builders.TemplateBuilderMaker;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.xml.sax.helpers.NamespaceSupport;

public class XMLTemplateReader implements TemplateReader {

    private XMLEventReader reader;
    private TemplateBuilderMaker maker;
    private Stack<StartElement> eventStack;
    private NamespaceSupport namespaceSupport;

    public XMLTemplateReader(XMLEventReader reader, NamespaceSupport namespaceSupport) {
        this.reader = reader;
        eventStack = new Stack<>();
        maker = new TemplateBuilderMaker();
        this.namespaceSupport = namespaceSupport;
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
                    if (!eventStack.isEmpty()) eventStack.pop();
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCharacterEvent(Characters characters, TemplateBuilder currentParent) {
        String data = characters.getData();
        StartElement element = eventStack.peek();
        TemplateBuilder leafBuilder = createLeaf(data, element);
        currentParent.addChild(leafBuilder);
        addAttributeAsChildrenBuilder(element.getAttributes(), leafBuilder);
        iterateReader(currentParent);
    }

    private void handleStartElementEvent(StartElement startElement, TemplateBuilder currentParent) {
        if (startElement.getName().toString().equals("wfs:FeatureCollection"))
            handleFeatureCollectionElement(startElement, (RootBuilder) currentParent);
        else {
            StartElement parent = !eventStack.isEmpty() ? eventStack.peek() : null;
            eventStack.add(startElement);
            if (parent != null) {
                Attribute attribute = parent.getAttributeByName(new QName("isCollection"));
                String qName = parent.getName().toString();
                boolean collection =
                        attribute != null && Boolean.valueOf(attribute.getValue()).booleanValue();
                boolean isFeature = qName.equals("gml:featureMembers");
                maker.collection(collection)
                        .name(qName)
                        .namespaces(namespaceSupport)
                        .filter(getAttributeValueIfPresent(parent, "$filter"))
                        .source(getAttributeValueIfPresent(parent, "$source"))
                        .root(isFeature);
                TemplateBuilder parentBuilder = maker.build();
                Iterator<Attribute> attributeIterator = parent.getAttributes();
                addAttributeAsChildrenBuilder(attributeIterator, parentBuilder);
                currentParent.addChild(parentBuilder);
                currentParent = parentBuilder;
            }
        }
        iterateReader(currentParent);
    }

    private TemplateBuilder createLeaf(String data, StartElement startElement) {
        maker.namespaces(namespaceSupport)
                .name(startElement.getName().toString())
                .textContent(data);
        String filter = getAttributeValueIfPresent(startElement, "$filter");
        maker.filter(filter);
        TemplateBuilder builder = maker.build();

        return builder;
    }

    private void addAttributeAsChildrenBuilder(
            Iterator<Attribute> attributes, TemplateBuilder parentBuilder) {
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            if (!attribute.isNamespace()) {
                String localPart = attribute.getName().getLocalPart();
                if (!localPart.equals("$filter")
                        && !localPart.equals("$source")
                        && !localPart.equals("isCollection")) {
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
            String prefix = attribute.getName().getPrefix();
            if (prefix.startsWith("xmlns"))
                rootElementAttributes.addNamespace(
                        strName(attribute.getName()), attribute.getValue());
            else if (prefix.startsWith("xsi")) {
                rootElementAttributes.addSchemaLocations(
                        strName(attribute.getName()), attribute.getValue());
            }
        }
        rootBuilder.addEncodingHint(ROOT_ELEMENT_ATTRIBUTES, rootElementAttributes);
    }

    String strName(QName qName) {
        return qName.getPrefix() + ":" + qName.getLocalPart();
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
