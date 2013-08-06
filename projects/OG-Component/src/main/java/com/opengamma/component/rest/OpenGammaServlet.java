/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.ComponentRepository;
import com.opengamma.util.rest.HttpMethodFilter;
import com.opengamma.util.rest.NoCachingFilter;
import com.opengamma.util.rest.UrlSuffixFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;

/**
 * The OpenGamma servlet that integrates Jetty and OpenGamma components.
 */
public class OpenGammaServlet extends ServletContainer {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(OpenGammaServlet.class);

  public OpenGammaServlet() {
    super();
  }

  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props, WebConfig webConfig) throws ServletException {
    DefaultResourceConfig cfg = new DefaultResourceConfig();
    if (props.containsKey(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS) == false) {
      props.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, new ArrayList<Object>(Arrays.asList(new HttpMethodFilter(), new UrlSuffixFilter())));
    }
    if (props.containsKey(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS) == false) {
      props.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, new ArrayList<Object>(Arrays.asList(new NoCachingFilter())));
    }
    cfg.setPropertiesAndFeatures(props);
    return cfg;
  }

  @Override
  protected void initiate(ResourceConfig rc, WebApplication wa) {
    Application app = createApplication();
    try {
      // initialize the Jetty system
      // if more advanced control is needed, the second line can take an IoCComponentProviderFactory
      rc.add(app);
      wa.initiate(rc);
      
    } catch (RuntimeException ex) {
      s_logger.error("Exception occurred during intialization", ex);
      throw ex;
    }
  }

  /**
   * Creates the JaxRs application from the repository.
   * 
   * @return the application, not null
   */
  protected Application createApplication() {
    ComponentRepository repo = ComponentRepository.getFromServletContext(getServletContext());
    final Set<Object> singletons = repo.getRestComponents().buildJaxRsSingletons();
    final Set<Class<?>> classes = repo.getRestComponents().buildJaxRsClasses();
    Application app = new Application() {
      @Override
      public Set<Class<?>> getClasses() {
        return classes;
      }
      @Override
      public Set<Object> getSingletons() {
        return singletons;
      }
    };
    return app;
  }

}
