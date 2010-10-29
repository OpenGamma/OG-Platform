/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.web;

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

  public WebHomeUris(UriInfo uriInfo) {
    _uriInfo = uriInfo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI home() {
    return WebHomeResource.uri(_uriInfo);
  }

}
