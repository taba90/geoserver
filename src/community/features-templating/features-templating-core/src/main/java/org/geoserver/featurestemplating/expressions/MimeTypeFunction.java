package org.geoserver.featurestemplating.expressions;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import org.geoserver.ows.Request;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

public class MimeTypeFunction extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl("mimeType", parameter("result", String.class));

    public MimeTypeFunction() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object object) {
        if (!(object instanceof Request)) {
            throw new UnsupportedOperationException(
                    NAME.getName() + " function works with request object only");
        }
        Request request = (Request) object;
        return request.getOutputFormat();
    }
}
