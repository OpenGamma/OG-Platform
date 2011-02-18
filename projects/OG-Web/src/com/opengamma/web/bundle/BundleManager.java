/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.util.ArgumentChecker;

/**
 * Manages the bundles and act as the root Bundle 
 */
public class BundleManager {

  /**
   * Map containing bundles referenced by id
   */
  private Map<String, Bundle> _bundleMap = new ConcurrentHashMap<String, Bundle>();
  
  public Bundle getBundle(String id) {
    return _bundleMap.get(id);
  }
  
  public void addBundle(Bundle abundle) {
    ArgumentChecker.notNull(abundle, "bundle");
    ArgumentChecker.notNull(abundle.getId(), "bundle.Id");
    _bundleMap.put(abundle.getId(), abundle);
  }
  
}
