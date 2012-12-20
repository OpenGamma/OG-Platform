/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;

/**
 * Represents a call to {@link ViewResultListener#processCompleted()}
 */
public class ProcessCompletedCall implements Function<ViewResultListener, Object> {

  @Override
  public Object apply(ViewResultListener viewProcessListener) {
    viewProcessListener.processCompleted();
    return null;
  }

}
