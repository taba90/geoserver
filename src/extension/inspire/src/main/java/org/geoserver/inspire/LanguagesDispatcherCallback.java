package org.geoserver.inspire;

import org.geoserver.ows.AbstractDispatcherCallback;
import org.geoserver.ows.Request;
import org.geotools.util.logging.Logging;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class LanguagesDispatcherCallback extends AbstractDispatcherCallback {

    private static final Logger LOGGER = Logging.getLogger(LanguagesDispatcherCallback.class);


    @Override
    public Request init(Request request) {
        Map<String,Object> rawKvp=request.getRawKvp();
        if (rawKvp!=null && rawKvp.containsKey("LANGUAGE")){
            String value=String.valueOf(rawKvp.get("LANGUAGE"));
            try {
                Properties mappings=getLanguageMapping();
                String isoLang=mappings.getProperty(value);
                if (isoLang==null){
                    LOGGER.info("A Language parameter was provided in the request but it cannot be resolved to a ISO lang code." +
                            " Parameter value is "+value);
                }
                rawKvp.put("ACCEPTLANGUAGES",isoLang);
                if (request.getKvp()!=null){
                    request.getKvp().put("ACCEPTLANGUAGES",isoLang);
                }
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        }
        return super.init(request);
    }

    private Properties getLanguageMapping() throws IOException {
        List<String> langs = new ArrayList<>();
        URL resource = getClass().getResource("available_languages.properties");
        try (InputStream inStream = resource.openStream()) {
            Properties list = new Properties();
            list.load(inStream);
            return list;
        }
    }
}
