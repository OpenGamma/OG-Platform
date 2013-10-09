/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.cache.MissingInput;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * A no-op function that relabels an input as any of the desired outputs. This will be selected during graph construction to handle any mismatches between value specifications the market data provider
 * is capable of recognizing and the value specifications corresponding to the requirements of a function. The will typically mean that a target has become re-labeled - for example the market data is
 * keyed off a {@link UniqueId} that is not easily converted to/from the {@link ExternalIdBundle} for the actual target.
 * <p>
 * This should be present in all function repositories with its preferred identifier.
 */
public final class MarketDataAliasingFunction extends IntrinsicFunction {

  /**
   * Shared instance.
   */
  public static final MarketDataAliasingFunction INSTANCE = new MarketDataAliasingFunction();

  /**
   * Preferred identifier this function will be available in a repository as.
   */
  public static final String UNIQUE_ID = "Alias";

  public MarketDataAliasingFunction() {
    super(UNIQUE_ID);
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Collection<ComputedValue> values = inputs.getAllValues();
    final Object value = values.isEmpty() ? MissingInput.MISSING_MARKET_DATA : Iterables.getOnlyElement(values).getValue();
    final Set<ComputedValue> result = Sets.newHashSetWithExpectedSize(desiredValues.size());
    for (ValueRequirement desiredValueReq : desiredValues) {
      final ValueSpecification desiredValue = new ValueSpecification(desiredValueReq.getValueName(), target.toSpecification(), desiredValueReq.getConstraints());
      result.add(new ComputedValue(desiredValue, value));
    }
    return result;
  }

}
