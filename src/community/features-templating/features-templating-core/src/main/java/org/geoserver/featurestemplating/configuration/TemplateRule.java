package org.geoserver.featurestemplating.configuration;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import org.geoserver.featurestemplating.expressions.RequestIsSingleFeature;
import org.geoserver.ows.Request;
import org.geoserver.util.XCQL;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;

@XmlRootElement(name = "rules")
public class TemplateRule implements Serializable {

    private String templateIdentifier;

    private String templateName;

    private boolean singleFeatureTemplate;

    private String outputFormat;

    private String service;

    private String operation;

    private String cqlFilter;

    private String regex;

    public String getTemplateName() {
        return templateName;
    }

    public boolean applyRule(Request request) {
        boolean result = true;
        if (outputFormat != null) {
            result =matchOutputFormat(request.getOutputFormat());
        }

        if (result && service != null) {
            result = request.getService().equals(service);
        }

        if (result && operation != null) {
            result = request.getOperation().equals(operation);
        }

        if (result) {
            RequestIsSingleFeature isSingleFeature = new RequestIsSingleFeature();
            Boolean isSingle = isSingleFeature.evaluate(null, Boolean.class);
            result = isSingle.booleanValue() == singleFeatureTemplate;
        }

        if (result && cqlFilter != null) {
            try {
                result = XCQL.toFilter(cqlFilter).evaluate(request);
            } catch (CQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (result && regex != null) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(request.getRequest());
            result = matcher.matches();
        }

        return result;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isSingleFeatureTemplate() {
        return singleFeatureTemplate;
    }

    public void setSingleFeatureTemplate(boolean singleFeatureTemplate) {
        this.singleFeatureTemplate = singleFeatureTemplate;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getCqlFilter() {
        return cqlFilter;
    }

    public void setCqlFilter(String cqlFilter) {
            this.cqlFilter = cqlFilter;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getTemplateIdentifier() {
        return templateIdentifier;
    }

    public void setTemplateIdentifier(String templateIdentifier) {
        this.templateIdentifier = templateIdentifier;
    }

    private boolean matchOutputFormat(String outputFormat){
        String nameIdentifier=TemplateIdentifier.getTemplateIdentifierFromOutputFormat(outputFormat).name();
        if (this.outputFormat.equals(SupportedMimeType.GML.name()))
            return nameIdentifier.startsWith(this.outputFormat);
        else
            return nameIdentifier.equals(this.outputFormat);
    }

    public void setTemplateInfo(TemplateInfo templateInfo){
        if (templateInfo!=null) {
            this.templateName = templateInfo.getFullName();
            this.templateIdentifier = templateInfo.getIdentifier();
        }
    }

    public TemplateInfo getTemplateInfo(){
        TemplateInfo ti=new TemplateInfo();
        if (templateName!=null && templateName.indexOf(":")!=-1){
            String [] nameSplit=templateName.split(":");
            if (nameSplit.length==3){
                ti.setWorkspace(nameSplit[0]);
                ti.setFeatureType(nameSplit[1]);
                ti.setTemplateName(nameSplit[2]);
            } else {
                ti.setWorkspace(nameSplit[0]);
                ti.setTemplateName(nameSplit[1]);
            }
        } else{
            ti.setIdentifier(ti.getIdentifier());
        }
        return ti;
    }
}
