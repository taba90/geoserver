package org.geoserver.featurestemplating.builders.visitors;

import org.geoserver.featurestemplating.builders.SourceBuilder;
import org.geoserver.featurestemplating.builders.impl.CompositeBuilder;
import org.geoserver.featurestemplating.builders.impl.DynamicValueBuilder;
import org.geoserver.featurestemplating.builders.impl.IteratingBuilder;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.featurestemplating.builders.impl.StaticBuilder;

public interface TemplateVisitor {

    Object visit(RootBuilder rootBuilder, Object extradata);

    Object visit(IteratingBuilder iteratingBuilder, Object extradata);

    Object visit(CompositeBuilder compositeBuilder, Object extradata);

    Object visit(DynamicValueBuilder dynamicBuilder, Object extradata);

    Object visit(StaticBuilder staticBuilder, Object extradata);

    Object visit(SourceBuilder sourceBuilder, Object extradata);
}
