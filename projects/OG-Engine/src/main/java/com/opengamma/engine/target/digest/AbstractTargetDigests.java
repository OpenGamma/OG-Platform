/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.digest;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;

/**
 * Partial implementation of {@link TargetDigests} based on the target type.
 */
public abstract class AbstractTargetDigests implements TargetDigests {

  private final ComputationTargetTypeMap<TargetDigests> _handlers = new ComputationTargetTypeMap<TargetDigests>();

  protected AbstractTargetDigests() {
  }

  protected void addHandler(final ComputationTargetType type, final TargetDigests digests) {
    _handlers.put(type, digests);
  }

  @Override
  public Object getDigest(final FunctionCompilationContext context, final ComputationTargetSpecification target) {
    final TargetDigests digests = _handlers.get(target.getType());
    if (digests == null) {
      return null;
    }
    return digests.getDigest(context, target);
  }
}
