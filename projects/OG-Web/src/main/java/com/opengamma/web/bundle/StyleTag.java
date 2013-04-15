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
   * @param inline  whether to inline the script
   * @return the HTML for the bundle, may be null
   */
  public String print(String bundleId, String media, boolean inline) {
    ArgumentChecker.notNull(bundleId, "bundleId");
    ArgumentChecker.notNull(media, "media");
    Bundle bundle = _data.getBundleManager().getBundle(bundleId);
    if (bundle == null) {
      s_logger.warn("{} not available ", bundleId);
      return "";
    }
    DeployMode mode = _data.getMode();
    switch (mode) {
      case DEV:
        return inline ? printDevInline(bundle, media) : printDevLinked(bundle, media);
      case PROD:
        return inline ? printProdInline(bundle, media) : printProdLinked(bundle, media);
      default:
        s_logger.warn("Unknown deployment mode type: " + mode);
        return null;
    }
  }

  private String printProdInline(Bundle bundle, String media) {
    StringBuilder buf = new StringBuilder();
    buf.append("<style type=\"text/css\" media=\"");
    buf.append(media);
    buf.append("\">\n");
    buf.append(_data.getCompressor().compressBundle(bundle));
    buf.append("\n</style>");
    return buf.toString();
  }

  private String printProdLinked(Bundle bundle, String media) {
    WebBundlesUris uris = new WebBundlesUris(_data);
    StringBuilder buf = new StringBuilder();
    buf.append("<link rel=\"stylesheet\" type=\"text/css\" media=\"");
    buf.append(media);
    buf.append("\" href=\"");
    buf.append(uris.bundle(DeployMode.PROD, bundle.getId()));
    buf.append("?" + BuildData.getBuildStamp());
    buf.append("\">");
    return buf.toString();
  }

  private String printDevInline(Bundle bundle, String media) {
    StringBuilder buf = new StringBuilder();
    buf.append("<style type=\"text/css\" media=\"");
    buf.append(media);
    buf.append("\">\n");
    buf.append(BundleUtils.readBundleSource(bundle));
    buf.append("</style>");
    return buf.toString();
  }

  private String printDevLinked(Bundle bundle, String media) {
    bundle = _data.getDevBundleManager().getBundle(bundle.getId());  // reload from dev manager
    WebBundlesUris uris = new WebBundlesUris(_data);
    StringBuilder buf = new StringBuilder();
    buf.append("<style type=\"text/css\" media=\"all\">\n");
    String imports = BundleUtils.buildImports(bundle, uris);
    buf.append(imports);
    buf.append("</style>");
    return buf.toString();
  }

}
