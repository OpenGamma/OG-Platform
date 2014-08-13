/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.cache;

import java.util.concurrent.FutureTask;

import com.google.common.cache.Cache;

/**
 * Provider of caches to the engine.
 */
public interface CacheProvider {

  Cache<MethodInvocationKey, FutureTask<Object>> get();
}
