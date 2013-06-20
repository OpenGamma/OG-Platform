/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Implementation of a {@link FunctionBlacklistQuery} interface that blacklists no functions.
 */
public final class DummyFunctionBlacklistQuery extends AbstractFunctionBlacklistQuery {

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters) {
    return false;
  }

  @Override
  public boolean isBlacklisted(final ComputationTargetSpecification target) {
    return false;
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
    return false;
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target, final ValueSpecification[] inputs,
      final ValueSpecification[] outputs) {
    return false;
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target, final Collection<ValueSpecification> inputs,
      final Collection<ValueSpecification> outputs) {
    return false;
  }

}
