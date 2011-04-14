/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 * Imports URI builder
 */
public final class BundleImportUriUtils {
 
  public static String buildImports(Bundle bundle, WebBundlesUris webBundleUris, String basePath) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(webBundleUris, "webBundleUris");
    ArgumentChecker.notNull(basePath, "basePath");
    
    StringBuilder buf = new StringBuilder();
    List<BundleNode> childNodes = bundle.getChildNodes();
    for (BundleNode node : childNodes) {
      if (node instanceof Bundle) {
        Bundle nodeBundle = (Bundle) node;
        buf.append("@import url('").append(webBundleUris.bundles(DeployMode.DEV, nodeBundle.getId()));
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
