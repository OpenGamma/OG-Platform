/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.fixedincome.BondSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.BondCalculator;
import com.opengamma.financial.interestrate.bond.BondCalculatorFactory;
import com.opengamma.financial.interestrate.bond.BondZSpreadCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondZSpreadFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(BondZSpreadFunction.class);
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondCalculatorFactory.getBondCalculator(BondCalculatorFactory.BOND_DIRTY_PRICE);

  public BondZSpreadFunction() {
  }

  @Override
  public String getShortName() {
    return "BondZSpreadFunction";
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final BondSecurity security = (BondSecurity) target.getSecurity();
    final Object curveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (curveObject == null) {
      throw new NullPointerException("Could not get " + ValueRequirementNames.YIELD_CURVE);
    }

    String curveName = null;
    for (ValueRequirement desiredValue : desiredValues) {
      curveName = YieldCurveFunction.getCurveName(desiredValue);
      if (curveName != null) {
        break;
      }
    }
    if (curveName == null) {
      throw new NullPointerException("Curve name not specified as value constraint in " + desiredValues);
    }

    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext
        .getConventionBundleSource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final BondSecurityConverter visitor = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    Bond bond = ((BondDefinition) security.accept(visitor)).toDerivative(now.toLocalDate(), curveName);
    final YieldCurveBundle bundle;
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    bundle = new YieldCurveBundle(new String[] {curveName }, new YieldAndDiscountCurve[] {curve });

    final Object priceObject = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (priceObject == null) {
      throw new NullPointerException("Could not get " + MarketDataRequirementNames.MARKET_VALUE);
    }
    final double cleanPrice = (Double) priceObject;
    final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bond, cleanPrice / 100.0);

    final Double zSpread = new BondZSpreadCalculator().calculate(bond, bundle, dirtyPrice);
    final ValueSpecification specification = new ValueSpecification(ValueRequirementNames.Z_SPREAD, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURVE, curveName).get());
    return Sets.newHashSet(new ComputedValue(specification, zSpread * 10000)); //report z-spread in BPS
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY) {
      final Security security = target.getSecurity();
      return security instanceof BondSecurity;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String curveName = YieldCurveFunction.getCurveName(context, desiredValue);
    return Sets.newHashSet(
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueId(), ValueProperties.with(ValuePropertyNames.CURVE, curveName).get()),
        new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, target.getSecurity().getUniqueId()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.Z_SPREAD, target.toSpecification(), createValueProperties().withAny(ValuePropertyNames.CURVE).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveName = null;
    for (ValueSpecification input : inputs.keySet()) {
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getValueName())) {
        curveName = input.getProperty(ValuePropertyNames.CURVE);
        break;
      }
    }
    ArgumentChecker.notNull(curveName, "curveName");
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.Z_SPREAD, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURVE, curveName).get()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  private Currency getCurrency(final ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getSecurity();
    return bond.getCurrency();
  }
}
