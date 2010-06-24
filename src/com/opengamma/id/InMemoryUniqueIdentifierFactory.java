/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This will provide a sequence of unique identifiers with the provided scheme
 */
public class InMemoryUniqueIdentifierFactory {
  private String _scheme;
  private AtomicLong _idCount = new AtomicLong(0);
  
  public InMemoryUniqueIdentifierFactory(String scheme) {
    _scheme = scheme;
  }
  
  public UniqueIdentifier getNextUniqueIdentifier() {
    long id = _idCount.addAndGet(1);
    return UniqueIdentifier.of(_scheme, Long.toString(id));
  }
}
