/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Position;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.swap.FixedFloatSwapSecurityToSwapConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ParRateFixedFloatSwapFunction extends AbstractFunction.NonCompiledInvoker {

  private static final ParRateCalculator CALCULATOR = ParRateCalculator.getInstance();

  public ParRateFixedFloatSwapFunction() {
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final SwapSecurity security = (SwapSecurity) position.getSecurity();
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    final String forwardCurveName = curveNames.getFirst();
    final String fundingCurveName = curveNames.getSecond();
    final ValueRequirement forwardCurveRequirement = YieldCurveFunction.getCurveRequirement(getCurrency(target), forwardCurveName, null, null);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new NullPointerException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = YieldCurveFunction.getCurveRequirement(getCurrency(target), fundingCurveName, null, null);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new NullPointerException("Could not get " + fundingCurveRequirement);
      }
    }
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final Clock snapshotClock = executionContext.getValuationClock();
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
    Swap<CouponFixed, Payment> swap;
    final YieldCurveBundle bundle;
    if (forwardCurveName.equals(fundingCurveName)) {
      swap = new FixedFloatSwapSecurityToSwapConverter(holidaySource, regionSource, conventionSource).getSwap(security, forwardCurveName, forwardCurveName, fixedRate,
          initialFloatingRate, now);
      final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) forwardCurveObject;
      bundle = new YieldCurveBundle(new String[] {forwardCurveName }, new YieldAndDiscountCurve[] {curve });
    } else {
      swap = new FixedFloatSwapSecurityToSwapConverter(holidaySource, regionSource, conventionSource).getSwap(security, forwardCurveName, fundingCurveName, fixedRate,
          initialFloatingRate, now);
      final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
      final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingCurveObject;
      bundle = new YieldCurveBundle(new String[] {forwardCurveName, fundingCurveName }, new YieldAndDiscountCurve[] {forwardCurve, fundingCurve });
    }
    final Double parRate = CALCULATOR.visit(swap, bundle);
    final ValueSpecification specification = new ValueSpecification(ValueRequirementNames.PAR_RATE, target.toSpecification(), createValueProperties().with(YieldCurveFunction.PROPERTY_FORWARD_CURVE,
        forwardCurveName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get());
    return Collections.singleton(new ComputedValue(specification, parRate));
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
          if ((payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg)
              || (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg)) {
            final Currency payLegCurrency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
            return payLegCurrency.equals(((InterestRateNotional) receiveLeg.getNotional()).getCurrency());
          }
        }
      }
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String forwardCurveName = YieldCurveFunction.getForwardCurveName(context, desiredValue);
    final String fundingCurveName = YieldCurveFunction.getFundingCurveName(context, desiredValue);
    if (forwardCurveName.equals(fundingCurveName)) {
      return Collections.singleton(YieldCurveFunction.getCurveRequirement(getCurrency(target), forwardCurveName, null, null));
    }
    return Sets.newHashSet(YieldCurveFunction.getCurveRequirement(getCurrency(target), forwardCurveName, forwardCurveName, fundingCurveName), YieldCurveFunction.getCurveRequirement(
        getCurrency(target), fundingCurveName, forwardCurveName, fundingCurveName));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PAR_RATE, target.toSpecification(), createValueProperties().withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(
        YieldCurveFunction.PROPERTY_FUNDING_CURVE).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PAR_RATE, target.toSpecification(), createValueProperties().with(YieldCurveFunction.PROPERTY_FORWARD_CURVE,
        curveNames.getFirst()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond()).get()));
  }

  @Override
  public String getShortName() {
    return "ParRateFixedFloatSwapFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  private Currency getCurrency(final ComputationTarget target) {
    final SwapSecurity swap = (SwapSecurity) target.getPosition().getSecurity();
    final InterestRateLeg leg = (InterestRateLeg) swap.getPayLeg();
    return ((InterestRateNotional) leg.getNotional()).getCurrency();
  }
}
