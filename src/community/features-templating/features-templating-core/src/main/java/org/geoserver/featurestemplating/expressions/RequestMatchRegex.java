/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.expressions;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.geoserver.ows.Request;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

/** Check if the current {@link Request} matches the regex passed as an argument of the Function. */
public class RequestMatchRegex extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl(
                    "requestMatchRegex",
                    parameter("result", Boolean.class),
                    parameter("regex", String.class));

    public RequestMatchRegex() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object object) {
        if (!(object instanceof Request)) {
            throw new UnsupportedOperationException(
                    NAME.getName() + " function works with request object only");
        }
        Request request = (Request) object;
        String regex = getParameters().get(0).evaluate(null, String.class);
        Pattern pattern = Pattern.compile(regex);
        String url = getFullURL(request.getHttpRequest());
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    private String getFullURL(HttpServletRequest request) {
        StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }
}
