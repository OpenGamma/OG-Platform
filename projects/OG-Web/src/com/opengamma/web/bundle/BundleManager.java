/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Manages the bundles and act as the root Bundle 
 */
public class BundleManager {

  private static final Logger s_logger = LoggerFactory.getLogger(BundleManager.class);
  /**
   * Map containing bundles referenced by id
   */
  private Map<String, Bundle> _bundleMap = new ConcurrentHashMap<String, Bundle>();
  
  private File _baseDir;
  
  public Bundle getBundle(String id) {
    return _bundleMap.get(id);
  }
  
  public void addBundle(Bundle abundle) {
    ArgumentChecker.notNull(abundle, "bundle");
    ArgumentChecker.notNull(abundle.getId(), "bundle.Id");
    _bundleMap.put(abundle.getId(), abundle);
    //recursively add children as well
    for (BundleNode node : abundle.getChildNodes()) {
      if (node instanceof Bundle) {
        addBundle((Bundle) node);
      }
    }
  }
  
  /**
   * Gets the baseDir field.
   * @return the baseDir
   */
  public File getBaseDir() {
    return _baseDir;
  }

  /**
   * Sets the baseDir field.
   * @param baseDir  the baseDir
   */
  public void setBaseDir(File baseDir) {
    _baseDir = baseDir;
  }
  
  public Set<String> getBundleIds() {
    return Collections.unmodifiableSet(_bundleMap.keySet());
  }
  
}
