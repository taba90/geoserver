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
import org.geotools.data.complex.filter.MultipleValueExtractor;
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
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class SimplifiedPropertyReplacer implements TemplateVisitor {


    private Stack<FeatureTypeMapping> mappingsStack;

    public SimplifiedPropertyReplacer(FeatureTypeMapping ftm) {
        this.mappingsStack= new Stack();
        mappingsStack.add(ftm);
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
            source=source.replaceAll("->","").replaceAll(" ","");
            String[] splitted = source.split("/");
            FeatureTypeMapping context = mappingsStack.peek();

            if (context.getSource().getName().getLocalPart().equals(source))
                currentXpath=strName(context.getTargetFeature().getName());
            else {
                List<NestedAttributeMapping> nestedMappings = context.getNestedMappings();
                try {
                    for (String part : splitted) {
                        String xpathPart = findMatchingXpathInNestedList(part, nestedMappings);
                        if (xpathPart != null) {
                            if (!currentXpath.equals("")) currentXpath += "/";
                            currentXpath += xpathPart;
                        }
                    }
                } catch (Exception e) {

                }
            }

        }
        if (currentXpath != null && !currentXpath.equals(""))
            sourceBuilder.setSource(currentXpath);
        sourceBuilder.getChildren().forEach(c -> c.accept(this, extradata));
        if (source!=null && !mappingsStack.isEmpty())
            mappingsStack.pop();
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
            MultipleValueExtractor extractor = new MultipleValueExtractor();
            expression.accept(extractor, null);
            String[] strProps = extractor.getAttributeNames();
            FeatureTypeMapping context=mappingsStack.peek();
            List<AttributeMapping> mappings = context.getAttributeMappings();
            for (String p : strProps) {
                String currentXpath="";
                p=p.replaceAll("->","").replaceAll(" ","");
                String[] splitted = p.split("/");
                boolean isNested=false;
                for (String part : splitted) {
                    if (isNested) context=mappingsStack.pop();
                    boolean matchedInNested=false;
                    String xpathPart=findMatchingXpathInNestedList(part,context.getNestedMappings());
                    if (xpathPart!=null){
                        matchedInNested=true;
                        isNested=true;
                        if (!currentXpath.equals("")) currentXpath+="/";
                        currentXpath+=xpathPart;
                    }
                    if (!matchedInNested){

                        xpathPart=findMatchingXpathInAttributeMappingList(part,mappings.stream().filter(m->!(m instanceof NestedAttributeMapping)).collect(Collectors.toList()));
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
        if(!xpaths.isEmpty()) {
            if (!xpaths.get(0).equals("")) {
                DuplicatingFilterVisitor dupVisitor = new DuplicatingFilterVisitor() {
                    @Override
                    public Object visit(PropertyName expression, Object extraData) {
                        String xpathstr = xpaths.get(0);
                        AttributeExpressionImpl pn = new AttributeExpressionImpl(
                                xpaths.get(0), expression.getNamespaceContext());
                        xpaths.remove(xpathstr);
                        return pn;
                    }
                };
                Expression simplified = (Expression) expression.accept(dupVisitor, null);
                if (simplified instanceof PropertyName)
                    dynamicBuilder.setXpath((AttributeExpressionImpl) simplified);
                else
                    dynamicBuilder.setCql(simplified);
            }
        }
        dynamicBuilder.getChildren().forEach(c->c.accept(this, extradata));
        return null;
    }

    @Override
    public Object visit(StaticBuilder staticBuilder, Object extradata) {
        staticBuilder.getChildren().forEach(c->c.accept(this, extradata));
        return null;
    }



    private String findMatchingXpathInNestedList(String pathPart, List<NestedAttributeMapping> nestedMappings) throws IOException {
        String result=null;
        for (NestedAttributeMapping mapping:nestedMappings){
            result=findMatchingXpathInNested(pathPart, mapping);
            if (result!=null)
                break;
        }
        return result;
    }

    private String findMatchingXpathInNested(String pathPart, NestedAttributeMapping nested) throws IOException {
        FeatureTypeMapping ftm=nested.getFeatureTypeMapping(null);
        String result=null;
        if(ftm.getSource().getName().getLocalPart().equals(pathPart)){
            result=nested.getTargetXPath().toString()+"/"+strName(ftm.getTargetFeature().getName());//+"/"+ftm.getTargetFeature().getName().toString();;
            mappingsStack.add(ftm);
        }
        return result;
    }

    private String findMatchingXpathInAttributeMappingList(String pathPart, List<AttributeMapping> mappings){
        String result=null;
        for (AttributeMapping mapping:mappings){
            MultipleValueExtractor extractor= new MultipleValueExtractor();
            mapping.getSourceExpression().accept(extractor,null);
            for (String attr:extractor.getAttributeNameSet()){
                if (attr!=null && attr.equals(pathPart)) {
                    result= mapping.getTargetXPath().toString();
                    break;
                }
            }
            if (result!=null)
                break;
            if (result == null){
                Expression idExpr=mapping.getIdentifierExpression();
                if (idExpr!=null && !idExpr.equals(Expression.NIL)){
                    idExpr.accept(extractor,null);
                    if (extractor.getAttributeNameSet().size()>0){
                        String idPn=extractor.getAttributeNames()[0];
                        if (pathPart.equals(idPn)) {
                            result = "@gml:id";
                            break;
                        }
                    }
                }
            }
            if (result==null){
                Map<Name,Expression> cProps=mapping.getClientProperties();
                if (cProps!=null) {
                    for (Name xpath : cProps.keySet()) {
                        Expression expression=cProps.get(xpath);
                        expression.accept(extractor,null);
                        if (!extractor.getAttributeNameSet().isEmpty()){
                            if (extractor.getAttributeNames()[0].equals(pathPart)) {
                                result = xpath.toString();
                                break;
                            }
                        }

                    }
                }
            }
        }
        return result;
    }

    private String strName(Name name){
        String prefix=mappingsStack.peek().getNamespaces().getPrefix(name.getNamespaceURI());
        return prefix + name.getSeparator() + name.getLocalPart();
    }
}
