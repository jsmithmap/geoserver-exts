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

public class ExportMapResourceTest extends ResourceTest {
    @Test
    public void testJson() throws Exception {
        JSON json = getAsJSON(baseURL + "cite/MapServer/export?f=json&bbox=-127.8,5.8,-63.5,70.1&bboxSR=4326");
        assertTrue(json instanceof JSONObject);
        JSONObject jobject = (JSONObject) json;
        assertTrue(jobject.containsKey("href"));
        assertTrue(jobject.containsKey("extent"));
        assertTrue(jobject.containsKey("height"));
        assertTrue(jobject.containsKey("width"));
    }

    @Test
    public void testImage() throws Exception {
        MockHttpServletResponse result = getAsServletResponse(baseURL + "cite/MapServer/export?f=image&bbox=-127.8,5.8,-63.5,70.1&bboxSR=4326&format=png");
        assertEquals("image/png", result.getContentType());
        assertEquals(200, result.getErrorCode());
    }
}
