package org.geoserver.featurestemplating.expressions;

import org.geoserver.ows.Request;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

public class MimeEqualsTo extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl(
                    "mimeEqualsTo",
                    parameter("result", Boolean.class),
                    parameter("value",String.class));

    public MimeEqualsTo () {
        super(NAME);
    }

    @Override
    public Object evaluate(Object object) {
        if (!(object instanceof Request)){
            throw new UnsupportedOperationException(NAME.getName()+" function works with request object only");
        }
        Request request=(Request) object;
        String outputFormat=getParameters().get(0).evaluate(null, String.class);
        return request.getOutputFormat().equals(outputFormat);
    }
}
