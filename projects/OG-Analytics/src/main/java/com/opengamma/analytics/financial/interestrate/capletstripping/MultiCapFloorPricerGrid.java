/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * This aligns the underlying caplets onto a strike-expiry grid, and fills in gaps in the resultant grid with phantom 
 * caplets (caplets that do not belong to any of the caps, and thus cannot have any effect on the cap prices). This
 * is used exclusively by {@link CapletStripperDirect}, which requires a grid of caplets.
 */
public class MultiCapFloorPricerGrid extends MultiCapFloorPricer {

  private final int _gridSize;

  //maps
  //this maps from the position of an option in _capletsArray to a position in the (flattened) grid.
  private final int[] _optionsToGridMap;
  //this maps from a cap to a set of points on the grid.
  private final int[][] _capToGridMap;

  /**
   * 
   * @param caps List of cap or floors (as {@link CapFloor}). The order is not important and will be retained by methods
   * returning cap values. 
   * @param curves The discount and index curves 
   * grid, which may involve some values that have no bearing on cap metrics - these are 'dummy' volatilities since none
   * of the caps contain corresponding caplets. Use this with 
   */
  public MultiCapFloorPricerGrid(final List<CapFloor> caps, final MulticurveProviderInterface curves) {
    super(caps, curves);

    _capToGridMap = new int[getNumCaps()][];

    final double[] strikes = getStrikes();
    final double[] capletExp = getCapletExpiries();
    final int nStrikes = strikes.length;
    final int nExp = capletExp.length;
    _gridSize = nStrikes * nExp;

    _optionsToGridMap = new int[getNumCaplets()];
    if (getNumCaplets() == _gridSize) {
      //here the options (caplets/floorlets) form a regular 2D grid in strike-expiry      
      for (int i = 0; i < _gridSize; i++) {
        _optionsToGridMap[i] = i;
      }
    } else if (getNumCaplets() < _gridSize) {
      int count = 0;
      for (final SimpleOptionData option : getCapletArray()) {
        final double k = option.getStrike();
        final double t = option.getTimeToExpiry();
        final int kIndex = Arrays.binarySearch(getStrikes(), k);
        final int tIndex = Arrays.binarySearch(getCapletExpiries(), t);
        _optionsToGridMap[count++] = kIndex * nExp + tIndex;
      }
    } else {
      throw new IllegalArgumentException("Something is wrong with the logic of MultiCapFloorPricerGrid");
    }

    // form a map from cap to grid positions
    for (int i = 0; i < getNumCaps(); i++) {
      final int[] map = getCapToCapletMap(i);
      final int n = map.length;
      _capToGridMap[i] = new int[n];
      for (int j = 0; j < n; j++) {
        _capToGridMap[i][j] = _optionsToGridMap[map[j]];
      }
    }
  }

  /**
   * Price a set of caps/floors from the (Black) volatility of caplets on a strike-expiry grid. 
   * This is mainly used to calibrate to cap prices by directly setting the individual caplet vols
   * @param capletVolGridValues The (Black) volatility of caplets on a (flatten) strike-expiry grid - this is flattened 
   * row-wise (so you have a block at one strike, then a block at the next strike)
   * @return The cap/floor prices (in the same order the caps were given in the constructor)
   */
  @Override
  public double[] priceFromCapletVols(final double[] capletVolGridValues) {
    ArgumentChecker.notEmpty(capletVolGridValues, "null caplet volatilities");
    ArgumentChecker.isTrue(_gridSize == capletVolGridValues.length, "Expect caplet vols on a (flatened) grid of {} strikes by {} expiries, so {} values. Given {} capletVols", getStrikes().length,
        getCapletExpiries().length, _gridSize, capletVolGridValues.length);

    //If _gridSize < _nCaplets not all the elements of capletVolGridValues are used. This is because some of the volatilities 
    //refer to caplets not found in any caps (we are considering) and are in effect dummy values for calibration methods that
    //require a complete (i.e. no missing elements) grid. 
    final int n = getNumCaplets();
    final double[] capletPrices = new double[n];
    for (int i = 0; i < n; i++) {
      capletPrices[i] = BlackFormulaRepository.price(getOption(i), capletVolGridValues[_optionsToGridMap[i]]);
    }
    return priceFromCapletPrices(capletPrices);
  }

  /**
   * This vega matrix gives the sensitivity of the ith cap to the volatility of the jth caplet (where the caplets are order by their expiry). of course
   * if a cap does not contain a particular caplet, that entry will be zero.
   * @param capletVolGridValues The volatilities of all the caplets that make up the set of caps
   * @return  vega matrix
   */
  @Override
  public DoubleMatrix2D vegaFromCapletVols(final double[] capletVolGridValues) {
    ArgumentChecker.notEmpty(capletVolGridValues, "null caplet volatilities");
    ArgumentChecker.isTrue(_gridSize == capletVolGridValues.length, "Expect caplet vols on a (flatened) grid of {} strikes by {} expiries, so {} values. Given {} capletVols", getStrikes().length,
        getCapletExpiries().length, _gridSize, capletVolGridValues.length);

    //do not compute the vega of the 'dummy' caplets 
    final double[] capletVega = new double[_gridSize];
    final int n = getNumCaplets();
    for (int i = 0; i < n; i++) {
      final int gridIndex = _optionsToGridMap[i];
      capletVega[gridIndex] = BlackFormulaRepository.vega(getOption(i), capletVolGridValues[gridIndex]);
    }

    final int nCaps = getNumCaps();
    final DoubleMatrix2D jac = new DoubleMatrix2D(nCaps, _gridSize);
    for (int i = 0; i < nCaps; i++) {
      final double[] data = jac.getData()[i];
      final int[] indicies = _capToGridMap[i];
      for (final int index : indicies) {
        data[index] = capletVega[index];
      }
    }
    return jac;
  }

  public int getGridSize() {
    return _gridSize;
  }

  @Override
  public DoublesPair[] getExpiryStrikeArray() {
    final DoublesPair[] res = new DoublesPair[_gridSize];
    final double[] strikes = getStrikes();
    final double[] exp = getCapletExpiries();
    final int n = strikes.length;
    final int m = exp.length;

    for (int i = 0; i < n; i++) {
      final double k = strikes[i];
      for (int j = 0; j < m; j++) {
        final double t = exp[j];
        res[j + i * m] = DoublesPair.of(t, k);
      }
    }

    return res;
  }

}
