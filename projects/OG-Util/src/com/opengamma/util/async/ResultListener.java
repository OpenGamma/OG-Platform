/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.async;

import com.opengamma.util.PublicAPI;

/**
 * Callback interface for notification of completion of an {@link AsynchronousOperation}.
 * 
 * @param <T> type of the result
 */
@PublicAPI
public interface ResultListener<T> {

  /**
   * Called when the operation is complete - i.e. a result value is available or an exception was thrown
   * 
   * @param result the result handle
   */
  void operationComplete(AsynchronousResult<T> result);

}
