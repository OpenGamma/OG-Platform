/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

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

/**
 * 
 */
public abstract class FixedFloatSwapFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _forwardCurveName;
  private final String _fundingCurveName;
  private final String _forwardValueRequirementName;
  private final String _fundingValueRequirementName;
  private final Currency _currency;

  public FixedFloatSwapFunction(final String currency, final String curveName, final String valueRequirementName) {
    this(Currency.getInstance(currency), curveName, valueRequirementName, curveName, valueRequirementName);
  }

  public FixedFloatSwapFunction(final String currency, final String forwardCurveName, final String forwardValueRequirementName, final String fundingCurveName, final String fundingValueRequirementName) {
    this(Currency.getInstance(currency), forwardCurveName, forwardValueRequirementName, fundingCurveName, fundingValueRequirementName);
  }

  public FixedFloatSwapFunction(final Currency currency, final String name, final String valueRequirementName) {
    this(currency, name, valueRequirementName, name, valueRequirementName);
  }

  public FixedFloatSwapFunction(final Currency currency, final String forwardCurveName, final String forwardValueRequirementName, final String fundingCurveName,
      final String fundingValueRequirementName) {
    Validate.notNull(currency, "currency");
    Validate.notNull(forwardCurveName, "forward curve name");
    Validate.notNull(forwardValueRequirementName, "forward value requirement name");
    Validate.notNull(fundingCurveName, "funding curve name");
    Validate.notNull(fundingValueRequirementName, "funding value requirement name");
    _currency = currency;
    _forwardCurveName = forwardCurveName;
    _forwardValueRequirementName = forwardValueRequirementName;
    _fundingCurveName = fundingCurveName;
    _fundingValueRequirementName = fundingValueRequirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final SwapSecurity security = (SwapSecurity) target.getSecurity();
    final ValueRequirement forwardCurveRequirement = new ValueRequirement(_forwardValueRequirementName, ComputationTargetType.PRIMITIVE, getCurrencyForTarget(target).getUniqueId());
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new NullPointerException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!_forwardCurveName.equals(_fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = new ValueRequirement(_fundingValueRequirementName, ComputationTargetType.PRIMITIVE, getCurrencyForTarget(target).getUniqueId());
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
      final Swap<?, ?> swap = new FixedFloatSwapSecurityToSwapConverter(holidaySource, regionSource, conventionSource).getSwap(security, _forwardCurveName, _forwardCurveName, fixedRate,
          initialFloatingRate, now);
      final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) forwardCurveObject;
      final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {_forwardCurveName}, new YieldAndDiscountCurve[] {curve});
      return getComputedValues(security, swap, bundle);
    }
    final Swap<?, ?> swap = new FixedFloatSwapSecurityToSwapConverter(holidaySource, regionSource, conventionSource).getSwap(security, _fundingCurveName, _forwardCurveName, fixedRate,
        initialFloatingRate, now);
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingCurveObject;
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {_forwardCurveName, _fundingCurveName}, new YieldAndDiscountCurve[] {forwardCurve, fundingCurve});
    return getComputedValues(security, swap, bundle);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY) {
      final Security security = target.getSecurity();
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
    return ComputationTargetType.SECURITY;
  }

  protected abstract Set<ComputedValue> getComputedValues(Security security, Swap<?, ?> swap, YieldCurveBundle bundle);

  protected String getForwardCurveName() {
    return _forwardCurveName;
  }

  protected String getForwardValueRequirementName() {
    return _forwardValueRequirementName;
  }

  protected String getFundingCurveName() {
    return _fundingCurveName;
  }

  protected String getFundingValueRequirementName() {
    return _fundingValueRequirementName;
  }

  protected Currency getCurrencyForTarget(final Security targetSecurity) {
    final SwapSecurity swap = (SwapSecurity) targetSecurity;
    final InterestRateLeg leg = (InterestRateLeg) swap.getPayLeg();
    return ((InterestRateNotional) leg.getNotional()).getCurrency();
  }

  protected Currency getCurrencyForTarget(final ComputationTarget target) {
    return getCurrencyForTarget(target.getSecurity());
  }

  protected Currency getCurrency() {
    return _currency;
  }
}
