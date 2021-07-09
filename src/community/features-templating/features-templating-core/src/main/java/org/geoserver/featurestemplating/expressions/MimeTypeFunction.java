/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.expressions;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import org.geoserver.ows.Request;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

/**
 * Returns the Mime Type of the current {@link Request}.
 */
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
        String outputFormat = request.getOutputFormat();
        if (outputFormat == null) {
            outputFormat = request.getKvp() != null ? (String) request.getKvp().get("f") : null;
        }
        return outputFormat;
    }
}
