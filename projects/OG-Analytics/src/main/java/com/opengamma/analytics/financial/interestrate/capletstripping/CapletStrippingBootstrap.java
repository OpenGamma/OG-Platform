/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Iterator;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Simplest possible caplet stripping algorithm. Each cap must be at the same strike (cap are normally quoted at fixed absolute strikes, except for the ATM
 * which this cannot handle), and have the same start (this could be relaxed with better decomposition logic). The co-starting caps are decomposed into a set
 * of spanning caps (simply by taking price difference). Since these spanning caps do not (by construction) share any underlying caplets, implied volatilities
 * (i.e. the common volatility of the caplet set) can be found that price each spanning cap. The resultant expiry dependent caplet volatility curve with of course
 * by piecewise constant.
 */
public class CapletStrippingBootstrap {

  private final SimpleOptionData[][] _caplets;
  private final double[] _intrinsicValues;
  private final double[] _endTimes;

  // private final List<CapFloorPricer> _capPricers;

  /**
   * Simple caplet bootstrapping
   * @param caps All caps must have same start time and strike
   * @param curves yield curves (i.e. discount and Ibor-projection)
   */
  public CapletStrippingBootstrap(final List<CapFloor> caps, final MulticurveProviderInterface curves) {
    ArgumentChecker.noNulls(caps, "caps null");
    ArgumentChecker.notNull(curves, "null curves");

    final int n = caps.size();
    _caplets = new SimpleOptionData[n][];
    _intrinsicValues = new double[n];
    _endTimes = new double[n];

    final Iterator<CapFloor> iter = caps.iterator();
    final CapFloor firstCap = iter.next();
    _caplets[0] = CapFloorDecomposer.toOptions(firstCap, curves);
    _intrinsicValues[0] = intrinsicValue(_caplets[0]);

    final double strike = firstCap.getStrike();
    final double startTime = firstCap.getStartTime();
    double endTime = firstCap.getEndTime();
    _endTimes[0] = endTime;
    int i1 = firstCap.getNumberOfPayments();
    int ii = 1;
    while (iter.hasNext()) {
      final CapFloor cap = iter.next();
      ArgumentChecker.isTrue(cap.getStrike() == strike, "caps must have same strike for this method");
      ArgumentChecker.isTrue(cap.getStartTime() == startTime, "caps must be co-starting");
      final double temp = cap.getEndTime();
      ArgumentChecker.isTrue(temp > endTime, "caps must be in order of increasing end time"); //TODO remove this by sorting caps
      // decompose caps
      final int i2 = cap.getNumberOfPayments();
      final CapFloorIbor[] caplets = cap.getPayments();
      final CapFloorIbor[] uniqueCaplets = new CapFloorIbor[i2 - i1];
      System.arraycopy(caplets, i1, uniqueCaplets, 0, i2 - i1);
      _caplets[ii] = CapFloorDecomposer.toOptions(uniqueCaplets, curves);
      _intrinsicValues[ii] = intrinsicValue(_caplets[ii]);
      i1 = i2;
      endTime = temp;
      _endTimes[ii] = endTime;
      ii++;
    }
  }

  // intrinsic value
  private double intrinsicValue(final SimpleOptionData[] data) {
    final int n = data.length;
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      sum += data[i].getIntrinsicValue();
    }
    return sum;
  }

  /**
   *
   * @param mktCapFlPrices market prices of caps
   * @return The set caplet/floorlet volatilities (indexed in ascending time order)
   */
  public double[] capletVolsFromPrices(final double[] mktCapFlPrices) {
    ArgumentChecker.notEmpty(mktCapFlPrices, "null cap prices");
    final int n = _caplets.length;
    ArgumentChecker.isTrue(n == mktCapFlPrices.length, "length of prices does not match number of caps");
    ArgumentChecker.isTrue(mktCapFlPrices[0] > _intrinsicValues[0], "prices must be greater than or equal to their intrinsic values");
    final double[] diffs = new double[n];
    diffs[0] = mktCapFlPrices[0];
    for (int i = 1; i < n; i++) {
      diffs[i] = mktCapFlPrices[i] - mktCapFlPrices[i - 1];
      ArgumentChecker.isTrue(diffs[i] >= _intrinsicValues[i], "prices must be greater than or equal to their intrinsic values");
    }

    return capletVolsFromPriceDiff(diffs);
  }

  private double[] capletVolsFromPriceDiff(final double[] diffs) {
    final int n = diffs.length;
    final double[] capletVols = new double[n];
    for (int i = 0; i < n; i++) {
      capletVols[i] = BlackFormulaRepository.impliedVolatility(_caplets[i], diffs[i]);
    }
    return capletVols;
  }

  /**
   *
   * @param mktCapFlVols market implied volatilities of caps
   * @return The set caplet/floorlet volatilities (indexed in ascending time order)
   */
  public double[] capletVolsFromCapVols(final double[] mktCapFlVols) {
    ArgumentChecker.notEmpty(mktCapFlVols, "null cap vols");
    final int n = _caplets.length;
    ArgumentChecker.isTrue(n == mktCapFlVols.length, "length of vols does not match number of caps");
    final double[] mktCapFlPrices = new double[n];

    mktCapFlPrices[0] = BlackFormulaRepository.price(_caplets[0], mktCapFlVols[0]);

    for (int i = 1; i < n; i++) {
      double sum = 0;
      for (int j = 0; j <= i; j++) {
        sum += BlackFormulaRepository.price(_caplets[j], mktCapFlVols[i]);
      }
      mktCapFlPrices[i] = sum;
    }
    return capletVolsFromPrices(mktCapFlPrices);
  }

  public double[] getEndTimes() {
    return _endTimes;
  }
}
