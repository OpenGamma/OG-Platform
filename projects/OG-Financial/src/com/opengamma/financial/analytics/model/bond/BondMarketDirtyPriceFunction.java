/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * 
 */
public class BondMarketDirtyPriceFunction extends BondMarketDataFunction {

  public BondMarketDirtyPriceFunction() {
    super(MarketDataRequirementNames.DIRTY_PRICE_MID);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final FunctionExecutionContext context, final double value, final BondSecurity security) {
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.MARKET_DIRTY_PRICE, security), getUniqueId());
    return Sets.newHashSet(new ComputedValue(specification, value));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.MARKET_DIRTY_PRICE, target.getSecurity()), getUniqueId()));
  }

  @Override
  public String getShortName() {
    return "BondMarketDirtyPriceFunction";
  }

}
