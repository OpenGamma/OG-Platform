/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SABRVegaCapFloorCMSSpreadFunction extends SABRVegaFunction {

  public SABRVegaCapFloorCMSSpreadFunction(final String currency, final String definitionName, String forwardCurveName, String fundingCurveName) {
    this(Currency.of(currency), definitionName, forwardCurveName, fundingCurveName);
  }

  public SABRVegaCapFloorCMSSpreadFunction(final Currency currency, final String definitionName, String forwardCurveName, String fundingCurveName) {
    super(currency, definitionName, false, forwardCurveName, fundingCurveName);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof CapFloorCMSSpreadSecurity && !isUseSABRExtrapolation();
  }

  @Override
  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs) {
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
    final DoubleFunction1D correlationFunction = getCorrelationFunction();
    final SABRInterestRateCorrelationParameters modelParameters = new SABRInterestRateCorrelationParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, correlationFunction);
    return new SABRInterestRateDataBundle(modelParameters, getYieldCurves(target, inputs));
  }

  private DoubleFunction1D getCorrelationFunction() {
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(Double x) {
        return 0.5;
      }

    };
  }
}
