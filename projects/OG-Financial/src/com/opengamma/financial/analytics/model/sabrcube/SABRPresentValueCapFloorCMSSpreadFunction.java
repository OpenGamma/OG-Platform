/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SABRPresentValueCapFloorCMSSpreadFunction extends SABRPresentValueFunction {

  public SABRPresentValueCapFloorCMSSpreadFunction(final String currency, final String definitionName, final String forwardCurveName, final String fundingCurveName) {
    this(Currency.of(currency), definitionName, forwardCurveName, fundingCurveName);
  }

  public SABRPresentValueCapFloorCMSSpreadFunction(final Currency currency, final String definitionName, final String forwardCurveName, final String fundingCurveName) {
    super(currency, definitionName, false, forwardCurveName, fundingCurveName);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof CapFloorCMSSpreadSecurity && !isUseSABRExtrapolation();
  }

  //  @Override
  //  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs) {
  //    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
  //    final ValueRequirement surfacesRequirement = getCubeRequirement(target);
  //    final Object surfacesObject = inputs.getValue(surfacesRequirement);
  //    if (surfacesObject == null) {
  //      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
  //    }
  //    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
  //    if (!surfaces.getCurrency().equals(currency)) {
  //      throw new OpenGammaRuntimeException("Don't know how this happened");
  //    }
  //    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
  //    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
  //    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
  //    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
  //    final DayCount dayCount = surfaces.getDayCount();
  //    final DoubleFunction1D correlationFunction = getCorrelationFunction();
  //    final SABRInterestRateCorrelationParameters modelParameters = new SABRInterestRateCorrelationParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, correlationFunction);
  //    return new SABRInterestRateDataBundle(modelParameters, getYieldCurves(target, inputs));
  //  }
  //
  //  private DoubleFunction1D getCorrelationFunction() {
  //    return new DoubleFunction1D() {
  //
  //      @Override
  //      public Double evaluate(Double x) {
  //        return 0.8;
  //      }
  //
  //    };
  //  }
}
