package org.geoserver.featurestemplating.builders.visitors;

import org.geoserver.featurestemplating.builders.SourceBuilder;
import org.geoserver.featurestemplating.builders.impl.CompositeBuilder;
import org.geoserver.featurestemplating.builders.impl.DynamicValueBuilder;
import org.geoserver.featurestemplating.builders.impl.IteratingBuilder;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.featurestemplating.builders.impl.StaticBuilder;
import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.NestedAttributeMapping;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.FilterAttributeExtractor;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SimplifiedPropertyReplacer implements TemplateVisitor {

    FeatureTypeMapping featureTypeMapping;

    FeatureTypeMapping lastMatchedMapping;

    public SimplifiedPropertyReplacer(FeatureTypeMapping ftm) {
        this.featureTypeMapping=ftm;
    }

    @Override
    public Object visit(RootBuilder rootBuilder, Object extradata) {
        rootBuilder.getChildren().forEach(c->c.accept(this, extradata));
        return null;
    }

    @Override
    public Object visit(SourceBuilder sourceBuilder, Object extradata) {
        String source = sourceBuilder.getStrSource();
        String currentXpath = "";
        if (source!=null) {
            String[] splitted = source.split("/");
            FeatureTypeMapping context;
            if (extradata instanceof FeatureTypeMapping)
                context = (FeatureTypeMapping) extradata;
            else context = featureTypeMapping;

            if (context.getSource().getName().getLocalPart().equals(source))
                currentXpath=context.getTargetFeature().getName().toString();
            else {
                List<NestedAttributeMapping> nestedMappings = context.getNestedMappings();
                try {
                    for (String part : splitted) {
                        String xpathPart = findMatchingXpathInNestedList(part, nestedMappings, true);
                        if (xpathPart != null) {
                            if (!currentXpath.equals("")) currentXpath += "/";
                            currentXpath += xpathPart;
                        }
                        return null;
                    }
                } catch (Exception e) {

                }
                if (currentXpath != null)
                    sourceBuilder.setSource(currentXpath);
            }

        }
        Object newExtradata = currentXpath != null ? lastMatchedMapping : extradata;
        sourceBuilder.getChildren().forEach(c -> c.accept(this, newExtradata));
        return null;
    }

    @Override
    public Object visit(IteratingBuilder iteratingBuilder, Object extradata) {
        visit((SourceBuilder)iteratingBuilder,extradata);
        return null;
    }

    @Override
    public Object visit(CompositeBuilder compositeBuilder, Object extradata) {
        visit((SourceBuilder)compositeBuilder,extradata);
        return null;
    }

    @Override
    public Object visit(DynamicValueBuilder dynamicBuilder, Object extradata) {
        List<String> xpaths=new ArrayList<>();
        Expression expression = dynamicBuilder.getCql() != null ? dynamicBuilder.getCql() :
                dynamicBuilder.getXpath();
        try {
            FilterAttributeExtractor extractor = new FilterAttributeExtractor();
            expression.accept(extractor, null);
            String[] strProps = extractor.getAttributeNames();
            FeatureTypeMapping context;
            if (extradata instanceof FeatureTypeMapping)
                context=(FeatureTypeMapping) extradata;
            else context=featureTypeMapping;
            List<AttributeMapping> mappings = context.getAttributeMappings();
            List<NestedAttributeMapping> nestedMappings = context.getNestedMappings();
            for (String p : strProps) {
                String currentXpath="";
                String[] splitted = p.split("/");
                for (String part : splitted) {
                    boolean matchedInNested=false;
                    String xpathPart=findMatchingXpathInNestedList(part,nestedMappings,false);
                    if (xpathPart!=null){
                        matchedInNested=true;
                        if (!currentXpath.equals("")) currentXpath+="/";
                        currentXpath+=xpathPart;
                    }
                    if (!matchedInNested){
                        xpathPart=findMatchingXpathInAttributeMappingList(part,mappings);
                        if (xpathPart!=null){
                            if (!currentXpath.equals("")) currentXpath+="/";
                            currentXpath+=xpathPart;
                        }
                    }
                }
                xpaths.add(currentXpath);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        if(!xpaths.isEmpty()){
            DuplicatingFilterVisitor dupVisitor=new DuplicatingFilterVisitor(){
                @Override
                public Object visit(PropertyName expression, Object extraData) {
                    String xpathstr=xpaths.get(0);
                    AttributeExpressionImpl pn=new AttributeExpressionImpl(
                            xpaths.get(0), expression.getNamespaceContext());
                    xpaths.remove(xpathstr);
                    return pn;
                }
            };
            Expression simplified=(Expression) expression.accept(dupVisitor,null);
            if (simplified instanceof PropertyName)
                dynamicBuilder.setXpath((AttributeExpressionImpl)simplified);
            else
                dynamicBuilder.setCql(simplified);
        }
        dynamicBuilder.getChildren().forEach(c->c.accept(this, extradata));
        return null;
    }

    @Override
    public Object visit(StaticBuilder staticBuilder, Object extradata) {
        staticBuilder.getChildren().forEach(c->c.accept(this, extradata));
        return null;
    }


    private String findMatchingXpathInNestedList(String pathPart, List<NestedAttributeMapping> nestedMappings, boolean updateCurrentFtm) throws IOException {
        String result=null;
        for (NestedAttributeMapping mapping:nestedMappings){
            result=findMatchingXpathInNested(pathPart, mapping,updateCurrentFtm);
            if (result!=null)
                break;
        }
        return result;
    }

    private String findMatchingXpathInNested(String pathPart, NestedAttributeMapping nested, boolean updateCurrentFtm) throws IOException {
        FeatureTypeMapping ftm=nested.getFeatureTypeMapping(null);
        String result=null;
        if(ftm.getMappingName().getLocalPart().equals(pathPart)){
            result=nested.getTargetXPath().toString();//+"/"+ftm.getTargetFeature().getName().toString();;
            if (updateCurrentFtm)
                lastMatchedMapping=ftm;
        }
        return result;
    }

    private String findMatchingXpathInAttributeMappingList(String pathPart, List<AttributeMapping> mappings){
        String result=null;
        for (AttributeMapping mapping:mappings){
            FilterAttributeExtractor extractor= new FilterAttributeExtractor();
            mapping.getSourceExpression().accept(extractor,null);
            for (String attr:extractor.getAttributeNameSet()){
                if (attr.equals(pathPart)) {
                    result= mapping.getTargetXPath().toString();
                    break;
                }
            }
        }
        return result;
    }
}
