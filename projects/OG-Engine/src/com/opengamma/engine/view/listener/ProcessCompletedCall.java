/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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
