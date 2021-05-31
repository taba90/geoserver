package org.geoserver.featurestemplating.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.geoserver.ows.Request;

@XmlRootElement(name = "TemplateLayerConfig")
public class TemplateLayerConfig implements Serializable {

    public static final String METADATA_KEY = "FEATURES_TEMPLATING_LAYER_CONF";

    @XmlElement(name = "rules")
    private List<TemplateRule> templateRules;

    public TemplateLayerConfig(List<TemplateRule> templateRules) {
        this.templateRules = templateRules;
    }

    public TemplateLayerConfig() {
        templateRules=new ArrayList<>();
    }

    public void addTemplateRule(TemplateRule rule) {
        if (this.templateRules==null)
            templateRules=new ArrayList<>();
        this.templateRules.add(rule);
    }

    public void updateRule(TemplateRule rule) {
        Optional<TemplateRule> ruleOptional =
                this.templateRules
                        .stream()
                        .filter(r -> r.getTemplateName().equals(rule.getTemplateName()))
                        .findFirst();
        ruleOptional.ifPresent(r -> templateRules.remove(r));
        templateRules.add(rule);
    }

    public void deleteRule(TemplateRule rule) {
        templateRules.remove(rule);
    }

    public String getTemplateNameMatchingRequest(Request request) {
        TemplateRule matchedRule = null;
        for (TemplateRule rule : templateRules) {
            if (rule.applyRule(request)) {
                matchedRule = rule;
                break;
            }
        }
        return matchedRule != null ? matchedRule.getTemplateName() : null;
    }

    public List<TemplateRule> getTemplateRules() {
        if (this.templateRules==null)
            this.templateRules=new ArrayList<>();
        return templateRules;
    }
}
