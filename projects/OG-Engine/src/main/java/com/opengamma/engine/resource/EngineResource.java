/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.resource;

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
