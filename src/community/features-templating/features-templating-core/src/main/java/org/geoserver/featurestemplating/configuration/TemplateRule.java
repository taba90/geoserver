package org.geoserver.featurestemplating.configuration;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import javax.xml.bind.annotation.XmlRootElement;

import org.geoserver.ows.Request;
import org.geoserver.util.XCQL;
import org.geotools.filter.text.cql2.CQLException;
import org.springframework.http.HttpHeaders;

@XmlRootElement(name = "rules")
public class TemplateRule implements Serializable {

    private String ruleId;

    private String templateIdentifier;

    private String templateName;

    private String outputFormat;

    private String service;

    private String cqlFilter;

    private boolean forceRule;


    public TemplateRule(){
        this.ruleId= UUID.randomUUID().toString();
    }

    public String getTemplateName() {
        return templateName;
    }

    public boolean applyRule(Request request) {
        boolean result = true;
        if (outputFormat != null) {
            result = matchOutputFormat(getOutputFormat(request));
        }

        if (result && cqlFilter != null) {
            try {
                result = XCQL.toFilter(cqlFilter).evaluate(request);
            } catch (CQLException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
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

    public String getCqlFilter() {
        return cqlFilter;
    }

    public void setCqlFilter(String cqlFilter) {
        this.cqlFilter = cqlFilter;
    }

    public String getTemplateIdentifier() {
        return templateIdentifier;
    }

    public void setTemplateIdentifier(String templateIdentifier) {
        this.templateIdentifier = templateIdentifier;
    }

    private boolean matchOutputFormat(String outputFormat) {
        TemplateIdentifier identifier=TemplateIdentifier.getTemplateIdentifierFromOutputFormat(outputFormat);
        if (identifier==null) return false;
        String nameIdentifier = identifier.name();
        if (this.outputFormat.equals(SupportedMimeType.GML.name()))
            return nameIdentifier.startsWith(this.outputFormat);
        else if (this.outputFormat.equals(SupportedMimeType.GEOJSON.name()))
            return nameIdentifier.equals(TemplateIdentifier.GEOJSON.name()) || nameIdentifier.equals(TemplateIdentifier.JSON.name());
        else return nameIdentifier.equals(this.outputFormat);
    }

    public void setTemplateInfo(TemplateInfo templateInfo) {
        if (templateInfo != null) {
            this.templateName = templateInfo.getFullName();
            this.templateIdentifier = templateInfo.getIdentifier();
        }
    }

    public TemplateInfo getTemplateInfo() {
        TemplateInfo ti = new TemplateInfo();
        if (templateName != null && templateName.indexOf(":") != -1) {
            String[] nameSplit = templateName.split(":");
            if (nameSplit.length == 3) {
                ti.setWorkspace(nameSplit[0]);
                ti.setFeatureType(nameSplit[1]);
                ti.setTemplateName(nameSplit[2]);
            } else {
                ti.setWorkspace(nameSplit[0]);
                ti.setTemplateName(nameSplit[1]);
            }
        }
        ti.setIdentifier(templateIdentifier);
        return ti;
    }

    private String getOutputFormat(Request request) {
        String outputFormat = request.getOutputFormat();
        if (outputFormat == null)
            outputFormat = request.getHttpRequest().getHeader(HttpHeaders.ACCEPT);
        if (outputFormat == null)
            outputFormat = request.getKvp() != null ? (String) request.getKvp().get("f") : null;
        return outputFormat;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public boolean isForceRule() {
        return forceRule;
    }

    public void setForceRule(boolean forceRule) {
        this.forceRule = forceRule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateRule that = (TemplateRule) o;
        return ruleId == that.ruleId
                && Objects.equals(templateIdentifier, that.templateIdentifier)
                && Objects.equals(templateName, that.templateName)
                && Objects.equals(outputFormat, that.outputFormat)
                && Objects.equals(service, that.service)
                && Objects.equals(cqlFilter, that.cqlFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                ruleId,
                templateIdentifier,
                templateName,
                outputFormat,
                service,
                cqlFilter);
    }
}
