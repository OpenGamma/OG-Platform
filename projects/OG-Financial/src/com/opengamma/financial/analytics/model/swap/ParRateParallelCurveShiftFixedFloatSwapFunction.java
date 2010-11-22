/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.core.common.Currency;
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
import com.opengamma.financial.analytics.swap.FixedFloatSwapSecurityToSwapConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.interestrate.ParRateParallelSensitivityCalculator;
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
public class ParRateParallelCurveShiftFixedFloatSwapFunction extends AbstractFunction.NonCompiledInvoker {

  private static final ParRateParallelSensitivityCalculator CALCULATOR = ParRateParallelSensitivityCalculator.getInstance();
  private final Currency _currency;
  private final String _forwardCurveName;
  private final String _forwardValueRequirementName;
  private final String _fundingCurveName;
  private final String _fundingValueRequirementName;

  public ParRateParallelCurveShiftFixedFloatSwapFunction(final String currency, final String curveName, final String valueRequirementName) {
    this(Currency.getInstance(currency), curveName, valueRequirementName, curveName, valueRequirementName);
  }

  public ParRateParallelCurveShiftFixedFloatSwapFunction(final String currency, final String forwardCurveName, final String forwardValueRequirementName, final String fundingCurveName,
      final String fundingValueRequirementName) {
    this(Currency.getInstance(currency), forwardCurveName, forwardValueRequirementName, fundingCurveName, fundingValueRequirementName);
  }

  public ParRateParallelCurveShiftFixedFloatSwapFunction(final Currency currency, final String name, final String valueRequirementName) {
    this(currency, name, valueRequirementName, name, valueRequirementName);
  }

  public ParRateParallelCurveShiftFixedFloatSwapFunction(final Currency currency, final String forwardCurveName, final String forwardValueRequirementName, final String fundingCurveName,
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
    final Position position = target.getPosition();
    final SwapSecurity security = (SwapSecurity) position.getSecurity();
    final ValueRequirement forwardCurveRequirement = new ValueRequirement(_forwardValueRequirementName, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier());
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new NullPointerException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!_forwardCurveName.equals(_fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = new ValueRequirement(_fundingValueRequirementName, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier());
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
      final Swap<?, ?> swap = new FixedFloatSwapSecurityToSwapConverter(holidaySource, regionSource, conventionSource).getSwap(security, _forwardCurveName,
          _forwardCurveName, fixedRate, initialFloatingRate, now);
      final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) forwardCurveObject;
      final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {_forwardCurveName}, new YieldAndDiscountCurve[] {curve});
      final Map<String, Double> parRateSensitivity = CALCULATOR.getValue(swap, bundle);
      final Set<ComputedValue> result = new HashSet<ComputedValue>();
      if (!(parRateSensitivity.containsKey(_forwardCurveName) && parRateSensitivity.containsKey(_fundingCurveName))) {
        throw new NullPointerException("Could not get par rate sensitivity for " + _forwardCurveName + " and " + _fundingCurveName);
      }
      for (final Map.Entry<String, Double> entry : parRateSensitivity.entrySet()) {
        final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT + "_" + entry.getKey() + "_"
            + _currency.getISOCode(), position), getUniqueIdentifier());
        result.add(new ComputedValue(specification, entry.getValue() / 10000));
      }
      return result;
    }
    final Swap<?, ?> swap = new FixedFloatSwapSecurityToSwapConverter(holidaySource, regionSource, conventionSource).getSwap(security, _fundingCurveName,
        _forwardCurveName, fixedRate, initialFloatingRate, now);
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingCurveObject;
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {_forwardCurveName, _fundingCurveName}, new YieldAndDiscountCurve[] {forwardCurve, fundingCurve});
    final Map<String, Double> parRateSensitivity = CALCULATOR.getValue(swap, bundle);
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    if (!(parRateSensitivity.containsKey(_forwardCurveName) && parRateSensitivity.containsKey(_fundingCurveName))) {
      throw new NullPointerException("Could not get par rate sensitivity for " + _forwardCurveName + " and " + _fundingCurveName);
    }
    for (final Map.Entry<String, Double> entry : parRateSensitivity.entrySet()) {
      final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT + "_" + entry.getKey() + "_"
          + _currency.getISOCode(), position), getUniqueIdentifier());
      result.add(new ComputedValue(specification, entry.getValue() / 10000));
    }
    return result;
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
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      if (_forwardCurveName.equals(_fundingCurveName)) {
        return Sets.newHashSet(new ValueRequirement(_forwardValueRequirementName, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier()));
      }
      return Sets.newHashSet(new ValueRequirement(_forwardValueRequirementName, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier()),
          new ValueRequirement(_fundingValueRequirementName, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      if (_forwardCurveName.equals(_fundingCurveName)) {
        return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT + "_" + _forwardCurveName + "_" + _currency.getISOCode(),
            target.getPosition()), getUniqueIdentifier()));
      }
      return Sets.newHashSet(
          new ValueSpecification(new ValueRequirement(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT + "_" + _forwardCurveName + "_" + _currency.getISOCode(), target.getPosition()),
              getUniqueIdentifier()),
          new ValueSpecification(new ValueRequirement(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT + "_" + _fundingCurveName + "_" + _currency.getISOCode(), target.getPosition()),
              getUniqueIdentifier()));
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
