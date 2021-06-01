package org.geoserver.featurestemplating.expressions;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geoserver.ows.Request;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;

public class RequestMatchRegex extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl("requestMatchRegex", parameter("result", Boolean.class),parameter("regex", String.class));

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
        String regex=getParameters().get(0).evaluate(null, String.class);
        Pattern pattern=Pattern.compile(regex);
        String url=getFullURL(request.getHttpRequest());
        Matcher matcher=pattern.matcher(url);
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
