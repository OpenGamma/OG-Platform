/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cmsspread;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
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
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeFunctionHelper;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class CapFloorCMSSpreadSABRFunction extends AbstractFunction.NonCompiledInvoker {
  @SuppressWarnings("unchecked")
  private static final VolatilityFunctionProvider<SABRFormulaData> SABR_FUNCTION = (VolatilityFunctionProvider<SABRFormulaData>) VolatilityFunctionFactory
      .getCalculator(VolatilityFunctionFactory.HAGAN);

  private final String _forwardCurveName;
  private final String _fundingCurveName;
  private final VolatilityCubeFunctionHelper _helper;
  private CapFloorCMSSpreadSecurityConverter _capFloorCMSSpreadVisitor;
  private FixedIncomeConverterDataProvider _converter;

  public CapFloorCMSSpreadSABRFunction(final String currency, final String definitionName, final String forwardCurveName, final String fundingCurveName) {
    this(Currency.of(currency), definitionName, forwardCurveName, fundingCurveName);
  }

  public CapFloorCMSSpreadSABRFunction(final Currency currency, final String definitionName, final String forwardCurveName, final String fundingCurveName) {
    Validate.notNull(currency, "currency");
    Validate.notNull(definitionName, "cube definition name");
    Validate.notNull(forwardCurveName, "forward curve name");
    Validate.notNull(fundingCurveName, "funding curve name");
    _helper = new VolatilityCubeFunctionHelper(currency, definitionName);
    _forwardCurveName = forwardCurveName;
    _fundingCurveName = fundingCurveName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    _capFloorCMSSpreadVisitor = new CapFloorCMSSpreadSecurityConverter(holidaySource, conventionSource);
    _converter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getCubeRequirement(target));
    if (_forwardCurveName.equals(_fundingCurveName)) {
      requirements.add(getCurveRequirement(target, _forwardCurveName, null, null));
      return requirements;
    }
    requirements.add(getCurveRequirement(target, _forwardCurveName, _forwardCurveName, _fundingCurveName));
    requirements.add(getCurveRequirement(target, _fundingCurveName, _forwardCurveName, _fundingCurveName));
    return requirements;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof CapFloorCMSSpreadSecurity;
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName, advisoryForward, advisoryFunding);
  }

  protected ValueRequirement getCubeRequirement(final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.CUBE, _helper.getDefinitionName()).get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, FinancialSecurityUtils.getCurrency(target.getSecurity()), properties);
  }

  protected CapFloorCMSSpreadSecurityConverter getVisitor() {
    return _capFloorCMSSpreadVisitor;
  }

  protected FixedIncomeConverterDataProvider getConverter() {
    return _converter;
  }

  protected VolatilityCubeFunctionHelper getHelper() {
    return _helper;
  }

  protected String getForwardCurveName() {
    return _forwardCurveName;
  }

  protected String getFundingCurveName() {
    return _fundingCurveName;
  }

  protected YieldCurveBundle getYieldCurves(final ComputationTarget target, final FunctionInputs inputs) {
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, _forwardCurveName, _forwardCurveName, _fundingCurveName);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!_forwardCurveName.equals(_fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, _fundingCurveName, _forwardCurveName, _fundingCurveName);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve : (YieldAndDiscountCurve) fundingCurveObject;
    return new YieldCurveBundle(new String[] {_fundingCurveName, _forwardCurveName}, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve});
  }

  protected SABRInterestRateParameters getModelParameters(final ComputationTarget target, final FunctionInputs inputs) {
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
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final DayCount dayCount = surfaces.getDayCount();
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, SABR_FUNCTION);
  }
}
