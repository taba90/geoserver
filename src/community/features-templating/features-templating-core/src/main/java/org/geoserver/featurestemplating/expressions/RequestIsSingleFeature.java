package org.geoserver.featurestemplating.expressions;

import org.geoserver.ows.Request;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

public class RequestIsSingleFeature extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl(
                    "isSingleFeature",
                    parameter("result", Boolean.class));

    public RequestIsSingleFeature (){
        super(NAME);
    }

    @Override
    public Object evaluate(Object object) {
            return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                    .map(
                            att ->
                                    (String)
                                            att.getAttribute(
                                                    "OGCFeatures:ItemId",
                                                    RequestAttributes.SCOPE_REQUEST))
                    .orElse(null);
    }
}
