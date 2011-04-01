/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public interface ViewCycleManager {
  
  /**
   * Constructs a reference wrapper to a view cycle, incrementing the reference count for that cycle. The reference
   * wrapper facilitates decrementing the reference count through the method {@link ViewCycleReference#release()}.
   * 
   * @param cycleId  the unique identifier of the view cycle for which a reference is required, not null
   * @return a reference wrapper to the view cycle, or {@code null} if the cycle was not found
   */
  ViewCycleReference createReference(UniqueIdentifier cycleId);
  
}
