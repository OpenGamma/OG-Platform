/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Style tag
 */
public class StyleTag {
  
  private static final Logger s_logger = LoggerFactory.getLogger(StyleTag.class);

  private final BundleManager _bundleManager;

  private final DeployMode _mode;
  
  private String _media;
  
  private String _bundleId;
  
  private final WebBundlesUris _webBundleUris;

  /**
   * Create style tag
   * 
   * @param bundleManager   the development bundle manager, not null.
   * @param mode            the deployment mode.
   * @param webBundleUris   the base webBundleUris.
   */
  public StyleTag(BundleManager bundleManager, DeployMode mode, WebBundlesUris webBundleUris) {
    ArgumentChecker.notNull(bundleManager, "bundleManager");
    ArgumentChecker.notNull(mode, "mode");
    ArgumentChecker.notNull(webBundleUris, "webBundleUris");
    
    _bundleManager = bundleManager;
    _mode = mode;
    _webBundleUris = webBundleUris;
  }

  /**
   * Gets the media field.
   * @return the media
   */
  public String getMedia() {
    return _media;
  }

  /**
   * Sets the media field.
   * @param media  the media
   */
  public void setMedia(String media) {
    _media = media;
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
    buf.append("href=\"").append(_webBundleUris.bundles(DeployMode.PROD, getBundleId()));
    buf.append("\">");
    return buf.toString();
  }

  private String printDev() {
    StringBuilder buf = new StringBuilder();
    Bundle bundle = _bundleManager.getBundle(_bundleId);
    String basePath = _bundleManager.getBaseDir().getName();
    buf.append("<style type=\"text/css\" media=\"all\">\n");
    String imports = BundleImportUriUtils.buildImports(bundle, _webBundleUris, basePath);
    buf.append(imports);
    buf.append("</style>");
    return buf.toString();
  }

  /**
   * Print the tag output
   * 
   * @param bundleId     the bundle id, not null
   * @param media   the media type, not null
   * @return the tag out
   */
  public String print(String bundleId, String media) {
    ArgumentChecker.notNull(bundleId, "bundleId");
    ArgumentChecker.notNull(media, "media");
    
    setBundleId(bundleId);
    setMedia(media);
    return print();    
  }
  
}
