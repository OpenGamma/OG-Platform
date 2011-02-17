/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.model;

import java.util.List;

/**
 * Node in a bundle
 */
public interface BundleNode {

  /**
   * Get all the fragement in the bundle
   * @return the list of all ordered fragments
   */
  List<Fragment> getAllFragment();
}
