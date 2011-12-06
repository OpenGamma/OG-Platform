/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;

/**
 * Class describing the SABR parameter surfaces used in interest rate modeling and the correlation parameters for CMS spread modeling.
 */
public class SABRInterestRateCorrelationParameters extends SABRInterestRateParameters {

  /**
   * The correlation as function of the strike.
   */
  private final DoubleFunction1D _correlation;

  /**
   * Constructor from the parameter surfaces and correlation function. The SABR volatility formula is HaganVolatilityFunction.
   * @param alpha The alpha parameters.
   * @param beta The beta parameters.
   * @param rho The rho parameters.
   * @param nu The nu parameters.
   * @param dayCount The standard day count for which the parameter surfaces are valid.
   * @param correlation The correlation function.
   */
  public SABRInterestRateCorrelationParameters(final InterpolatedDoublesSurface alpha, final InterpolatedDoublesSurface beta, final InterpolatedDoublesSurface rho,
      final InterpolatedDoublesSurface nu, DayCount dayCount, final DoubleFunction1D correlation) {
    super(alpha, beta, rho, nu, dayCount);
    Validate.notNull(correlation, "Correlation");
    _correlation = correlation;
  }

  /**
   * Builder from SABR parameter surfaces and the correlation function.
   * @param sabr The SABR parameters surfaces.
   * @param correlation The correlation function.
   * @return The SABR with correlation object.
   */
  public static SABRInterestRateCorrelationParameters from(final SABRInterestRateParameters sabr, final DoubleFunction1D correlation) {
    Validate.notNull(sabr, "SABR parameters");
    return new SABRInterestRateCorrelationParameters(sabr.getAlphaSurface(), sabr.getBetaSurface(), sabr.getRhoSurface(), sabr.getNuSurface(), sabr.getDayCount(), correlation);
  }

  /**
   * Gets the correlation function.
   * @return The correlation.
   */
  public DoubleFunction1D getCorrelation() {
    return _correlation;
  }

}
