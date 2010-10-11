/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.swap.FixedFloatSwapSecurityToSwapConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.interestrate.ParRateParallelSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.world.holiday.HolidaySource;
import com.opengamma.financial.world.region.RegionSource;

/**
 * 
 */
public class ParRateParallelCurveShiftFixedFloatSwapFunction extends AbstractFunction.NonCompiledInvoker {

  private static final ParRateParallelSensitivityCalculator CALCULATOR = ParRateParallelSensitivityCalculator.getInstance();
  private final String _name;
  private final Currency _currency;

  public ParRateParallelCurveShiftFixedFloatSwapFunction(final String currency, final String name) {
    this(Currency.getInstance(currency), name);
  }

  public ParRateParallelCurveShiftFixedFloatSwapFunction(final Currency currency, final String name) {
    Validate.notNull(currency, "currency");
    Validate.notNull(name, "name");
    _currency = currency;
    _name = name;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final SwapSecurity security = (SwapSecurity) position.getSecurity();
    final ValueRequirement requirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier());
    final Object yieldCurveObject = inputs.getValue(requirement);
    if (yieldCurveObject == null) {
      throw new NullPointerException("Could not get " + requirement);
    }
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final InterestRateLeg payLeg = (InterestRateLeg) security.getPayLeg();
    final InterestRateLeg receiveLeg = (InterestRateLeg) security.getReceiveLeg();
    double fixedRate;
    double initialFloatingRate;
    if (payLeg instanceof FixedInterestRateLeg) {
      fixedRate = ((FixedInterestRateLeg) payLeg).getRate();
      initialFloatingRate = ((FloatingInterestRateLeg) receiveLeg).getInitialFloatingRate();
    } else {
      fixedRate = ((FixedInterestRateLeg) receiveLeg).getRate();
      initialFloatingRate = ((FloatingInterestRateLeg) payLeg).getInitialFloatingRate();
    }
    final Swap<FixedCouponPayment, Payment> swap = new FixedFloatSwapSecurityToSwapConverter(holidaySource, regionSource, conventionSource).getSwap(security, _name, _name, fixedRate,
        initialFloatingRate, now);
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) yieldCurveObject;
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {_name}, new YieldAndDiscountCurve[] {curve});
    final Map<String, Double> parRateSensitivity = CALCULATOR.getValue(swap, bundle);
    //TODO change when we can handle more than one name
    if (parRateSensitivity.size() == 1 && parRateSensitivity.keySet().contains(_name)) {
      final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, position), getUniqueIdentifier());
      return Sets.newHashSet(new ComputedValue(specification, parRateSensitivity.get(_name) / 10000.));
    }
    throw new NullPointerException("Could not get par rate sensitivity for curve " + _name);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.POSITION) {
      final Security security = target.getPosition().getSecurity();
      if (security instanceof SwapSecurity) {
        final SwapSecurity swap = (SwapSecurity) security;
        if (swap.getPayLeg() instanceof InterestRateLeg && swap.getReceiveLeg() instanceof InterestRateLeg) {
          final InterestRateLeg payLeg = (InterestRateLeg) swap.getPayLeg();
          final InterestRateLeg receiveLeg = (InterestRateLeg) swap.getReceiveLeg();
          if ((payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) || (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg)) {
            final Currency payLegCurrency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
            return payLegCurrency.equals(((InterestRateNotional) receiveLeg.getNotional()).getCurrency()) && payLegCurrency.equals(_currency);
          }
        }
      }
    }
    return false;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, target.getPosition()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "ParRateParallelCurveSensitivityFixedFloatSwapFunction";
  }

  private Currency getCurrency(final ComputationTarget target) {
    final SwapSecurity swap = (SwapSecurity) target.getPosition().getSecurity();
    final InterestRateLeg leg = (InterestRateLeg) swap.getPayLeg();
    return ((InterestRateNotional) leg.getNotional()).getCurrency();
  }
}
