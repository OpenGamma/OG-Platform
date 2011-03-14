/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Script tag
 */
public class ScriptTag {
  
  private static final Logger s_logger = LoggerFactory.getLogger(StyleTag.class);

  private final BundleManager _bundleManager;

  private final DeployMode _mode;
  
  private String _bundleId;
  
  private final WebBundlesUris _webBundlesUris; 

  /**
   * Create Script tag
   * 
   * @param bundleManager   the development bundle manager, not null
   * @param mode            the deployment mode, not null
   * @param webBundlesUris  the base URI, not null.
   */
  public ScriptTag(BundleManager bundleManager, DeployMode mode, WebBundlesUris webBundlesUris) {
    ArgumentChecker.notNull(bundleManager, "bundleManager");
    ArgumentChecker.notNull(mode, "mode");
    ArgumentChecker.notNull(webBundlesUris, "webBundlesUris");
    
    _bundleManager = bundleManager;
    _mode = mode;
    _webBundlesUris = webBundlesUris;
  }

  /**
   * Gets the bundleId field.
   * @return the bundleId
   */
  public String getBundleId() {
    return _bundleId;
  }

  /**
   * Sets the bundleId field.
   * @param bundleId  the bundleId
   */
  public void setBundleId(String bundleId) {
    _bundleId = bundleId;
  }

  public String print() {
    switch (_mode) {
      case DEV:
        return printDev();
      case PROD:
        return printProd();
      default:
        s_logger.warn("unknown deployment mode type");
        return null;
    }
  }

  /**
   * @return
   */
  private String printProd() {
    StringBuilder buf = new StringBuilder();
    buf.append("<script");
    buf.append(" ");
    buf.append("src=\"");
    buf.append(_webBundlesUris.bundles(DeployMode.PROD, getBundleId()));
    buf.append("\">");
    buf.append("</script>");
    return buf.toString();
  }

  /**
   * @return
   */
  private String printDev() {
    return buildScripts(getBundleId());
  }

  /**
   * Print the tag output
   * 
   * @param bundleId     the bundle id, not null
   * @return the tag out
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
      List<Fragment> allFragment = bundle.getAllFragment();
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
