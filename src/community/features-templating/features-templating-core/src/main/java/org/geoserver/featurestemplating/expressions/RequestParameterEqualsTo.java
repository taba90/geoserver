package org.geoserver.featurestemplating.expressions;

import org.geoserver.ows.Request;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

public class RequestParameterEqualsTo extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl(
                    "parameterEqualsTo",
                    parameter("result", Boolean.class),
                    parameter("name",String.class),
                    parameter("value",Object.class));

    public RequestParameterEqualsTo () {
        super(NAME);
    }

    @Override
    public Object evaluate(Object object) {
        if (!(object instanceof Request)){
            throw new UnsupportedOperationException(NAME.getName()+" function works with request object only");
        }
        Request request=(Request) object;
        String parameter=getParameters().get(0).evaluate(null,String.class);
        Object value=request.getRawKvp().get(parameter.toUpperCase());
        Object parameter2=getParameters().get(1).evaluate(null, Object.class);
        return parameter2.equals(value);
    }
}
