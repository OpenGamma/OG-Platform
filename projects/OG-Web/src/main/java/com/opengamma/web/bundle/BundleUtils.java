/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.opengamma.DataNotFoundException;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility to assist in the creation of bundles in HTML.
 */
public final class BundleUtils {

  /**
   * Builds the imports for a bundle.
   * 
   * @param bundle  the bundle, not null
   * @param webBundleUris  the URI helper, not null
   * @return the bundle HTML import text, not null
   */
  public static String buildImports(Bundle bundle, WebBundlesUris webBundleUris) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(webBundleUris, "webBundleUris");
    
    StringBuilder buf = new StringBuilder();
    List<BundleNode> childNodes = bundle.getChildNodes();
    for (BundleNode node : childNodes) {
      if (node instanceof Bundle) {
        Bundle nodeBundle = (Bundle) node;
        buf.append("@import url('");
        buf.append(webBundleUris.bundle(DeployMode.DEV, nodeBundle.getId()).getPath());
        buf.append("');\n");
      }
      if (node instanceof Fragment) {
        Fragment fragment = (Fragment) node;
        buf.append("@import url('" + fragment.getPath() + "');\n");
      }
    }
    return buf.toString();
  }

  /**
   * Reads and combines a bundle.
   * 
   * @param bundle  the bundle to read, not null
   * @return the combined source code, not null
   */
  public static String readBundleSource(Bundle bundle) {
    List<Fragment> allFragments = bundle.getAllFragments();
    StringBuilder buf = new StringBuilder(1024);
    for (Fragment fragment : allFragments) {
      try {
        buf.append(IOUtils.toString(fragment.getUri()));
        buf.append("\n");
      } catch (IOException ex) {
        throw new DataNotFoundException("IOException reading " + fragment.getUri());
      }
    }
    return buf.toString();
  }

}
