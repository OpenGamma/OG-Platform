/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;


/**
 * An adapter for {@code ComputationTargetResolver} that allows unhandled targets to be
 * passed to another implementation.
 */
public abstract class ComputationTargetResolverAdapter implements ComputationTargetResolver {
  
  private final ComputationTargetResolver _underlying;
  
  public ComputationTargetResolverAdapter (final ComputationTargetResolver underlying) {
    _underlying = underlying;
  }
  
  protected ComputationTargetResolver getUnderlying () {
    return _underlying;
  }
  
  @Override
  public ComputationTarget resolve(ComputationTargetSpecification specification) {
    return getUnderlying ().resolve (specification);
  }

}
