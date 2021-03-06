package org.geotools.ysld.encode;


import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.UserLayer;
import org.geotools.ysld.YamlMap;
import org.geotools.ysld.YamlSeq;
import org.geotools.ysld.Ysld;
import org.junit.Test;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Function;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class YsldEncodeTest {
    @Test
    public void testFunction() throws Exception {
         StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();

        UserLayer layer = styleFactory.createUserLayer();
        sld.layers().add(layer);

        Style style = styleFactory.createStyle();
        layer.userStyles().add(style);

        Rule rule = styleFactory.createRule();

        style.featureTypeStyles().add(styleFactory.createFeatureTypeStyle());
        style.featureTypeStyles().get(0).rules().add(rule);

        Function func =
            filterFactory.function("strEndsWith", filterFactory.property("foo"), filterFactory.literal("bar"));
        rule.setFilter(filterFactory.equal(func, filterFactory.literal(true), false));

        StringWriter out = new StringWriter();
        Ysld.encode(sld, out);

        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        String filter = obj.seq("feature-styles").map(0).seq("rules").map(0).str("filter");
        assertEquals("strEndsWith(foo,'bar') = 'true'", filter);
    }

    @Test
    public void testRenderingTransformation() throws IOException {

        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();

        UserLayer layer = styleFactory.createUserLayer();
        sld.layers().add(layer);

        Style style = styleFactory.createStyle();
        layer.userStyles().add(style);

        FeatureTypeStyle featureStyle = styleFactory.createFeatureTypeStyle();
        style.featureTypeStyles().add(featureStyle);

        Function p1 = filterFactory.function("parameter", filterFactory.literal("data"));
        Function p2 = filterFactory.function("parameter", filterFactory.literal("levels"), filterFactory.literal(1000),
            filterFactory.literal(1100), filterFactory.literal(1200));

        Function rt = filterFactory.function("ras:Contour", p1, p2);
        featureStyle.setTransformation(rt);

        StringWriter out = new StringWriter();
        Ysld.encode(sld, out);

        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        YamlMap tx = obj.seq("feature-styles").map(0).map("transform");
        assertEquals("ras:Contour", tx.get("name"));
        YamlSeq levels = tx.map("params").seq("levels");
        assertEquals("1000", levels.str(0));
        assertEquals("1100", levels.str(1));
        assertEquals("1200", levels.str(2));
    }

    @Test
    public void testRenderingTransformationHeatmap() throws IOException {
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();

        UserLayer layer = styleFactory.createUserLayer();
        sld.layers().add(layer);

        Style style = styleFactory.createStyle();
        layer.userStyles().add(style);

        FeatureTypeStyle featureStyle = styleFactory.createFeatureTypeStyle();
        style.featureTypeStyles().add(featureStyle);

        Function p1 = filterFactory.function("parameter", filterFactory.literal("data"));
        Function p2 = filterFactory.function("parameter",
            filterFactory.literal("weightAttr"), filterFactory.literal("pop2000"));
        Function p3 = filterFactory.function("parameter",
                filterFactory.literal("radius"), filterFactory.literal("100"));
        Function p4 = filterFactory.function("parameter",
                filterFactory.literal("pixelsPerCell"), filterFactory.literal("10"));

        Function rt = filterFactory.function("vec:Heatmap", p1, p2, p3, p4);
        featureStyle.setTransformation(rt);

        StringWriter out = new StringWriter();
        Ysld.encode(sld, out);

        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        YamlMap tx = obj.seq("feature-styles").map(0).map("transform");
        assertEquals("vec:Heatmap", tx.get("name"));

        YamlMap params = tx.map("params");
        assertEquals("pop2000", params.get("weightAttr"));
        assertEquals("100", params.str("radius"));
        assertEquals("10", params.str("pixelsPerCell"));

    }
}
