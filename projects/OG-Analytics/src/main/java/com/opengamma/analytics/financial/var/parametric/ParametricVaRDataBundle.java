/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.parametric;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.Matrix;

/**
 * 
 */
public class ParametricVaRDataBundle {
  private final List<String> _names;
  private final DoubleMatrix1D _expectedReturn;
  private final Matrix<?> _sensitivities;
  private final DoubleMatrix2D _covarianceMatrix;
  private final int _order;

  public ParametricVaRDataBundle(final Matrix<?> sensitivities, final DoubleMatrix2D covarianceMatrix, final int order) {
    this(null, getEmptyExpectedReturnVector(sensitivities), sensitivities, covarianceMatrix, order);
  }

  public ParametricVaRDataBundle(final DoubleMatrix1D expectedReturn, final Matrix<?> sensitivities, final DoubleMatrix2D covarianceMatrix, final int order) {
    this(null, expectedReturn, sensitivities, covarianceMatrix, order);
  }

  public ParametricVaRDataBundle(final List<String> names, final Matrix<?> sensitivities, final DoubleMatrix2D covarianceMatrix, final int order) {
    this(names, getEmptyExpectedReturnVector(sensitivities), sensitivities, covarianceMatrix, order);
  }

  public ParametricVaRDataBundle(final List<String> names, final DoubleMatrix1D expectedReturn, final Matrix<?> sensitivities, final DoubleMatrix2D covarianceMatrix, final int order) {
    Validate.notNull(sensitivities, "sensitivities");
    Validate.notNull(covarianceMatrix, "covariance matrix");
    Validate.notNull(expectedReturn, "expected return");
    Validate.isTrue(order > 0);
    Validate.isTrue(covarianceMatrix.getNumberOfRows() == covarianceMatrix.getNumberOfColumns());
    if (sensitivities instanceof DoubleMatrix1D) {
      Validate.isTrue(sensitivities.getNumberOfElements() == covarianceMatrix.getNumberOfRows());
      Validate.isTrue(sensitivities.getNumberOfElements() == expectedReturn.getNumberOfElements());
      if (names != null) {
        Validate.isTrue(sensitivities.getNumberOfElements() == names.size());
      }
    } else if (sensitivities instanceof DoubleMatrix2D) {
      Validate.isTrue(((DoubleMatrix2D) sensitivities).getNumberOfRows() == covarianceMatrix.getNumberOfRows());
      Validate.isTrue(((DoubleMatrix2D) sensitivities).getNumberOfRows() == expectedReturn.getNumberOfElements());
      if (names != null) {
        Validate.isTrue(((DoubleMatrix2D) sensitivities).getNumberOfRows() == names.size());
      }
    } else {
      throw new IllegalArgumentException("Can only handle 1- and 2-d sensitivity matrices");
    }
    _names = names;
    _expectedReturn = expectedReturn;
    _sensitivities = sensitivities;
    _covarianceMatrix = covarianceMatrix;
    _order = order;
  }

  private static DoubleMatrix1D getEmptyExpectedReturnVector(final Matrix<?> sensitivities) {
    if (sensitivities instanceof DoubleMatrix1D) {
      return new DoubleMatrix1D(new double[((DoubleMatrix1D) sensitivities).getNumberOfElements()]);
    } else if (sensitivities instanceof DoubleMatrix2D) {
      return new DoubleMatrix1D(new double[((DoubleMatrix2D) sensitivities).getNumberOfRows()]);
    }
    throw new IllegalArgumentException("Can only handle 1- and 2-d sensitivity matrices");
  }

  public DoubleMatrix1D getExpectedReturn() {
    return _expectedReturn;
  }

  public Matrix<?> getSensitivities() {
    return _sensitivities;
  }

  public DoubleMatrix2D getCovarianceMatrix() {
    return _covarianceMatrix;
  }

  public List<String> getNames() {
    return _names;
  }

  public int getOrder() {
    return _order;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _covarianceMatrix.hashCode();
    result = prime * result + ((_names == null) ? 0 : _names.hashCode());
    result = prime * result + _expectedReturn.hashCode();
    result = prime * result + _order;
    result = prime * result + _sensitivities.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ParametricVaRDataBundle other = (ParametricVaRDataBundle) obj;
    if (!ObjectUtils.equals(_covarianceMatrix, other._covarianceMatrix)) {
      return false;
    }
    if (!ObjectUtils.equals(_expectedReturn, other._expectedReturn)) {
      return false;
    }
    if (!ObjectUtils.equals(_names, other._names)) {
      return false;
    }
    if (!ObjectUtils.equals(_sensitivities, other._sensitivities)) {
      return false;
    }
    return _order == other._order;
  }

}
