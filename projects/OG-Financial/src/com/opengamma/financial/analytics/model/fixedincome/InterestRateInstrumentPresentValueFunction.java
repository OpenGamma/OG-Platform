/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.fixedincome.CashSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FRASecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.fixedincome.SwapSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterestRateInstrumentPresentValueFunction extends AbstractFunction.NonCompiledInvoker {
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Security security = target.getSecurity();

    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext
        .getConventionBundleSource(executionContext);
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, conventionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource,
        regionSource);
    final FinancialSecurityVisitorAdapter<FixedIncomeInstrumentConverter<?>> visitor =
        FinancialSecurityVisitorAdapter.<FixedIncomeInstrumentConverter<?>> builder().cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter).swapSecurityVisitor(swapConverter).create();
    FixedIncomeInstrumentConverter<?> definition = ((FinancialSecurity) security).accept(visitor);
    Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    String forwardCurveName = curveNames.getFirst();
    String fundingCurveName = curveNames.getSecond();
    ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null);
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
    FixedIncomeInstrumentCurveExposureHelper helper = new FixedIncomeInstrumentCurveExposureHelper(fundingCurveName,
        forwardCurveName);
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve
        : (YieldAndDiscountCurve) fundingCurveObject;
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {forwardCurveName, fundingCurveName},
        new YieldAndDiscountCurve[] {forwardCurve, fundingCurve});
    InterestRateDerivative derivative = definition.toDerivative(now, helper.getExposuresForSecurity(security));
    final Double presentValue = CALCULATOR.visit(derivative, bundle);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(
        ValueRequirementNames.PRESENT_VALUE, security), createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(security).getCode())
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).get());
    return Collections.singleton(new ComputedValue(specification, presentValue));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType(target.getSecurity());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target,
      ValueRequirement desiredValue) {
    final String forwardCurveName = YieldCurveFunction.getForwardCurveName(context, desiredValue);
    final String fundingCurveName = YieldCurveFunction.getFundingCurveName(context, desiredValue);
    if (forwardCurveName.equals(fundingCurveName)) {
      return Collections.singleton(getCurveRequirement(target, forwardCurveName, null, null));
    }
    return Sets.newHashSet(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName),
        getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    return Collections
        .singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(),

        createValueProperties().with(ValuePropertyNames.CURRENCY,
                FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
                .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst())
                .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond())
                .get()));
  }

  @Override
  public String getShortName() {
    return "InterestRateInstrumentPresentValueFunction";
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName,
      final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName,
        advisoryForward, advisoryFunding);
  }

}
