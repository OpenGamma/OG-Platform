/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import java.util.Collection;
import java.util.Properties;

import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.CacheWriterFactory;
import net.sf.ehcache.writer.writebehind.operations.SingleOperationType;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.ArgumentChecker;

/**
 * Cache writer factory.
 * @param <D> the document type
 */
public class UidToDocumentCacheWriterFactory<D extends AbstractDocument> extends CacheWriterFactory {

  private AbstractChangeProvidingMaster<D> _underlying;

  public UidToDocumentCacheWriterFactory(AbstractChangeProvidingMaster<D> underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  @Override
  public CacheWriter createCacheWriter(Ehcache ehcache, Properties properties) {
    return new UidToDocumentCacheWriter();
  }

  /**
   * Cache writer.
   */
  public class UidToDocumentCacheWriter implements CacheWriter {

    @Override
    public CacheWriter clone(Ehcache ehcache) throws CloneNotSupportedException { //TODO ????
      throw new CloneNotSupportedException();
    }

    @Override
    public void init() {
      // Empty
    }

    @Override
    public void dispose() throws CacheException {
      // Empty
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Element element) throws CacheException {
      try {
        _underlying.get((UniqueId) element.getObjectKey());

        // Update if no exception
        _underlying.update((D) element.getObjectValue());
      } catch (DataNotFoundException e) {
        // Add if not found
        _underlying.add((D) element.getObjectValue());
      }
    }

    @Override
    public void writeAll(Collection<Element> elements) throws CacheException {
      // TODO
    }

    @Override
    public void delete(CacheEntry cacheEntry) throws CacheException {
      // TODO
    }

    @Override
    public void deleteAll(Collection<CacheEntry> cacheEntries) throws CacheException {
      // TODO
    }

    @Override
    public void throwAway(Element element, SingleOperationType singleOperationType, RuntimeException e) {
      // TODO
    }
  }
}
