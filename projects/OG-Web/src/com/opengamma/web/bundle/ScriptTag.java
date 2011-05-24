/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Tag used to generate the HTML output for Javascript.
 */
public class ScriptTag {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ScriptTag.class);

  /**
   * The request data.
   */
  private final WebBundlesData _data;

  /**
   * Creates an instance.
   * 
   * @param data  the request data, not null
   */
  public ScriptTag(WebBundlesData data) {
    ArgumentChecker.notNull(data, "data");
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Outputs the HTML for the bundle.
   * 
   * @param bundleId  the bundle ID, not null
   * @return the HTML for the bundle, may be null
   */
  public String print(String bundleId) {
    ArgumentChecker.notNull(bundleId, "bundleId");
    DeployMode mode = _data.getMode();
    switch (mode) {
      case DEV:
        return printDev(bundleId);
      case PROD:
        return printProd(bundleId);
      default:
        s_logger.warn("Unknown deployment mode type: " + mode);
        return null;
    }
  }

  private String printProd(String bundleId) {
    WebBundlesUris uris = new WebBundlesUris(_data);
    StringBuilder buf = new StringBuilder();
    buf.append("<script");
    buf.append(" ");
    buf.append("src=\"");
    buf.append(uris.bundle(DeployMode.PROD, bundleId));
    buf.append("\">");
    buf.append("</script>");
    return buf.toString();
  }

  private String printDev(String bundleId) {
    StringBuilder buf = new StringBuilder();
    Bundle bundle = _data.getDevBundleManager().getBundle(bundleId);
    if (bundle != null) {
      List<Fragment> allFragment = bundle.getAllFragments();
      for (Fragment fragment : allFragment) {
        buf.append("<script src=\"/");
        buf.append(buildFragmentUrl(fragment));
        buf.append("\"></script>\n");
      }
    } else {
      s_logger.warn("{} not available ", bundleId);
    }
    return buf.toString();
  }

  private String buildFragmentUrl(Fragment fragment) {
    String uri = fragment.getFile().toURI().toASCIIString();
    String baseDir = _data.getDevBundleManager().getBaseDir().getName();
    int indexOf = uri.indexOf(baseDir);
    return uri.substring(indexOf);
  }

}
