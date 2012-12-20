/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.ehcache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

/**
 * A simple no-op implementation of {@link CacheEventListener} which can be extended cleanly.
 */
public abstract class AbstractCacheEventListener implements CacheEventListener {

  @Override
  public void dispose() {
  }

  @Override
  public void notifyElementEvicted(Ehcache cache, Element element) {
  }

  @Override
  public void notifyElementExpired(Ehcache cache, Element element) {
  }

  @Override
  public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
  }

  @Override
  public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
  }

  @Override
  public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
  }

  @Override
  public void notifyRemoveAll(Ehcache cache) {
  }
  
  @Override
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

}
