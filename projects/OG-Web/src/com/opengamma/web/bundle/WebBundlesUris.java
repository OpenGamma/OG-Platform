/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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
  
  public URI bundles(DeployMode mode, String bundleId) {
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
