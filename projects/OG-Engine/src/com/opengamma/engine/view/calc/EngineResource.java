/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.id.UniqueIdentifiable;

/**
 * Represents an engine resource which participates in counted referencing and explicit releasing.
 */
public interface EngineResource extends UniqueIdentifiable {

  /**
   * Releases this resource. Called when there are no further references to the resource.
   */
  void release();
  
}
