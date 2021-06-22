package org.geoserver.inspire;

import org.geoserver.ows.Request;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LanguageDispatcherCallbackTest {

    @Test
    public void testLanguageToAcceptLanguages(){
        Request request=new Request();
        Map<String,Object> kvp=new HashMap<>();
        kvp.put("LANGUAGE","ita");
        request.setRawKvp(kvp);
        request.setKvp(kvp);
        new LanguagesDispatcherCallback().init(request);
        String acceptLanguagesRawKvp=request.getRawKvp().get("ACCEPTLANGUAGES").toString();
        String acceptLanguagesKvp=request.getKvp().get("ACCEPTLANGUAGES").toString();
        assertEquals("it",acceptLanguagesRawKvp);
        assertEquals("it",acceptLanguagesKvp);
    }
}
