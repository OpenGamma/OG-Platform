/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.web.security;

import java.net.URI;

import com.opengamma.engine.security.Security;

/**
 * URIs for web-based securities.
 */
public class WebSecuritiesUris {

  /**
   * The data.
   */
  private final WebSecuritiesData _data;

  public WebSecuritiesUris(WebSecuritiesData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI securities() {
    return WebSecuritiesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI security() {
    return WebSecurityResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param security  the security, not null
   * @return the URI
   */
  public URI security(final Security security) {
    return WebSecurityResource.uri(_data, security.getUniqueIdentifier());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI securityVersions() {
    return WebSecurityVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI securityVersion() {
    return WebSecurityVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param security  the security, not null
   * @return the URI
   */
  public URI securityVersion(final Security security) {
    return WebSecurityVersionResource.uri(_data, security.getUniqueIdentifier());
  }

}
