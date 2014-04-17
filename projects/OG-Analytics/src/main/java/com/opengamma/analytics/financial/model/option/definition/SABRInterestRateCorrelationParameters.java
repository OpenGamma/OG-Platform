/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

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
   * @param correlation The correlation function.
   */
  public SABRInterestRateCorrelationParameters(final InterpolatedDoublesSurface alpha, final InterpolatedDoublesSurface beta, final InterpolatedDoublesSurface rho,
      final InterpolatedDoublesSurface nu, final DoubleFunction1D correlation) {
    super(alpha, beta, rho, nu);
    ArgumentChecker.notNull(correlation, "Correlation");
    _correlation = correlation;
  }

  /**
   * Constructor from the parameter surfaces and correlation function. The SABR volatility formula is HaganVolatilityFunction.
   * @param alpha The alpha parameters.
   * @param beta The beta parameters.
   * @param rho The rho parameters.
   * @param nu The nu parameters.
   * @param dayCount The standard day count for which the parameter surfaces are valid.
   * @param correlation The correlation function.
   * @deprecated Used the constructor without day count.
   */
  @Deprecated
  public SABRInterestRateCorrelationParameters(final InterpolatedDoublesSurface alpha, final InterpolatedDoublesSurface beta, final InterpolatedDoublesSurface rho,
      final InterpolatedDoublesSurface nu, final DayCount dayCount, final DoubleFunction1D correlation) {
    super(alpha, beta, rho, nu, dayCount);
    ArgumentChecker.notNull(correlation, "Correlation");
    _correlation = correlation;
  }

  /**
   * Builder from SABR parameter surfaces and the correlation function.
   * @param sabr The SABR parameters surfaces.
   * @param correlation The correlation function.
   * @return The SABR with correlation object.
   */
  public static SABRInterestRateCorrelationParameters from(final SABRInterestRateParameters sabr, final DoubleFunction1D correlation) {
    ArgumentChecker.notNull(sabr, "SABR parameters");
    return new SABRInterestRateCorrelationParameters(sabr.getAlphaSurface(), sabr.getBetaSurface(), sabr.getRhoSurface(), sabr.getNuSurface(), correlation);
  }

  /**
   * Gets the correlation function.
   * @return The correlation.
   */
  public DoubleFunction1D getCorrelation() {
    return _correlation;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _correlation.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof SABRInterestRateCorrelationParameters)) {
      return false;
    }
    final SABRInterestRateCorrelationParameters other = (SABRInterestRateCorrelationParameters) obj;
    if (!ObjectUtils.equals(_correlation, other._correlation)) {
      return false;
    }
    return true;
  }

}
