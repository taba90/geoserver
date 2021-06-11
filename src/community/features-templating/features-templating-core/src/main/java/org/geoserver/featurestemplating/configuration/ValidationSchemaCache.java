package org.geoserver.featurestemplating.configuration;

import static org.geoserver.featurestemplating.configuration.TemplateInfoDao.TEMPLATE_DIR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geotools.xml.resolver.SchemaCache;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ValidationSchemaCache implements EntityResolver {

    private static final String SHCEMA_CACHE_DIR = "schema-cache";
    private GeoServerDataDirectory dd;
    private SchemaCache schemaCache;

    public ValidationSchemaCache(GeoServerDataDirectory dd) {
        this.dd = dd;
        Resource templateDir = dd.get(TEMPLATE_DIR);
        File dir = templateDir.dir();
        if (!dir.exists()) dir.mkdir();
        Resource resource = templateDir.get(SHCEMA_CACHE_DIR);
        File cacheDir = resource.dir();
        if (!cacheDir.exists()) cacheDir.mkdir();
        this.schemaCache = new SchemaCache(cacheDir, true);
    }

    public String getLocalURL(String url) {
        return this.schemaCache.resolveLocation(url);
    }

    public static ValidationSchemaCache get() {
        return GeoServerExtensions.bean(ValidationSchemaCache.class);
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        String resolved = this.schemaCache.resolveLocation(systemId);
        try {
            return new InputSource(new FileInputStream(new File(new URL(resolved).toURI())));
        } catch (URISyntaxException e) {
            throw new SAXException(e);
        }
    }
}
