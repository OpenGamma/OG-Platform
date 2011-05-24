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
   * The bundle manager.
   */
  private final BundleManager _bundleManager;
  /**
   * The bundle URI helper.
   */
  private final WebBundlesUris _webBundleUris; 
  /**
   * The deploy mode.
   */
  private final DeployMode _mode;
  /**
   * The bundle ID.
   */
  private String _bundleId;

  /**
   * Creates an instance.
   * 
   * @param bundleManager  the development bundle manager, not null
   * @param webBundleUris  the URI helper, not null.
   * @param mode  the deployment mode, not null
   */
  public ScriptTag(BundleManager bundleManager, WebBundlesUris webBundleUris, DeployMode mode) {
    ArgumentChecker.notNull(bundleManager, "bundleManager");
    ArgumentChecker.notNull(webBundleUris, "webBundleUris");
    ArgumentChecker.notNull(mode, "mode");
    
    _bundleManager = bundleManager;
    _webBundleUris = webBundleUris;
    _mode = mode;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the bundle ID.
   * 
   * @return the bundle ID, may be null
   */
  public String getBundleId() {
    return _bundleId;
  }

  /**
   * Sets the bundle ID.
   * 
   * @param bundleId  the bundle ID, may be null
   */
  public void setBundleId(String bundleId) {
    _bundleId = bundleId;
  }

  //-------------------------------------------------------------------------
  /**
   * Outputs the HTML for the bundle.
   * 
   * @return the HTML for the bundle, may be null
   */
  public String print() {
    switch (_mode) {
      case DEV:
        return printDev();
      case PROD:
        return printProd();
      default:
        s_logger.warn("Unknown deployment mode type: " + _mode);
        return null;
    }
  }

  private String printProd() {
    StringBuilder buf = new StringBuilder();
    buf.append("<script");
    buf.append(" ");
    buf.append("src=\"");
    buf.append(_webBundleUris.bundle(DeployMode.PROD, getBundleId()));
    buf.append("\">");
    buf.append("</script>");
    return buf.toString();
  }

  private String printDev() {
    return buildScripts(getBundleId());
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
    setBundleId(bundleId);
    return print();    
  }

  private String buildScripts(String bundleId) {
    StringBuilder buf = new StringBuilder();
    Bundle bundle = _bundleManager.getBundle(bundleId);
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
    String baseDir = _bundleManager.getBaseDir().getName();
    int indexOf = uri.indexOf(baseDir);
    return uri.substring(indexOf);
  }

}
