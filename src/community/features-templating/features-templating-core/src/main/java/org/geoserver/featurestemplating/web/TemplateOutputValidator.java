package org.geoserver.featurestemplating.web;

import com.github.jsonldjava.utils.JsonUtils;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.geoserver.featurestemplating.configuration.SupportedFormat;
import org.geoserver.featurestemplating.configuration.ValidationSchemaCache;
import org.geoserver.featurestemplating.validation.JSONLDContextValidation;
import org.geoserver.util.ErrorHandler;
import org.geotools.xml.resolver.SchemaCache;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

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
        TemplateValidationErrorHandler errorHandler=new TemplateValidationErrorHandler();
        try (ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes())) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            dbFactory.setValidating(true);
            dbFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            dBuilder.setEntityResolver(ValidationSchemaCache.get());
            dBuilder.setErrorHandler(errorHandler);
            dBuilder.parse(is);
        } catch (Exception e) {
            message = e.getMessage();
            result = false;
        }
        if(!errorHandler.errors.isEmpty()){
            result=false;
            StringBuilder builder=new StringBuilder("The following errors occured while validating the gml output: ");
            for (int i=0; i<errorHandler.errors.size();i++){
                String error =errorHandler.errors.get(i);
                builder.append(i+1).append(" ").append(error).append(".");
            }
            this.message=builder.toString();
        }

        return result;
    }

    private boolean validateGeoJSON(String input) {
        return true;
    }

    public String getMessage() {
        return message;
    }

    public class TemplateValidationErrorHandler extends DefaultHandler {

        List<String> errors=new ArrayList<>();
        @Override
        public void error(SAXParseException e) throws SAXException {
            super.error(e);
            String message=e.getMessage();
            if (message.startsWith("cvc-elt"))
                errors.add(e.getMessage());
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            super.fatalError(e);
            errors.add(e.getMessage());
        }
    }
}
