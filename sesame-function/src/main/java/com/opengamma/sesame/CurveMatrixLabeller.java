package com.opengamma.sesame;

import java.util.List;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * Allows matrices to be labelled with meaningful values. For
 * instance, it may be desirable to label a sensitivity
 * matrix with the nodal point tenors from the curve used
 * to derive the sensitivities.
 */
public class CurveMatrixLabeller {

  /**
   * The list of labels to be used.
   */
  private final List<?> _labels;

  /**
   * Creates a labeller with the specified labels.
   *
   * @param labels  list of labels to be used
   */
  public CurveMatrixLabeller(List<?> labels) {
    _labels = ArgumentChecker.notNull(labels, "labels");
  }

  /**
   * Label a one dimensional matrix with the values from the
   * labeller. The size of the matrix must match the number
   * of labels else an exception will be thrown.
   *
   * @param matrix  the matrix to be labelled
   * @return a new labelled matrix
   */
  public DoubleLabelledMatrix1D labelMatrix(DoubleMatrix1D matrix) {

    ArgumentChecker.notNull(matrix, "matrix");
    int matrixSize = matrix.getNumberOfElements();
    ArgumentChecker.isTrue(matrixSize == _labels.size(),
        "Mismatch between matrix size: [{}] and labels size: [{}]", matrixSize, _labels.size());

    return new DoubleLabelledMatrix1D(createIndexArray(matrixSize), _labels.toArray(), matrix.getData());
  }

  /**
   * Label a two dimensional matrix with the values from the
   * labeller. The matrix must be square and its order must
   * match the number of labels else an exception will be thrown.
   *
   * @param matrix  the matrix to be labelled
   * @return a new labelled matrix
   */
  public DoubleLabelledMatrix2D labelMatrix(DoubleMatrix2D matrix) {

    ArgumentChecker.notNull(matrix, "matrix");

    int numRows = matrix.getNumberOfRows();
    int numCols = matrix.getNumberOfColumns();

    ArgumentChecker.isTrue(numCols == _labels.size() && numRows == _labels.size(),
        "Mismatch between matrix size: [{}x{}] and labels size: [{}]", numRows, numCols, _labels.size());

    Double[] keys = createIndexArray(numRows);
    return new DoubleLabelledMatrix2D(keys, _labels.toArray(), keys, _labels.toArray(), matrix.getData());
  }

  /**
   * Create an array of the specified size where the values
   * in the array are sequential integer values from 0. For
   * example, for size 3, the array will contain {@code [0, 1, 2]}.
   *
   * @param size  the size of array to be created
   * @return an array containing sequential values
   */
  private Double[] createIndexArray(int size) {
    Double[] keys = new Double[size];
    for (int i = 0; i < size; i++) {
      keys[i] = (double) i;
    }
    return keys;
  }

}
