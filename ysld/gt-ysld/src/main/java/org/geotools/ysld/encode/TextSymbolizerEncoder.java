package org.geotools.ysld.encode;

import org.geotools.styling.Halo;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.TextSymbolizer;

public class TextSymbolizerEncoder extends SymbolizerEncoder<TextSymbolizer> {

    TextSymbolizerEncoder(TextSymbolizer text) {
        super(text);
    }

    @Override
    protected void encode(TextSymbolizer text) {
        put("label", text.getLabel());
        inline(new FillEncoder(text.getFill()));
        inline(new HaloEncoder(text.getHalo()));
        inline(new FontEncoder(text.getFont()));
        inline(new PlacementEncoder(text.getLabelPlacement()));
        super.encode(text);
    }

    static class PlacementEncoder extends Encoder<LabelPlacement> {
        PlacementEncoder(LabelPlacement placement) {
            super(placement);
        }

        @Override
        protected void encode(LabelPlacement placement) {
            push("placement");
            if (placement instanceof LinePlacement) {
                put("type", "line");
                put("offset", ((LinePlacement) placement).getPerpendicularOffset());
            }
            else if (placement instanceof PointPlacement) {
                PointPlacement pp = (PointPlacement) placement;
                put("type", "point");

                inline(new AnchorPointEncoder(pp.getAnchorPoint()));
                inline(new DisplacementEncoder(pp.getDisplacement()));
                put("rotation", nullIf(pp.getRotation(), 0));
            }

        }
    }

    static class HaloEncoder extends Encoder<Halo> {
        public HaloEncoder(Halo halo) {
            super(halo);
        }

        @Override
        protected void encode(Halo h) {
            push("halo");
            inline(new FillEncoder(h.getFill()));
            put("radius", h.getRadius());
        }

    }
}
