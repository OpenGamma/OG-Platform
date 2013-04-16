/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An implementation of {@link BinaryDataStore} backed by a
 * {@link ConcurrentMap}.
 * This class is internally synchronized.
 */
public class InMemoryBinaryDataStore extends AbstractBinaryDataStore implements BinaryDataStore {
  private final ConcurrentMap<Long, byte[]> _underlyingMap = new ConcurrentHashMap<Long, byte[]>();

  @Override
  public void delete() {
    // Technically we don't have to do anything here. But just in case this isn't reclaimed
    // quickly enough by the garbage collector, or this instance has gone to old space or something,
    // we want to help out as much as we can.
    _underlyingMap.clear();
  }

  @Override
  public byte[] get(long identifier) {
    return _underlyingMap.get(identifier);
  }

  @Override
  public void put(long identifier, byte[] data) {
    _underlyingMap.put(identifier, data);
  }

}
