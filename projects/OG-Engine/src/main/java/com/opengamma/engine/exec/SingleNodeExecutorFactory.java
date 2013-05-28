/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import com.opengamma.engine.exec.plan.SingleNodeExecutionPlanner;

/**
 * 
 */
public class SingleNodeExecutorFactory extends PlanBasedGraphExecutorFactory {

  public SingleNodeExecutorFactory() {
    super(new SingleNodeExecutionPlanner());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
