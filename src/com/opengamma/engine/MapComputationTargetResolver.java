/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link ComputationTargetResolver} backed solely by a {@code Map}.
 *
 */
public class MapComputationTargetResolver implements ComputationTargetResolver {
  private final Map<ComputationTargetSpecification, ComputationTarget> _backingMap =
    new HashMap<ComputationTargetSpecification, ComputationTarget>();

  @Override
  public ComputationTarget resolve(ComputationTargetSpecification specification) {
    return _backingMap.get(specification); 
  }
  
  public void addTarget(ComputationTarget target) {
    _backingMap.put(target.toSpecification(), target);
  }

}
