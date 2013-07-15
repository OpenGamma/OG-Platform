/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link SequencePartitioningViewProcessWorkerFactory} that uses statically set parameters for the partitioning.
 */
public class StaticSequencePartitioningViewProcessWorkerFactory extends SequencePartitioningViewProcessWorkerFactory implements InitializingBean {

  private volatile int _saturation = 1;
  private volatile int _minimumCycles = 1;
  private volatile int _maximumCycles = Integer.MAX_VALUE;

  public StaticSequencePartitioningViewProcessWorkerFactory(final ViewProcessWorkerFactory delegate) {
    super(delegate);
  }

  public void setSaturation(final int saturation) {
    ArgumentChecker.isTrue(saturation > 0, "saturation");
    _saturation = saturation;
  }

  public int getSaturation() {
    return _saturation;
  }

  public void setMinimumCycles(final int minimumCycles) {
    ArgumentChecker.isTrue(minimumCycles > 0, "minimumCycles");
    _minimumCycles = minimumCycles;
  }

  public int getMinimumCycles() {
    return _minimumCycles;
  }

  public void setMaximumCycles(final int maximumCycles) {
    ArgumentChecker.isTrue(maximumCycles > 0, "maximumCycles");
    _maximumCycles = maximumCycles;
  }

  public int getMaximumCycles() {
    return _maximumCycles;
  }

  // SequencePartitioningViewProcessWorkerFactory

  @Override
  protected int estimateSaturation(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
    return getSaturation();
  }

  @Override
  protected int estimateMinimumCycles(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
    return getMinimumCycles();
  }

  @Override
  protected int estimateMaximumCycles(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
    return getMaximumCycles();
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.isTrue(getMinimumCycles() <= getMaximumCycles(), "minimumCycles/maximumCycles");
  }

}
