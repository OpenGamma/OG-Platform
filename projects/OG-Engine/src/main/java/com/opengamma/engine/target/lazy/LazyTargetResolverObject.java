/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Stub object that is backed by a lazily resolved {@link ComputationTarget} from the associated resolver.
 */
/* package */abstract class LazyTargetResolverObject implements UniqueIdentifiable {

  private final ComputationTargetResolver.AtVersionCorrection _resolver;
  private final ComputationTargetSpecification _spec;
  private volatile ComputationTarget _resolved;

  public LazyTargetResolverObject(final ComputationTargetResolver.AtVersionCorrection resolver, final ComputationTargetSpecification spec) {
    _resolver = resolver;
    _spec = spec;
  }

  protected ComputationTargetResolver.AtVersionCorrection getTargetResolver() {
    return _resolver;
  }

  protected ComputationTargetSpecification getTargetSpecification() {
    return _spec;
  }

  protected ComputationTarget getResolvedTarget() {
    if (_resolved == null) {
      synchronized (this) {
        if (_resolved == null) {
          _resolved = getTargetResolver().resolve(getTargetSpecification());
          if (_resolved == null) {
            throw new OpenGammaRuntimeException("Unable to resolve target " + getTargetSpecification());
          }
        }
      }
    }
    return _resolved;
  }

  @Override
  public UniqueId getUniqueId() {
    return getTargetSpecification().getUniqueId();
  }

}
