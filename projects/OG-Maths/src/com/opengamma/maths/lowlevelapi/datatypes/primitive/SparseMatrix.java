/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * SparseMatrix is a class to wrap the underlying sparse storage types.
 * The class attempts to pick the most suitable representation for a sparse matrix based on the data given and encapsulates an object of that type.
 * Currently supported encapsulations include:
 * {@link CompressedSparseColumnFormatMatrix}
 * {@link CompressedSparseRowFormatMatrix}
 * {@link SparseCoordinateFormatMatrix}
 */
public class SparseMatrix implements MatrixPrimitive {
  private SparseMatrixType _type;

  /**
   * majorness is an enumerated type that is used to specify the anticipated access pattern of the sparse data.
   * The column enumeration is used to indicate column major access (ideal for the RHS of Matrix * Matrix)
   * The row enumeration is used to indicate row major access (ideal for the LHS of Matrix * Matrix and indeed most BLAS2 operations)
   */
  public enum majorness {
    /**
     * column, indicates that entries in the columns are sequential in memory. 
     */
    column,
    /**
     * row, indicates that entries in the rows are sequential in memory. 
     */ 
    row
  };

  /**
   *  Constructors
   */

  /**
   * Constructs a sparse matrix from double array of arrays data.
   * @param indata is an array of arrays containing data to be turned into a sparse matrix representation
   * @param m is the number of rows in the matrix (use if there are empty rows in indata and a matrix of a specific size is needed for conformance)
   * @param n is the number of columns in the matrix (use if there are empty columns in indata and a matrix of a specific size is needed for conformance)
   * @param t is an enumerated of type majorness, takes values "column" and "row" depending on anticipated access pattern.
   */
  public SparseMatrix(double[][] indata, int m, int n, majorness t) {
    if (MatrixPrimitiveUtils.isRagged(indata)) {
      throw new NotImplementedException("Construction from a ragged array of arrays is not implemented");
    }

    // test sanity of requested data lengths
    if (indata.length > m) {
      throw new IllegalArgumentException("Bad input to constructor, final matrix with " +
          m + " rows requested but the input data has " + indata.length + " and there is no sane way to truncate.");
    }

    if (indata[0].length > n) {
      throw new IllegalArgumentException("Bad input to constructor, final matrix with " +
          n + " columns requested but the input data has " + indata[0].length + " and there is no sane way to truncate.");
    }

    // hold final matrix size
    int s1 = indata.length;
    int s2 = indata[0].length;

    if (indata.length != m) {
      s1 = m;
      if (indata[0].length != n) {
        s2 = n;
      }
    }
    // size the matrix correctly for the construction
    double[][] tmp = new double[s1][s2];

    // unwind
    for (int i = 0; i < indata.length; i++) {
      for (int j = 0; j < indata[0].length; j++) {
        tmp[i][j] = indata[i][j];
      }
    }

    // test nnz and return something sane?! 0.6 is a magic number roughly based on memory density estimates
    // TODO: Come up with a more intelligent estimate of memory density patterns to pick optimal formats. Load testing needed.
    int nnz = MatrixPrimitiveUtils.numberOfNonZeroElementsInMatrix(tmp);
    double density = ((double) nnz / (s1 * s2));
    if (density < 0.6) {
      if (t.equals(majorness.row)) {
        _type = new CompressedSparseRowFormatMatrix(tmp);
      } else {
        _type = new CompressedSparseColumnFormatMatrix(tmp);
      }
    } else {
      _type = new SparseCoordinateFormatMatrix(tmp);
    }
  }

  /**
   * Constructs a sparse matrix from double array of arrays data
   * @param indata is an array of arrays containing data to be turned into a sparse matrix representation
   * The constructor assumes that the matrix dimensions can be derived from the dimensions of the arrays of arrays passed in (i.e. no empty rows and columns)
   * If for reasons of conformability a matrix of a specific dimension is needed then use the alternative constructor that allows this feature.
   */
  public SparseMatrix(double[][] indata) {
    this(indata, indata.length, indata[0].length);
  }

  /**
   * Constructs a sparse matrix from the DoubleMatrix2D type
   * @param indata is a DoubleMatrix2D containing data to be turned into a sparse matrix representation
   * @param m is the number of rows in the matrix (use if there are empty rows in indata and a matrix of a specific size is needed for conformance)
   * @param n is the number of columns in the matrix (use if there are empty columns in indata and a matrix of a specific size is needed for conformance)
   */
  public SparseMatrix(DoubleMatrix2D indata, int m, int n) {
    this(indata.toArray(), m, n);
  }

  /**
   * Constructs a sparse matrix from the DoubleMatrix2D type
   * @param indata is a DoubleMatrix2D containing data to be turned into a sparse matrix representation
   * The constructor assumes that the matrix dimensions can be derived from the dimensions of the DoubleMatrix2D passed in (i.e. no empty rows and columns)
   * If for reasons of conformability a matrix of a specific dimension is needed then use the alternative constructor that allows this feature.
   */
  public SparseMatrix(DoubleMatrix2D indata) {
    this(indata.toArray(), indata.getNumberOfRows(), indata.getNumberOfColumns());
  }

  /**
   * Constructs a sparse matrix from double array of arrays data
   * @param indata is an array of arrays containing data to be turned into a sparse matrix representation
   * @param m is the number of rows in the matrix (use if there are empty rows in indata and a matrix of a specific size is needed for conformance)
   * @param n is the number of columns in the matrix (use if there are empty columns in indata and a matrix of a specific size is needed for conformance)
   */
  public SparseMatrix(double[][] indata, int m, int n) {
    this(indata, m, n, majorness.row); // default constructor to row major
  }

  /**
   * constructor duplicates with additional flag  for "expert" users to indicate that the data should be stored, if possible, as CRC
   */

  /**
   * Constructs a sparse matrix from double array of arrays data
   * @param indata is an array of arrays containing data to be turned into a sparse matrix representation
   * @param t is an enumerated of type majorness, takes values "column" and "row" depending on anticipated access pattern.
   * The constructor assumes that the matrix dimensions can be derived from the dimensions of the arrays of arrays passed in (i.e. no empty rows and columns)
   * If for reasons of conformability a matrix of a specific dimension is needed then use the alternative constructor that allows this feature.
   */
  public SparseMatrix(double[][] indata, majorness t) {
    this(indata, indata.length, indata[0].length, t);
  }

  /**
   * Constructs a sparse matrix from the DoubleMatrix2D type
   * @param indata is a DoubleMatrix2D containing data to be turned into a sparse matrix representation
   * @param m is the number of rows in the matrix (use if there are empty rows in indata and a matrix of a specific size is needed for conformance)
   * @param n is the number of columns in the matrix (use if there are empty columns in indata and a matrix of a specific size is needed for conformance)
   * @param t is an enumerated of type majorness, takes values "column" and "row" depending on anticipated access pattern.
   */
  public SparseMatrix(DoubleMatrix2D indata, int m, int n, majorness t) {
    this(indata.toArray(), m, n, t);
  }

  /**
   * Constructs a sparse matrix from the DoubleMatrix2D type
   * @param indata is a DoubleMatrix2D containing data to be turned into a sparse matrix representation
   * The constructor assumes that the matrix dimensions can be derived from the dimensions of the DoubleMatrix2D passed in (i.e. no empty rows and columns)
   * If for reasons of conformability a matrix of a specific dimension is needed then use the alternative constructor that allows this feature.
   * @param t is an enumerated of type majorness, takes values "column" and "row" depending on anticipated access pattern.
   */
  public SparseMatrix(DoubleMatrix2D indata, majorness t) {
    this(indata.toArray(), indata.getNumberOfRows(), indata.getNumberOfColumns(), t);
  }

  /**
   * Methods
   */

  /**
   * Gets the sparse object for use in other methods.
   * @return A SparseMatrixType object that is most suitable to represent the data given to the constructor.
   */
  public SparseMatrixType getSparseObject() {
    return _type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfElements() {
    return _type.getNumberOfElements();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getEntry(int... indices) {
    return _type.getEntry(indices);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getFullRow(int index) {
    return _type.getFullRow(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getFullColumn(int index) {
    return _type.getFullColumn(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getRowElements(int index) {
    return _type.getRowElements(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getColumnElements(int index) {
    return _type.getColumnElements(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNonZeroElements() {
    return _type.getNumberOfNonZeroElements();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfRows() {
    return _type.getNumberOfRows();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfColumns() {
    return _type.getNumberOfColumns();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[][] toArray() {
    return _type.toArray();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return _type.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    return _type.equals(obj);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return _type.hashCode();
  }

}
