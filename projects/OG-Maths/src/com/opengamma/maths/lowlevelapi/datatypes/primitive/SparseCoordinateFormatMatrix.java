/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.functions.utilities.Find;
import com.opengamma.maths.lowlevelapi.functions.utilities.Max;
import com.opengamma.maths.lowlevelapi.functions.utilities.Permute;
import com.opengamma.maths.lowlevelapi.functions.utilities.Sort;
import com.opengamma.maths.lowlevelapi.functions.utilities.Unique;
import com.opengamma.maths.lowlevelapi.functions.utilities.View;
import com.opengamma.maths.lowlevelapi.functions.utilities.Find.condition;
import com.opengamma.maths.lowlevelapi.functions.utilities.Sort.direction;

/**
 * Converts or instantiates a matrix to Sparse Coordinate Format (COO). COO is a non optimal method of storing sparse matrix data and is generally used as
 * an intermediary storage format prior to conversion to @see {@link CompressedSparseRowFormatMatrix} or {@link CompressedSparseColumnFormatMatrix}.
 * Only the non-zero components of the matrix are stored (note: there is no tolerance for testing zero to machine precision, zero is solely tested bitwise).
 * The COO format requires three vectors:
 * (x,y): form a coordinate pairing to give the location of an element within the sparse matrix
 * values: contains the values at coordinate pairings (x,y)
 */

public class SparseCoordinateFormatMatrix extends SparseMatrixType {
  private double[] _values;
  private int[] _x;
  private int[] _y;
  private int _els;
  private int _rows;
  private int _cols;
  private int _maxEntriesInARow;

  // constructors
  /**
   * Construct from DoubleMatrix2D type
   * @param aMatrix is a DoubleMatrix2D
   */
  public SparseCoordinateFormatMatrix(final DoubleMatrix2D aMatrix) {
    Validate.notNull(aMatrix);

    //get number of elements
    _els = aMatrix.getNumberOfElements();

    // tmp arrays, in case we get in a fully populated matrix, intelligent design upstream should ensure that this is overkill!
    double[] valuesTmp = new double[_els];
    int[] xTmp = new int[_els];
    int[] yTmp = new int[_els];

    // we need unwind the array aMatrix into coordinate form
    int localmaxEntriesInARow;
    _maxEntriesInARow = -1; // set max entries in a column negative, so that maximiser will work
    int ptr = 0;
    for (int i = 0; i < aMatrix.getNumberOfRows(); i++) {
      localmaxEntriesInARow = 0;
      for (int j = 0; j < aMatrix.getNumberOfColumns(); j++) {
        if (Double.doubleToLongBits(aMatrix.getEntry(i, j)) != 0L) {
          xTmp[ptr] = j;
          yTmp[ptr] = i;
          valuesTmp[ptr] = aMatrix.getEntry(i, j);
          ptr++;
          localmaxEntriesInARow++;
        }
      }
      if (localmaxEntriesInARow > _maxEntriesInARow) {
        _maxEntriesInARow = localmaxEntriesInARow;
      }
    }

    _values = Arrays.copyOfRange(valuesTmp, 0, ptr);
    _x = Arrays.copyOfRange(xTmp, 0, ptr);
    _y = Arrays.copyOfRange(yTmp, 0, ptr);
    _rows = aMatrix.getNumberOfRows();
    _cols = aMatrix.getNumberOfColumns();
  }

  /**
   * Construct from double[][] type
   *  @param aMatrix is an array of arrays describing a matrix
   */
  public SparseCoordinateFormatMatrix(final double[][] aMatrix) {
    // for now, convert to DoubleMatrix2D and use it's constructor
    this(new DoubleMatrix2D(aMatrix));
  }

  /**
   * Construct from vectors of coordinates with corresponding values
   * @param x x-coordinates of data points (column number)
   * @param y y-coordinates of data points (row number)
   * @param values value of data points (value)
   * @param m the number of rows to implement in the instantiated matrix
   * @param n the number of columns to implement in the instantiated matrix
   */
  public SparseCoordinateFormatMatrix(int[] x, int[] y, double[] values, int m, int n) {
    Validate.notNull(x);
    Validate.notNull(y);
    Validate.notNull(values);
    Validate.isTrue(x.length == y.length, "Vector lengths do not match, therefore one to one coordinate pairings do not exist");
    Validate.isTrue(x.length == values.length, "Vector lengths do not match, therefore one to one coordinate pairings do not exist");
    Validate.isTrue(m >= 1, "m (number of rows) must be greater than or equal to one");
    Validate.isTrue(n >= 1, "n (number of columns) must be greater than or equal to one");
    Validate.isTrue(Max.value(y) <= m - 1, "Number of rows requested (m) is less than the most positive row coordinate");
    Validate.isTrue(Max.value(x) <= n - 1, "Number of columns requested (n) is less than the most positive column coordinate");
    _els = x.length;
    // twiddle rows into ascending order
    int[] rowPerm = Sort.getIndex(y);
    int[] localRSx = Permute.stateless(x, rowPerm); // local row sorted X
    int[] localRSy = Permute.stateless(y, rowPerm); // local row sorted Y
    double[] localRSv = Permute.stateless(values, rowPerm); // local row sorted values

    // temp storage
    int[] yTmp = new int[_els];
    int[] xTmp = new int[_els];
    double[] valuesTmp = new double[_els];

    // unique rows and sort (should be sorted anyway)
    int[] yU = Unique.bitwise(localRSy); // unique row indexes
    Sort.valuesInplace(yU, direction.ascend);

    // walk vectors permute as needed assign to tmp Storage
    _maxEntriesInARow = -1;
    int ptr = 0;
    for (int i = 0; i < yU.length; i++) {

      int[] indexesOfXOnThisRow = Find.indexes(localRSy, condition.eq, yU[i]);

      int[] xlocaltmp = View.byIndex(localRSx, indexesOfXOnThisRow);
      int[] xlocaltmpsortedindex = Sort.getIndex(xlocaltmp);
      xlocaltmp = Permute.stateless(xlocaltmp, xlocaltmpsortedindex);
      double[] vlocaltmp = Permute.stateless(View.byIndex(localRSv, indexesOfXOnThisRow), xlocaltmpsortedindex);
      Validate.notNull(indexesOfXOnThisRow, "Y coordinate given with no corresponding X coordinate, this should never happen");
      for (int j = 0; j < indexesOfXOnThisRow.length; j++) {
        yTmp[ptr] = yU[i];
        xTmp[ptr] = xlocaltmp[j];
        valuesTmp[ptr] = vlocaltmp[j];
        ptr++;
      }
      if (indexesOfXOnThisRow.length > _maxEntriesInARow) {
        _maxEntriesInARow = indexesOfXOnThisRow.length;
      }
    }

    _values = Arrays.copyOfRange(valuesTmp, 0, ptr);
    _x = Arrays.copyOfRange(xTmp, 0, ptr);
    _y = Arrays.copyOfRange(yTmp, 0, ptr);
    _rows = m;
    _cols = n;

  }

  // here we shall shove in constructors for just about all types (eventually).

  // Methods
  /**
   * Returns the "coordinates" of the columns where there are nonzero entries
   * @return _x, the column coordinates
   */
  public int[] getColumnCoordinates() {
    return _x;
  }

  /**
   * Returns the "coordinates" of the rows where there are nonzero entries
   * @return _y, the row coordinates
   */
  public int[] getRowCoordinates() {
    return _y;
  }

  /**
   * Returns the nonzero entries
   * @return _values, the values
   */
  public double[] getNonZeroEntries() {
    return _values;
  }

  /**
   * Returns the number of rows corresponding to the full matrix representation
   * @return _rows, the number of rows in the matrix
   */
  @Override
  public int getNumberOfRows() {
    return _rows;
  }

  /**
   * Returns the number of columns corresponding to the full matrix representation
   * @return _cols, the number of columns in the matrix
   */
  @Override
  public int getNumberOfColumns() {
    return _cols;
  }

  /**
   * Converts COO to array of arrays
   * @return tmp, an array of arrays corresponding to the full matrix representation
   */
  @Override
  public double[][] toArray() {
    double[][] tmp = new double[_rows][_cols];
    for (int i = 0; i < _x.length; i++) {
      tmp[_y[i]][_x[i]] = _values[i];
    }
    return tmp;
  }

  /**
   * Converts COO to DoubleMatrix2D
   * @return tmp, a DoubleMatrix2D corresponding to the full matrix representation
   */
  public DoubleMatrix2D toDenseMatrix() {
    return new DoubleMatrix2D(this.toArray());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getFullRow(int index) {
    double[] tmp = new double[_cols];
    // brute force
    for (int i = 0; i < _y.length; i++) {
      if (_y[i] == index) {
        tmp[_x[i]] = _values[i];
      }
    }
    return tmp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getFullColumn(int index) {
    double[] tmp = new double[_rows];
    // brute force
    for (int i = 0; i < _y.length; i++) {
      if (_x[i] == index) {
        tmp[_y[i]] = _values[i];
      }
    }
    return tmp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getRowElements(int index) {
    double[] tmp = new double[_cols]; //overkill
    int ptr = 0;
    // brute force
    for (int i = 0; i < _y.length; i++) {
      if (_y[i] == index) {
        tmp[ptr] = _values[i];
        ptr++;
      }
    }
    return Arrays.copyOfRange(tmp, 0, ptr);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getColumnElements(int index) {
    double[] tmp = new double[_rows]; //overkill
    int ptr = 0;
    // brute force
    for (int i = 0; i < _y.length; i++) {
      if (_x[i] == index) {
        tmp[ptr] = _values[i];
        ptr++;
      }
    }
    return Arrays.copyOfRange(tmp, 0, ptr);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNonZeroElements() {
    return _values.length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfElements() {
    return _els;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getMaxNonZerosInSignificantDirection() {
    return _maxEntriesInARow;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getEntry(int... indices) {
    Validate.notNull(_x);
    Validate.notNull(_y);
    Validate.notNull(_values);

    // no real fast way of doing this, brute force used
    for (int i = 0; i < _y.length; i++) {
      if (_y[i] == indices[0] && _x[i] == indices[1]) {
        return _values[i];
      }
    }
    return 0.0; // index pairing not found so value must be a zero entry
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "SparseCoordinateFormatMatrix:\n"
        + "x=" + Arrays.toString(_x) + "\n"
        + "y=" + Arrays.toString(_y) + "\n"
        + "data=" + Arrays.toString(_values) + "\n"
        + "number of nonzero elements=" + _els;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _els;
    result = prime * result + Arrays.hashCode(_values);
    result = prime * result + Arrays.hashCode(_x);
    result = prime * result + Arrays.hashCode(_y);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SparseCoordinateFormatMatrix other = (SparseCoordinateFormatMatrix) obj;
    if (_els != other._els) {
      return false;
    }
    if (!Arrays.equals(_values, other._values)) {
      return false;
    }
    if (!Arrays.equals(_x, other._x)) {
      return false;
    }
    if (!Arrays.equals(_y, other._y)) {
      return false;
    }
    return true;
  }

}
