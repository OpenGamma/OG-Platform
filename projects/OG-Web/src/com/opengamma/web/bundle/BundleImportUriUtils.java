/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility to assist in the creation of {@code @import} statements in HTML.
 */
public final class BundleImportUriUtils {

  /**
   * Builds the imports for a bundle.
   * 
   * @param bundle  the bundle, not null
   * @param webBundleUris  the URI helper, not null
   * @param basePath  the base path, not null
   * @return the bundle HTML import text, not null
   */
  public static String buildImports(Bundle bundle, WebBundlesUris webBundleUris, String basePath) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(webBundleUris, "webBundleUris");
    ArgumentChecker.notNull(basePath, "basePath");
    
    StringBuilder buf = new StringBuilder();
    List<BundleNode> childNodes = bundle.getChildNodes();
    for (BundleNode node : childNodes) {
      if (node instanceof Bundle) {
        Bundle nodeBundle = (Bundle) node;
        buf.append("@import url('");
        buf.append(webBundleUris.bundle(DeployMode.DEV, nodeBundle.getId()));
        buf.append("');\n");
      }
      if (node instanceof Fragment) {
        Fragment fragment = (Fragment) node;
        String uri = fragment.getFile().toURI().toASCIIString();
        int indexOf = uri.indexOf(basePath);
        buf.append("@import url('/" + uri.substring(indexOf) + "');\n");
      }
    }
    return buf.toString();
  }

}
