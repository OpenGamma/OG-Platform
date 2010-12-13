/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.interestrate.bond.BondCalculator;
import com.opengamma.financial.interestrate.bond.BondCalculatorFactory;
import com.opengamma.financial.interestrate.bond.BondYieldCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 */
public class BondYieldFunction extends BondFunction {
  private static final BondYieldCalculator CALCULATOR = BondYieldCalculator.getInstance();
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondCalculatorFactory.getBondCalculator(BondCalculatorFactory.BOND_DIRTY_PRICE);

  public BondYieldFunction() {
    super(MarketDataRequirementNames.MARKET_VALUE, "PX_LAST");
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final Position position, final Bond bond, final Object value) {
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.YTM, position), getUniqueIdentifier());
    final BondSecurity security = (BondSecurity) position.getSecurity();
    final Frequency frequency = security.getCouponFrequency();
    double paymentsPerYear;
    if (frequency instanceof SimpleFrequency) {
      paymentsPerYear = ((SimpleFrequency) frequency).getPeriodsPerYear();
    } else if (frequency instanceof PeriodFrequency) {
      paymentsPerYear = ((PeriodFrequency) frequency).toSimpleFrequency().getPeriodsPerYear();
    } else {
      throw new IllegalArgumentException("Can only handle SimpleFrequency and PeriodFrequency");
    }
    final double cleanPrice = (Double) value;
    final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bond, cleanPrice / 100.0);
    double yield = CALCULATOR.calculate(bond, dirtyPrice);
    yield = paymentsPerYear * (Math.exp(yield / paymentsPerYear) - 1.0); //TODO this really shouldn't be done in here
    //TODO not correct for USD in last coupon period - need money market yield then
    return Sets.newHashSet(new ComputedValue(specification, yield * 100.));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.YTM, target.getPosition()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "BondYieldFunction";
  }

}
