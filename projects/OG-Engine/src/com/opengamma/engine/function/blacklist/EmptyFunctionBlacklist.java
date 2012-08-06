/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of an empty {@link FunctionBlacklist}.
 */
public class EmptyFunctionBlacklist extends AbstractFunctionBlacklist {

  public EmptyFunctionBlacklist() {
    super("EMPTY", new AbstractExecutorService() {

      @Override
      public void shutdown() {
      }

      @Override
      public List<Runnable> shutdownNow() {
        return null;
      }

      @Override
      public boolean isShutdown() {
        return true;
      }

      @Override
      public boolean isTerminated() {
        return true;
      }

      @Override
      public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return true;
      }

      @Override
      public void execute(final Runnable command) {
      }

    });
  }

  @Override
  public Set<FunctionBlacklistRule> getRules() {
    return Collections.emptySet();
  }

}
