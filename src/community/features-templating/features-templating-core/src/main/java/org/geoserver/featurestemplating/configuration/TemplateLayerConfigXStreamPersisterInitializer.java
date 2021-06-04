/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.configuration;

import org.geoserver.config.util.XStreamPersister;
import org.geoserver.config.util.XStreamPersisterInitializer;

public class TemplateLayerConfigXStreamPersisterInitializer implements XStreamPersisterInitializer {

    @Override
    public void init(XStreamPersister persister) {
        persister
                .getXStream()
                .allowTypesByWildcard(
                        new String[] {"emsa.europa.eu.starogc.authorization.web.model.*"});
        persister.getXStream().alias("rules", TemplateRule.class);
        persister.getXStream().alias("TemplateLayerConfig", TemplateLayerConfig.class);
        persister.registerBreifMapComplexType("TemplateRuleType", TemplateRule.class);
        persister.registerBreifMapComplexType("LayerConfigType", TemplateLayerConfig.class);
    }
}
