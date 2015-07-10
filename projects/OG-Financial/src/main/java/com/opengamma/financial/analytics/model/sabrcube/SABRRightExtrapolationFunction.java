/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import static com.opengamma.engine.value.ValueRequirementNames.SABR_SURFACES;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.sabr.RightExtrapolationSABRDiscountingFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * Base class for functions that calculate risk for swaptions, CMS and cap/floors
 * using SABR with right extrapolation.
 * @deprecated Use descendants of {@link RightExtrapolationSABRDiscountingFunction}
 */
@Deprecated
public abstract class SABRRightExtrapolationFunction extends SABRFunction {
  /** Property name for the cutoff strike after which extrapolation is used */
  public static final String PROPERTY_CUTOFF_STRIKE = "SABRExtrapolationCutoffStrike";
  /** Property name for the tail thickness parameter */
  public static final String PROPERTY_TAIL_THICKNESS_PARAMETER = "SABRTailThicknessParameter";

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.SWAPTION_SECURITY.or(FinancialSecurityTypes.SWAP_SECURITY).or(FinancialSecurityTypes.CAP_FLOOR_SECURITY);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    if (security instanceof SwapSecurity) {
      if (!InterestRateInstrumentType.isFixedIncomeInstrumentType((SwapSecurity) security)) {
        return false;
      }
      final InterestRateInstrumentType type = SwapSecurityUtils.getSwapType((SwapSecurity) security);
      if ((type != InterestRateInstrumentType.SWAP_FIXED_CMS) && (type != InterestRateInstrumentType.SWAP_CMS_CMS) && (type != InterestRateInstrumentType.SWAP_IBOR_CMS)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cutoffNames = constraints.getValues(PROPERTY_CUTOFF_STRIKE);
    if (cutoffNames == null || cutoffNames.size() != 1) {
      return null;
    }
    final Set<String> muNames = constraints.getValues(PROPERTY_TAIL_THICKNESS_PARAMETER);
    if (muNames == null || muNames.size() != 1) {
      return null;
    }
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    return requirements;
  }

  @Override
  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency,
      final YieldCurveBundle yieldCurves, final ValueRequirement desiredValue) {
    final Object surfacesObject = inputs.getValue(SABR_SURFACES);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get SABR parameter surfaces");
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final SABRInterestRateParameters modelParameters = new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface);
    return new SABRInterestRateDataBundle(modelParameters, yieldCurves);
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency) {
    return properties.copy()
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_RIGHT_EXTRAPOLATION)
        .withAny(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .withAny(PROPERTY_CUTOFF_STRIKE)
        .withAny(PROPERTY_TAIL_THICKNESS_PARAMETER).get();
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency, final ValueRequirement desiredValue) {
    final String cubeDefinitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
    final String cubeSpecificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
    final String surfaceDefinitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION);
    final String surfaceSpecificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String fittingMethod = desiredValue.getConstraint(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    final String cutoff = desiredValue.getConstraint(PROPERTY_CUTOFF_STRIKE);
    final String mu = desiredValue.getConstraint(PROPERTY_TAIL_THICKNESS_PARAMETER);
    return properties.copy()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION, cubeDefinitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION, cubeSpecificationName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION, surfaceDefinitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION, surfaceSpecificationName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_RIGHT_EXTRAPOLATION)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .with(PROPERTY_CUTOFF_STRIKE, cutoff)
        .with(PROPERTY_TAIL_THICKNESS_PARAMETER, mu).get();
  }

}
