/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import com.opengamma.util.ArgumentChecker;

/**
 * Abstract base class for RESTful resources intended for websites.
 * <p>
 * Websites and web-services are related but different RESTful elements.
 * This is because a website needs to bend the RESTful rules in order to be usable.
 */
public abstract class AbstractWebResource {

  /**
   * The servlet context.
   */
  private ServletContext _servletContext;
  /**
   * The Freemarker outputter.
   */
  private FreemarkerOutputter _freemarker;
  
  /**
   * JSON output formatter
   */
  private JSONOutputter _jsonOutputter = new JSONOutputter();

  /**
   * Creates the resource, used by the root resource.
   */
  protected AbstractWebResource() {
    // see setServletContext()
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebResource(final AbstractWebResource parent) {
    ArgumentChecker.notNull(parent, "parent");
    _servletContext = parent._servletContext;
    _freemarker = parent._freemarker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the servlet context.
   * @return the servlet context, not null
   */
  public ServletContext getServletContext() {
    return _servletContext;
  }

  /**
   * Setter used to inject the ServletContext.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   * @param servletContext  the servlet context, not null
   */
  @Context
  public void setServletContext(final ServletContext servletContext) {
    _servletContext = servletContext;
    _freemarker = new FreemarkerOutputter(servletContext);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Freemarker outputer.
   * @return the Freemarker outputter, not null
   */
  public FreemarkerOutputter getFreemarker() {
    return _freemarker;
  }
  
  /**
   * Gets the JSON security outputter
   * @return the json out putter, not null
   */
  protected JSONOutputter getJSONOutputter() {
    return _jsonOutputter;
  }

}
