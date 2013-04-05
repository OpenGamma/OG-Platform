/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

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
   *  Stamp to be appended to resource urls
   */
  private static String _stamp = "default";

  /**
   * Creates an instance.
   * 
   * @param data  the request data, not null
   */
  public ScriptTag(WebBundlesData data) {
    ArgumentChecker.notNull(data, "data");
    _data = data;
    Properties prop = new Properties();

    try {
      String resource = ClassLoader.getSystemResource("com/opengamma/web/bundle/BuildData.txt").getPath();
      prop.load(new FileInputStream(new File(resource)));
      _stamp = prop.getProperty("version");
      _stamp += prop.getProperty("build.date");

    } catch (IOException e) {
      s_logger.warn("Failed to load type mappings for value names", e);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Outputs the HTML for the bundle.
   * 
   * @param bundleId  the bundle ID, not null
   * @param inline  whether to inline the script
   * @return the HTML for the bundle, may be null
   */
  public String print(String bundleId, boolean inline) {
    ArgumentChecker.notNull(bundleId, "bundleId");
    Bundle bundle = _data.getBundleManager().getBundle(bundleId);
    if (bundle == null) {
      s_logger.warn("{} not available ", bundleId);
      return "";
    }
    DeployMode mode = _data.getMode();
    switch (mode) {
      case DEV:
        return inline ? printDevInline(bundle) : printDevLinked(bundle);
      case PROD:
        return inline ? printProdInline(bundle) : printProdLinked(bundle);
      default:
        s_logger.warn("Unknown deployment mode type: " + mode);
        return null;
    }
  }

  private String printProdInline(Bundle bundle) {
    StringBuilder buf = new StringBuilder();
    buf.append("<script src=\"text/javascript\"><!--//--><![CDATA[//><!--\n");
    buf.append(_data.getCompressor().compressBundle(bundle));
    buf.append("//--><!]]>\n</script>");
    return buf.toString();
  }

  private String printProdLinked(Bundle bundle) {
    StringBuilder buf = new StringBuilder();
    buf.append("<script src=\"");
    WebBundlesUris uris = new WebBundlesUris(_data);
    buf.append(uris.bundle(DeployMode.PROD, bundle.getId()));
    buf.append("?" + _stamp);
    buf.append("\"></script>");
    return buf.toString();
  }

  private String printDevInline(Bundle bundle) {
    StringBuilder buf = new StringBuilder();
    buf.append("<script src=\"text/javascript\"><!--//--><![CDATA[//><!--\n");
    buf.append(BundleUtils.readBundleSource(bundle));
    buf.append("//--><!]]>\n</script>");
    return buf.toString();
  }

  private String printDevLinked(Bundle bundle) {
    bundle = _data.getDevBundleManager().getBundle(bundle.getId());  // reload from dev manager
    StringBuilder buf = new StringBuilder();
    List<Fragment> allFragment = bundle.getAllFragments();
    for (Fragment fragment : allFragment) {
      buf.append("<script src=\"");
      buf.append(fragment.getPath());
      buf.append("\"></script>\n");
    }
    return buf.toString();
  }

}
