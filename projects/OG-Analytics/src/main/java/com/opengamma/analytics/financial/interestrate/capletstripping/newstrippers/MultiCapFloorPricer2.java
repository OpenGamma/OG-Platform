/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.newstrippers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapFloorDecomposer;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MultiCapFloorPricer2 {

  private final int _nCaps;
  private final int _nCaplets;
  private final SimpleOptionData[] _capletsArray;
  private final SimpleOptionData[][] _capCaplets;
  private final int[][] _capletIndices;

  @SuppressWarnings("synthetic-access")
  public MultiCapFloorPricer2(final List<CapFloor> caps, final MulticurveProviderInterface curves) {
    ArgumentChecker.noNulls(caps, "null caps");
    ArgumentChecker.notNull(curves, "null curve");

    // check all the caps are on the same index and with the same strike
    final Iterator<CapFloor> iter = caps.iterator();
    final CapFloor firstCap = iter.next();
    final IborIndex iborIndex = firstCap.getNthPayment(0).getIndex();

    while (iter.hasNext()) {
      final CapFloor cap = iter.next();
      ArgumentChecker.isTrue(iborIndex.equals(cap.getNthPayment(0).getIndex()), "caps of different index");
    }

    _nCaps = caps.size();
    _capletIndices = new int[_nCaps][];
    _capCaplets = new SimpleOptionData[_nCaps][];

    // List<CapFloorIbor> caplets = new ArrayList<>();

    // ensure a unique set of caplets in ascending order of strike then fixing time
    final Set<CapFloorIbor> capletSet = new TreeSet<>(new CapletsComparator());
    for (int i = 0; i < _nCaps; i++) {
      final CapFloor cap = caps.get(i);
      final CapFloorIbor[] capletArray = cap.getPayments();
      final int n = capletArray.length;
      _capletIndices[i] = new int[n];
      for (int j = 0; j < n; j++) {
        capletSet.add(capletArray[j]);
      }
    }
    final List<CapFloorIbor> capletList = new ArrayList<>(capletSet);
    _nCaplets = capletList.size();
    _capletsArray = CapFloorDecomposer.toOptions(capletList.toArray(new CapFloorIbor[_nCaplets]), curves);

    // Form a map from caplets in individual caps to the master caplet list (we are only sorting the extra references here)
    for (int i = 0; i < _nCaps; i++) {
      final CapFloor cap = caps.get(i);
      final CapFloorIbor[] capletArray = cap.getPayments();
      final int n = capletArray.length;
      _capCaplets[i] = new SimpleOptionData[n];
      for (int j = 0; j < n; j++) {
        final int index = capletList.indexOf(capletArray[j]);
        _capletIndices[i][j] = index;
        _capCaplets[i][j] = _capletsArray[index];
      }
    }
  }

  /**
   * Order caplets by (ascending) order of strike, then by (ascending) order of fixing time.
   */
  private class CapletsComparator implements Comparator<CapFloorIbor> {
    @Override
    public int compare(final CapFloorIbor o1, final CapFloorIbor o2) {
      final int a = Doubles.compare(o1.getStrike(), o2.getStrike());
      if (a != 0) {
        return a;
      }
      return Doubles.compare(o1.getFixingTime(), o2.getFixingTime());
    }

  }

  public double[] getCapletVols(final VolatilitySurface volSurface) {
    final int nCaplets = _capletsArray.length;
    final double[] vols = new double[nCaplets];
    for (int i = 0; i < nCaplets; i++) {
      final SimpleOptionData caplet = _capletsArray[i];
      vols[i] = volSurface.getVolatility(caplet.getTimeToExpiry(), caplet.getStrike());
    }
    return vols;
  }

  /**
   * Price a set of caps/floors (that will generally share some caplets/floorlets) using a VolatilityModel1D for the caplet volatilities - this will give a
   * (Black) volatility dependent on the forward, strike and expiry of each caplet. The individual cap prices are of course the sum of the prices of each of
   * their constituent caplets.
   * @param volSurface the (Black) volatility surface of the underlying caplets
   * @return The cap/floor prices (in the same order the caps were given in the constructor)
   */
  public double[] price(final VolatilitySurface volSurface) {
    return priceFromCapletVols(getCapletVols(volSurface));
  }

  /**
   * Price a set of caps/floors from the (Black) volatility of the set of unique caplets. These caplets are sorted by ascending order of fixingTime. This is mainly
   * used to calibrate to cap prices by directly setting the individual caplet vols
   * @param capletVols The (Black) volatility of the unique caplets sorted by ascending order of fixingTime.
   * @return The cap/floor prices (in the same order the caps were given in the constructor)
   */
  public double[] priceFromCapletVols(final double[] capletVols) {
    ArgumentChecker.notEmpty(capletVols, "null caplet volatilities");
    ArgumentChecker.isTrue(_nCaplets == capletVols.length, "capletVols wrong length");
    final double[] capletPrices = new double[_nCaplets];
    for (int i = 0; i < _nCaplets; i++) {
      capletPrices[i] = BlackFormulaRepository.price(_capletsArray[i], capletVols[i]);
    }
    return priceFromCapletPrices(capletPrices);
  }

  /**
   * This vega matrix gives the sensitivity of the ith cap to the volatility of the jth caplet (where the caplets are order by their expiry). of course
   * if a cap does not contain a particular caplet, that entry will be zero.
   * @param capletVols The volatilities of all the caplets that make up the set of caps
   * @return  vega matrix
   */
  public DoubleMatrix2D vegaFromCapletVols(final double[] capletVols) {
    ArgumentChecker.notEmpty(capletVols, "null caplet volatilities");
    ArgumentChecker.isTrue(_nCaplets == capletVols.length, "capletVols wrong length");
    final double[] capletVega = new double[_nCaplets];
    for (int i = 0; i < _nCaplets; i++) {
      capletVega[i] = BlackFormulaRepository.vega(_capletsArray[i], capletVols[i]);
    }

    final double[][] jac = new double[_nCaps][_nCaplets];

    for (int i = 0; i < _nCaps; i++) {
      final int[] indicies = _capletIndices[i];
      final int n = indicies.length;
      for (int j = 0; j < n; j++) {
        final int index = indicies[j];
        jac[i][index] = capletVega[index];
      }
    }
    return new DoubleMatrix2D(jac);
  }

  /**
   * Price a set of cap/floors from the implied volatilities of the caps/floors - this (by definition) is the common volatility applied to each of the underlying
   * caplets making up the cap. Since different caps will likely have some caplets in common, this pricing involves pricing the same caplets with different
   * volatilities depending on what cap you are considering.
   * @param capVolatilities the cap/floor (Black) volatilities
   * @return The cap/floor prices (in the same order the caps were given in the constructor)
   */
  public double[] price(final double[] capVolatilities) {
    ArgumentChecker.notEmpty(capVolatilities, "null cap volatilities");
    ArgumentChecker.isTrue(_nCaps == capVolatilities.length, "capVolatilities wrong length");
    final double[] res = new double[_nCaps];
    for (int i = 0; i < _nCaps; i++) {
      res[i] = BlackFormulaRepository.price(_capCaplets[i], capVolatilities[i]);
    }
    return res;
  }

  private double[] priceFromCapletPrices(final double[] capletPrices) {
    return aggregateToCaps(capletPrices);
  }

  /**
   * The implied volatilities for a set of caps from their prices. The implied volatility of a cap (or floor) is defined as the common (Black)
   * volatility applied to each of the constituent caplets such that the sum of the (Black) prices of the caplets equals the cap price. As the caps will generally
   * share some caplets, this is inconsistent as a model since a forward rate (which forms the payoff of a caplet) will 'see' a different volatility depending
   * on what cap is being priced. The cap implied volatilities should be viewed as nothing more than monotonic mapping from prices.
   * @param capPrices The cap prices (in the same order the caps were given in the constructor)
   * @return The cap/floor implied volatilities (in the same order the caps were given in the constructor)
   */
  protected double[] impliedVols(final double[] capPrices) {
    ArgumentChecker.notEmpty(capPrices, "null cap prices");
    ArgumentChecker.isTrue(_nCaps == capPrices.length, "capPrices wrong length");
    final double[] res = new double[_nCaps];
    for (int i = 0; i < _nCaps; i++) {
      res[i] = BlackFormulaRepository.impliedVolatility(_capCaplets[i], capPrices[i]);
    }
    return res;
  }

  /**
   * The implied volatilities for a set of caps from a model that describes the (Black) volatility of the individual constituent caplets. The individual cap
   * prices are of course the sum of the prices of each of their constituent caplets. The implied volatility of a cap (or floor) is defined as the common (Black)
   * volatility applied to each of the constituent caplets such that the sum of the (Black) prices of the caplets equals the cap price.
   * @param capletVolCurve model describing the (Black) volatility of the underlying caplets
   * @return The cap/floor implied volatilities (in the same order the caps were given in the constructor)
   */
  public double[] impliedVols(final VolatilitySurface volSurface) {
    final double[] prices = price(volSurface);
    return impliedVols(prices);
  }

  /**
   * The sensitivity of the prices of a set of caps to their implied volatility
   * @param capVolatilities the cap/floor (Black) volatilities
   * @return The cap/floor vega (in the same order the caps were given in the constructor)
   */
  public double[] vega(final double[] capVolatilities) {
    ArgumentChecker.notEmpty(capVolatilities, "null cap volatilities");
    ArgumentChecker.isTrue(_nCaps == capVolatilities.length, "capVolatilities wrong length");
    final double[] res = new double[_nCaps];
    for (int i = 0; i < _nCaps; i++) {
      final int n = _capletIndices[i].length;
      double sum = 0.0;
      for (int j = 0; j < n; j++) {
        sum += BlackFormulaRepository.vega(_capCaplets[i][j], capVolatilities[i]);
      }
      res[i] = sum;
    }
    return res;
  }

  /**
   * Get the total number of unique caplets in the set of caps supplied
   * @return total number of unique caplets
   */
  public int getTotalNumberOfCaplets() {
    return _capletsArray.length;
  }

  /**
   *  get the sorted array of unique caplet expiry times from the set of caps supplied
   * @return caplet expiry times
   */
  public double[] getCapletExpiries() {
    final int n = _capletsArray.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = _capletsArray[i].getTimeToExpiry();
    }
    Arrays.sort(res);
    return res;
  }

  // intrinsic value
  public double[] getIntrinsicCapValues() {
    final int n = _nCaplets;
    final double[] intr = new double[n];
    for (int i = 0; i < n; i++) {
      intr[i] = _capletsArray[i].getIntrinsicValue();
    }
    return aggregateToCaps(intr);
  }

  /**
   * Aggregate additive values computer on caplets in caps
   * @param values computed on caplets
   * @return values aggregated to caps
   */
  private double[] aggregateToCaps(final double[] values) {

    final double[] res = new double[_nCaps];
    for (int i = 0; i < _nCaps; i++) {
      final int[] indicies = _capletIndices[i];
      final int n = indicies.length;
      double sum = 0;
      for (int j = 0; j < n; j++) {
        final int index = indicies[j];
        sum += values[index];
      }
      res[i] = sum;
    }
    return res;
  }

  /**
   * @deprecated remove and replace with something to just return strike-expiry pair
   * @return
   */
  @Deprecated
  public SimpleOptionData[] getCapletArray() {
    return _capletsArray;
  }

}
