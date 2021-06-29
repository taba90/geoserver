package org.geoserver.web.data.resource;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

public class LocalesDropdown extends DropDownChoice<Locale> {

    public LocalesDropdown(String id, IModel<Locale> model) {
        super(id, model, getLocales());
        ChoiceRenderer<Locale> locales =
                new ChoiceRenderer<Locale>() {
                    @Override
                    public Object getDisplayValue(Locale object) {
                        return object.toLanguageTag();
                    }

                    @Override
                    public String getIdValue(Locale object, int index) {
                        return object.toLanguageTag();
                    }
                };
        this.setChoiceRenderer(locales);
    }

    private static List<Locale> getLocales() {
        return Stream.of(Locale.getAvailableLocales())
                .filter(l -> l != null)
                .sorted(
                        new Comparator<Locale>() {
                            @Override
                            public int compare(Locale o1, Locale o2) {
                                return o1.toLanguageTag().compareTo(o2.toLanguageTag());
                            }
                        })
                .collect(Collectors.toList());
    }
}
