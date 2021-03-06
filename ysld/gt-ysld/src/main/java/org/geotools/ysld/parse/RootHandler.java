package org.geotools.ysld.parse;

import org.geotools.styling.*;
import org.geotools.ysld.YamlMap;
import org.geotools.ysld.YamlObject;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;

public class RootHandler extends YsldParseHandler {

    StyledLayerDescriptor sld;
    Style style;

    public RootHandler() {
        super(new Factory());
    }

    @Override
    public void handle(YamlObject<?> obj, YamlParseContext context) {
        sld = factory.style.createStyledLayerDescriptor();

        NamedLayer layer = factory.style.createNamedLayer();
        sld.layers().add(layer);

        layer.styles().add(style = factory.style.createStyle());

        YamlMap root = obj.map();
        style.setName(root.str("name"));
        if (root.has("title")) {
            style.setTitle(root.str("title"));
        }
        if (root.has("abstract")) {
            style.setAbstract(root.str("abstract"));
        }
        style.setTitle(root.str("title"));
        style.setAbstract(root.str("abstract"));
        style.setName(root.str("name"));

        if (root.has("feature-styles")) {
            context.push("feature-styles", new FeatureStyleHandler(style, factory));
        }
        else if (root.has("rules")) {
            context.push("rules", new RuleHandler(newFeatureTypeStyle(), factory));
        }
        else if (root.has("symbolizers")) {
            context.push("symbolizers", new SymbolizersHandler(newRule(), factory));
        }
        else if (root.has("point") || root.has("line") || root.has("polygon")
            || root.has("text") || root.has("raster")) {
            context.push(new SymbolizersHandler(newRule(), factory));
        }
    }

    public FeatureTypeStyle newFeatureTypeStyle() {
        FeatureTypeStyle fts = factory.style.createFeatureTypeStyle();
        style.featureTypeStyles().add(fts);
        return fts;
    }

    public Rule newRule() {
        Rule rule = factory.style.createRule();
        newFeatureTypeStyle().rules().add(rule);
        return rule;
    }

    public StyledLayerDescriptor sld() {
        return sld;
    }
}
