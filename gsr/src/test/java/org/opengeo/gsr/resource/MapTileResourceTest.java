/* Copyright (c) 2013 - 2014 Boundless - http://boundlessgeo.com All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.opengeo.gsr.resource;

import com.mockrunner.mock.web.MockHttpServletResponse;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import static org.junit.Assert.*;
import org.junit.Test;

public class MapTileResourceTest extends ResourceTest {
    @Test
    public void testImage() throws Exception {
        MockHttpServletResponse result = getAsServletResponse(baseURL + "cite/MapServer/tile/0/0/0");
        assertEquals("image/png", result.getContentType());
        assertEquals(200, result.getErrorCode());
    }
}
