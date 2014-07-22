/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.newstrippers;

import java.util.Iterator;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.capletstripping.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletVolProvider;
import com.opengamma.analytics.financial.interestrate.capletstripping.MultiCapFloorPricer;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * provide caplet volatilities for a single strike according to the value of some model parameters 
 */
public abstract class SingleStrike implements CapletVolProvider {

  private final MultiCapFloorPricer _pricer;
  private final int _nCaps;
  private final int _nCaplets;
  private final double[] _capStartTimes;
  private final double[] _capEndTimes;
  private final double _strike;
  private final double[] _capletExp;

  public SingleStrike(final List<CapFloor> caps, final MulticurveProviderInterface curves) {
    ArgumentChecker.noNulls(caps, "caps null");
    ArgumentChecker.notNull(curves, "null curves");
    _nCaps = caps.size();

    _capStartTimes = new double[_nCaps];
    _capEndTimes = new double[_nCaps];
    final Iterator<CapFloor> iter = caps.iterator();
    CapFloor cap = iter.next();
    _strike = cap.getStrike();
    _capStartTimes[0] = cap.getStartTime();
    _capEndTimes[0] = cap.getEndTime();
    int ii = 1;
    while (iter.hasNext()) {
      cap = iter.next();
      ArgumentChecker.isTrue(cap.getStrike() == _strike, "All caps are requied to have the same strike");
      _capStartTimes[ii] = cap.getStartTime();
      _capEndTimes[ii] = cap.getEndTime();
      ii++;
    }

    _pricer = new MultiCapFloorPricer(caps, curves);
    _nCaplets = _pricer.getTotalNumberOfCaplets();
    _capletExp = _pricer.getCapletExpiries();
  }

  //  protected void checkPrices(final double[] capPrices) {
  //    ArgumentChecker.notEmpty(capPrices, "null cap prices");
  //    final int n = getnCaps();
  //    ArgumentChecker.isTrue(n == capPrices.length, "wrong number of capPrices, should have {}, but {} given", n, capPrices.length);
  //    final double[] base = getPricer().getIntrinsicCapValues();
  //    for (int i = 0; i < n; i++) {
  //      ArgumentChecker.isTrue(capPrices[i] >= base[i], "Cap price {} lower that intrinisic value {}", capPrices[i], base[i]);
  //    }
  //  }
  //
  //  protected void checkVols(final double[] capVols) {
  //    ArgumentChecker.notEmpty(capVols, "null cap vols");
  //    final int n = getnCaps();
  //    ArgumentChecker.isTrue(n == capVols.length, "wrong number of capVols, should have {}, but {} given", n, capVols.length);
  //    for (int i = 0; i < n; i++) {
  //      ArgumentChecker.isTrue(capVols[i] >= 0.0, "Cap vol {} less than zero", capVols[i]);
  //    }
  //  }
  //
  //  protected void checkErrors(final double[] errors) {
  //    ArgumentChecker.notEmpty(errors, "null errors");
  //    final int n = getnCaps();
  //    ArgumentChecker.isTrue(n == errors.length, "wrong number of errors, should have {}, but {} given", n, errors.length);
  //    for (int i = 0; i < n; i++) {
  //      ArgumentChecker.isTrue(errors[i] > 0.0, "erros {} less than zero or equal to zero", errors[i]);
  //    }
  //  }

  /**
   * Gets the pricer.
   * @return the pricer
   */
  public MultiCapFloorPricer getPricer() {
    return _pricer;
  }

  /**
   * Gets the nCaps.
   * @return the nCaps
   */
  public int getnCaps() {
    return _nCaps;
  }

  protected double[] getCapletExp() {
    return _capletExp;
  }

  /**
   * Gets the nCaplets.
   * @return the nCaplets
   */
  public int getnCaplets() {
    return _nCaplets;
  }

  protected double[] getCapStartTimes() {
    return _capStartTimes;
  }

  protected double[] getCapEndTimes() {
    return _capEndTimes;
  }

  public double getStrike() {
    return _strike;
  }

}
