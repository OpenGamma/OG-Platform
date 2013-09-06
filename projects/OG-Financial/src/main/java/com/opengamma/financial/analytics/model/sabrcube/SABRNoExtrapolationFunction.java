/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.sabr.SABRDiscountingFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * Base class for functions that calculate risk for swaptions, CMS and cap/floors
 * using SABR with no right extrapolation.
 * @deprecated Use descendants of {@link SABRDiscountingFunction}
 */
@Deprecated
public abstract class SABRNoExtrapolationFunction extends SABRFunction {

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
  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency,
      final DayCount dayCount, final YieldCurveBundle yieldCurves, final ValueRequirement desiredValue) {
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    final ValueRequirement surfacesRequirement = getCubeRequirement(cubeName, currency, fittingMethod);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final SABRInterestRateParameters modelParameters = new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, VolatilityFunctionFactory.HAGAN_FORMULA);
    return new SABRInterestRateDataBundle(modelParameters, yieldCurves);
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency) {
    return properties.copy()
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(ValuePropertyNames.CUBE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_NO_EXTRAPOLATION).get();
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency, final ValueRequirement desiredValue) {
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    return properties.copy()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CUBE, cubeName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_NO_EXTRAPOLATION).get();
  }
}
