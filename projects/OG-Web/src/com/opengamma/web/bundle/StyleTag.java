/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Tag used to generate the HTML output for CSS.
 */
public class StyleTag {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(StyleTag.class);

  /**
   * The request data.
   */
  private final WebBundlesData _data;

  /**
   * Creates an instance.
   * 
   * @param data  the request data, not null
   */
  public StyleTag(WebBundlesData data) {
    ArgumentChecker.notNull(data, "data");
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Outputs the HTML for the bundle.
   * 
   * @param bundleId  the bundle ID, not null
   * @param media  the media type, not null
   * @return the HTML for the bundle, may be null
   */
  public String print(String bundleId, String media) {
    ArgumentChecker.notNull(bundleId, "bundleId");
    ArgumentChecker.notNull(media, "media");
    DeployMode mode = _data.getMode();
    switch (mode) {
      case DEV:
        return printDev(bundleId, media);
      case PROD:
        return printProd(bundleId, media);
      default:
        s_logger.warn("Unknown deployment mode type: " + mode);
        return null;
    }
  }

  private String printProd(String bundleId, String media) {
    WebBundlesUris uris = new WebBundlesUris(_data);
    StringBuilder buf = new StringBuilder();
    buf.append("<link");
    buf.append(" ");
    buf.append("rel=\"stylesheet\"");
    buf.append(" ");
    buf.append("type=\"text/css\"");
    buf.append(" ");
    buf.append("media=\"").append(media).append("\"");
    buf.append(" ");
    buf.append("href=\"").append(uris.bundle(DeployMode.PROD, bundleId));
    buf.append("\">");
    return buf.toString();
  }

  private String printDev(String bundleId, String media) {
    WebBundlesUris uris = new WebBundlesUris(_data);
    StringBuilder buf = new StringBuilder();
    Bundle bundle = _data.getDevBundleManager().getBundle(bundleId);
    if (bundle != null) {
      String basePath = _data.getDevBundleManager().getBaseDir().getName();
      buf.append("<style type=\"text/css\" media=\"all\">\n");
      String imports = BundleImportUriUtils.buildImports(bundle, uris, basePath);
      buf.append(imports);
      buf.append("</style>");
    } else {
      s_logger.warn("{} not available ", bundleId);
    }
    return buf.toString();
  }

}
