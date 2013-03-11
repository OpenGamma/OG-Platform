/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;

/**
 * Manages the CSS/Javascript bundles.
 * <p>
 * This acts as the root of the tree.
 */
public class BundleManager {

  /**
   * The map of bundles by ID.
   */
  private Map<String, Bundle> _bundleMap = new ConcurrentHashMap<String, Bundle>();

  /**
   * Creates an instance.
   */
  public BundleManager() {
  }

  //-------------------------------------------------------------------------
  /**
   * Looks up a bundle by ID.
   * 
   * @param id  the ID to look up, not null
   * @return the bundle, null if not found
   */
  public Bundle getBundle(String id) {
    return _bundleMap.get(id);
  }

  /**
   * Adds a bundle to the manager.
   * 
   * @param bundle  the bundle to add, not null
   */
  public void addBundle(Bundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(bundle.getId(), "bundle.id");
    
    // recursively add children as well
    for (Bundle loop : bundle.getAllBundles()) {
      _bundleMap.put(loop.getId(), loop);
    }
  }

  /**
   * Gets the set of all bundle IDs.
   * 
   * @return the unmodifiable bundle ID set, not null
   */
  public Set<String> getBundleIds() {
    return Collections.unmodifiableSet(_bundleMap.keySet());
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
    for (Entry<String, Bundle> entry : _bundleMap.entrySet()) {
      builder.append(entry.getKey(), entry.getValue().toString());
    }
    return builder.toString();
//    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
