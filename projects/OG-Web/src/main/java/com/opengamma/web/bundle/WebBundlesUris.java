/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.net.URI;

/**
 * URIs for web-based bundles.
 */
public class WebBundlesUris {

  /**
   * The data.
   */
  private final WebBundlesData _data;

  /**
   * Creates an instance.
   * 
   * @param data  the web data, not null
   */
  public WebBundlesUris(WebBundlesData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI bundles() {
    return WebBundlesResource.uri(_data);
  }
  
  /**
   * Gets the URI for a bundle.
   * 
   * @param mode  the deployment mode, not null
   * @param bundleId  the bundle ID, not null
   * @return the URI, not null
   */
  public URI bundle(DeployMode mode, String bundleId) {
    switch (mode) {
      case PROD:
        return WebProdBundleResource.uri(_data, bundleId);
      case DEV:
        return WebDevBundleResource.uri(_data, bundleId);
      default:
        return bundles();
    }
  }
  
}
