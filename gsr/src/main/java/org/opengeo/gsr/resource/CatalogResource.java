/* Copyright (c) 2013 - 2014 Boundless - http://boundlessgeo.com All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.opengeo.gsr.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServer;
import org.opengeo.gsr.core.exception.ServiceError;
import org.opengeo.gsr.core.exception.ServiceException;
import org.opengeo.gsr.core.format.GeoServicesJsonFormat;
import org.opengeo.gsr.service.AbstractService;
import org.opengeo.gsr.service.CatalogService;
import org.opengeo.gsr.service.GeometryService;
import org.opengeo.gsr.service.MapService;
import org.opengeo.gsr.service.FeatureService;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * @author Juan Marin, OpenGeo
 */
public class CatalogResource extends Resource {
    private final String productName = "OpenGeo Suite";

    private final String specVersion = "1.0";

    private final double currentVersion = 10.1;

	private final GeoServer geoServer;

    public CatalogResource(Context context, Request request, Response response, Class<?> clazz,
            GeoServer geoServer) {
        super(context, request, response);
        this.geoServer = geoServer;
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }
    
    @Override
    public Representation getRepresentation(Variant variant) {
    	String format = getRequest().getResourceRef().getQueryAsForm().getFirstValue("f");
    	if (format == null) format = getRequest().getEntityAsForm().getFirstValue("f");
    	GeoServicesJsonFormat gsFormat = new GeoServicesJsonFormat();
		if ("json".equals(format)) {
			try {
				List<AbstractService> services = new ArrayList<AbstractService>();
				for (WorkspaceInfo ws : geoServer.getCatalog().getWorkspaces()) {
					MapService ms = new MapService(ws.getName());
					FeatureService fs = new FeatureService(ws.getName());
					services.add(ms);
					services.add(fs);
				}
				services.add(new GeometryService("Geometry"));
				CatalogService catalogService = new CatalogService("services",
						specVersion, productName, currentVersion,
						Collections.<String> emptyList(), services);
				return gsFormat.toRepresentation(catalogService);
			} catch (Exception e) {
				List<String> details = new ArrayList<String>();
				details.add(e.getMessage());
				return gsFormat
						.toRepresentation(new ServiceException(
								new ServiceError((Status.SERVER_ERROR_INTERNAL
										.getCode()), "Internal Server Error",
										details)));
			}
		} else {
			List<String> details = new ArrayList<String>();
			details.add("Format " + format + " is not supported");
			return gsFormat.toRepresentation(new ServiceException(new ServiceError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Output format not supported", details)));
		}
    }
    
    @Override
    public boolean allowPost() {
    	return true;
    }
    
    @Override
    public void handlePost() {
    	handleGet();
    }
}
