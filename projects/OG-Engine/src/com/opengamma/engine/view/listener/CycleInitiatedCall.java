/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.CycleInfo;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

import java.util.Map;
import java.util.Set;

/**
 * Represents a call to {@link com.opengamma.engine.view.listener.ViewResultListener#cycleInitiated(com.opengamma.engine.view.calc.ViewCycle)}
 */
public class CycleInitiatedCall implements Function<ViewResultListener, Object> {

  private CycleInfo _cycleInfo;

  public CycleInitiatedCall(CycleInfo cycleInfo) {
    update(cycleInfo);
  }

  public void update(CycleInfo cycleInfo) {
    _cycleInfo = cycleInfo;
  }

  public CycleInfo getCycleInfo() {
    return _cycleInfo;
  }

  @Override
  public Object apply(ViewResultListener listener) {
    listener.cycleInitiated(getCycleInfo());
    return null;
  }

}
