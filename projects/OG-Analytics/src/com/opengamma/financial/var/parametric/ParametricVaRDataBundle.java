/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * 
 */
public class ParametricVaRDataBundle {
  private final List<String> _names;
  private final Matrix<?> _sensitivities;
  private final DoubleMatrix2D _covarianceMatrix;
  private final int _order;

  public ParametricVaRDataBundle(final Matrix<?> sensitivities, final DoubleMatrix2D covarianceMatrix, final int order) {
    this(null, sensitivities, covarianceMatrix, order);
  }

  public ParametricVaRDataBundle(final List<String> names, final Matrix<?> sensitivities, final DoubleMatrix2D covarianceMatrix, final int order) {
    Validate.notNull(sensitivities, "sensitivities");
    Validate.notNull(covarianceMatrix, "covariance matrix");
    Validate.isTrue(order > 0);
    Validate.isTrue(covarianceMatrix.getNumberOfRows() == covarianceMatrix.getNumberOfColumns());
    if (sensitivities instanceof DoubleMatrix1D) {
      Validate.isTrue(sensitivities.getNumberOfElements() == covarianceMatrix.getNumberOfRows());
      if (names != null) {
        Validate.isTrue(sensitivities.getNumberOfElements() == names.size());
      }
    } else if (sensitivities instanceof DoubleMatrix2D) {
      Validate.isTrue(((DoubleMatrix2D) sensitivities).getNumberOfRows() == covarianceMatrix.getNumberOfRows());
      if (names != null) {
        Validate.isTrue(((DoubleMatrix2D) sensitivities).getNumberOfRows() == names.size());
      }
    } else {
      throw new IllegalArgumentException("Can only handle 1- and 2- sensitivity matrices");
    }
    _names = names;
    _sensitivities = sensitivities;
    _covarianceMatrix = covarianceMatrix;
    _order = order;
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
    result = prime * result + ((_covarianceMatrix == null) ? 0 : _covarianceMatrix.hashCode());
    result = prime * result + ((_names == null) ? 0 : _names.hashCode());
    result = prime * result + _order;
    result = prime * result + ((_sensitivities == null) ? 0 : _sensitivities.hashCode());
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
    if (!ObjectUtils.equals(_names, other._names)) {
      return false;
    }
    if (!ObjectUtils.equals(_sensitivities, other._sensitivities)) {
      return false;
    }
    return _order == other._order;
  }

}
