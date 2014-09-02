/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CapFloorPricer {

  private final SimpleOptionData[] _caplets;
  private final int _n;

  /**
   * Decomposes a cap (floor) down to relevant information about its caplets (floorlets), i.e. the forward (ibor)
   * values, the fixing times and
   * the discount factors. Each caplet (floorlet), and hence the whole cap (floor) can then be priced by suppling a
   * VolatilityModel1D
   * (which gives a Black vol for a particular forward/strike/expiry) or a VolatilityTermStructure (which gives the vol
   * simply as a function of expiry)
   * @param cap a cap or floor
   * @param curves The relevant curves
   */
  public CapFloorPricer(final CapFloor cap, final MulticurveProviderInterface curves) {
    _caplets = CapFloorDecomposer.toOptions(cap, curves);
    _n = _caplets.length;
  }

  /**
   * price a cap given its volatility
   * @param vol The cap volatility
   * @return cap price
   */
  public double price(final double vol) {
    double sum = 0;
    for (int i = 0; i < _n; i++) {
      sum += BlackFormulaRepository.price(_caplets[i], vol);
    }
    return sum;
  }

  /**
   * Price a cap (floor) with a VolatilityModel1D. This allows the same cap to be prices with different models
   * (different models include different
   * parameters for the same model), with repeating calculations (e.g. as part of a caplet stripping routine)
   * @param volModel VolatilityModel1D which gives a Black vol for a particular forward/strike/expiry
   * @return The cap (floor) price
   * @deprecated discourage use of {@link VolatilityModel1D}
   */
  @Deprecated
  public double price(final VolatilityModel1D volModel) {
    double sum = 0;
    for (int i = 0; i < _n; i++) {
      final double vol = volModel.getVolatility(_caplets[i]);
      sum += BlackFormulaRepository.price(_caplets[i], vol);
    }
    return sum;
  }

  /**
   * Price the cap using a (caplet) volatility surface. This will give a (Black) volatility dependent on the
   * strike and expiry of each caplet. The cap price is of course the sum of the prices of each of the constituent
   * caplets.
   * @param volSurface the (Black) volatility surface of the underlying caplets
   * @return The cap/floor price
   */
  public double price(final VolatilitySurface volSurface) {
    double sum = 0;
    for (int i = 0; i < _n; i++) {
      final SimpleOptionData caplet = _caplets[i];
      final double vol = volSurface.getVolatility(caplet.getTimeToExpiry(), caplet.getStrike());
      sum += BlackFormulaRepository.price(_caplets[i], vol);
    }
    return sum;
  }

  /**
   * Price the cap from the volatilities of its caplets.
   * @param capletVols The (Black) volatility of caplets. These <b>must</b> be order by (ascending) order of fixing
   * time.
   * @return The cap price
   */
  public double price(final double[] capletVols) {
    ArgumentChecker.isTrue(capletVols.length == _n, "number of caplets is not equal to number of vols");
    double sum = 0;
    for (int i = 0; i < _n; i++) {
      final double vol = capletVols[i];
      sum += BlackFormulaRepository.price(_caplets[i], vol);
    }
    return sum;
  }

  /**
   * The implied volatility of a cap.
   * @param capPrice The cap price
   * @return The cap implied volatility
   */
  public double impliedVol(final double capPrice) {
    return BlackFormulaRepository.impliedVolatility(_caplets, capPrice);
  }

  /**
   * 
   * @param capletVolModel model of caplet volatility
   * @return the implied volatility
   * @deprecated discourage use of {@link VolatilityModel1D}
   */
  @Deprecated
  public double impliedVol(final VolatilityModel1D capletVolModel) {
    final double price = price(capletVolModel);
    return impliedVol(price);
  }

  /**
   * get the implied volatility of a cap from a (caplet) volatility surface - this will give a (Black) volatility
   * dependent on the
   * strike and expiry of each caplet.
   * @param volSurface The (caplet) volatility surface
   * @return The cap implied volatility
   */
  public double impliedVol(final VolatilitySurface volSurface) {
    final double price = price(volSurface);
    return impliedVol(price);
  }

  /**
   * get the implied volatility of a cap from the volatilities of the underlying caplets.
   * @param capletVols The (Black) volatility of caplets. These <b>must</b> be order by (ascending) order of fixing
   * time.
   * @return The cap implied volatility
   */
  public double impliedVol(final double[] capletVols) {
    final double price = price(capletVols);
    return impliedVol(price);
  }

  /**
   * get the cap vega; the sensitivity of its price to its volatility
   * @param capVolatility The cap volatility
   * @return The cap vega
   */
  public double vega(final double capVolatility) {
    double sum = 0;
    for (int i = 0; i < _n; i++) {
      sum += BlackFormulaRepository.vega(_caplets[i], capVolatility);
    }
    return sum;
  }

  /**
   * 
   * @param capletVolModel model of caplet volatility
   * @return the vega
   * @deprecated discourage use of {@link VolatilityModel1D}
   */
  @Deprecated
  public double vega(final VolatilityModel1D capletVolModel) {
    final double vol = impliedVol(capletVolModel);
    return vega(vol);
  }

  /**
   * get the cap vega; the sensitivity of its price to its volatility
   * @param volSurface The (caplet) volatility surface
   * @return the cap vega
   */
  public double vega(final VolatilitySurface volSurface) {
    final double vol = impliedVol(volSurface);
    return vega(vol);
  }

  /**
   * get the cap vega; the sensitivity of its price to its volatility
   * @param capletVols The (Black) volatility of caplets. These <b>must</b> be order by (ascending) order of fixing
   * time.
   * @return the cap vega
   */
  public double vega(final double[] capletVols) {
    final double vol = impliedVol(capletVols);
    return vega(vol);
  }

  /**
   * Get the forward rates for the period covered by the caplets.
   * @return the forward rates - these are order by (caplet) fixing time
   */
  public double[] getCapletForwardRates() {
    final double[] fwds = new double[_n];
    for (int i = 0; i < _n; i++) {
      fwds[i] = _caplets[i].getForward();
    }
    return fwds;
  }

  /**
   * get the cap forward. This is the swap rate for the same period (on the same index).
   * @return The cap forward
   */
  protected double getCapForward() {
    double sum1 = 0;
    double sum2 = 0;
    final double[] df = getDiscountFactors();
    final double[] fwds = getCapletForwardRates();
    // COMMENT - the discount factor includes the accrual fraction
    for (int i = 0; i < _n; i++) {
      sum1 += df[i] * fwds[i];
      sum2 += df[i];
    }
    return sum1 / sum2;
  }

  /**
   * Gets the caplet expiries.
   * @return the caplet expiries
   */
  public double[] getCapletExpiries() {
    final double[] t = new double[_n];
    for (int i = 0; i < _n; i++) {
      t[i] = _caplets[i].getTimeToExpiry();
    }
    return t;
  }

  /**
   * Gets the discount factors. <b>Note</b> this is purely the number to multiple the result of Black's formula
   * ({@link BlackFormulaRepository}) by; this is the discount factor from the end of the period (when payment is
   * made) multiplied by the year fraction.
   * @return the discount factors
   */
  protected double[] getDiscountFactors() {
    final double[] df = new double[_n];
    for (int i = 0; i < _n; i++) {
      df[i] = _caplets[i].getDiscountFactor();
    }
    return df;
  }

  /**
   * Gets the strike
   * @return the strike
   */
  protected double getStrike() {
    return _caplets[0].getStrike();
  }

  /**
   * Gets the number of caplets
   * @return the number of caplets
   */
  public int getNumberCaplets() {
    return _n;
  }

  /**
   * get the underlying caplets (order by fixing time) as an array of {@link SimpleOptionData}
   * @return array of {@link SimpleOptionData}
   */
  public SimpleOptionData[] getCapletAsOptionData() {
    return _caplets;
  }

}
