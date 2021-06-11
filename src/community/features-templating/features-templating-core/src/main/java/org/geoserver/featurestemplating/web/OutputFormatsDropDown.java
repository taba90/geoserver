package org.geoserver.featurestemplating.web;

import java.util.Arrays;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.geoserver.featurestemplating.configuration.SupportedFormat;

public class OutputFormatsDropDown extends DropDownChoice<SupportedFormat> {

    public OutputFormatsDropDown(String id, IModel<SupportedFormat> model) {
        super(id);
        this.setChoices(Arrays.asList(SupportedFormat.values()));
        this.setModel(model);
        this.setChoiceRenderer(getFormatChoiceRenderer());
    }

    public OutputFormatsDropDown(
            String id, IModel<SupportedFormat> model, String templateExtension) {
        super(id);
        this.setChoices(SupportedFormat.getByExtension(templateExtension));
        this.setModel(model);
        this.setChoiceRenderer(getFormatChoiceRenderer());
    }

    private EnumChoiceRenderer<SupportedFormat> getFormatChoiceRenderer() {
        return new EnumChoiceRenderer<SupportedFormat>() {
            @Override
            public String getDisplayValue(SupportedFormat object) {
                return object.getFormat();
            }
        };
    }
}
