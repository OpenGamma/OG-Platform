/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * This decomposes a set of caps or floors to the unique set of underlying caplets (or floorlets) to allow 
 * efficient simultaneous pricing of the caps. 
 * {@link CapFloorPricer}
 */
public class MultiCapFloorPricer {

  private final int _nCaps;
  private final int _nCaplets;
  private final SimpleOptionData[] _capletsArray;

  private final double[] _capletExp;
  private final double[] _strikes;

  private final double[] _capStartTimes;
  private final double[] _capEndTimes;

  //maps
  //this maps from a cap to the position of an option in _capletsArray
  private final int[][] _capToCapletsMap;
  //this maps from a cap to a set of SimpleOptionData - the same thing can be achieved by using _capToCapletsMap on _capletsArray
  private final SimpleOptionData[][] _capToOptionsMap;

  /**
   * 
   * @param caps List of cap or floors (as {@link CapFloor}). The order is not important and will be retained by methods
   * returning cap values. 
   * @param curves The discount and index curves 
   */
  public MultiCapFloorPricer( List<CapFloor> caps,  MulticurveProviderInterface curves) {
    ArgumentChecker.noNulls(caps, "null caps");
    ArgumentChecker.notNull(curves, "null curve");

    IborIndex iborIndex = caps.get(0).getNthPayment(0).getIndex();

    _nCaps = caps.size();
    _capToCapletsMap = new int[_nCaps][];
    _capToOptionsMap = new SimpleOptionData[_nCaps][];

    // ensure a unique set of caplets in ascending order of strike then fixing time
    Set<CapFloorIbor> capletSet = new TreeSet<>(new CapletsComparator());
    Set<Double> strikes = new TreeSet<>();
    Set<Double> expiries = new TreeSet<>();
    Set<Double> capStartTimes = new TreeSet<>();
    Set<Double> capEndTimes = new TreeSet<>();
    int count = 0;
    Iterator<CapFloor> iter = caps.iterator();
    while (iter.hasNext()) {
      CapFloor cap = iter.next();
      // check all the caps are on the same index
      ArgumentChecker.isTrue(iborIndex.equals(cap.getNthPayment(0).getIndex()), "caps of different index");
      capStartTimes.add(cap.getStartTime());
      capEndTimes.add(cap.getEndTime());
      CapFloorIbor[] capletArray = cap.getPayments();
      int n = capletArray.length;
      _capToCapletsMap[count++] = new int[n];
      for (int j = 0; j < n; j++) {
        CapFloorIbor caplet = capletArray[j];
        strikes.add(caplet.getStrike());
        expiries.add(caplet.getFixingTime());
        capletSet.add(caplet);
      }
    }

    _capStartTimes = ArrayUtils.toPrimitive(capStartTimes.toArray(new Double[0]));
    _capEndTimes = ArrayUtils.toPrimitive(capEndTimes.toArray(new Double[0]));
    _strikes = ArrayUtils.toPrimitive(strikes.toArray(new Double[0]));
    _capletExp = ArrayUtils.toPrimitive(expiries.toArray(new Double[0]));

    //represent the unique set of caplets as SimpleOptionData
    _capletsArray = CapFloorDecomposer.toOptions(capletSet.toArray(new CapFloorIbor[0]), curves);
    _nCaplets = _capletsArray.length;

    // Form a map from caplets in individual caps to the master caplet list (we are only sorting the extra references here)
    List<CapFloorIbor> capletList = new ArrayList<>(capletSet);
    for (int i = 0; i < _nCaps; i++) {
      CapFloor cap = caps.get(i);
      CapFloorIbor[] capletArray = cap.getPayments();
      int n = capletArray.length;
      _capToOptionsMap[i] = new SimpleOptionData[n];
      for (int j = 0; j < n; j++) {
        int index = capletList.indexOf(capletArray[j]);
        _capToCapletsMap[i][j] = index;
        _capToOptionsMap[i][j] = _capletsArray[index];
      }
    }
  }

  /**
   * Order caplets by (ascending) order of fixing time, then by (ascending) order of strike.
   */
  private class CapletsComparator implements Comparator<CapFloorIbor> {
    @Override
    public int compare( CapFloorIbor o1,  CapFloorIbor o2) {
      int a = Doubles.compare(o1.getStrike(), o2.getStrike());
      if (a != 0) {
        return a;
      }
      return Doubles.compare(o1.getFixingTime(), o2.getFixingTime());
    }

  }

  /**
   * get the volatility of the underlying caplets (ordered by expiry then strike), picked off a (caplet)
   * volatility surface 
   * @param volSurface A volatility surface
   * @return caplet volatilities 
   */
  public double[] getCapletVols( VolatilitySurface volSurface) {
    int nCaplets = _capletsArray.length;
    double[] vols = new double[nCaplets];
    for (int i = 0; i < nCaplets; i++) {
      SimpleOptionData caplet = _capletsArray[i];
      vols[i] = volSurface.getVolatility(caplet.getTimeToExpiry(), caplet.getStrike());
    }
    return vols;
  }

  /**
   * Price a set of caps/floors (that will generally share some caplets/floorlets) using a (caplet) volatility surface. This will give a
   * (Black) volatility dependent on the strike and expiry of each caplet. The individual cap prices are of course the sum of the prices of each of
   * their constituent caplets.
   * @param volSurface the (Black) volatility surface of the underlying caplets
   * @return The cap/floor prices (in the same order the caps were given in the constructor)
   */
  public double[] price( VolatilitySurface volSurface) {
    return priceFromCapletVols(getCapletVols(volSurface));
  }

  /**
   * Price a set of caps/floors from the (Black) volatility of caplets on a strike-expiry grid. 
   * This is mainly used to calibrate to cap prices by directly setting the individual caplet vols
   * @param capletVols The (Black) volatility of caplets. These <b>must</b> be order by (ascending) order of fixing time,
   *  then by (ascending) order of strike.
   * @return The cap/floor prices (in the same order the caps were given in the constructor)
   */
  public double[] priceFromCapletVols( double[] capletVols) {
    ArgumentChecker.notEmpty(capletVols, "null caplet volatilities");
    ArgumentChecker.isTrue(_nCaplets == capletVols.length, "Expected {} caplet vols but given ", _nCaplets, capletVols.length);
    double[] capletPrices = new double[_nCaplets];
    for (int i = 0; i < _nCaplets; i++) {
      capletPrices[i] = BlackFormulaRepository.price(_capletsArray[i], capletVols[i]);
    }
    return priceFromCapletPrices(capletPrices);
  }

  /**
   * Price a set of cap/floors from the implied volatilities of the caps/floors - this (by definition) is the common volatility applied to each of the underlying
   * caplets making up the cap. Since different caps will likely have some caplets in common, this pricing involves pricing the same caplets with different
   * volatilities depending on what cap you are considering.
   * @param capVolatilities the cap/floor (Black) volatilities. These must be in the same order as the cap passed to the constructor.
   * @return The cap/floor prices (in the same order the caps were given in the constructor)
   */
  public double[] price( double[] capVolatilities) {
    ArgumentChecker.notEmpty(capVolatilities, "null cap volatilities");
    ArgumentChecker.isTrue(_nCaps == capVolatilities.length, "capVolatilities wrong length");
    double[] res = new double[_nCaps];
    for (int i = 0; i < _nCaps; i++) {
      res[i] = BlackFormulaRepository.price(_capToOptionsMap[i], capVolatilities[i]);
    }
    return res;
  }

  /**
   *  Price a set of caps/floors from the (Black) prices of the (unique set of) underlying caplets.
   * @param capletPrices These <b>must</b> be order by (ascending) order of fixing time,
   *  then by (ascending) order of strike.
   * @return The cap/floor prices (in the same order the caps were given in the constructor)
   */
  protected double[] priceFromCapletPrices( double[] capletPrices) {
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
  public double[] impliedVols( double[] capPrices) {
    ArgumentChecker.notEmpty(capPrices, "null cap prices");
    ArgumentChecker.isTrue(_nCaps == capPrices.length, "capPrices wrong length");
    double[] res = new double[_nCaps];
    for (int i = 0; i < _nCaps; i++) {
      res[i] = BlackFormulaRepository.impliedVolatility(_capToOptionsMap[i], capPrices[i]);
    }
    return res;
  }

  /**
   * The implied volatilities for a set of caps from a model that describes the (Black) volatility of the individual constituent caplets. The individual cap
   * prices are of course the sum of the prices of each of their constituent caplets. The implied volatility of a cap (or floor) is defined as the common (Black)
   * volatility applied to each of the constituent caplets such that the sum of the (Black) prices of the caplets equals the cap price.
   * @param volSurface model describing the (Black) volatility of the underlying caplets
   * @return The cap/floor implied volatilities (in the same order the caps were given in the constructor)
   */
  public double[] impliedVols( VolatilitySurface volSurface) {
    double[] prices = price(volSurface);
    return impliedVols(prices);
  }

  /**
   * The sensitivity of the prices of a set of caps to their implied volatility
   * @param capVolatilities the cap/floor (Black) volatilities
   * @return The cap/floor vega (in the same order the caps were given in the constructor)
   */
  public double[] vega( double[] capVolatilities) {
    ArgumentChecker.notEmpty(capVolatilities, "null cap volatilities");
    ArgumentChecker.isTrue(_nCaps == capVolatilities.length, "capVolatilities wrong length");
    double[] res = new double[_nCaps];
    for (int i = 0; i < _nCaps; i++) {
      int n = _capToCapletsMap[i].length;
      double sum = 0.0;
      for (int j = 0; j < n; j++) {
        sum += BlackFormulaRepository.vega(_capToOptionsMap[i][j], capVolatilities[i]);
      }
      res[i] = sum;
    }
    return res;
  }

  /**
   * This vega matrix gives the sensitivity of the ith cap to the volatility of the jth caplet (where the caplets are order by their expiry). of course
   * if a cap does not contain a particular caplet, that entry will be zero.
   * @param capletVols The volatilities of all the caplets that make up the set of caps
   * @return  vega matrix
   */
  public DoubleMatrix2D vegaFromCapletVols( double[] capletVols) {
    ArgumentChecker.notEmpty(capletVols, "null caplet volatilities");
    ArgumentChecker.isTrue(_nCaplets == capletVols.length, "Expected {} caplet vols but given ", _nCaplets, capletVols.length);

    double[] capletVega = new double[_nCaplets];
    for (int i = 0; i < _nCaplets; i++) {
      capletVega[i] = BlackFormulaRepository.vega(_capletsArray[i], capletVols[i]);
    }

    DoubleMatrix2D jac = new DoubleMatrix2D(_nCaps, _nCaplets);
    for (int i = 0; i < _nCaps; i++) {
      double[] data = jac.getData()[i];
      int[] indices = _capToCapletsMap[i];
      for ( int index : indices) {
        data[index] = capletVega[index];
      }
    }
    return jac;
  }

  /**
   * This vega matrix gives the sensitivity of the implied volatility of the ith cap to the volatility of the jth 
   * caplet. of course if a cap does not contain a particular caplet, that entry will be zero.
   * @param capletVols The volatilities of all the caplets that make up the set of caps
   * @return  cap volatility-vega matrix
   */
  public DoubleMatrix2D capVolVega( double[] capletVols) {

    //cap vega matrix - sensitivity of cap prices to the volatilities of the caplets 
    DoubleMatrix2D vega = vegaFromCapletVols(capletVols);
    double[] capPrices = priceFromCapletVols(capletVols);
    double[] capVols = impliedVols(capPrices);

    //sensitivity of the cap prices to their volatilities 
    double[] capVega = vega(capVols);

    int nCaplets = capletVols.length;
    DoubleMatrix2D capVolVega = new DoubleMatrix2D(_nCaps, nCaplets);
    for (int i = 0; i < _nCaps; i++) {
      double[] temp = capVolVega.getData()[i];
      double[] vegaRow = vega.getData()[i];
      double invVega = 1.0 / capVega[i];
      for (int j = 0; j < nCaplets; j++) {
        temp[j] = invVega * vegaRow[j];
      }
    }

    return capVolVega;
  }

  /**
   *  get the sorted array of unique caplet expiry times from the set of caps supplied
   * @return caplet expiry times
   */
  public double[] getCapletExpiries() {
    return _capletExp;
  }

  /**
   *  get the sorted array of unique strikes from the set of caps supplied
   * @return caplet expiry times
   */
  public double[] getStrikes() {
    return _strikes;
  }

  /**
   * get the intrinsic (i.e. minimum) value of the caps - this is the cap price for zero volatility. 
   * @return The intrinsic values
   */
  public double[] getIntrinsicCapValues() {
    int n = _nCaplets;
    double[] intr = new double[n];
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
  private double[] aggregateToCaps( double[] values) {

    double[] res = new double[_nCaps];
    for (int i = 0; i < _nCaps; i++) {
      int[] indices = _capToCapletsMap[i];
      int n = indices.length;
      double sum = 0;
      for (int j = 0; j < n; j++) {
        int index = indices[j];
        sum += values[index];
      }
      res[i] = sum;
    }
    return res;
  }

  /**
   * The ordered set of cap start times 
   * @return cap start times
   */
  public double[] getCapStartTimes() {
    return _capStartTimes;
  }

  /**
   * The ordered set of cap end times 
   * @return cap end times
   */
  public double[] getCapEndTimes() {
    return _capEndTimes;
  }

  /**
   * get an array of expiry-strike values (as a {@link DoublesPair} of the underlying caplets. These are order by
   * (ascending) order of fixing time, then by (ascending) order of strike.
   * @return DoublesPair of caplet expiry and strike 
   */
  public DoublesPair[] getExpiryStrikeArray() {
    DoublesPair[] res = new DoublesPair[_nCaplets];
    for (int i = 0; i < _nCaplets; i++) {
      SimpleOptionData option = _capletsArray[i];
      res[i] = DoublesPair.of(option.getTimeToExpiry(), option.getStrike());
    }
    return res;
  }

  /**
   * Gets number of caps 
   * @return the number of caps 
   */
  public int getNumCaps() {
    return _nCaps;
  }

  /**
   * Gets the number of unique caplets 
   * @return the the number of unique caplets 
   */
  public int getNumCaplets() {
    return _nCaplets;
  }

  /**
   * for a particular cap (given by index), this gives the indices of its underlying caplets in the master caplet list 
   * (which is ordered by fixing time, then strike)
   * @param index The index of the cap (using the same order as the constructor)
   * @return indices of the caplets belonging to the cap 
   */
  protected int[] getCapToCapletMap( int index) {
    return _capToCapletsMap[index];
  }

  /**
   * get the caplet at a particular index (where the caplets are order by fixing time, then strike) as a {@link SimpleOptionData}
   * @param index the caplet index 
   * @return caplet as a {@link SimpleOptionData}
   */
  protected SimpleOptionData getOption( int index) {
    return _capletsArray[index];
  }

  /**
   * get the underlying caplets (order by fixing time, then strike) as an array of {@link SimpleOptionData}
   * @return array of {@link SimpleOptionData}
   */
  protected SimpleOptionData[] getCapletArray() {
    return _capletsArray;
  }

  /**
   * Get the forward rates for the period covered by the caplets. 
   * @return the forward rates - these are order by (caplet) fixing time, then strike.
   */
  public double[] getCapletForwardRates() {
    int n = _capletsArray.length;
    Set<Double> ts = new TreeSet<>();
    for (int i = 0; i < n; i++) {
      ts.add(_capletsArray[i].getForward());
    }
    return ArrayUtils.toPrimitive(ts.toArray(new Double[0]));
  }

}
