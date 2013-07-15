/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import org.fudgemsg.FudgeContext;

/**
 * An implementation of {@link ViewComputationCacheSource} that generates map backed caches.
 */
public class InMemoryViewComputationCacheSource extends DefaultViewComputationCacheSource {

  /**
   * @param fudgeContext Fudge context to use for serialization
   */
  public InMemoryViewComputationCacheSource(final FudgeContext fudgeContext) {
    super(new InMemoryIdentifierMap(), fudgeContext, new DefaultFudgeMessageStoreFactory(
        new InMemoryBinaryDataStoreFactory(), fudgeContext), new DefaultFudgeMessageStoreFactory(
            new InMemoryBinaryDataStoreFactory(), fudgeContext));
  }

}
