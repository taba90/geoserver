package org.geoserver.featurestemplating.expressions;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.util.Optional;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class RequestIsSingleFeature extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl("isSingleFeature", parameter("result", Boolean.class));

    public RequestIsSingleFeature() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object object) {
        String optionalParam= Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(
                        att ->
                                (String)
                                        att.getAttribute(
                                                "OGCFeatures:ItemId",
                                                RequestAttributes.SCOPE_REQUEST))
                .orElse(null);
        return optionalParam!=null;
    }
}
