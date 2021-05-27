package org.geoserver.featurestemplating.configuration;

public class TemplateRuleBuilder {

    private String templateName;

    private boolean singleFeatureTemplate;

    private String outputFormat;

    private String service;

    private String operation;

    private String cqlTemplateRule;

    private String regex;

    public TemplateRuleBuilder() {}

    public TemplateRuleBuilder name(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public TemplateRuleBuilder singleFeature(boolean singleFeatureTemplate) {
        this.singleFeatureTemplate = singleFeatureTemplate;
        return this;
    }

    public TemplateRuleBuilder outputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    public TemplateRuleBuilder service(String service) {
        this.service = service;
        return this;
    }

    public TemplateRuleBuilder operation(String operation) {
        this.operation = operation;
        return this;
    }

    public TemplateRuleBuilder cqlRule(String cqlTemplateRule) {
        this.cqlTemplateRule = cqlTemplateRule;
        return this;
    }

    public TemplateRuleBuilder regex(String regex) {
        this.regex = regex;
        return this;
    }

    public TemplateRule build() {
        TemplateRule rule = new TemplateRule();
        rule.setTemplateName(this.templateName);
        rule.setOperation(this.operation);
        rule.setService(this.service);
        rule.setOutputFormat(this.outputFormat);
        rule.setRegex(this.regex);
        rule.setSingleFeatureTemplate(this.singleFeatureTemplate);
        rule.setCqlFilter(this.cqlTemplateRule);
        reset();
        return rule;
    }

    public void reset() {
        this.templateName = null;
        this.operation = null;
        this.service = null;
        this.outputFormat = null;
        this.singleFeatureTemplate = false;
        this.cqlTemplateRule = null;
        this.regex = null;
    }
}
