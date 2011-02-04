/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.interestrate.bond.BondCalculator;
import com.opengamma.financial.interestrate.bond.BondCalculatorFactory;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 */
public class BondPreviousCloseDirtyPriceFunction extends BondFunction {
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondCalculatorFactory.getBondCalculator(BondCalculatorFactory.BOND_DIRTY_PRICE);
  private static final BusinessDayConvention PREVIOUS = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding");

  public BondPreviousCloseDirtyPriceFunction() {
    super(MarketDataRequirementNames.MARKET_VALUE);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final FunctionExecutionContext context, final Currency currency, final Position position, final BondDefinition bondDefinition, final Object value,
      final LocalDate now, final String yieldCurveName) {
    final double cleanPrice = (Double) value;
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.DIRTY_PRICE, position), getUniqueId());
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final Calendar calendar = new HolidaySourceCalendarAdapter(holidaySource, currency);
    final LocalDate previousClose = PREVIOUS.adjustDate(calendar, now.minusDays(1));
    final Bond bond = bondDefinition.toDerivative(previousClose, yieldCurveName);
    final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bond, cleanPrice / 100.0);
    return Sets.newHashSet(new ComputedValue(specification, dirtyPrice * 100.0));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.DIRTY_PRICE, target.getPosition()), getUniqueId()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "BondDirtyPriceFunction";
  }

}
