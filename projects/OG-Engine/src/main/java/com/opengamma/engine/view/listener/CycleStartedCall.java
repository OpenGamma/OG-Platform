/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;

/**
 * Represents a call to {@link ViewResultListener#cycleStarted(ViewCycleMetadata)}.
 */
public class CycleStartedCall implements Function<ViewResultListener, Object> {

  private final ViewCycleMetadata _cycleMetadata;

  public CycleStartedCall(ViewCycleMetadata cycleMetadata) {
    _cycleMetadata = cycleMetadata;
  }

  public ViewCycleMetadata getCycleMetadata() {
    return _cycleMetadata;
  }

  @Override
  public Object apply(ViewResultListener listener) {
    listener.cycleStarted(getCycleMetadata());
    return null;
  }

}
