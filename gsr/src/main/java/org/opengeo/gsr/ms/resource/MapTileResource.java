/* Copyright (c) 2013 - 2014 Boundless - http://boundlessgeo.com All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.opengeo.gsr.ms.resource;

import org.opengeo.gsr.ms.util.ByteArrayRepresentation;
import org.opengeo.gsr.ms.util.FakeHttpServletRequest;
import org.opengeo.gsr.ms.util.FakeHttpServletResponse;
import org.geoserver.catalog.Catalog;
import org.geoserver.ows.Dispatcher;

import com.vividsolutions.jts.geom.Envelope;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;

import java.util.HashMap;
import java.util.Map;

public class MapTileResource extends Resource {
    private Dispatcher dispatcher;
    private Catalog catalog;

    public MapTileResource(Context context, Request request, Response response, Catalog catalog, Dispatcher dispatcher) {
        super(context, request, response);
        this.catalog = catalog;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleGet() {
        String levelText = (String) getRequest().getAttributes().get("level");
        String colText = (String) getRequest().getAttributes().get("col");
        String rowText = (String) getRequest().getAttributes().get("row");
        int level = Integer.valueOf(levelText);
        int row = Integer.valueOf(rowText);
        int col = Integer.valueOf(colText);
        Map<String, String> query = createWMSQuery(level, row, col);
        try {
            FakeHttpServletResponse response = dispatch(query);
            getResponse().setEntity(new ByteArrayRepresentation(new MediaType(response.getContentType()), response.getBodyBytes()));
        } catch (Exception e) {
            getResponse().setEntity(new StringRepresentation("Tile failed" + e.getMessage()));
        }
    }

    private FakeHttpServletResponse dispatch(Map<String, String> query) throws Exception {
        FakeHttpServletRequest request = new FakeHttpServletRequest(query);
        FakeHttpServletResponse response = new FakeHttpServletResponse();
        dispatcher.handleRequest(request, response);
        return response;
    }

    private Map<String, String> createWMSQuery(int level, int row, int col) {
        Map<String, String> query = new HashMap<String, String>();
        Envelope env = tileEnvelope(level, row, col);
        query.put("service", "WMS");
        query.put("version", "1.1.0");
        query.put("request", "GetMap");
        query.put("height", "256");
        query.put("width", "256");
        query.put("format", "image/png");
        query.put("bbox", asBBox(env));
        String workspaceName = (String) getRequest().getAttributes().get("workspace");
        query.put("layers", getLayerNames(workspaceName));
        return query;
    }

    private Envelope tileEnvelope(int level, int row, int col) {
        double size = 360d / Math.pow(2, level);
        double minx = -180d + row * size;
        double maxx = -180d + (row + 1) * size;
        double miny =  90d - (col + 1) * size;
        double maxy =  90d - col * size;
        return new Envelope(minx, maxx, miny, maxy);
    }

    private String getLayerNames(String workspaceName) {
        LayersAndTables layersAndTables = LayersAndTables.find(catalog, workspaceName);
        StringBuffer buff = new StringBuffer();
        boolean first = true;
        for (LayerOrTable layer : layersAndTables.layers) {
            if (!first) buff.append(",");
            buff.append(layer.layer.getName());
            first = false;
        }
        return buff.toString();
    }

    private String asBBox(Envelope env) {
        return "" + env.getMinX() + "," + env.getMinY() + "," + env.getMaxX() + "," + env.getMaxY();
    }
}
