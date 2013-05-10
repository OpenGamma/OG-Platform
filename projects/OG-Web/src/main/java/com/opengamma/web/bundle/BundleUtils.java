/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.sass.JRubySassCompiler;

/**
 * Utility to assist in the creation of bundles in HTML.
 */
public final class BundleUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(BundleUtils.class);
  
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
        buf.append(webBundleUris.bundle(DeployMode.DEV, nodeBundle.getId()));
        buf.append("');\n");
      }
      if (node instanceof Fragment) {
        Fragment fragment = (Fragment) node;
        BundleType type = BundleType.getType(fragment.getUri().toString());
        if (type == BundleType.SCSS) {
          buf.append("@import url('");
          String fragmentPath = buildFragmentPath(fragment);
          buf.append(webBundleUris.sassfragment(fragmentPath));
          buf.append("');\n");
        } else {
          buf.append("@import url('" + fragment.getPath() + "');\n");
        }
      }
    }
    return buf.toString();
  }

  private static String buildFragmentPath(Fragment fragment) {
    String fragmentPath = fragment.getPath().toLowerCase();
    fragmentPath = fragmentPath.startsWith("/") ? fragmentPath.substring(1) : fragmentPath;
    fragmentPath = fragmentPath.replace("." + BundleType.SCSS.getSuffix(), "." + BundleType.CSS.getSuffix());
    return fragmentPath;
  }

  /**
   * Reads and combines a bundle.
   * 
   * @param bundle  the bundle to read, not null
   * @return the combined source code, not null
   */
  public static String readBundleSource(Bundle bundle) {
    
    JRubySassCompiler sassCompiler = JRubySassCompiler.getInstance();
    
    List<Fragment> allFragments = bundle.getAllFragments();
    StringBuilder buf = new StringBuilder(1024);
    for (Fragment fragment : allFragments) {
      try {
        String fragmentContent = IOUtils.toString(fragment.getUri());
        BundleType type = BundleType.getType(fragment.getUri().toString());
        if (type == BundleType.SCSS) {
          s_logger.debug("raw sass:\n {}\n", fragmentContent);
          fragmentContent = sassCompiler.sassConvert(fragmentContent);
          s_logger.debug("compiled sass:\n {}\n", fragmentContent);
        }
        buf.append(fragmentContent);
        buf.append("\n");
      } catch (IOException ex) {
        throw new DataNotFoundException("IOException reading " + fragment.getUri());
      }
    }
    return buf.toString();
  }

}
