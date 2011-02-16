/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.common.Currency;
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
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.swap.FixedFloatSwapSecurityToSwapConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class FixedFloatSwapFunction extends AbstractFunction.NonCompiledInvoker {

  public FixedFloatSwapFunction() {
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final SwapSecurity security = (SwapSecurity) target.getSecurity();
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    final String forwardCurveName = curveNames.getFirst();
    final String fundingCurveName = curveNames.getSecond();
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new NullPointerException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, fundingCurveName, null, null);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new NullPointerException("Could not get " + fundingCurveRequirement);
      }
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
    if (fundingCurveObject == null) {
      final Swap<?, ?> swap = new FixedFloatSwapSecurityToSwapConverter(holidaySource, regionSource, conventionSource).getSwap(security, forwardCurveName, forwardCurveName, fixedRate,
          initialFloatingRate, now);
      final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) forwardCurveObject;
      final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {forwardCurveName }, new YieldAndDiscountCurve[] {curve });
      return getComputedValues(inputs, security, swap, bundle, forwardCurveName, forwardCurveName);
    }
    final Swap<?, ?> swap = new FixedFloatSwapSecurityToSwapConverter(holidaySource, regionSource, conventionSource).getSwap(security, fundingCurveName, forwardCurveName, fixedRate,
        initialFloatingRate, now);
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingCurveObject;
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {forwardCurveName, fundingCurveName }, new YieldAndDiscountCurve[] {forwardCurve, fundingCurve });
    return getComputedValues(inputs, security, swap, bundle, forwardCurveName, fundingCurveName);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (!(security instanceof SwapSecurity)) {
      return false;
    }
    final SwapSecurity swap = (SwapSecurity) security;
    if (!(swap.getPayLeg() instanceof InterestRateLeg) || !(swap.getReceiveLeg() instanceof InterestRateLeg)) {
      return false;
    }
    final InterestRateLeg payLeg = (InterestRateLeg) swap.getPayLeg();
    final InterestRateLeg receiveLeg = (InterestRateLeg) swap.getReceiveLeg();
    if ((payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) || (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg)) {
      final Currency payLegCurrency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
      return payLegCurrency.equals(((InterestRateNotional) receiveLeg.getNotional()).getCurrency());
    } else {
      return false;
    }
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  protected abstract Set<ComputedValue> getComputedValues(FunctionInputs inputs, Security security, Swap<?, ?> swap, YieldCurveBundle bundle, String forwardCurveName, String fundingCurveName);

  protected Currency getCurrencyForTarget(final Security targetSecurity) {
    final SwapSecurity swap = (SwapSecurity) targetSecurity;
    final InterestRateLeg leg = (InterestRateLeg) swap.getPayLeg();
    return ((InterestRateNotional) leg.getNotional()).getCurrency();
  }

  protected Currency getCurrencyForTarget(final ComputationTarget target) {
    return getCurrencyForTarget(target.getSecurity());
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(getCurrencyForTarget(target), curveName, advisoryForward, advisoryFunding);
  }

  protected ValueRequirement getJacobianRequirement(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    return YieldCurveFunction.getJacobianRequirement(getCurrencyForTarget(target), forwardCurveName, fundingCurveName);
  }

}
