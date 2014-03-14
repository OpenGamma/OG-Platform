/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import javax.servlet.ServletContext;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.util.OpenGammaClock;

/**
 * Abstract base class for RESTful resources intended for websites.
 * <p>
 * This extends the superclass {@code AbstractWebResource} to add {@link FreemarkerOutputter Freemarker}.
 * <p>
 * Websites and web-services are related but different RESTful elements.
 * This is because a website needs to bend the RESTful rules in order to be usable.
 */
public abstract class AbstractSingletonWebResource extends AbstractWebResource {

  /**
   * Creates the resource.
   */
  protected AbstractSingletonWebResource() {
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * <p>
   * This sets up default root data for all subclasses.
   * 
   * @param uriInfo  the URI information, not null
   * @return the output root data, not null
   */
  protected FlexiBean createRootData(UriInfo uriInfo) {
    // inline FreemarkerOutputter.createRootData() to avoid extra ServletContext parameter
    FlexiBean out = new FlexiBean();
    out.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    out.put("timeFormatter", DateTimeFormatter.ofPattern("HH:mm:ss"));
    out.put("offsetFormatter", DateTimeFormatter.ofPattern("Z"));
    out.put("homeUris", new WebHomeUris(uriInfo));
    out.put("baseUri", uriInfo.getBaseUri().toString());
    return out;
  }

  /**
   * Gets the Freemarker outputer.
   * 
   * @param context  the servlet context, not null
   * @return the Freemarker outputter, not null
   */
  protected FreemarkerOutputter getFreemarker(ServletContext context) {
    return new FreemarkerOutputter(context);
  }

}
