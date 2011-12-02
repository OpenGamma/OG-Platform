/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.StringUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.PagingRequest;

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
  protected FreemarkerOutputter getFreemarker() {
    return _freemarker;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds the paging request.
   * <p>
   * This method is lenient, applying sensible default values.
   * 
   * @param pgIdx  the paging first-item index, null if not input
   * @param pgNum  the paging page, null if not input
   * @param pgSze  the paging size, null if not input
   * @return the paging request, not null
   */
  protected PagingRequest buildPagingRequest(Integer pgIdx, Integer pgNum, Integer pgSze) {
    int size = (pgSze != null ? pgSze : PagingRequest.DEFAULT_PAGING_SIZE);
    if (pgIdx != null) {
      return PagingRequest.ofIndex(pgIdx, size);
    } else if (pgNum != null) {
      return PagingRequest.ofPage(pgNum, size);
    } else {
      return PagingRequest.ofPage(1, size);
    }
  }

  /**
   * Builds the sort order.
   * <p>
   * This method is lenient, returning the default in case of error.
   * 
   * @param <T>  the sort order type
   * @param order  the sort order, null or empty returns default
   * @param defaultOrder  the default order, not null
   * @return the sort order, not null
   */
  protected <T extends Enum<T>> T buildSortOrder(String order, T defaultOrder) {
    if (StringUtils.isEmpty(order)) {
      return defaultOrder;
    }
    order = order.toUpperCase(Locale.ENGLISH);
    if (order.endsWith(" ASC")) {
      order = StringUtils.replace(order, " ASC", "_ASC");
    } else if (order.endsWith(" DESC")) {
      order = StringUtils.replace(order, " DESC", "_DESC");
    } else if (order.endsWith("_ASC") == false && order.endsWith("_DESC") == false) {
      order = order + "_ASC";
    }
    try {
      Class<T> cls = defaultOrder.getDeclaringClass();
      return Enum.valueOf(cls, order);
    } catch (IllegalArgumentException ex) {
      return defaultOrder;
    }
  }

}
