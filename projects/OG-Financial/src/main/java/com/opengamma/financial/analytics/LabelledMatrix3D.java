/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.QuickSorter.ArrayQuickSorter;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents a 3D labeled matrix. The dimensions are named X, Y, Z - values[Z][Y][X].
 * 
 * @param <KX> Key type for the X dimension
 * @param <KY> Key type for the Y dimension
 * @param <KZ> Key type for the Z dimension
 * @param <TX> Tolerance type for X key comparisons
 * @param <TY> Tolerance type for Y key comparisons
 * @param <TZ> Tolerance type for Z key comparisons
 * @param <SUBCLASS> Instantiating sub-class
 */
public abstract class LabelledMatrix3D<KX, KY, KZ, TX, TY, TZ, SUBCLASS> {

  private final KX[] _xKeys;
  private final Object[] _xLabels;
  private final KY[] _yKeys;
  private final Object[] _yLabels;
  private final KZ[] _zKeys;
  private final Object[] _zLabels;
  private final double[][][] _values;

  /**
   * Creates a new 3D labeled matrix. The labels are the {@link Object#toString} forms of the keys.
   * 
   * @param xKeys keys for the X dimension
   * @param yKeys keys for the Y dimension
   * @param zKeys keys for the Z dimension
   * @param values values of the matrix in the shape [Z][Y][X]
   */
  public LabelledMatrix3D(final KX[] xKeys, final KY[] yKeys, final KZ[] zKeys, final double[][][] values) {
    this(xKeys, LabelledMatrixUtils.toString(xKeys), yKeys, LabelledMatrixUtils.toString(yKeys), zKeys, LabelledMatrixUtils.toString(zKeys), values);
  }

  /**
   * Creates a new 3D labeled matrix.
   * 
   * @param xKeys keys for the X dimension
   * @param xLabels labels for the X dimension
   * @param yKeys keys for the Y dimension
   * @param yLabels labels for the Y dimension
   * @param zKeys keys for the Z dimension
   * @param zLabels labels for the Z dimension
   * @param values values of the matrix in the shape [Z][Y][X]
   */
  public LabelledMatrix3D(final KX[] xKeys, final Object[] xLabels, final KY[] yKeys, final Object[] yLabels, final KZ[] zKeys, final Object[] zLabels, final double[][][] values) {
    ArgumentChecker.notNull(xKeys, "xKeys");
    ArgumentChecker.notNull(xLabels, "xLabels");
    ArgumentChecker.notNull(yKeys, "yKeys");
    ArgumentChecker.notNull(yLabels, "yLabels");
    ArgumentChecker.notNull(zKeys, "zKeys");
    ArgumentChecker.notNull(zLabels, "zLabels");
    ArgumentChecker.notNull(values, "values");
    final int x = xKeys.length;
    Validate.isTrue(xLabels.length == x, "invalid xLabels length");
    final int y = yKeys.length;
    Validate.isTrue(yLabels.length == y, "invalid yLabels length");
    final int z = zKeys.length;
    Validate.isTrue(zLabels.length == z, "invalid zLabels length");
    Validate.isTrue(values.length == z, "invalid zKeys length");
    _xKeys = Arrays.copyOf(xKeys, x);
    _xLabels = Arrays.copyOf(xLabels, x);
    _yKeys = Arrays.copyOf(yKeys, y);
    _yLabels = Arrays.copyOf(yLabels, y);
    _zKeys = Arrays.copyOf(zKeys, z);
    _zLabels = Arrays.copyOf(zLabels, z);
    _values = new double[z][y][x];
    for (int iz = 0; iz < z; iz++) {
      Validate.isTrue(values[iz].length == y, "invalid yKeys length");
      for (int iy = 0; iy < y; iy++) {
        Validate.isTrue(values[iz][iy].length == x, "invalid xKeys length");
        System.arraycopy(values[iz][iy], 0, _values[iz][iy], 0, x);
      }
    }
    quickSortX();
    quickSortY();
    quickSortZ();
  }

  /**
   * Creates a new labeled matrix instance. This is called at the end of operations so that the sub-class can return an object
   * of its own type.
   * 
   * @param xKeys keys for the X dimension of the new matrix
   * @param xLabels labels for the X dimension of the new matrix
   * @param yKeys keys for the Y dimension of the new matrix
   * @param yLabels labels for the Y dimension of the new matrix
   * @param zKeys keys for the Z dimension of the new matrix
   * @param zLabels labels for the Z dimension of the new matrix
   * @param values values of the new matrix in the shape [Z][Y][X]
   * @return the new matrix
   */
  protected abstract SUBCLASS create(final KX[] xKeys, final Object[] xLabels, final KY[] yKeys, final Object[] yLabels, final KZ[] zKeys, final Object[] zLabels,
      final double[][][] values);

  private void quickSortX() {
    (new ArrayQuickSorter<KX>(_xKeys) {

      private final TX _tolerance = getDefaultToleranceX();

      @Override
      protected int compare(final KX first, final KX second) {
        return compareKeysX(first, second, _tolerance);
      }

      @Override
      protected void swap(final int first, final int second) {
        super.swap(first, second);
        swap(_xLabels, first, second);
        for (int iz = 0; iz < _zKeys.length; iz++) {
          for (int iy = 0; iy < _yKeys.length; iy++) {
            swap(_values[iz][iy], first, second);
          }
        }
      }

    }).sort();
  }

  private void quickSortY() {
    (new ArrayQuickSorter<KY>(_yKeys) {

      private final TY _tolerance = getDefaultToleranceY();

      @Override
      protected int compare(final KY first, final KY second) {
        return compareKeysY(first, second, _tolerance);
      }

      @Override
      protected void swap(final int first, final int second) {
        super.swap(first, second);
        swap(_yLabels, first, second);
        for (int iz = 0; iz < _zKeys.length; iz++) {
          swap(_values[iz], first, second);
        }
      }

    }).sort();
  }

  private void quickSortZ() {
    (new ArrayQuickSorter<KZ>(_zKeys) {

      private final TZ _tolerance = getDefaultToleranceZ();

      @Override
      protected int compare(final KZ first, final KZ second) {
        return compareKeysZ(first, second, _tolerance);
      }

      @Override
      protected void swap(final int first, final int second) {
        super.swap(first, second);
        swap(_zLabels, first, second);
        swap(_values, first, second);
      }

    }).sort();
  }

  /**
   * Returns the default tolerance value for comparison of X dimension keys. This will be used for the initial sort
   * of a matrix, or if a tolerance is not specified for the other operations. It may be null if the
   * {@link #compareKeysX} method accepts it (i.e. ignores it).
   * 
   * @return the default tolerance for X key comparisons
   */
  public TX getDefaultToleranceX() {
    return null;
  }

  /**
   * Returns the default tolerance value for comparison of Y dimension keys. This will be used for the initial sort
   * of a matrix, or if a tolerance is not specified for the other operations. It may be null if the
   * {@link #compareKeysY} method accepts it (i.e. ignores it).
   * 
   * @return the default tolerance for Y key comparisons
   */
  public TY getDefaultToleranceY() {
    return null;
  }

  /**
   * Returns the default tolerance value for comparison of Z dimension keys. This will be used for the initial sort
   * of a matrix, or if a tolerance is not specified for the other operations. It may be null if the
   * {@link #compareKeysZ} method accepts it (i.e. ignores it).
   * 
   * @return the default tolerance for Z key comparisons
   */
  public TZ getDefaultToleranceZ() {
    return null;
  }

  /**
   * Compares two X dimension keys.
   * 
   * @param key1 first key to compare
   * @param key2 second key to compare
   * @param tolerance comparison tolerance
   * @return negative if the first key is before the second, positive if after, zero if equal (given the tolerance)  
   */
  protected abstract int compareKeysX(final KX key1, final KX key2, final TX tolerance);

  /**
   * Returns a {@link Comparator} wrapping the {@link #compareKeysX} method for a given tolerance.
   * 
   * @param tolerance comparison tolerance
   * @return a comparator
   */
  protected Comparator<KX> compareKeysX(final TX tolerance) {
    return new Comparator<KX>() {
      @Override
      public int compare(final KX key1, final KX key2) {
        return compareKeysX(key1, key2, tolerance);
      }
    };
  }

  /**
   * Compares two Y dimension keys.
   * 
   * @param key1 first key to compare
   * @param key2 second key to compare
   * @param tolerance comparison tolerance
   * @return negative if the first key is before the second, positive if after, zero if equal (given the tolerance)  
   */
  protected abstract int compareKeysY(final KY key1, final KY key2, final TY tolerance);

  /**
   * Returns a {@link Comparator} wrapping the {@link #compareKeysY} method for a given tolerance.
   * 
   * @param tolerance comparison tolerance
   * @return a comparator
   */
  protected Comparator<KY> compareKeysY(final TY tolerance) {
    return new Comparator<KY>() {
      @Override
      public int compare(final KY key1, final KY key2) {
        return compareKeysY(key1, key2, tolerance);
      }
    };
  }

  /**
   * Compares two Z dimension keys.
   * 
   * @param key1 first key to compare
   * @param key2 second key to compare
   * @param tolerance comparison tolerance
   * @return negative if the first key is before the second, positive if after, zero if equal (given the tolerance)  
   */
  protected abstract int compareKeysZ(final KZ key1, final KZ key2, final TZ tolerance);

  /**
   * Returns a {@link Comparator} wrapping the {@link #compareKeysZ} method for a given tolerance.
   * 
   * @param tolerance comparison tolerance
   * @return a comparator
   */
  protected Comparator<KZ> compareKeysZ(final TZ tolerance) {
    return new Comparator<KZ>() {
      @Override
      public int compare(final KZ key1, final KZ key2) {
        return compareKeysZ(key1, key2, tolerance);
      }
    };
  }

  /**
   * Adds a labeled matrix to this one to create a new matrix.
   * <p>
   * Each key triple in the other matrix is checked to see if it is in the current; if so, the value for that triple is added. If the key triple is
   * not present, the new key triple, labels and value are attached to the matrix. This method ignores the label - if there is a key already present
   * but the labels do not match, then the new label is the original.
   * 
   * @param other Another labeled matrix
   * @param xTolerance tolerance for detecting a match on the X keys
   * @param yTolerance tolerance for detecting a match on the Y keys
   * @param zTolerance tolerance for detecting a match on the Z keys
   * @return The sum of the matrices
   */
  public SUBCLASS addIgnoringLabel(final LabelledMatrix3D<KX, KY, KZ, TX, TY, TZ, ?> other, final TX xTolerance, final TY yTolerance, final TZ zTolerance) {
    return addImpl(other, xTolerance, yTolerance, zTolerance, true);
  }

  /**
   * Adds a labeled matrix to this one to create a new matrix.
   * <p>
   * Each key triple in the other matrix is checked to see if it is in the current; if so, the value for that triple is added. If the key triple is
   * not present, the new key triple, labels and value are attached to the matrix. This method ignores the label - if there is a key already present
   * but the labels do not match, then the new label is the original.
   * 
   * @param other Another labeled matrix
   * @return The sum of the matrices
   */
  public SUBCLASS addIgnoringLabel(final LabelledMatrix3D<KX, KY, KZ, TX, TY, TZ, ?> other) {
    return addIgnoringLabel(other, getDefaultToleranceX(), getDefaultToleranceY(), getDefaultToleranceZ());
  }

  /**
   * Adds a labeled matrix to this one to create a new matrix.
   * <p>
   * Each key triple in the other matrix is checked to see if it is in the current; if so, the value for that triple is added. If the key triple is
   * not present, the new key triple, labels and value are attached to the matrix. If the key labels on the two matrices differ, an exception is thrown.
   * 
   * @param other Another labeled matrix
   * @param xTolerance tolerance for detecting a match on the X keys
   * @param yTolerance tolerance for detecting a match on the Y keys
   * @param zTolerance tolerance for detecting a match on the Z keys
   * @return The sum of the matrices
   */
  public SUBCLASS add(final LabelledMatrix3D<KX, KY, KZ, TX, TY, TZ, ?> other, final TX xTolerance, final TY yTolerance, final TZ zTolerance) {
    return addImpl(other, xTolerance, yTolerance, zTolerance, false);
  }

  /**
   * Adds a labeled matrix to this one to create a new matrix.
   * <p>
   * Each key triple in the other matrix is checked to see if it is in the current; if so, the value for that triple is added. If the key triple is
   * not present, the new key triple, labels and value are attached to the matrix. If the key labels on the two matrices differ, an exception is thrown.
   * 
   * @param other Another labeled matrix
   * @return The sum of the matrices
   */
  public SUBCLASS add(final LabelledMatrix3D<KX, KY, KZ, TX, TY, TZ, ?> other) {
    return add(other, getDefaultToleranceX(), getDefaultToleranceY(), getDefaultToleranceZ());
  }

  protected SUBCLASS addImpl(final LabelledMatrix3D<KX, KY, KZ, TX, TY, TZ, ?> other, final TX xTolerance, final TY yTolerance, final TZ zTolerance,
      final boolean ignoreLabels) {
    Validate.notNull(other, "other");
    // Check the new X keys and labels
    int iThis = 0, iOther = 0;
    ObjectArrayList<KX> newXKeys = null;
    ObjectArrayList<Object> newXLabels = null;
    final KX[] otherXKeys = other.getXKeys();
    final Object[] otherXLabels = other.getXLabels();
    final int[] otherXIndex = new int[otherXKeys.length];
    while ((iThis < _xKeys.length) && (iOther < otherXKeys.length)) {
      final int cmp = compareKeysX(_xKeys[iThis], otherXKeys[iOther], xTolerance);
      if (cmp < 0) {
        iThis++;
      } else if (cmp > 0) {
        if (newXKeys == null) {
          newXKeys = ObjectArrayList.wrap(_xKeys);
          newXLabels = ObjectArrayList.wrap(_xLabels);
        }
        otherXIndex[iOther] = newXKeys.size();
        newXKeys.add(otherXKeys[iOther]);
        newXLabels.add(otherXLabels[iOther]);
        iOther++;
      } else {
        if (!ignoreLabels && !ObjectUtils.equals(_xLabels[iThis], otherXLabels[iOther])) {
          throw new IllegalArgumentException("Label mismatch for X key " + _xKeys[iThis] + " - " + _xLabels[iThis] + " vs " + otherXLabels[iOther]);
        }
        otherXIndex[iOther] = iThis;
        iThis++;
        iOther++;
      }
    }
    if (iOther < otherXKeys.length) {
      if (newXKeys == null) {
        newXKeys = ObjectArrayList.wrap(_xKeys);
        newXLabels = ObjectArrayList.wrap(_xLabels);
      }
      do {
        otherXIndex[iOther] = newXKeys.size();
        newXKeys.add(otherXKeys[iOther]);
        newXLabels.add(otherXLabels[iOther]);
        iOther++;
      } while (iOther < otherXKeys.length);
    }
    // Check the new Y keys and labels
    iThis = 0;
    iOther = 0;
    ObjectArrayList<KY> newYKeys = null;
    ObjectArrayList<Object> newYLabels = null;
    final KY[] otherYKeys = other.getYKeys();
    final Object[] otherYLabels = other.getYLabels();
    final int[] otherYIndex = new int[otherYKeys.length];
    while ((iThis < _yKeys.length) && (iOther < otherYKeys.length)) {
      final int cmp = compareKeysY(_yKeys[iThis], otherYKeys[iOther], yTolerance);
      if (cmp < 0) {
        iThis++;
      } else if (cmp > 0) {
        if (newYKeys == null) {
          newYKeys = ObjectArrayList.wrap(_yKeys);
          newYLabels = ObjectArrayList.wrap(_yLabels);
        }
        otherYIndex[iOther] = newYKeys.size();
        newYKeys.add(otherYKeys[iOther]);
        newYLabels.add(otherYLabels[iOther]);
        iOther++;
      } else {
        if (!ignoreLabels && !ObjectUtils.equals(_yLabels[iThis], otherYLabels[iOther])) {
          throw new IllegalArgumentException("Label mismatch for Y key " + _yKeys[iThis] + " - " + _yLabels[iThis] + " vs " + otherYLabels[iOther]);
        }
        otherYIndex[iOther] = iThis;
        iThis++;
        iOther++;
      }
    }
    if (iOther < otherYKeys.length) {
      if (newYKeys == null) {
        newYKeys = ObjectArrayList.wrap(_yKeys);
        newYLabels = ObjectArrayList.wrap(_yLabels);
      }
      do {
        otherYIndex[iOther] = newYKeys.size();
        newYKeys.add(otherYKeys[iOther]);
        newYLabels.add(otherYLabels[iOther]);
        iOther++;
      } while (iOther < otherYKeys.length);
    }
    // Check the new Z keys and labels
    iThis = 0;
    iOther = 0;
    ObjectArrayList<KZ> newZKeys = null;
    ObjectArrayList<Object> newZLabels = null;
    final KZ[] otherZKeys = other.getZKeys();
    final Object[] otherZLabels = other.getZLabels();
    final int[] otherZIndex = new int[otherZKeys.length];
    while ((iThis < _zKeys.length) && (iOther < otherZKeys.length)) {
      final int cmp = compareKeysZ(_zKeys[iThis], otherZKeys[iOther], zTolerance);
      if (cmp < 0) {
        iThis++;
      } else if (cmp > 0) {
        if (newZKeys == null) {
          newZKeys = ObjectArrayList.wrap(_zKeys);
          newZLabels = ObjectArrayList.wrap(_zLabels);
        }
        otherZIndex[iOther] = newZKeys.size();
        newZKeys.add(otherZKeys[iOther]);
        newZLabels.add(otherZLabels[iOther]);
        iOther++;
      } else {
        if (!ignoreLabels && !ObjectUtils.equals(_zLabels[iThis], otherZLabels[iOther])) {
          throw new IllegalArgumentException("Label mismatch for Z key " + _zKeys[iThis] + " - " + _zLabels[iThis] + " vs " + otherZLabels[iOther]);
        }
        otherZIndex[iOther] = iThis;
        iThis++;
        iOther++;
      }
    }
    if (iOther < otherZKeys.length) {
      if (newZKeys == null) {
        newZKeys = ObjectArrayList.wrap(_zKeys);
        newZLabels = ObjectArrayList.wrap(_zLabels);
      }
      do {
        otherZIndex[iOther] = newZKeys.size();
        newZKeys.add(otherZKeys[iOther]);
        newZLabels.add(otherZLabels[iOther]);
        iOther++;
      } while (iOther < otherZKeys.length);
    }
    // Build the new matrix
    final KX[] xKeys = (newXKeys != null) ? newXKeys.toArray(_xKeys) : _xKeys;
    final Object[] xLabels = (newXLabels != null) ? newXLabels.toArray(_xLabels) : _xLabels;
    final KY[] yKeys = (newYKeys != null) ? newYKeys.toArray(_yKeys) : _yKeys;
    final Object[] yLabels = (newYLabels != null) ? newYLabels.toArray(_yLabels) : _yLabels;
    final KZ[] zKeys = (newZKeys != null) ? newZKeys.toArray(_zKeys) : _zKeys;
    final Object[] zLabels = (newZLabels != null) ? newZLabels.toArray(_zLabels) : _zLabels;
    final double[][][] values = new double[zKeys.length][yKeys.length][xKeys.length];
    for (int z = 0; z < _values.length; z++) {
      for (int y = 0; y < _values[z].length; y++) {
        System.arraycopy(_values[z][y], 0, values[z][y], 0, _values[z][y].length);
      }
    }
    final double[][][] otherValues = other.getValues();
    for (int z = 0; z < otherValues.length; z++) {
      final int iz = otherZIndex[z];
      for (int y = 0; y < otherValues[z].length; y++) {
        final int iy = otherYIndex[y];
        for (int x = 0; x < otherValues[z][y].length; x++) {
          final int ix = otherXIndex[x];
          values[iz][iy][ix] += otherValues[z][y][x];
        }
      }
    }
    return create(xKeys, xLabels, yKeys, yLabels, zKeys, zLabels, values);
  }

  /**
   * Returns the keys for the X dimension.
   * 
   * @return X keys
   */
  public KX[] getXKeys() {
    return _xKeys;
  }

  /**
   * Returns the labels for the X dimension.
   * 
   * @return X labels
   */
  public Object[] getXLabels() {
    return _xLabels;
  }

  /**
   * Returns the keys for the Y dimension.
   * 
   * @return Y keys
   */
  public KY[] getYKeys() {
    return _yKeys;
  }

  /**
   * Returns the labels for the Y dimension.
   * 
   * @return Y labels
   */
  public Object[] getYLabels() {
    return _yLabels;
  }

  /**
   * Returns the keys for the Z dimension.
   * 
   * @return Z keys
   */
  public KZ[] getZKeys() {
    return _zKeys;
  }

  /**
   * Returns the labels for the Z dimension.
   * 
   * @return Z labels
   */
  public Object[] getZLabels() {
    return _zLabels;
  }

  /**
   * Returns the values of the matrix.
   * 
   * @return the matrix values
   */
  public double[][][] getValues() {
    return _values;
  }

  @Override
  public int hashCode() {
    int hc = 1;
    hc += (hc << 4) + Arrays.hashCode(_values);
    hc += (hc << 4) + Arrays.hashCode(_xKeys);
    hc += (hc << 4) + Arrays.hashCode(_xLabels);
    hc += (hc << 4) + Arrays.hashCode(_yKeys);
    hc += (hc << 4) + Arrays.hashCode(_yLabels);
    hc += (hc << 4) + Arrays.hashCode(_zKeys);
    hc += (hc << 4) + Arrays.hashCode(_zLabels);
    return hc;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LabelledMatrix3D)) {
      return false;
    }
    final LabelledMatrix3D other = (LabelledMatrix3D) obj;
    return Arrays.deepEquals(_values, other._values)
        && Arrays.equals(_xKeys, other._xKeys)
        && Arrays.equals(_xLabels, other._xLabels)
        && Arrays.equals(_yKeys, other._yKeys)
        && Arrays.equals(_yLabels, other._yLabels)
        && Arrays.equals(_zKeys, other._zKeys)
        && Arrays.equals(_zLabels, other._zLabels);
  }
}
