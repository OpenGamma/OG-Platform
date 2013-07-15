/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

/**
 * A source of {@link FudgeMessageStore} objects for a given cache key.
 */
public interface FudgeMessageStoreFactory {

  FudgeMessageStore createMessageStore(ViewComputationCacheKey cacheKey);

}
