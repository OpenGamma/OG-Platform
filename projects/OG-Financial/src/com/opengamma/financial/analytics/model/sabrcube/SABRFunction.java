/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.CapFloorCMSSpreadSecurityConverter;
import com.opengamma.financial.analytics.conversion.CapFloorSecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwaptionSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class SABRFunction extends AbstractFunction.NonCompiledInvoker {
  /** String labelling the type of SABR calculation (with right extrapolation) */
  public static final String SABR_RIGHT_EXTRAPOLATION = "SABRRightExtrapolation";
  /** String labelling the type of SABR extrapolation (none) */
  public static final String SABR_NO_EXTRAPOLATION = "SABRNoExtrapolation";

  private FinancialSecurityVisitor<InstrumentDefinition<?>> _securityVisitor;
  private SecuritySource _securitySource;
  private FixedIncomeConverterDataProvider _definitionConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    _securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final SwaptionSecurityConverter swaptionConverter = new SwaptionSecurityConverter(_securitySource, swapConverter);
    final CapFloorSecurityConverter capFloorVisitor = new CapFloorSecurityConverter(holidaySource, conventionSource, regionSource);
    final CapFloorCMSSpreadSecurityConverter capFloorCMSSpreadSecurityVisitor = new CapFloorCMSSpreadSecurityConverter(holidaySource, conventionSource, regionSource);
    _securityVisitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().swapSecurityVisitor(swapConverter).swaptionVisitor(swaptionConverter).capFloorVisitor(capFloorVisitor)
        .capFloorCMSSpreadVisitor(capFloorCMSSpreadSecurityVisitor).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> forwardCurveNames = constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> fundingCurveNames = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurveNames == null || fundingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> cubeNames = constraints.getValues(ValuePropertyNames.CUBE);
    if (cubeNames == null || cubeNames.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationMethods = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethods == null || curveCalculationMethods.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurveNames.iterator().next();
    final String fundingCurveName = fundingCurveNames.iterator().next();
    final String cubeName = cubeNames.iterator().next();
    final String curveCalculationMethod = curveCalculationMethods.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod, currency);
    final ValueRequirement fundingCurveRequirement = getCurveRequirement(fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod, currency);
    final ValueRequirement cubeRequirement = getCubeRequirement(cubeName, currency);
    return Sets.newHashSet(forwardCurveRequirement, fundingCurveRequirement, cubeRequirement);
  }

  protected ValueRequirement getCurveRequirement(final String curveName, final String advisoryForward, final String advisoryFunding, final String calculationMethod,
      final Currency currency) {
    return YieldCurveFunction.getCurveRequirement(currency, curveName, advisoryForward, advisoryFunding, calculationMethod);
  }

  protected ValueRequirement getCubeRequirement(final String cubeName, final Currency currency) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CUBE, cubeName)
        .with(ValuePropertyNames.CURRENCY, currency.getCode()).get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, currency, properties);
  }

  protected FinancialSecurityVisitor<InstrumentDefinition<?>> getVisitor() {
    return _securityVisitor;
  }

  protected FixedIncomeConverterDataProvider getConverter() {
    return _definitionConverter;
  }

  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

  protected YieldCurveBundle getYieldCurves(final FunctionInputs inputs, final Currency currency, final ValueRequirement desiredValue) {
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final Object forwardCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(currency, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final Object fundingCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(currency, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    if (fundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get funding curve");
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingCurveObject;
    return new YieldCurveBundle(new String[] {fundingCurveName, forwardCurveName}, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve});
  }

  protected abstract SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency,
      final ValueRequirement desiredValue);

}
