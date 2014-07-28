package org.opengeo.gsr.ms.util;

import java.io.BufferedReader;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class FakeHttpServletRequest implements HttpServletRequest {
    private final Map<String, String> query;

    public FakeHttpServletRequest(Map<String, String> query) {
        this.query = query;
    }

    private static final Enumeration<Object> noElements = new Enumeration<Object>() {
        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            return null;
        }
    };

    private static <T> Enumeration<T> emptyEnumeration() {
        return (Enumeration<T>) noElements;
    }

    public boolean authenticate(HttpServletResponse response) {
        return false;
    }

    public Object getAttribute(String name) {
        return null;
    }

    public Enumeration<String> getAttributeNames() {
        return emptyEnumeration();
    }

    public String getAuthType() {
        return null;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public int getContentLength() {
        return -1;
    }

    public String getContentType() {
        return null;
    }

    public String getContextPath() {
        return "/geoserver";
    }

    public Cookie[] getCookies() {
        return new Cookie[] {};
    }

    public long getDateHeader(String name) {
        return 0;
    }

    public int getIntHeader(String name) {
        return 0;
    }

    public String getHeader(String name) {
        return null;
    }

    public Enumeration<String> getHeaderNames() {
        return emptyEnumeration();
    }

    public Enumeration<String> getHeaders(String name) {
        return emptyEnumeration();
    }

    public ServletInputStream getInputStream() {
        return null;
    }

    public int getIntHeader() {
        return 0;
    }

    public String getLocalAddr() {
        return null;
    }

    public Locale getLocale() {
        return null;
    }

    public Enumeration<Locale> getLocales() {
        return emptyEnumeration();
    }

    public String getLocalName() {
        return null;
    }

    public int getLocalPort() {
        return 0;
    }

    public String getMethod() {
        return "GET";
    }

    public String getParameter(String name) {
        return query.get(name);
    }

    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> array_ified = new HashMap<String, String[]>();
        for (Map.Entry<String, String> e : query.entrySet()) {
            array_ified.put(e.getKey(), new String[] { e.getValue() });
        }
        return array_ified;
    }

    public Enumeration<String> getParameterNames() {
        return emptyEnumeration();
    }

    public String[] getParameterValues(String name) {
        return new String[] {};
    }

    public String getPathInfo() {
        return "/ows";
    }

    public String getPathTranslated() {
        return "/ows";
    }

    public String getProtocol() {
        return null;
    }

    public String getQueryString() {
        return null;
    }

    public BufferedReader getReader() {
        return null;
    }

    public String getRealPath(String path) {
        return null;
    }

    public String getRemoteAddr() {
        return null;
    }

    public String getRemoteHost() {
        return null;
    }

    public int getRemotePort() {
        return 0;
    }

    public String getRemoteUser() {
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    public String getRequestedSessionId() {
        return null;
    }

    public String getRequestURI() {
        return "/geoserver/ows";
    }

    public StringBuffer getRequestURL() {
        return null;
    }

    public String getScheme() {
        return "http";
    }

    public String getServerName() {
        return null;
    }

    public int getServerPort() {
        return 0;
    }

    public String getServletPath() {
        return null;
    }

    public HttpSession getSession() {
        return null;
    }

    public HttpSession getSession(boolean create) {
        return null;
    }

    public Principal getUserPrincipal() {
        return null;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

    public boolean isSecure() {
        return false;
    }

    public boolean isUserInRole(String role) {
        return false;
    }

    public void login(String username, String password) {
    }

    public void logout() {
    }

    public void removeAttribute(String name) {
    }

    public void setAttribute(String name, Object o) {
    }

    public void setCharacterEncoding(String evn) {
    }
}
