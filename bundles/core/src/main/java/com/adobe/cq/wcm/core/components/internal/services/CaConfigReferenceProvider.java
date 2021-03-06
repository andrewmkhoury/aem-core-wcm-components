/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2020 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.wcm.core.components.internal.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.osgi.service.component.annotations.Component;

import com.adobe.cq.wcm.core.components.config.HtmlPageItemsConfig;
import com.adobe.cq.wcm.core.components.internal.DataLayerConfig;
import com.adobe.cq.wcm.core.components.internal.services.pdfviewer.PdfViewerCaConfig;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.reference.Reference;
import com.day.cq.wcm.api.reference.ReferenceProvider;

import static com.adobe.cq.wcm.core.components.util.ComponentUtils.NN_SLING_CONFIGS;

/**
 * Provides Context Aware Configuration references for a given resource.
 */
@Component(
        service = ReferenceProvider.class
)
public class CaConfigReferenceProvider implements ReferenceProvider {

    private static final String CA_CONFIG_REFERENCE_TYPE = "caconfig";

    /**
     * The @{@link ConfigurationResourceResolver} service.
     */
    @org.osgi.service.component.annotations.Reference
    private ConfigurationResourceResolver configurationResourceResolver;

    @Override
    public List<Reference> findReferences(Resource resource) {
        List<Reference> references = new ArrayList<>();
        // If the resource is not part of a page: stop the processing
        PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
        if (pageManager == null) {
            return references;
        }
        Page page = pageManager.getContainingPage(resource);
        if (page == null) {
            return references;
        }
        addCaConfigReference(HtmlPageItemsConfig.class.getName(), resource, references);
        addCaConfigReference(DataLayerConfig.class.getName(), resource, references);
        addCaConfigReference(PdfViewerCaConfig.class.getName(), resource, references);
        return references;
    }

    private void addCaConfigReference(String configName, Resource resource, List<Reference> references) {
        Resource configResource = configurationResourceResolver.getResource(resource, NN_SLING_CONFIGS, configName);
        if (configResource != null) {
            ValueMap properties = configResource.getValueMap();
            Calendar lastModified = properties.get(JcrConstants.JCR_LASTMODIFIED, Calendar.class);
            references.add(new Reference(CA_CONFIG_REFERENCE_TYPE, configName, configResource, (lastModified != null) ? lastModified.getTimeInMillis() : -1));
        }
    }
}
