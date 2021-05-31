package org.geoserver.featurestemplating.web;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.geoserver.featurestemplating.configuration.SupportedMimeType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutputFormatsDropDown extends DropDownChoice<String> {

    public OutputFormatsDropDown(String id, IModel<String> model) {
        super(id);
        List<String> mimeTypes=getSupportedOutputFormats();
        this.setChoices(mimeTypes);
        this.setModel(model);
    }

    private List<String> getSupportedOutputFormats() {
        return Stream.of(SupportedMimeType.values())
                .map(smt -> smt.name())
                .collect(Collectors.toList());
    }
}
