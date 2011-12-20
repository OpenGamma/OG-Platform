/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.math.surface.InterpolatedDoublesSurface;

/**
 * Class describing the SABR parameter surfaces and the extrapolation parameters used in interest rate modelling.
 */
public class SABRInterestRateExtrapolationParameters extends SABRInterestRateParameters {

  /**
   * The cut-off strike above which the extrapolation is used.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;

  /**
   * Constructor from the parameter surfaces and extrapolation parameters. The SABR volatility formula is HaganVolatilityFunction.
   * @param alpha The alpha parameters.
   * @param beta The beta parameters.
   * @param rho The rho parameters.
   * @param nu The nu parameters.
   * @param dayCount The standard day count for which the parameter surfaces are valid.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public SABRInterestRateExtrapolationParameters(final InterpolatedDoublesSurface alpha, final InterpolatedDoublesSurface beta, final InterpolatedDoublesSurface rho,
      final InterpolatedDoublesSurface nu, final DayCount dayCount, final double cutOffStrike, final double mu) {
    super(alpha, beta, rho, nu, dayCount);
    this._cutOffStrike = cutOffStrike;
    this._mu = mu;
  }

  /**
   * Builder from SABR volatility surfaces.
   * @param sabr The SABR surfaces parameters.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   * @return The SABR with extrapolation parameters.
   */
  public static SABRInterestRateExtrapolationParameters from(final SABRInterestRateParameters sabr, final double cutOffStrike, final double mu) {
    return new SABRInterestRateExtrapolationParameters(sabr.getAlphaSurface(), sabr.getBetaSurface(), sabr.getRhoSurface(), sabr.getNuSurface(), sabr.getDayCount(), cutOffStrike, mu);
  }

  /**
   * Gets the cut-off strike.
   * @return The cut-off strike.
   */
  public double getCutOffStrike() {
    return _cutOffStrike;
  }

  /**
   * Gets The tail thickness parameter.
   * @return The tail parameter.
   */
  public double getMu() {
    return _mu;
  }

}
