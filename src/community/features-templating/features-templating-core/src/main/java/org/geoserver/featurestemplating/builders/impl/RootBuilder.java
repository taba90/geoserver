/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.builders.impl;

import java.io.IOException;
import java.util.*;
import org.geoserver.featurestemplating.builders.EncodingHints;
import org.geoserver.featurestemplating.builders.TemplateBuilder;
import org.geoserver.featurestemplating.builders.VendorOptions;
import org.geoserver.featurestemplating.builders.flat.FlatBuilder;
import org.geoserver.featurestemplating.builders.visitors.TemplateVisitor;
import org.geoserver.featurestemplating.expressions.TemplateCQLManager;
import org.geoserver.featurestemplating.writers.TemplateOutputWriter;

/** The root of the builders' tree. It triggers the evaluation process */
public class RootBuilder implements TemplateBuilder {

    private List<TemplateBuilder> children;

    private VendorOptions vendorOptions;

    private EncodingHints encodingHints;

    protected List<String> supportedOptions = new ArrayList<>();

    public RootBuilder() {
        super();
        this.children = new ArrayList<>(2);
        this.vendorOptions = new VendorOptions();
    }

    @Override
    public void addChild(TemplateBuilder builder) {
        this.children.add(builder);
    }

    @Override
    public void evaluate(TemplateOutputWriter writer, TemplateBuilderContext context)
            throws IOException {
        for (TemplateBuilder jb : children) {
            jb.evaluate(writer, context);
        }
    }

    @Override
    public List<TemplateBuilder> getChildren() {
        return children;
    }

    /**
     * Set the vendor option
     *
     * @param vendorOption a string array containing vendor option name and value
     */
    public void setVendorOptions(String[] vendorOption) {
        TemplateCQLManager cqlManager = new TemplateCQLManager(vendorOption[1], null);
        vendorOptions.put(vendorOption[0], cqlManager.getExpressionFromString());
    }

    public void addVendorOption(String name, String value) {
        vendorOptions.put(name, value);
    }

    public void addVendorOptions(VendorOptions vendorOptions) {
        vendorOptions.putAll(vendorOptions);
    }

    public boolean needsReload() {
        TemplateBuilder aChild = getChildren().get(0);
        boolean isCachedFlattened = aChild instanceof FlatBuilder;
        boolean isFlatOutput =
                vendorOptions.get(VendorOptions.FLAT_OUTPUT, Boolean.class, false).booleanValue();
        if (isCachedFlattened && !isFlatOutput) return true;
        else if (!isCachedFlattened && isFlatOutput) return true;
        else return false;
    }

    @Override
    public void addEncodingHint(String key, Object value) {
        if (encodingHints == null) this.encodingHints = new EncodingHints();
        encodingHints.put(key, value);
    }

    @Override
    public EncodingHints getEncodingHints() {
        if (encodingHints == null) encodingHints = new EncodingHints();
        return encodingHints;
    }

    public VendorOptions getVendorOptions() {
        return this.vendorOptions;
    }

    @Override
    public Object accept(TemplateVisitor visitor, Object value) {
        return visitor.visit(this, value);
    }
}
