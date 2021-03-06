package org.geotools.ysld.parse;


import org.geotools.ysld.Tuple;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.ysld.YamlMap;
import org.geotools.ysld.YamlObject;
import org.geotools.ysld.YamlSeq;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingEndEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceEndEvent;

public abstract class ColorMapHandler extends YsldParseHandler {

    ColorMap colorMap;

    public ColorMapHandler(Factory factory) {
        super(factory);
        colorMap = factory.style.createColorMap();
    }

    @Override
    public void handle(YamlObject<?> obj, YamlParseContext context) {
        colorMap(colorMap);

        YamlMap map = obj.map();
        if (map.has("type")) {
            String value = map.str("type");
            if ("ramp".equals(value)) {
                colorMap.setType(ColorMap.TYPE_RAMP);
            }
            else if ("intervals".equals(value)) {
                colorMap.setType(ColorMap.TYPE_INTERVALS);
            }
            else if ("values".equals(value)) {
                colorMap.setType(ColorMap.TYPE_VALUES);
            }
            else {
                LOG.warning("Unknown color map type: " + value);
            }
        }
        context.push("entries", new EntriesHandler());
    }

    protected abstract void colorMap(ColorMap colorMap);

    class EntriesHandler extends YsldParseHandler {

        protected EntriesHandler() {
            super(ColorMapHandler.this.factory);
        }

        @Override
        public void handle(YamlObject<?> obj, YamlParseContext context) {
            YamlSeq seq = obj.seq();
            for (Object o : seq.raw()) {
                String val = o.toString();

                Tuple q = null;
                try {
                    q = Tuple.of(4).parse(val);
                }
                catch(IllegalArgumentException e) {
                    throw new IllegalArgumentException(String.format(
                            "Bad entry: '%s', must be of form (<color>,[<opacity>],[<value>],[<label>])", val), e);
                }

                ColorMapEntry e = factory.style.createColorMapEntry();
                if (q.at(0) != null) {
                    e.setColor(Util.expression(q.strAt(0), factory));
                }
                if (q.at(1) != null) {
                    e.setOpacity(Util.expression(q.strAt(1), factory));
                }
                if (q.at(2)!= null) {
                    e.setQuantity(Util.expression(q.strAt(2), factory));
                }
                if (q.at(3) != null) {
                    e.setLabel(q.strAt(3));
                }

                colorMap.addColorMapEntry(e);
            }
        }
    }
}
