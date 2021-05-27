package org.geoserver.featurestemplating.configuration;

import org.geoserver.featurestemplating.expressions.RequestIsSingleFeature;
import org.geoserver.ows.Request;
import org.geoserver.util.XCQL;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XmlRootElement(name = "rules")
public class TemplateRule implements Serializable {

    private String templateName;

    private boolean singleFeatureTemplate;

    private String outputFormat;

    private String service;

    private String operation;

    private Filter cqlFilter;

    private String regex;

    public String getTemplateName() {
        return templateName;
    }

    public boolean applyRule (Request request){
        boolean result=true;
        if (outputFormat!=null){
            result=outputFormat.equalsIgnoreCase(request.getOutputFormat());
        }

        if (result && service!=null){
            result=request.getService().equals(service);
        }

        if (result && operation!=null){
            result=request.getOperation().equals(operation);
        }

        if (result){
            RequestIsSingleFeature isSingleFeature = new RequestIsSingleFeature();
            Boolean isSingle=isSingleFeature.evaluate(null,Boolean.class);
            result=isSingle.booleanValue() == singleFeatureTemplate;
        }

        if (result && cqlFilter !=null){
            result= cqlFilter.evaluate(request);
        }

        if (result && regex!=null){
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher=pattern.matcher(request.getRequest());
            result=matcher.matches();
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
        if (cqlFilter!=null)
            return CQL.toCQL(cqlFilter);
        return null;
    }

    public void setCqlFilter(String cqlFilter) {
        try {
            this.cqlFilter = XCQL.toFilter(cqlFilter);
        } catch (CQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
