/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.depgraph.DependencyGraphBuilderPLAT1049.GraphBuildingContext;

/**
 * Interface to request the next resolution be "pumped" into the chain of those requesting
 * notification. This allows lazy production of alternative values deep into the resolution
 * chain. 
 */
/* package */interface ResolutionPump {

  /**
   * Pass the next result to the associated callback object, or call the {@link ResolvedValueCallback#failed} method
   * if no more results are available.
   * 
   * @param context the graph building context
   */
  void pump(final GraphBuildingContext context);

  /**
   * Discard the pump; it must not call any further methods on the {@link ResolvedValueCallback}.
   * 
   * @param context the graph building context
   */
  void close(final GraphBuildingContext context);

  /**
   * {@link ContextRunnable} form of the {@link #pump} method.
   */
  class Pump implements ContextRunnable {

    private final ResolutionPump _instance;

    public Pump(final ResolutionPump instance) {
      _instance = instance;
    }

    @Override
    public void run(final GraphBuildingContext context) {
      _instance.pump(context);
    }

  }

  /**
   * {@link ContextRunnable} form of the {@link #close} method.
   */
  class Close implements ContextRunnable {

    private final ResolutionPump _instance;

    public Close(final ResolutionPump instance) {
      _instance = instance;
    }

    @Override
    public void run(final GraphBuildingContext context) {
      _instance.close(context);
    }

  }

}
