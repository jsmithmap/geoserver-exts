/* Copyright (c) 2013 - 2014 Boundless - http://boundlessgeo.com All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.opengeo.gsr.ms.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.util.JSONBuilder;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.URLMangler;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.rest.util.RESTUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.temporal.object.DefaultInstant;
import org.geotools.temporal.object.DefaultPeriod;
import org.geotools.temporal.object.DefaultPosition;
import org.opengeo.gsr.core.exception.ServiceError;
import org.opengeo.gsr.core.feature.FeatureEncoder;
import org.opengeo.gsr.core.format.GeoServicesJsonFormat;
import org.opengeo.gsr.core.geometry.Envelope;
import org.opengeo.gsr.core.geometry.GeometryEncoder;
import org.opengeo.gsr.core.geometry.SpatialReference;
import org.opengeo.gsr.core.geometry.SpatialReferenceEncoder;
import org.opengeo.gsr.core.geometry.SpatialReferences;
import org.opengeo.gsr.core.geometry.SpatialRelationship;
import org.opengeo.gsr.ms.util.FakeHttpServletRequest;
import org.opengeo.gsr.ms.util.FakeHttpServletResponse;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class ExportMapResource extends Resource {
    private Dispatcher dispatcher;
    private Catalog catalog;

    public ExportMapResource(Context context, Request request, Response response, Catalog catalog, Dispatcher dispatcher) {
        super(context, request, response);
        this.catalog = catalog;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleGet() { // getRepresentation(Variant v) {
        Form options = getRequest().getResourceRef().getQueryAsForm();
        String f = options.getFirstValue("f");
        if ("json".equals(f)) {
            try {
                Export export = doExport(options);
                getResponse().setEntity(new JsonRepresentation(export));
            } catch (Exception e) {
                getResponse().setEntity(new StringRepresentation(e.getMessage()));
            }
        } else if ("image".equals(f)) {
            String format = options.getFirstValue("format");
            try {
                Export export = doExport(options);
                Map<String, String> query = createWMSQuery(export);
                FakeHttpServletResponse response = dispatch(query);
                getResponse().setEntity(new ByteArrayRepresentation(new MediaType(response.getContentType()), response.getBodyBytes()));
            } catch (Exception e) {
                getResponse().setEntity(new StringRepresentation("Image failed: " + e));
            }
        }
    }

    private FakeHttpServletResponse dispatch(Map<String, String> query) throws Exception {
        FakeHttpServletRequest request = new FakeHttpServletRequest(query);
        FakeHttpServletResponse response = new FakeHttpServletResponse();
        dispatcher.handleRequest(request, response);
        return response;
    }

    private final Export doExport(Form options) throws TransformException, FactoryException {
        ReferencedEnvelope bbox = getBBox(options);
        int[] size = getSize(options);
        double dpi = 96d;
        double scale = RendererUtilities.calculateScale(bbox, size[0], size[1], dpi);
        return new Export(size[0], size[1], bbox, scale);
    }

    private final Map<String, String> createWMSQuery(Export export) {
        Map<String, String> query = new HashMap<String, String>();
        query.put("service", "wms");
        query.put("version", "1.1.0");
        query.put("request", "GetMap");
        String workspaceName = (String) getRequest().getAttributes().get("workspace");
        String layers = getLayerNames(workspaceName);
        query.put("layers", layers);
        query.put("height", String.valueOf(export.height));
        query.put("width", String.valueOf(export.width));
        query.put("format", "image/png");
        query.put("bbox", toBBOX(export.extent));
        return query;
    }

    private final String toBBOX(ReferencedEnvelope e) {
        return e.getMinX() + "," + e.getMinY() + "," + e.getMaxX() + "," + e.getMaxY();
    }

    private ReferencedEnvelope getBBox(Form options) {
        String coords = options.getFirstValue("bbox");
        String[] splitCoords = coords.split(",");
        if (splitCoords.length != 4) throw new RuntimeException("Bad bbox string");
        double[] parsedCoords = new double[4];
        for (int i = 0; i < 4; i++) {
            parsedCoords[i] = Double.valueOf(splitCoords[i]);
        }

        CoordinateReferenceSystem crs = SpatialReferenceEncoder.parseSpatialReference(options.getFirstValue("bboxSR"));
        if (crs == null) crs = DefaultGeographicCRS.WGS84;
        return new ReferencedEnvelope(parsedCoords[0], parsedCoords[2], parsedCoords[1], parsedCoords[3], crs);
    }

    private int[] getSize(Form options) {
        String widthAndHeight = options.getFirstValue("size");
        if (widthAndHeight == null) {
            return new int[] { 400, 400 };
        } else {
            String[] splitValues = widthAndHeight.split(",");
            return new int[] {
                Integer.valueOf(splitValues[0]),
                Integer.valueOf(splitValues[1])
            };
        }
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

    private final static class Export {
        public final int width;
        public final int height;
        public final ReferencedEnvelope extent;
        public final double scale;

        public Export(int width, int height, ReferencedEnvelope extent, double scale) {
            this.width = width;
            this.height = height;
            this.extent = extent;
            this.scale = scale;
        }
    }

    private class JsonRepresentation extends OutputRepresentation {
        private Export export;

        public JsonRepresentation(Export export) {
            super(MediaType.APPLICATION_JSON);
            this.export = export;
        }

        public void write(OutputStream out) throws IOException {
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            JSONBuilder builder = new JSONBuilder(writer);
            builder.object();
            try {
                String baseUrl = ResponseUtils.baseURL(RESTUtils.getServletRequest(getRequest()));
                String href = ResponseUtils.buildURL(baseUrl, "ows", createWMSQuery(export), URLMangler.URLType.SERVICE);
                builder.key("href").value(href);
            } catch (Exception e) {
                builder.key("hreferror").value(e.toString()); // getMessage());
            }
            builder.key("extent");
            try {
                GeometryEncoder.referencedEnvelopeToJson(export.extent, SpatialReferences.fromCRS(export.extent.getCoordinateReferenceSystem()), builder);
            } catch (FactoryException e) {
                builder.value(null);
            }
            builder.key("width").value(export.width);
            builder.key("height").value(export.height);
            builder.key("scale").value(export.scale);
            builder.endObject();
            writer.flush();
        }
    }

    private class ByteArrayRepresentation extends OutputRepresentation {
        private byte[] bytes;
        public ByteArrayRepresentation(MediaType mtype, byte[] bytes) {
            super(mtype);
            this.bytes = bytes;
        }
        public void write(OutputStream out) throws IOException {
            out.write(bytes);
        }
    }
}
