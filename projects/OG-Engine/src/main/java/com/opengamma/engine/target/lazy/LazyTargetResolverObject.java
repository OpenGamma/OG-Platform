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
  private volatile ComputationTarget _target;

  public LazyTargetResolverObject(final ComputationTargetResolver.AtVersionCorrection resolver, final ComputationTargetSpecification spec) {
    _resolver = resolver;
    _spec = spec;
  }

  protected ComputationTargetResolver.AtVersionCorrection getTargetResolver() {
    return _resolver;
  }

  protected ComputationTargetSpecification getTargetSpecification() {
    ComputationTarget target = _target;
    if (target != null) {
      return target.toSpecification();
    }
    return _spec;
  }

  protected ComputationTarget getResolvedTarget() {
    if (_target == null) {
      synchronized (this) {
        if (_target == null) {
          _target = getTargetResolver().resolve(getTargetSpecification());
          if (_target == null) {
            throw new OpenGammaRuntimeException("Unable to resolve target " + getTargetSpecification());
          }
        }
      }
    }
    return _target;
  }

  @Override
  public UniqueId getUniqueId() {
    ComputationTarget target = _target;
    if (target != null) {
      return target.getUniqueId();
    }
    UniqueId uid = _spec.getUniqueId();
    if (uid.isVersioned()) {
      return uid;
    }
    return getResolvedTarget().getUniqueId();
  }

}
