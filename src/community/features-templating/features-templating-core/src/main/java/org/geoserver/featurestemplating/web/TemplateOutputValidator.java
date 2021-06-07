package org.geoserver.featurestemplating.web;

import com.github.jsonldjava.utils.JsonUtils;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.geoserver.featurestemplating.configuration.SupportedFormat;
import org.geoserver.featurestemplating.validation.JSONLDContextValidation;
import org.w3c.dom.Document;

public class TemplateOutputValidator {

    private SupportedFormat outputFormat;

    private String message;

    public TemplateOutputValidator(SupportedFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public boolean validate(String input) {
        boolean result = true;
        if (outputFormat.equals(SupportedFormat.JSONLD)) result = validateJSONLD(input);
        else if (outputFormat.equals(SupportedFormat.GML)) result = validateGML(input);
        else if (outputFormat.equals(SupportedFormat.GEOJSON)) result = validateGeoJSON(input);
        if (result) this.message = "Result is valid";
        return result;
    }

    private boolean validateJSONLD(String input) {
        boolean result = true;
        try {
            Object json = JsonUtils.fromString(input);
            JSONLDContextValidation contextValidation = new JSONLDContextValidation();
            contextValidation.validate(json);
        } catch (Exception e) {
            message = e.getMessage();
            result = false;
        }
        return result;
    }

    private boolean validateGML(String input) {
        boolean result = true;
        try (ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes())) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            String schemaLocation =
                    doc.getElementsByTagName("wfs:FeatureCollection")
                            .item(0)
                            .getAttributes()
                            .getNamedItem("xsi:schemaLocation")
                            .getNodeValue();

            String[] urls = schemaLocation.split(" ");
            for (String xsdUrl : urls) {
                Source xmlFile = new StreamSource(new StringReader(input));
                SchemaFactory schemaFactory =
                        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new URL(xsdUrl));
                Validator validator = schema.newValidator();
                validator.validate(xmlFile);
            }
        } catch (Exception e) {
            message = e.getMessage();
            result = false;
        }

        return result;
    }

    private boolean validateGeoJSON(String input) {
        return true;
    }

    public String getMessage() {
        return message;
    }
}
