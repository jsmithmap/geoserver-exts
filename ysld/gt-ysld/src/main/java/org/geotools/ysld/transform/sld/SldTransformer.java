package org.geotools.ysld.transform.sld;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Writer;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 * Transforms an XML/SLD stream to a Yaml/Ysld stream.
 */
public class SldTransformer {

    XMLStreamReader xml;
    SldTransformContext context;

    public SldTransformer(XMLStreamReader xml, Writer yaml) {
        this.xml = xml;
        context = new SldTransformContext(yaml);
    }

    public SldTransformContext context() {
        return context;
    }

    public void transform() throws IOException, XMLStreamException {
        context.stream().document().push(new RootHandler());

        boolean root = true;
        Integer next = xml.hasNext() ? xml.next() : null;
        while(next != null) {
            context.reset();

            SldTransformHandler h = context.handlers.peek();
            switch(next) {
                case PROCESSING_INSTRUCTION:
                case COMMENT:
                case SPACE:
                    break;
                case START_DOCUMENT:
                    //h.document(xml, context);
                    break;
                case START_ELEMENT:
                    if (root) {
                        // root element, fill in some context
                        String ver = xml.getAttributeValue(null, "version");
                        if (ver != null) {
                            context.version(ver);
                        }
                    }
                    root = false;
                    h.element(xml, context);
                    break;
                case ATTRIBUTE:
                    h.attribute(xml, context);
                    break;
                case CHARACTERS:
                    h.characters(xml, context);
                    break;
                case END_ELEMENT:
                    h.endElement(xml, context);
                    break;
                case END_DOCUMENT:
                    //h.endDocument(xml, context);
                    break;
            }

            if (context.moveToNext) {
                next = xml.hasNext() ? xml.next() : null;
            }

        }

        context.endDocument().endStream();
    }

}
