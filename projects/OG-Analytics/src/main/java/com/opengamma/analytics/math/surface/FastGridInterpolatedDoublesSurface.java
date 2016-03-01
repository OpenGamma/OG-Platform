/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.tuple.Pair;

// NOTE kirk 2016-03-01 -- This class is *INTENTIONALLY* not following current
// best practices for generalization or code structure. It is 100% optimized for
// performance in the case of running large numbers of historical simulations
// on option pricing. DO NOT attempt to make this class more stylistically in line
// with other code without carefully understanding and measuring the performance
// impact of what you're doing.
// It is *COMPLETELY INTENTIONAL* that there is not a single collection or
// primitive wrapper in this class. Do not add either.

/**
 * A fast implementation of {@code Surface} designed specifically for use in grid-based
 * volatilty surfaces that commonly come up in exchange traded derivatives contexts.
 * It is optimized for performance over generality, and avoids unnecessary data structure
 * copies or mutations.
 * It has a relatively expensive construction cost to allow fast interpolation
 * and transformation.
 */
public final class FastGridInterpolatedDoublesSurface extends Surface<Double, Double, Double> {
  private static final long serialVersionUID = 1L;
  private final int _size;
  private final double[] _xvalues;
  private final Interpolator1DDataBundle[] _yzBundles;
  private final Interpolator1D _xInterpolator;
  private final Interpolator1D _yInterpolator;
  
  private FastGridInterpolatedDoublesSurface(
      int size,
      double[] xvalues, Interpolator1DDataBundle[] yzBundles,
      Interpolator1D xInterpolator, Interpolator1D yInterpolator,
      String name) {
    super(name);
    // Intentionally we don't do ArgumentChecker here. It's done in the factory methods.
    _size = size;
    _xvalues = xvalues;
    _yzBundles = yzBundles;
    _xInterpolator = xInterpolator;
    _yInterpolator = yInterpolator;
  }      
  
  /**
   * Factory method for constructing a new instance using primitive inputs.
   * This method will not copy the inputs, but will sort them in place. Therefore
   * calling code should release control over the arrays.
   * 
   * @param xdata X values in triplet form.
   * @param ydata Y values in triplet form.
   * @param zdata Z values in triplet form.
   * @param xInterpolator X interpolator to use
   * @param yInterpolator Y interpolator to use
   * @param name Curve name
   * @return a new surface instance
   */
  public static FastGridInterpolatedDoublesSurface fromUnsortedNoCopy(
      double[] xdata, double[] ydata, double[] zdata,
      Interpolator1D xInterpolator, Interpolator1D yInterpolator,
      String name) {
    ArgumentChecker.notNull(xdata, "xdata");
    ArgumentChecker.notNull(ydata, "ydata");
    ArgumentChecker.notNull(zdata, "zdata");
    ArgumentChecker.notNull(xInterpolator, "xInterpolator");
    ArgumentChecker.notNull(yInterpolator, "yInterpolator");
    ArgumentChecker.isTrue(xdata.length > 0, "Must have non-zero size");
    ArgumentChecker.isTrue(xdata.length == ydata.length, "xdata and ydata must be same size");
    ArgumentChecker.isTrue(xdata.length == zdata.length, "xdata and zdata must be same size");
    
    // NOTE -- This method would be so much easier with built-in tuples.....
    
    // First, order xyz triplet by x
    ParallelArrayBinarySort.parallelBinarySort(xdata, ydata, zdata);
    
    // First pass through xdata: just check the cardinality of x and yz data sets.
    // Start the loop intentionally at 1 for clarity as we know that there's at least
    // one value and we get the first x value outside the loop.
    double currentX = xdata[0];
    int numXValues = 1;
    for (int i = 1; i < xdata.length; i++) {
      if (Double.doubleToRawLongBits(currentX) != Double.doubleToRawLongBits(xdata[i])) {
        numXValues++;
        currentX = xdata[i];
      }
    }
    
    // Now we know the cardinality of xdata, we need to determine the cardinality
    // of each yz set.
    int[] numYZValues = new int[numXValues];
    int yzValuesInThisLoop = 0;
    int xOrdinal = 0;
    currentX = xdata[0];
    for (int i = 0; i < xdata.length; i++) {
      if (Double.doubleToRawLongBits(currentX) != Double.doubleToRawLongBits(xdata[i])) {
        numYZValues[xOrdinal] = yzValuesInThisLoop;
        xOrdinal++;
        yzValuesInThisLoop = 0;
        currentX = xdata[i];
      }
      yzValuesInThisLoop++;
    }
    numYZValues[xOrdinal] = yzValuesInThisLoop;
    
    // Second pass: actually build the yz arrays.
    // The logic by looping on n makes the logic simpler than checking for
    // changes in the xvalue and should make for better inlining and codegen.
    Interpolator1DDataBundle[] bundles = new Interpolator1DDataBundle[numXValues];
    double[] xvalues = new double[numXValues];
    int indexIntoInputArrays = 0;
    for (int i = 0; i < numXValues; i++) {
      xvalues[i] = xdata[indexIntoInputArrays]; 
      double[] ybundle = new double[numYZValues[i]];
      double[] zbundle = new double[numYZValues[i]];
      for (int j = 0; j < numYZValues[i]; j++) {
        ybundle[j] = ydata[indexIntoInputArrays];
        zbundle[j] = zdata[indexIntoInputArrays];
        indexIntoInputArrays++;
      }
      // We know yz is unsorted at this stage. But there's no need to copy because
      // we're dropping the reference at the end of this.
      bundles[i] = new ArrayInterpolator1DDataBundle(ybundle, zbundle, false, false);
    }

    assert xvalues.length == bundles.length;
    return new FastGridInterpolatedDoublesSurface(xdata.length, xvalues, bundles, xInterpolator, yInterpolator, name);
  }
  
  /**
   * Duplicate this surface, but multiply every Z value by a particular factor.
   * 
   * @param factor number to multiply every Z value by
   * @return A duplicate surface with modified Z values
   */
  public FastGridInterpolatedDoublesSurface withMultiplicativeZTransformation(double factor) {
    
    Interpolator1DDataBundle[] yzBundles = new Interpolator1DDataBundle[_yzBundles.length]; 
    for (int i = 0; i < _xvalues.length; i++) {
      double[] oldZValues = _yzBundles[i].getValues();
      double[] newZValues = new double[oldZValues.length];
      for (int j = 0; j < oldZValues.length; j++) {
        newZValues[j] = oldZValues[j] * factor;
      }
      // yz is still sorted from the constructor. So don't need to sort or copy.
      yzBundles[i] = new ArrayInterpolator1DDataBundle(_yzBundles[i].getKeys(), newZValues, true, false);
    }
    
    return new FastGridInterpolatedDoublesSurface(_size, _xvalues, yzBundles, _xInterpolator, _yInterpolator, getName());
  }
  
  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("This method should never have been added to Surface<?,?,?>");
  }

  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("This method should never have been added to Surface<?,?,?>");
  }

  @Override
  public Double[] getZData() {
    throw new UnsupportedOperationException("This method should never have been added to Surface<?,?,?>");
  }

  @Override
  public int size() {
    return _size;
  }

  @Override
  public Double getZValue(Double x, Double y) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notNull(y, "y");
    
    double[] yzInterpolated = new double[_xvalues.length];
    for (int i = 0; i < _xvalues.length; i++) {
      yzInterpolated[i] = _yInterpolator.interpolate(_yzBundles[i], y);
    }
    // This is correct. We know the inputs are sorted, and because all arrays
    // are either immutable and owned by this instance or are created inside
    // this method no need to copy for performance.
    Interpolator1DDataBundle xzBundle = new ArrayInterpolator1DDataBundle(_xvalues, yzInterpolated, true, false);
    Double result = _xInterpolator.interpolate(xzBundle, x);
    return result;
  }

  @Override
  public Double getZValue(Pair<Double, Double> xy) {
    return getZValue(xy.getFirst(), xy.getSecond());
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("You have no reason to put this into a hashing data structure.");
  }

  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException("You have no reason to compare this instance with another.");
  }

}
