/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import org.fudgemsg.FudgeContext;

/**
 * An implementation of {@link ViewComputationCacheSource} that generates map backed caches.
 */
public class MapViewComputationCacheSource extends AbstractViewComputationCacheSource {

  /**
   * @param fudgeContext Fudge context to use for serialization
   */
  public MapViewComputationCacheSource(final FudgeContext fudgeContext) {
    super(new MapValueSpecificationIdentifierSource(), fudgeContext);
  }

  @Override
  protected ValueSpecificationIdentifierBinaryDataStore constructDataStore(ViewComputationCacheKey key) {
    return new MapValueSpecificationIdentifierBinaryDataStore();
  }
  
  public static ViewComputationCache createMapViewComputationCache(final FudgeContext fudgeContext) {
    return new StandardViewComputationCache(new MapValueSpecificationIdentifierSource(), new MapValueSpecificationIdentifierBinaryDataStore(), fudgeContext);
  }

}
