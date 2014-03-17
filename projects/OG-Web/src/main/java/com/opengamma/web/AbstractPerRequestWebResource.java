/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.util.ArgumentChecker;

/**
 * Abstract base class for RESTful resources intended for websites.
 * <p>
 * This extends the superclass {@code AbstractWebResource} to add per-request behavior.
 * It is intended that singleton resources should extend {@code AbstractWebResource} directly,
 * while resources that must be created on every request should extend this class.
 * This class also adds support for {@link FreemarkerOutputter Freemarker}.
 * <p>
 * Websites and web-services are related but different RESTful elements.
 * This is because a website needs to bend the RESTful rules in order to be usable.
 * 
 * @param <T>  the data subclass
 */
public abstract class AbstractPerRequestWebResource<T extends WebPerRequestData> extends AbstractWebResource {

  /**
   * The servlet context.
   */
  private ServletContext _servletContext;
  /**
   * The Freemarker outputter.
   */
  private FreemarkerOutputter _freemarker;
  /**
   * The data.
   */
  private T _data;

  /**
   * Creates the resource, used by the root resource.
   * 
   * @param data  the per-request data, not null
   */
  protected AbstractPerRequestWebResource(T data) {
    // see setServletContext()
    _data = ArgumentChecker.notNull(data, "data");
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractPerRequestWebResource(final AbstractPerRequestWebResource<T> parent) {
    ArgumentChecker.notNull(parent, "parent");
    _servletContext = parent._servletContext;
    _freemarker = parent._freemarker;
    _data = parent._data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the servlet context.
   * 
   * @return the servlet context, not null
   */
  public ServletContext getServletContext() {
    return _servletContext;
  }

  /**
   * Setter used to inject the ServletContext.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   * 
   * @param servletContext  the servlet context, not null
   */
  @Context
  public void setServletContext(final ServletContext servletContext) {
    _servletContext = servletContext;
    _freemarker = new FreemarkerOutputter(servletContext);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI info.
   * 
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return _data.getUriInfo();
  }

  /**
   * Setter used to inject the URIInfo.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   * @param uriInfo  the URI info, not null
   */
  @Context
  public final void setUriInfo(final UriInfo uriInfo) {
    _data.setUriInfo(uriInfo);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the data.
   * 
   * @return the data, not null
   */
  public T data() {
    return _data;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    return FreemarkerOutputter.createRootData(getUriInfo());
  }

  /**
   * Gets the Freemarker outputer.
   * 
   * @return the Freemarker outputter, not null
   */
  protected FreemarkerOutputter getFreemarker() {
    return _freemarker;
  }

}
