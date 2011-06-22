/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.fixedincome.SwapSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.swaption.SwaptionSecurityConverter;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeFunctionHelper;
import com.opengamma.financial.analytics.volatility.sabr.SABRFittedSurfaces;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SwaptionDummyPresentValueFunction extends AbstractFunction.NonCompiledInvoker {
  @SuppressWarnings("unchecked")
  private static final VolatilityFunctionProvider<SABRFormulaData> SABR_FUNCTION = (VolatilityFunctionProvider<SABRFormulaData>) VolatilityFunctionFactory
      .getCalculator(VolatilityFunctionFactory.HAGAN);
  private static final PresentValueSABRCalculator CALCULATOR = PresentValueSABRCalculator.getInstance();
  private SwaptionSecurityConverter _swaptionVisitor;
  private final VolatilityCubeFunctionHelper _helper;

  public SwaptionDummyPresentValueFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public SwaptionDummyPresentValueFunction(final Currency currency, final String definitionName) {
    _helper = new VolatilityCubeFunctionHelper(currency, definitionName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource);
    _swaptionVisitor = new SwaptionSecurityConverter(securitySource, conventionSource, swapConverter);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final SwaptionSecurity swaptionSecurity = (SwaptionSecurity) target.getSecurity();
    final FixedIncomeInstrumentConverter<?> swaptionDefinition = swaptionSecurity.accept(_swaptionVisitor);
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    final SABRInterestRateDataBundle data = new SABRInterestRateDataBundle(getModelParameters(target, inputs), getYieldCurves(curveNames.getFirst(), curveNames.getSecond(), target, inputs));
    final InterestRateDerivative swaption = swaptionDefinition.toDerivative(now, curveNames.getFirst(), curveNames.getSecond());
    final double presentValue = CALCULATOR.visit(swaption, data);
    final ValueSpecification specification = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties()
        .with(ValuePropertyNames.CURRENCY, swaptionSecurity.getCurrency().getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst())
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond())
        .with(ValuePropertyNames.CUBE, _helper.getKey().getName()).get());
    return Sets.newHashSet(new ComputedValue(specification, presentValue));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String forwardCurveName = YieldCurveFunction.getForwardCurveName(context, desiredValue);
    final String fundingCurveName = YieldCurveFunction.getFundingCurveName(context, desiredValue);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getCubeRequirement(target));
    if (forwardCurveName.equals(fundingCurveName)) {
      requirements.add(getCurveRequirement(target, forwardCurveName, null, null));
      return requirements;
    }
    requirements.add(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName));
    requirements.add(getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(),
        createValueProperties()
            .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
            .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
            .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
            .with(ValuePropertyNames.CUBE, _helper.getKey().getName()).get()));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof SwaptionSecurity;
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName,
      final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName,
        advisoryForward, advisoryFunding);
  }

  protected ValueRequirement getCubeRequirement(final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.CUBE, _helper.getKey().getName()).get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, FinancialSecurityUtils.getCurrency(target.getSecurity()), properties);
  }

  private YieldCurveBundle getYieldCurves(final String forwardCurveName, final String fundingCurveName, final ComputationTarget target, final FunctionInputs inputs) {
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, fundingCurveName, null, null);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve
        : (YieldAndDiscountCurve) fundingCurveObject;
    return new YieldCurveBundle(new String[] {forwardCurveName, fundingCurveName},
        new YieldAndDiscountCurve[] {forwardCurve, fundingCurve});
  }

  private SABRInterestRateParameters getModelParameters(final ComputationTarget target, final FunctionInputs inputs) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueRequirement surfacesRequirement = getCubeRequirement(target);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    if (!surfaces.getCurrency().equals(currency)) {
      throw new OpenGammaRuntimeException("Don't know how this happened");
    }
    final VolatilitySurface alphaSurface = surfaces.getAlphaSurface();
    final VolatilitySurface betaSurface = surfaces.getBetaSurface();
    final VolatilitySurface nuSurface = surfaces.getNuSurface();
    final VolatilitySurface rhoSurface = surfaces.getRhoSurface();
    final DayCount dayCount = surfaces.getDayCount();
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, SABR_FUNCTION);
  }
}
