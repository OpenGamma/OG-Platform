/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.util.PublicAPI;


/**
 * Represents a reference to a view computation cycle, providing the ability to query the computation cycle to obtain
 * more calculation results than those normally provided. The use of this reference wrapper also allows safe, explicit
 * resource management by client code.
 * <p>
 * A computation cycle is potentially a heavyweight, remote object, and the data to which access is provided may be
 * held on further remote calculation nodes while the computation cycle remains referenced. Interactions should be kept
 * to a minimum, batching queries where possible.
 * <p>
 * To avoid resource leaks, always call {@link #release()} when this reference is no longer required.
 * <p>
 * A single reference may be used concurrently provided that {@link #release()} is not called prematurely.
 */
@PublicAPI
public interface ViewCycleReference {

  ViewCycle getCycle();
  
  /**
   * Releases this reference to the computation cycle. A call to this method is mandatory to avoid resource leaks; the
   * computation cycle is discarded only when every reference to it has been released.
   * <p>
   * This method may be called multiple times; only the first call will have any effect.
   */
  void release();
  
}
