/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeContext;

/**
 * An implementation of {@link ViewComputationCacheSource} that generates map backed caches.
 */
public class InMemoryViewComputationCacheSource extends DefaultViewComputationCacheSource {

  /**
   * @param fudgeContext Fudge context to use for serialization
   */
  public InMemoryViewComputationCacheSource(final FudgeContext fudgeContext) {
    super(new InMemoryIdentifierMap(), fudgeContext, new InMemoryBinaryDataStoreFactory(), new InMemoryBinaryDataStoreFactory());
  }

}
