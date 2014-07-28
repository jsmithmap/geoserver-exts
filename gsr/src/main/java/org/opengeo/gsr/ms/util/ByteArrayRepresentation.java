/* Copyright (c) 2013 - 2014 Boundless - http://boundlessgeo.com All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.opengeo.gsr.ms.util;

import java.io.IOException;
import java.io.OutputStream;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

public class ByteArrayRepresentation extends OutputRepresentation {
    private byte[] bytes;
    public ByteArrayRepresentation(MediaType mtype, byte[] bytes) {
        super(mtype);
        this.bytes = bytes;
    }
    public void write(OutputStream out) throws IOException {
        out.write(bytes);
    }
}
