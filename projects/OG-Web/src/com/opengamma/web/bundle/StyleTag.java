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
   * The media.
   */
  private String _media;
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
  public StyleTag(BundleManager bundleManager, WebBundlesUris webBundleUris, DeployMode mode) {
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

  /**
   * Gets the media.
   * 
   * @return the media
   */
  public String getMedia() {
    return _media;
  }

  /**
   * Sets the media.
   * 
   * @param media  the media
   */
  public void setMedia(String media) {
    _media = media;
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
    buf.append("<link");
    buf.append(" ");
    buf.append("rel=\"stylesheet\"");
    buf.append(" ");
    buf.append("type=\"text/css\"");
    buf.append(" ");
    buf.append("media=\"").append(getMedia()).append("\"");
    buf.append(" ");
    buf.append("href=\"").append(_webBundleUris.bundle(DeployMode.PROD, getBundleId()));
    buf.append("\">");
    return buf.toString();
  }

  private String printDev() {
    StringBuilder buf = new StringBuilder();
    Bundle bundle = _bundleManager.getBundle(_bundleId);
    if (bundle != null) {
      String basePath = _bundleManager.getBaseDir().getName();
      buf.append("<style type=\"text/css\" media=\"all\">\n");
      String imports = BundleImportUriUtils.buildImports(bundle, _webBundleUris, basePath);
      buf.append(imports);
      buf.append("</style>");
    } else {
      s_logger.warn("{} not available ", _bundleId);
    }
    return buf.toString();
  }

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
    
    setBundleId(bundleId);
    setMedia(media);
    return print();    
  }

}
