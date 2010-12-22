/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.bond.BondSecurityToBondDefinitionConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.BondCalculator;
import com.opengamma.financial.interestrate.bond.BondCalculatorFactory;
import com.opengamma.financial.interestrate.bond.BondZSpreadCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 */
public class BondZSpreadFunction extends AbstractFunction.NonCompiledInvoker {
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondCalculatorFactory.getBondCalculator(BondCalculatorFactory.BOND_DIRTY_PRICE);
  private final String _curveName;
  private final String _requirementName;

  public BondZSpreadFunction(final String curveName, final String valueRequirementName) {
    Validate.notNull(curveName, "curve name");
    Validate.notNull(valueRequirementName, "value requirement name");
    _curveName = curveName;
    _requirementName = valueRequirementName;
  }

  @Override
  public String getShortName() {
    return "BondZSpreadFunction";
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final BondSecurity security = (BondSecurity) position.getSecurity();
    final ValueRequirement curveRequirement = new ValueRequirement(_requirementName, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier());
    final Object curveObject = inputs.getValue(curveRequirement);
    if (curveObject == null) {
      throw new NullPointerException("Could not get " + curveRequirement);
    }

    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final Bond bond = new BondSecurityToBondDefinitionConverter(holidaySource, conventionSource).getBond(security, true).toDerivative(now.toLocalDate(), _curveName);

    final YieldCurveBundle bundle;
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    bundle = new YieldCurveBundle(new String[] {_curveName}, new YieldAndDiscountCurve[] {curve});

    final ValueRequirement priceRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueIdentifier());
    final Object priceObject = inputs.getValue(priceRequirement);
    if (priceObject == null) {
      throw new NullPointerException("Could not get " + priceRequirement);
    }
    final double cleanPrice = (Double) priceObject;
    final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bond, cleanPrice / 100.0);

    final Double zSpread = new BondZSpreadCalculator().calculate(bond, bundle, dirtyPrice);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.Z_SPREAD, position), getUniqueIdentifier());
    return Sets.newHashSet(new ComputedValue(specification, zSpread * 10000)); //report z-spread in BPS
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.POSITION) {
      final Security security = target.getPosition().getSecurity();
      return security instanceof BondSecurity;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueRequirement(_requirementName, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier()), new ValueRequirement(
          MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, target.getPosition().getSecurity().getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.Z_SPREAD, target.getPosition()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  private Currency getCurrency(final ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getPosition().getSecurity();
    return bond.getCurrency();
  }
}
