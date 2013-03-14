/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.util.ArgumentChecker;

/**
 * Wraps multiple {@link FunctionBlacklistMaintainer} instances up as a single point that a provider of failure information should reference.
 */
public class MultipleFunctionBlacklistMaintainer implements FunctionBlacklistMaintainer {

  private final Collection<FunctionBlacklistMaintainer> _underlying;

  public MultipleFunctionBlacklistMaintainer(final Collection<FunctionBlacklistMaintainer> underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = new ArrayList<FunctionBlacklistMaintainer>(underlying);
  }

  private Collection<FunctionBlacklistMaintainer> getUnderlying() {
    return _underlying;
  }

  @Override
  public void failedJobItem(final CalculationJobItem item) {
    for (FunctionBlacklistMaintainer underlying : getUnderlying()) {
      underlying.failedJobItem(item);
    }
  }

  @Override
  public void failedJobItems(final Collection<CalculationJobItem> items) {
    for (FunctionBlacklistMaintainer underlying : getUnderlying()) {
      underlying.failedJobItems(items);
    }
  }

}
