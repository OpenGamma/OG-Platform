/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.view.listener.ViewResultListener;

/**
 * Factory producing listeners to be attached to the results of a view process for a specific purpose.
 */
public interface ViewResultListenerFactory {

  /**
   * Creates a new view result listener.
   * 
   * @return the view result listener, not null
   */
  ViewResultListener createViewResultListener();
}
