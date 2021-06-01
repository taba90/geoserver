package org.geoserver.featurestemplating.expressions;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import org.geoserver.ows.Request;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.geotools.util.Converters;
import org.opengis.filter.capability.FunctionName;

public class RequestParameterFunction extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl(
                    "requestParam",
                    parameter("result", String.class),
                    parameter("name", String.class));

    public RequestParameterFunction() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object object) {
        if (!(object instanceof Request)) {
            throw new UnsupportedOperationException(
                    NAME.getName() + " function works with request object only");
        }
        Request request = (Request) object;
        String parameter = getParameters().get(0).evaluate(null, String.class);
        Object value = request.getRawKvp().get(parameter.toUpperCase());
        return Converters.convert(value, String.class);
    }
}
