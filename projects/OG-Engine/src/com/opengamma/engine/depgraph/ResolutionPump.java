/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

/**
 * Interface to request the next resolution be "pumped" into the chain of those requesting
 * notification. This allows lazy production of alternative values deep into the resolution
 * chain. 
 */
/* package */interface ResolutionPump {

  /**
   * Pass the next result to the associated callback object, or call the {@link ResolvedValueCallback#failed} method
   * if no more results are available.
   */
  void pump();

}
