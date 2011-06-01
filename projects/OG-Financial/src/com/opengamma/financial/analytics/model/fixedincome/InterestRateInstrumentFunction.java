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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.fixedincome.CashSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FRASecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.fixedincome.SwapSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class InterestRateInstrumentFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _valueRequirementName;
  private FinancialSecurityVisitorAdapter<FixedIncomeInstrumentConverter<?>> _visitor;

  public InterestRateInstrumentFunction(String valueRequirementName) {
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public void init(FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext
        .getConventionBundleSource(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, conventionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource,
        regionSource);
    _visitor =
        FinancialSecurityVisitorAdapter.<FixedIncomeInstrumentConverter<?>>builder()
            .cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter).swapSecurityVisitor(swapConverter)
            .create();
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target, Set<ValueRequirement> desiredValues) {
    FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    FixedIncomeInstrumentConverter<?> definition = security.accept(_visitor);
    //TODO see todo below
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
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve
        : (YieldAndDiscountCurve) fundingCurveObject;
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {forwardCurveName, fundingCurveName},
        new YieldAndDiscountCurve[] {forwardCurve, fundingCurve});
    InterestRateDerivative derivative = definition
        .toDerivative(now, FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security,
            fundingCurveName, forwardCurveName));
    return getComputedValues(derivative, bundle, security, curveNames);
  }

  public abstract Set<ComputedValue> getComputedValues(InterestRateDerivative derivative, YieldCurveBundle bundle,
      FinancialSecurity security, Pair<String, String> curveNames);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType((FinancialSecurity) target.getSecurity());
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
    Security security = target.getSecurity();
    String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    return Collections.singleton(
        new ValueSpecification(_valueRequirementName, target.toSpecification(), createValueProperties()
            .with(ValuePropertyNames.CURRENCY, currency)
            .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
            .get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    //final String forwardCurveName = YieldCurveFunction.getForwardCurveName(context);
    //final String fundingCurveName = YieldCurveFunction.getFundingCurveName(context);
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    return Collections
        .singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(),
            FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(
                (FinancialSecurity) target.getSecurity(),
                curveNames.getSecond(), curveNames.getFirst())));
    //TODO relying on the order from YieldCurveFunction is not clever
  }

  @Override
  public String getShortName() {
    return "InterestRateInstrumentFunction";
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName,
      final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName,
        advisoryForward, advisoryFunding);
  }

}
