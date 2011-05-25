/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.List;

/**
 * A node in the bundle of CSS/Javascript files.
 */
public interface BundleNode {

  /**
   * Get all the bundles in the bundle.
   * 
   * @return the ordered list of all bundles, may be unmodifiable, not null
   */
  List<Bundle> getAllBundles();
  /**
   * Get all the fragments in the bundle.
   * 
   * @return the ordered list of all fragments, may be unmodifiable, not null
   */
  List<Fragment> getAllFragments();

}
