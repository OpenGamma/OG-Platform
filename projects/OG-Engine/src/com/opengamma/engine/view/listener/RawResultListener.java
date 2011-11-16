/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.opengamma.engine.view.ViewResultModel;

/**
 * A listener to computation results.
 */
public interface RawResultListener {

  /**
   * Called when a result is available.
   *
   * @param result  the result, not null
   */
  void resultAvailable(ViewResultModel result);
  
}
