package org.geoserver.importer.web;

import org.apache.wicket.Application;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.importer.Importer;

/**
 * Importer web utilities.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class ImporterWebUtils {

    static Importer importer() {
        return GeoServerApplication.get().getBeanOfType(Importer.class);
    }

    static boolean isDevMode() {
        return Application.DEVELOPMENT.equalsIgnoreCase(GeoServerApplication.get().getConfigurationType());
    }

    static void disableLink(ComponentTag tag) {
        tag.setName("a");
        tag.addBehavior(new SimpleAttributeModifier("class", "disabled"));
    }
}
