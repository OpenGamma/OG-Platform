/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.util.function.BiFunction;

/**
 * Base class for testing the {@link ParallelRecompilationViewProcessWorker} inner classes.
 */
/* package */abstract class AbstractParallelRecompilationTest {

  protected abstract void testImpl(
      BiFunction<ParallelRecompilationViewProcessWorker,
      ViewExecutionOptions, Void> callback) throws InterruptedException;

  public void testParallel() throws InterruptedException {
    testImpl(new BiFunction<ParallelRecompilationViewProcessWorker, ViewExecutionOptions, Void>() {
      @Override
      public Void apply(ParallelRecompilationViewProcessWorker a, ViewExecutionOptions b) {
        a.startParallel(b);
        return null;
      }
    });
  }

  public void testImmediate() throws InterruptedException {
    testImpl(new BiFunction<ParallelRecompilationViewProcessWorker, ViewExecutionOptions, Void>() {
      @Override
      public Void apply(ParallelRecompilationViewProcessWorker a, ViewExecutionOptions b) {
        a.startImmediate(b);
        return null;
      }
    });
  }

  public void testDeferred() throws InterruptedException {
    testImpl(new BiFunction<ParallelRecompilationViewProcessWorker, ViewExecutionOptions, Void>() {
      @Override
      public Void apply(ParallelRecompilationViewProcessWorker a, ViewExecutionOptions b) {
        a.startDeferred(b);
        return null;
      }
    });
  }

}
