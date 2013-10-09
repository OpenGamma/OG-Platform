/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

/**
 * URIs for web-based securities.
 */
public class WebHomeUris {

  /**
   * The data.
   */
  private final UriInfo _uriInfo;

  /**
   * Creates an instance.
   * 
   * @param uriInfo  the request URI information, not null
   */
  public WebHomeUris(UriInfo uriInfo) {
    _uriInfo = uriInfo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI of the home page.
   * @return the URI
   */
  public URI home() {
    return WebHomeResource.uri(_uriInfo);
  }

  /**
   * Gets the URI of the about page.
   * @return the URI
   */
  public URI about() {
    return WebAboutResource.uri(_uriInfo);
  }

}
