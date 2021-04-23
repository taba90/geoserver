package org.geoserver.featurestemplating.configuration;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;

public class TemplateRule implements Serializable {

    private String regex;

    public TemplateRule(String regex) {
        this.regex = regex;
    }

    public boolean applies(HttpServletRequest request) {
        return true;
    }

    public String getRegex() {
        return regex;
    }
}
