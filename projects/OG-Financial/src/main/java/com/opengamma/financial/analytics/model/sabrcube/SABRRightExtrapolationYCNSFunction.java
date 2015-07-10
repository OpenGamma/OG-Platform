/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import static com.opengamma.engine.value.ValueRequirementNames.SABR_SURFACES;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRExtrapolationCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
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
import com.opengamma.financial.analytics.model.sabr.SABRDiscountingFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * Base class for the calculation of yield curve node sensitivities of instruments
 * priced using the SABR model.
 * @deprecated Use descendants of {@link SABRDiscountingFunction}
 */
@Deprecated
public class SABRRightExtrapolationYCNSFunction extends SABRYCNSFunction {

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
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cutoffNames = constraints.getValues(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE);
    if (cutoffNames == null || cutoffNames.size() != 1) {
      return null;
    }
    final Set<String> muNames = constraints.getValues(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER);
    if (muNames == null || muNames.size() != 1) {
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
  protected ValueProperties.Builder createValueProperties(final Currency currency) {
    return createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, currency.getCode())
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION)
        .withAny(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .withAny(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE)
        .withAny(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER);
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String cubeDefinitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
    final String cubeSpecificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
    final String surfaceDefinitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION);
    final String surfaceSpecificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION);
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String fittingMethod = desiredValue.getConstraint(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String cutoff = desiredValue.getConstraint(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE);
    final String mu = desiredValue.getConstraint(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER);
    return createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION, cubeDefinitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION, cubeSpecificationName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION, surfaceDefinitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION, surfaceSpecificationName)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .with(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE, cutoff)
        .with(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER, mu);
  }

  @Override
  protected PresentValueNodeSensitivityCalculator getNodeSensitivityCalculator(final ValueRequirement desiredValue) {
    final Double cutoff = Double.parseDouble(desiredValue.getConstraint(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE));
    final Double mu = Double.parseDouble(desiredValue.getConstraint(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER));
    return PresentValueNodeSensitivityCalculator.using(new PresentValueCurveSensitivitySABRExtrapolationCalculator(cutoff, mu));
  }

}
