/* Copyright (c) 2013 - 2014 Boundless - http://boundlessgeo.com All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.opengeo.gsr.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.geoserver.catalog.rest.AbstractCatalogFinder;
import org.geoserver.config.GeoServer;
import org.geoserver.ows.Dispatcher;
import org.geoserver.wms.WMS;
import org.opengeo.gsr.fs.resource.FeatureServiceResource;
import org.opengeo.gsr.fs.resource.FeatureResource;
import org.opengeo.gsr.fs.resource.LayerResource;
import org.opengeo.gsr.ms.resource.LayerListResource;
import org.opengeo.gsr.ms.resource.LegendResource;
import org.opengeo.gsr.ms.resource.ExportMapResource;
import org.opengeo.gsr.ms.resource.MapResource;
import org.opengeo.gsr.ms.resource.MapTileResource;
import org.opengeo.gsr.ms.resource.QueryResource;
import org.opengeo.gsr.resource.CatalogResource;
import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

/**
 * @author Juan Marin, OpenGeo
 */
public class ServiceFinder extends Router {
    final private GeoServer geoServer;

    @SuppressWarnings("unused")
    final private WMS wms;

    final private Dispatcher dispatcher;

    public ServiceFinder(GeoServer geoServer, WMS wms, Dispatcher dispatcher) {
        this.geoServer = geoServer;
        this.wms = wms;
        this.dispatcher = dispatcher;
    }

    { 
        setRoutingMode(BEST);
        Finder catalogService = new Finder() {
                @Override
                public Resource findTarget(Request request, Response response) {
                        return new CatalogResource(getContext(), request, response, CatalogService.class, geoServer);
                }
        };
        Router mapService = new Router(getContext()) {
                {
                        Finder rootResourceFinder = new Finder(getContext()) {
                                @Override
                                public Resource findTarget(Request request, Response response) {
                                        return new MapResource(getContext(), request, response, geoServer);
                                }
                        };
                        Finder layerListFinder = new Finder(getContext()) {
                                @Override
                                public Resource findTarget(Request request, Response response) {
                                        return new LayerListResource(getContext(), request, response, geoServer.getCatalog());
                                };
                        };
                        Finder queryFinder = new Finder(getContext()) {
                                @Override
                                public Resource findTarget(Request request, Response response) {
                                        return new QueryResource(getContext(), request, response, geoServer.getCatalog());
                                }
                        };
                        Finder legendFinder = new Finder(getContext()) {
                                @Override
                                public Resource findTarget(Request request, Response response) {
                                        return new LegendResource(getContext(), request, response, geoServer.getCatalog());
                                }
                        };
                        Finder exportMapFinder = new Finder(getContext()) {
                                @Override
                                public Resource findTarget(Request request, Response response) {
                                        return new ExportMapResource(getContext(), request, response, geoServer.getCatalog(), dispatcher);
                                }
                        };
                        Finder tileFinder = new Finder(getContext()) {
                            @Override
                            public Resource findTarget(Request request, Response response) {
                                return new MapTileResource(getContext(), request, response, geoServer.getCatalog(), dispatcher);
                            }
                        };
                        attach("", rootResourceFinder);
                        attach("/export", exportMapFinder);
                        attach("/layers", layerListFinder);
                        attach("/{layerOrTable}/query", queryFinder);
                        attach("/legend", legendFinder);
                        attach("/tile/{level}/{row}/{col}", tileFinder);
                }
        };
        Router featureService = new Router(getContext()) {
                {
                        Finder rootResourceFinder = new Finder(getContext()) {
                                @Override
                                public Resource findTarget(Request request, Response response) {
                                        return new FeatureServiceResource(getContext(), request, response, geoServer);
                                }
                        };
                        Finder featureFinder = new Finder(getContext()) {
                                @Override
                                public Resource findTarget(Request request, Response response) {
                                        return new FeatureResource(getContext(), request, response, geoServer.getCatalog());
                                }
                        };
                        Finder layerFinder = new Finder(getContext()) {
                                @Override
                                public Resource findTarget(Request request, Response response) {
                                        return new LayerResource(getContext(), request, response, geoServer.getCatalog());
                                }
                        };
                        attach("", rootResourceFinder);
                        attach("/{layerOrTable}/{featureId}", featureFinder);
                        attach("/{layerOrTable}", layerFinder);
                }
        };
                attach("/{workspace}/CatalogServer", catalogService);
                attach("/{workspace}/MapServer", mapService);
                attach("/{workspace}/FeatureServer", featureService);
                attach("", catalogService);
    }
    
//    public Resource findTarget(Request request, Response response) {
//        Resource resource = null;
//        try {
//            Map<String, Object> attributes = request.getAttributes();
//            String serviceType = "CatalogServer";
//            if (attributes.get("serviceType") != null) {
//                serviceType = attributes.get("serviceType").toString();
//            }
//            String params = attributes.get("params").toString();
//            Map<String, String> paramsMap = getParamsMap(params);
//            String format = paramsMap.get("f");
//            
//            String operation = "";
//            if (attributes.get("operation") != null) {
//                operation = attributes.get("operation").toString();
//            }
//            
//            switch (ServiceType.valueOf(serviceType)) {
//            case CatalogServer:
//                resource = new CatalogResource(null, request, response, CatalogService.class,
//                        geoServer);
//                break;
//            case MapServer:
//                if ("".equals(operation)) {
//                    resource = new MapResource(null, request, response, geoServer, format);
//                } else if ("layers".equals(operation)) {
//                    resource = new LayerListResource(null, request, response, catalog, format);
//                } else if ("query".equals(operation)) {
//                    resource = new QueryResource(null, request, response, catalog, format);
//                } else if ("legend".equals(operation)) {
//                    resource = new LegendResource(null, request, response, catalog, format);
//                }
//                break;
//            case FeatureServer:
//                if ("".equals(operation)) {
//                    resource = new FeatureServiceResource(null, request, response, geoServer, format);
//                } else if (attributes.get("layerOrTable") != null) {
//                    // in this case the 'operation' parameter ends up being the feature id
//                    resource = new FeatureResource(null, request, response, catalog, format, operation);
//                } else {
//                    resource = new LayerResource(null, request, response, catalog, format, operation);
//                }
//                break;
//            case GeocodeServer:
//                break;
//            case GeometryServer:
//                break;
//            case GPServer:
//                break;
//            case ImageServer:
//                break;
//            }
//        } catch (Exception e) {
//            Writer w = new StringWriter();
//            PrintWriter pw = new PrintWriter(w);
//            pw.println("Unsupported request: " + request.getResourceRef());
//            e.printStackTrace(pw);
//            pw.close();
//            response.setEntity(w.toString(), MediaType.TEXT_HTML);
//            resource = new Resource(getContext(), request, response);
//        }
//        return resource;
//    }
//    
//    private Map<String, String> getParamsMap(String params) {
//        Map<String, String> paramsMap = new HashMap<String, String>();
//        StringTokenizer tokenizer = new StringTokenizer(params, "&");
//        while (tokenizer.hasMoreTokens()) {
//            String str = tokenizer.nextToken();
//            StringTokenizer tokenizer2 = new StringTokenizer(str, "=");
//            while (tokenizer2.hasMoreTokens()) {
//                if (tokenizer2.countTokens() == 2) {
//                    paramsMap.put(tokenizer2.nextToken(), tokenizer2.nextToken());
//                } else {
//                    tokenizer2.nextToken();
//                }
//            }
//        }
//        return paramsMap;
//    }
}
