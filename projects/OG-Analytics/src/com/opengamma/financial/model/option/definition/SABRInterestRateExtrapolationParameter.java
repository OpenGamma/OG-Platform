/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * Class describing the SABR parameter surfaces and the extrapolation parameters used in interest rate modeling.
 */
public class SABRInterestRateExtrapolationParameter extends SABRInterestRateParameters {

  /**
   * The cut-off strike above which the extrapolation is used.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;

  /**
   * Constructor from the parameter surfaces and extrapolation parmeters. The SABR volatility formula is HaganVolatilityFunction.
   * @param alpha The alpha parameters.
   * @param beta The beta parameters.
   * @param rho The rho parameters.
   * @param nu The nu parameters.
   * @param dayCount The standard day count for which the parameter surfaces are valid.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public SABRInterestRateExtrapolationParameter(VolatilitySurface alpha, VolatilitySurface beta, VolatilitySurface rho, VolatilitySurface nu, DayCount dayCount, double cutOffStrike, double mu) {
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
  public static SABRInterestRateExtrapolationParameter from(final SABRInterestRateParameters sabr, final double cutOffStrike, final double mu) {
    return new SABRInterestRateExtrapolationParameter(sabr.getAlphaSurface(), sabr.getBetaSurface(), sabr.getRhoSurface(), sabr.getNuSurface(), sabr.getDayCount(), cutOffStrike, mu);
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
