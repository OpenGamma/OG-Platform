/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MultipleNodeBatchExecutorFactory extends MultipleNodeExecutorFactory implements DependencyGraphExecutorFactory<Object>, InitializingBean {

  /**
   * Used to write stuff to the batch database.
   */
  private ResultWriterFactory _resultWriterFactory;

  @Override
  public MultipleNodeExecutor createExecutor(final SingleComputationCycle cycle) {
    ArgumentChecker.notNull(cycle, "cycle");
    return new MultipleNodeBatchExecutor(cycle, getMinimumJobItems(), getMaximumJobItems(), getMinimumJobCost(),
        getMaximumJobCost(), getMaximumConcurrency(), getFunctionCosts(), getExecutionPlanCache(), getResultWriterFactory());
  }

  public ResultWriterFactory getResultWriterFactory() {
    return _resultWriterFactory;
  }

  public void setResultWriterFactory(ResultWriterFactory resultWriterFactory) {
    this._resultWriterFactory = resultWriterFactory;
  }
  
}
