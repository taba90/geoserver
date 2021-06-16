package org.geoserver.featurestemplating.web;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.geoserver.featurestemplating.configuration.TemplateFileManager;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfoDao;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.ParamResourceModel;
import org.geoserver.web.wicket.SimpleAjaxLink;

public class TemplateInfoPage extends GeoServerSecuredPage {

    private GeoServerTablePanel<TemplateInfo> tablePanel;

    private AjaxLink<Object> remove;

    public TemplateInfoPage() {
        add(
                new AjaxLink<Object>("addNew") {

                    private static final long serialVersionUID = -4136656891019857299L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        setResponsePage(
                                new TemplateConfigurationPage(
                                        new Model<>(new TemplateInfo()), true));
                    }
                });

        add(
                remove =
                        new AjaxLink<Object>("removeSelected") {
                            private static final long serialVersionUID = 2421854498051377608L;

                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                TemplateInfoDao dao = TemplateInfoDao.get();
                                TemplateFileManager fileManager = TemplateFileManager.get();
                                dao.findAll().forEach(ti -> fileManager.delete(ti));
                                dao.deleteAll(tablePanel.getSelection());
                                tablePanel.modelChanged();
                                target.add(tablePanel);
                                target.add(TemplateInfoPage.this);
                            }

                            @Override
                            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                                super.updateAjaxAttributes(attributes);
                                attributes
                                        .getAjaxCallListeners()
                                        .add(
                                                new AjaxCallListener() {

                                                    @Override
                                                    public CharSequence getPrecondition(
                                                            Component component) {
                                                        CharSequence message =
                                                                new ParamResourceModel(
                                                                                "confirmRemove",
                                                                                TemplateInfoPage
                                                                                        .this)
                                                                        .getString();
                                                        message =
                                                                JavaScriptUtils.escapeQuotes(
                                                                        message);
                                                        return "return confirm('" + message + "');";
                                                    }
                                                });
                            }
                        });
        tablePanel =
                new GeoServerTablePanel<TemplateInfo>(
                        "tablePanel", new TemplateInfoProvider(), true) {
                    @Override
                    protected Component getComponentForProperty(
                            String id,
                            IModel<TemplateInfo> itemModel,
                            GeoServerDataProvider.Property<TemplateInfo> property) {
                        if (property.equals(TemplateInfoProvider.NAME)) {
                            return new SimpleAjaxLink<TemplateInfo>(
                                    id, itemModel, TemplateInfoProvider.NAME.getModel(itemModel)) {

                                @Override
                                protected void onClick(AjaxRequestTarget target) {
                                    setResponsePage(
                                            new TemplateConfigurationPage(getModel(), false));
                                }
                            };
                        } else if (property.equals(TemplateInfoProvider.EXTENSION))
                            return new Label(
                                    id, TemplateInfoProvider.EXTENSION.getModel(itemModel));
                        else if (property.equals(TemplateInfoProvider.WORKSPACE))
                            return new Label(
                                    id, TemplateInfoProvider.WORKSPACE.getModel(itemModel));
                        else if (property.equals(TemplateInfoProvider.FEATURE_TYPE_INFO)) {
                            return new Label(
                                    id, TemplateInfoProvider.FEATURE_TYPE_INFO.getModel(itemModel));
                        }
                        return null;
                    }

                    @Override
                    protected void onSelectionUpdate(AjaxRequestTarget target) {
                        remove.setEnabled(tablePanel.getSelection().size() > 0);
                        target.add(remove);
                    }
                };
        tablePanel.setOutputMarkupId(true);
        tablePanel.setEnabled(true);
        add(tablePanel);
        remove.setOutputMarkupId(true);
        remove.setEnabled(false);
    }
}
