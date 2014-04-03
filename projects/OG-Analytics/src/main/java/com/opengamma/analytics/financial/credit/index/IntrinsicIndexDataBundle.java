/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.index;

import java.util.Arrays;
import java.util.BitSet;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class IntrinsicIndexDataBundle {
  private static final double TOL = 1e-12;

  private final int _indexSize;
  private final int _nDefaults;

  private final double _indexFactor;
  private final double[] _weights;
  private final double[] _lgd;
  private final ISDACompliantCreditCurve[] _creditCurves;
  private final BitSet _defaulted;

  public IntrinsicIndexDataBundle(final ISDACompliantCreditCurve[] creditCurves, final double[] recoveryRates) {
    ArgumentChecker.noNulls(creditCurves, "creditCurves");
    ArgumentChecker.notEmpty(recoveryRates, "recoveryRates");
    _indexSize = creditCurves.length;
    ArgumentChecker.isTrue(_indexSize == recoveryRates.length, "Length of recoveryRates ({}) does not match index size ({})", recoveryRates.length, _indexSize);
    _nDefaults = 0;

    _lgd = new double[_indexSize];

    for (int i = 0; i < _indexSize; i++) {
      final double lgd = 1 - recoveryRates[i];
      if (lgd < 0.0 || lgd > 1.0) {
        throw new IllegalArgumentException("recovery rate must be between 0 and 1.Value of " + recoveryRates[i] + " given at index " + i);
      }
      _lgd[i] = lgd;
    }

    _weights = new double[_indexSize];
    Arrays.fill(_weights, 1.0 / _indexSize);
    _creditCurves = creditCurves;
    _defaulted = new BitSet(_indexSize);
    _indexFactor = 1.0;
  }

  public IntrinsicIndexDataBundle(final ISDACompliantCreditCurve[] creditCurves, final double[] recoveryRates, final double[] weights) {
    ArgumentChecker.noNulls(creditCurves, "creditCurves");
    ArgumentChecker.notEmpty(recoveryRates, "recoveryRates");
    ArgumentChecker.notEmpty(weights, "weights");
    _indexSize = creditCurves.length;
    ArgumentChecker.isTrue(_indexSize == recoveryRates.length, "Length of recoveryRates ({}) does not match index size ({})", recoveryRates.length, _indexSize);
    ArgumentChecker.isTrue(_indexSize == weights.length, "Length of weights ({}) does not match index size ({})", weights.length, _indexSize);

    _nDefaults = 0;

    _lgd = new double[_indexSize];
    double sum = 0.0;
    for (int i = 0; i < _indexSize; i++) {
      if (weights[i] <= 0.0) {
        throw new IllegalArgumentException("weights must be positive. Value of " + weights[i] + " given at index " + i);
      }
      sum += weights[i];
      final double lgd = 1 - recoveryRates[i];
      if (lgd < 0.0 || lgd > 1.0) {
        throw new IllegalArgumentException("recovery rate must be between 0 and 1.Value of " + recoveryRates[i] + " given at index " + i);
      }
      _lgd[i] = lgd;
    }
    if (Math.abs(sum - 1.0) > TOL) {
      throw new IllegalArgumentException("weights do not sum to 1.0, but " + sum);
    }

    _weights = new double[_indexSize];
    System.arraycopy(weights, 0, _weights, 0, _indexSize);
    _creditCurves = creditCurves;
    _defaulted = new BitSet(_indexSize);
    _indexFactor = 1.0;
  }

  public IntrinsicIndexDataBundle(final ISDACompliantCreditCurve[] creditCurves, final double[] recoveryRates, final BitSet defaulted) {
    ArgumentChecker.notNull(creditCurves, "creditCurves"); //we do allow null entries if listed as defaulted
    ArgumentChecker.notEmpty(recoveryRates, "recoveryRates");
    ArgumentChecker.notNull(defaulted, "defaulted");

    _indexSize = creditCurves.length;
    ArgumentChecker.isTrue(_indexSize == recoveryRates.length, "Length of recoveryRates ({}) does not match index size ({})", recoveryRates.length, _indexSize);
    // Correction made  PLAT-6323 
    //    ArgumentChecker.isTrue(_indexSize == defaulted.length(), "Length of defaulted ({}) does not match index size ({})", defaulted.length(), _indexSize);
    ArgumentChecker.isTrue(_indexSize >= defaulted.length(), "Length of defaulted ({}) is greater than index size ({})", defaulted.length(), _indexSize);

    _nDefaults = defaulted.cardinality();

    _lgd = new double[_indexSize];

    for (int i = 0; i < _indexSize; i++) {
      if (creditCurves[i] == null && !defaulted.get(i)) {
        throw new IllegalArgumentException("Null credit curve, but not set as defaulted in alive list. Index is " + i);
      }
      final double lgd = 1 - recoveryRates[i];
      if (lgd < 0.0 || lgd > 1.0) {
        throw new IllegalArgumentException("recovery rate must be between 0 and 1.Value of " + recoveryRates[i] + " given at index " + i);
      }
      _lgd[i] = lgd;
    }

    _weights = new double[_indexSize];
    Arrays.fill(_weights, 1.0 / _indexSize);
    _creditCurves = creditCurves;
    _defaulted = defaulted;
    // Correction made PLAT-6328
    //    _indexFactor = _nDefaults / ((double) _indexSize);
    _indexFactor = (((double) _indexSize) - _nDefaults) * _weights[0];
  }

  public IntrinsicIndexDataBundle(final ISDACompliantCreditCurve[] creditCurves, final double[] recoveryRates, final double[] weights, final BitSet defaulted) {
    ArgumentChecker.notNull(creditCurves, "creditCurves"); //we do allow null entries if listed as defaulted
    ArgumentChecker.notEmpty(recoveryRates, "recoveryRates");
    ArgumentChecker.notEmpty(weights, "weights");
    ArgumentChecker.notNull(defaulted, "defaulted");

    _indexSize = creditCurves.length;
    ArgumentChecker.isTrue(_indexSize == recoveryRates.length, "Length of recoveryRates ({}) does not match index size ({})", recoveryRates.length, _indexSize);
    ArgumentChecker.isTrue(_indexSize == weights.length, "Length of weights ({}) does not match index size ({})", weights.length, _indexSize);
    // Correction made  PLAT-6323 
    //    ArgumentChecker.isTrue(_indexSize == defaulted.length(), "Length of defaulted ({}) does not match index size ({})", defaulted.length(), _indexSize);
    ArgumentChecker.isTrue(_indexSize >= defaulted.length(), "Length of defaulted ({}) is greater than index size ({})", defaulted.length(), _indexSize);

    _nDefaults = defaulted.cardinality();

    _lgd = new double[_indexSize];
    double sum = 0.0;
    for (int i = 0; i < _indexSize; i++) {
      if (creditCurves[i] == null && !defaulted.get(i)) {
        throw new IllegalArgumentException("Null credit curve, but not set as defaulted in alive list. Index is " + i);
      }
      if (weights[i] <= 0.0) {
        throw new IllegalArgumentException("weights must be positive. Value of " + weights[i] + " given at index " + i);
      }
      sum += weights[i];
      final double lgd = 1 - recoveryRates[i];
      if (lgd < 0.0 || lgd > 1.0) {
        throw new IllegalArgumentException("recovery rate must be between 0 and 1.Value of " + recoveryRates[i] + " given at index " + i);
      }
      _lgd[i] = lgd;
    }

    double f = 1.0;
    if (_nDefaults > 0) {
      for (int i = defaulted.nextSetBit(0); i >= 0; i = defaulted.nextSetBit(i + 1)) {
        f -= weights[i];
      }
    }
    _indexFactor = f;

    if (Math.abs(sum - 1.0) > TOL) {
      throw new IllegalArgumentException("weights do not sum to 1.0, but " + sum);
    }

    _weights = new double[_indexSize];
    System.arraycopy(weights, 0, _weights, 0, _indexSize);
    _creditCurves = creditCurves;
    _defaulted = defaulted;
  }

  private IntrinsicIndexDataBundle(final int indexSize, final int nDefaults, final double indexFactor, final double[] weights, final double[] lgd, final ISDACompliantCreditCurve[] creditCurves,
      final BitSet defaulted) {
    _indexSize = indexSize;
    _nDefaults = nDefaults;
    _indexFactor = indexFactor;
    _weights = weights;
    _lgd = lgd;
    _creditCurves = creditCurves;
    _defaulted = defaulted;
  }

  /**
   * Gets the (initial) index size 
   * @return the index size
   */
  public int getIndexSize() {
    return _indexSize;
  }

  /**
   * Gets the number of defaults the index has suffered
   * @return the number of defaults 
   */
  public int getNumOfDefaults() {
    return _nDefaults;
  }

  /**
   * Gets the weight of a particular name in the index. 
   * @param index The index of the constituent name 
   * @return The weight
   */
  public double getWeight(final int index) {
    return _weights[index];
  }

  /**
   * Gets the Loss-Given-Default (LGD) for a  particular name,
   * @param index The index of the constituent name 
   * @return The LGD
   */
  public double getLGD(final int index) {
    return _lgd[index];
  }

  /**
   * Gets the credit curve for a particular name,
   * * @param index The index of the constituent name 
   * @return a credit curve
   */
  public ISDACompliantCreditCurve getCreditCurve(final int index) {
    return _creditCurves[index];
  }

  public ISDACompliantCreditCurve[] getCreditCurves() {
    return _creditCurves;
  }

  /**
   * Get whether a particular name has defaulted 
   * @param index The index of the constituent name 
   * @return true if the name has defaulted 
   */
  public boolean isDefaulted(final int index) {
    return _defaulted.get(index);
  }

  /**
   * Get the index factor
   * @return the index factor 
   */
  public double getIndexFactor() {
    return _indexFactor;
  }

  /**
   * Replace the credit curves with a new set 
   * @param curves Credit curves. Must be the same length as the index size, and only null for defaulted names 
   * @return new IntrinsicIndexDataBundle with given curves 
   */
  public IntrinsicIndexDataBundle withCreditCurves(final ISDACompliantCreditCurve[] curves) {
    ArgumentChecker.notNull(curves, "curves");
    //  caught by notNull above
    //    if (_nDefaults == 0) {
    //      ArgumentChecker.noNulls(curves, "curves");
    //    }
    final int n = curves.length;
    ArgumentChecker.isTrue(n == _indexSize, "wrong number of curves. Require {}, but {} given", _indexSize, n);
    for (int i = 0; i < n; i++) {
      if (curves[i] == null && !_defaulted.get(i)) {
        throw new IllegalArgumentException("null curve at index " + i + ", but this is not listed as defaulted");
      }
    }

    return new IntrinsicIndexDataBundle(_indexSize, _nDefaults, _indexFactor, _weights, _lgd, curves, _defaulted);
  }

  /**
   * Produce a new data bundle with the name at the given index marked as defaulted. The number of defaults {@link #getNumOfDefaults} is incremented and the index factor 
   *  {@link #getIndexFactor} adjusted down - everything else remained unchanged. 
   * @param index The index of the name to set as defaulted. If this name is already marked as defaulted, an exception is thrown 
   * @return  new data bundle with the name at the given index marked as defaulted
   */
  public IntrinsicIndexDataBundle withDefault(final int index) {
    ArgumentChecker.isTrue(index < _indexSize, "index ({}) should be smaller than index size ({})", index, _indexSize);    //Added line PLAT-6324
    if (_defaulted.get(index)) {
      throw new IllegalArgumentException("Index " + index + " is already defaulted");
    }
    final BitSet defaulted = (BitSet) _defaulted.clone();
    defaulted.set(index);

    return new IntrinsicIndexDataBundle(_indexSize, _nDefaults + 1, _indexFactor - _weights[index], _weights, _lgd, _creditCurves, defaulted);
  }

  /**
  * Produce a new data bundle with the names at the given indices marked as defaulted. The number of defaults {@link #getNumOfDefaults} is incremented and the index factor 
   * {@link #getIndexFactor} adjusted down - everything else remained unchanged. 
   * @param index The indices of the names to set as defaulted. If any name is already marked as defaulted (or the list contains duplicates), an exception is thrown 
   * @return  new data bundle with the names at the given indices marked as defaulted
   */
  public IntrinsicIndexDataBundle withDefault(final int... index) {
    ArgumentChecker.notEmpty(index, "index");
    final BitSet defaulted = (BitSet) _defaulted.clone();
    final int n = index.length;
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      final int jj = index[i];
      ArgumentChecker.isTrue(jj < _indexSize, "index ({}) should be smaller than index size ({})", jj, _indexSize); //Added line PLAT-6324
      if (defaulted.get(jj)) {
        throw new IllegalArgumentException("Index " + jj + " is already defaulted");
      }
      defaulted.set(jj);
      sum += _weights[jj];
    }

    return new IntrinsicIndexDataBundle(_indexSize, _nDefaults + n, _indexFactor - sum, _weights, _lgd, _creditCurves, defaulted);
  }

}
