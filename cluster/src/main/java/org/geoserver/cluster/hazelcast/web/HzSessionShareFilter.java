package org.geoserver.cluster.hazelcast.web;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.geoserver.cluster.hazelcast.HzCluster;

import com.google.common.collect.Iterators;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.web.WebFilter;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Creates and delegates to a WebFilter if clustering is enabled.  The delegate is created lazily.
 * @author Kevin Smith, OpenGeo
 *
 */
public class HzSessionShareFilter implements Filter {
    // Need to use a delegator because WebFilter#doFilter is final and assumes that a Hazelcast
    // instance has been created.

    // TODO when the Servlet API dependency is updated to 3.0, this can all be made a lot simpler.

    WebFilter delegate;
    ServletContext srvCtx;
    HzCluster cluster;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        srvCtx = filterConfig.getServletContext();
        WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
    }

    protected HzCluster getCluster() {
        if(cluster==null) {
            ApplicationContext ac = 
                    WebApplicationContextUtils.getRequiredWebApplicationContext(srvCtx);
            cluster = ac.getBean("hzCluster", HzCluster.class);
        }
        return cluster;
    }
    
    private void createDelegate() throws ServletException {
        
        // Stop if clustering is not enabled
        if(!getCluster().isSessionSharing()) return;
        
        // Don't bother if we already have one
        if(delegate!=null) return;
        
        // Create the delegate and override its getInstance method to use the cluster's instance
        delegate = new WebFilter(){

            @Override
            protected HazelcastInstance getInstance(Properties properties)
                    throws ServletException {
                return getCluster().getHz();
            }
            
        };
        
        initDelegate();
    }
    
    private void initDelegate() throws ServletException {

        // Set up init-params for the delegate instance
        // TODO Maybe make these configurable in cluster.properties
        final Map<String, String> params = new HashMap<String,String>();
        params.put("map-name", "geoserver-sessions");
        params.put("sticky-session", Boolean.toString(getCluster().isStickySession()));
        params.put("instance-name",  getCluster().getHz().getConfig().getInstanceName());
        
        FilterConfig config = new FilterConfig() {

            @Override
            public String getFilterName() {
                return "hazelcast";
            }

            @Override
            public ServletContext getServletContext() {
                return srvCtx;
            }

            @Override
            public String getInitParameter(String name) {
                return params.get(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Iterators.asEnumeration(params.keySet().iterator());
            }
            
        };
        
        delegate.init(config);
    }
    
    
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        createDelegate();
        
        if(delegate!=null){
            delegate.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        if(delegate!=null) delegate.destroy();
    }

}
