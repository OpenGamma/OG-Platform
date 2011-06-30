/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

/**
 * 
 */
public abstract class MergingViewCycleExecutionSequence implements ViewCycleExecutionSequence {

  protected ViewCycleExecutionOptions merge(ViewCycleExecutionOptions nextCycle, ViewCycleExecutionOptions defaults) {
    if (defaults != null) {
      if (nextCycle.getMarketDataSpecification() == null) {
        nextCycle.setMarketDataSpecification(defaults.getMarketDataSpecification());
      }
      if (nextCycle.getValuationTime() == null) {
        nextCycle.setValuationTime(defaults.getValuationTime());
      }
    }
    return nextCycle;
  }

}
