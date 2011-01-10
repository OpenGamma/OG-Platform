/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

/**
 * A generic listener for merged updates
 * 
 * @param <T>  the type of the updates
 */
public interface MergedUpdateListener<T> {
  
  void handleResult(T result);
  
}
