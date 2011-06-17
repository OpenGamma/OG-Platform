/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.fixedincome.SwapSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.swaption.SwaptionSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter.Builder;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurityVisitor;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SwaptionPresentValueFunction extends AbstractFunction.NonCompiledInvoker {
  private SwapSecurityConverter _swapConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    _swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext); //TODO need a security source from the compilation context
    ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    SwaptionSecurityVisitor<FixedIncomeInstrumentConverter<?>> swaptionVisitor = new SwaptionSecurityConverter(securitySource, conventionSource, _swapConverter);
    FinancialSecurityVisitor<FixedIncomeInstrumentConverter<?>> visitor = FinancialSecurityVisitorAdapter.<FixedIncomeInstrumentConverter<?>> builder().swaptionVisitor(swaptionVisitor).create();
    final SwaptionSecurity swaptionSecurity = (SwaptionSecurity) target.getSecurity();
    FixedIncomeInstrumentConverter<?> swaptionDefinition = swaptionSecurity.accept(visitor);
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    InterestRateDerivative swaption = swaptionDefinition.toDerivative(now, curveNames.getFirst(), curveNames.getSecond());
    final ValueSpecification specification = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties()
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond()).get());
    return Sets.newHashSet(new ComputedValue(specification, 0.004));
    //    final HistoricalDataSource dataSource = OpenGammaExecutionContext
    //        .getHistoricalDataSource(executionContext);
    //    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    //    final String forwardCurveName = curveNames.getFirst();
    //    final String fundingCurveName = curveNames.getSecond();
    //    final String cubeName = null;
    //    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null);
    //    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    //    if (forwardCurveObject == null) {
    //      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    //    }
    //    Object fundingCurveObject = null;
    //    if (!forwardCurveName.equals(fundingCurveName)) {
    //      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, fundingCurveName, null, null);
    //      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
    //      if (fundingCurveObject == null) {
    //        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
    //      }
    //    }
    //    final ValueRequirement volatilityCubeRequirement = getVolatilityCubeRequirement(target, cubeName);
    //    final Object volatilityCubeObject = inputs.getValue(forwardCurveRequirement);
    //    if (volatilityCubeObject == null) {
    //      throw new OpenGammaRuntimeException("Could not get " + volatilityCubeRequirement);
    //    }
    //    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    //    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve
    //        : (YieldAndDiscountCurve) fundingCurveObject;
    //    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {forwardCurveName, fundingCurveName},
    //        new YieldAndDiscountCurve[] {forwardCurve, fundingCurve});
    //    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String forwardCurveName = YieldCurveFunction.getForwardCurveName(context, desiredValue);
    final String fundingCurveName = YieldCurveFunction.getFundingCurveName(context, desiredValue);
    //final SwaptionSecurity security = (SwaptionSecurity) target.getSecurity();
    //final ValueRequirement volatilityCube = null; // = getVolatilityCubeRequirement(target, cubeName);
    if (forwardCurveName.equals(fundingCurveName)) {
      return Sets.newHashSet(getCurveRequirement(target, forwardCurveName, null, null));
      //      return Sets.newHashSet(getCurveRequirement(target, forwardCurveName, null, null), volatilityCube);
    }
    return Sets.newHashSet(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName), getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName));
    //    return Sets.newHashSet(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName),
    //        getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName), volatilityCube);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    //TODO add cube
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties()
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond()).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    //TODO add cube
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties().withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(ValuePropertyNames.CUBE).get()));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof SwaptionSecurity;
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName, advisoryForward, advisoryFunding);
  }
  //
  //  protected ValueRequirement getVolatilityCubeRequirement(final ComputationTarget target, final String cubeName) {
  //    return null;
  //  }
}
