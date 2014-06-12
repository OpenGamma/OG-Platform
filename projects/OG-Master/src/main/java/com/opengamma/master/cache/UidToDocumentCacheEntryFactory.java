/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.ArgumentChecker;

/**
 * Cache factory.
 * @param <D>  the document type
 */
public class UidToDocumentCacheEntryFactory<D extends AbstractDocument> implements CacheEntryFactory {

  /** The underlying master. */
  private final AbstractChangeProvidingMaster<D> _underlying;

  public UidToDocumentCacheEntryFactory(AbstractChangeProvidingMaster<D> underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  @Override
  public Object createEntry(Object key) throws Exception {
    return _underlying.get((UniqueId) key);
  }

}
