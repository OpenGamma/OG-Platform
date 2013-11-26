/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 *
 */
public class BondMarketCleanPriceFunction extends BondMarketDataFunction {

  /**
   * Sets the value requirement name to {@link MarketDataRequirementNames#MARKET_VALUE}.
   */
  public BondMarketCleanPriceFunction() {
    super(MarketDataRequirementNames.MARKET_VALUE);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final FunctionExecutionContext context, final double value, final BondSecurity security, final ComputationTargetSpecification target) {
    final ValueProperties.Builder properties = createValueProperties();
    final ValueSpecification specification = new ValueSpecification(ValueRequirementNames.MARKET_CLEAN_PRICE, target, properties.get());
    return Sets.newHashSet(new ComputedValue(specification, value * 100));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.MARKET_CLEAN_PRICE, target.toSpecification(), properties.get()));
  }

}
