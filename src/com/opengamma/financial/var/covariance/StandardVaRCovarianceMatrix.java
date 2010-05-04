/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.var.covariance;


/**
 * 
 * 
 * @author emcleod
 */
public class StandardVaRCovarianceMatrix {
/*
 *   private final StandardVaRDataBundle _data;
  private final CovarianceMatrixCalculator _calculator;
  private DoubleMatrix2D _matrix;
  private List<DomainSpecificIdentifiers> _keys;


 *   public StandardVaRCovarianceMatrix(final StandardVaRDataBundle data, final CovarianceMatrixCalculator calculator) {
 
    if (data == null)
      throw new IllegalArgumentException("VaR data bundle was null");
    if (calculator == null)
      throw new IllegalArgumentException("Covariance calculator was null");
    init(data, calculator);
    _data = data;
    _calculator = calculator;
  }

  private void init(final StandardVaRDataBundle data, final CovarianceMatrixCalculator calculator) {
    final Map<DomainSpecificIdentifiers, DoubleTimeSeries> map = data.getAllReturnData();
    _keys = new ArrayList<DomainSpecificIdentifiers>();
    final DoubleTimeSeries[] ts = new DoubleTimeSeries[map.size()];
    int count = 0;
    for (final Map.Entry<DomainSpecificIdentifiers, DoubleTimeSeries> entry : map.entrySet()) {
      _keys.add(entry.getKey());
      ts[count++] = entry.getValue();
      count++;
    }
    _matrix = calculator.evaluate(ts);
  }

  public StandardVaRDataBundle getData() {
    return _data;
  }

  public CovarianceMatrixCalculator getCalculator() {
    return _calculator;
  }

  public DoubleMatrix2D getCovarianceMatrix() {
    return _matrix;
  }

  public List<DomainSpecificIdentifiers> getSecurityKeys() {
    return _keys;
  }

  public StandardVaRCovarianceMatrix getSubSetMatrix(final Set<DomainSpecificIdentifiers> keys) {
    return new StandardVaRCovarianceMatrix(getData().getSubSetBundle(keys), getCalculator());
  }

  public Double getCovariance(final DomainSpecificIdentifiers key1, final DomainSpecificIdentifiers key2) {
    final int index1 = _keys.indexOf(key1);
    if (index1 == -1)
      throw new IllegalArgumentException("Security " + key1 + " was not in the data set");
    final int index2 = _keys.indexOf(key2);
    if (index2 == -1)
      throw new IllegalArgumentException("Security " + key2 + " was not in the data set");
    return _matrix.getQuick(index1, index2);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (getCalculator() == null ? 0 : getCalculator().hashCode());
    result = prime * result + (getData() == null ? 0 : getData().hashCode());
    result = prime * result + (getSecurityKeys() == null ? 0 : getSecurityKeys().hashCode());
    result = prime * result + (getCovarianceMatrix() == null ? 0 : getCovarianceMatrix().hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final StandardVaRCovarianceMatrix other = (StandardVaRCovarianceMatrix) obj;
    if (getCalculator() == null) {
      if (other.getCalculator() != null)
        return false;
    } else if (!getCalculator().equals(other.getCalculator()))
      return false;
    if (getData() == null) {
      if (other.getData() != null)
        return false;
    } else if (!getData().equals(other.getData()))
      return false;
    if (getSecurityKeys() == null) {
      if (other.getSecurityKeys() != null)
        return false;
    } else if (!getSecurityKeys().equals(other.getSecurityKeys()))
      return false;
    if (getCovarianceMatrix() == null) {
      if (other.getCovarianceMatrix() != null)
        return false;
    } else if (!getCovarianceMatrix().equals(other.getCovarianceMatrix()))
      return false;
    return true;
  }*/
}
