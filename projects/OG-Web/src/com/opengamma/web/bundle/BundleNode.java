/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.List;

/**
 * Node in a bundle
 */
public interface BundleNode {

  /**
   * Get all the fragments in the bundle.
   * 
   * @return the list of all ordered fragments
   */
  List<Fragment> getAllFragment();

}
