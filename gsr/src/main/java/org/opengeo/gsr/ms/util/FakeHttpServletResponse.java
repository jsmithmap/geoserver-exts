package org.opengeo.gsr.ms.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class FakeHttpServletResponse implements HttpServletResponse {
    private ByteArrayOutputStream body;
    private ServletOutputStream out;
    private String contentType = null;

    public FakeHttpServletResponse() {
        body = null;
    }

    public void addCookie(Cookie cookie) {
    }

    public void addDateHeader(String name, long date) {
    }

    public void addHeader(String name, String value) {
    }

    public void addIntHeader(String name, int value) {
    }

    public boolean containsHeader(String name) {
        return false;
    }

    public String encodeRedirectUrl(String url) {
        return null;
    }

    public String encodeRedirectURL(String url) {
        return null;
    }

    public String encodeUrl(String url) {
        return null;
    }

    public String encodeURL(String url) {
        return null;
    }

    public void flushBuffer() {
    }

    public int getBufferSize() {
        return 0;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public String getContentType() {
        return contentType;
    }

    public String getHeader(String name) {
        return null;
    }

    public Collection<String> getHeaderNames() {
        return null;
    }

    public Collection<String> getHeaders(String name) {
        return null;
    }

    public Locale getLocale() {
        return null;
    }

    public ServletOutputStream getOutputStream() {
        if (out == null) {
            body = new ByteArrayOutputStream();
            out = new ServletOutputStreamImpl(body);
        }
        return out;
    }

    public int getStatus() {
        return 0;
    }

    public PrintWriter getWriter() {
        return null;
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
    }

    public void resetBuffer() {
    }

    public void setBufferSize(int size) {
    }

    public void setCharacterEncoding(String encoding) {
    }

    public void setContentLength(int length) {
    }

    public void setContentType(String type) {
        this.contentType = type;
    }

    public void setLocale(Locale locale) {
    }

    public void sendError(int sc) {
    }

    public void sendError(int sc, String msg) {
    }

    public void sendRedirect(String location) {
    }

    public void setDateHeader(String name, long date) {
    }

    public void setHeader(String name, String value) {
    }

    public void setIntHeader(String name, int value) {
    }

    public void setStatus(int sc) {
    }

    public void setStatus(int sc, String message) {
    }

    public byte[] getBodyBytes() {
        if (body == null) throw new IllegalStateException("Can't get body content when nothing was written to it");
        return body.toByteArray();
    }

    private static class ServletOutputStreamImpl extends ServletOutputStream {
        private OutputStream out;
        public ServletOutputStreamImpl(OutputStream out) {
            this.out = out;
        }

        public void write(int i) throws IOException {
            out.write(i);
        }
    }
}
