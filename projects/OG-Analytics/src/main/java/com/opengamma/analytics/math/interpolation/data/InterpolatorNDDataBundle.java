/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterpolatorNDDataBundle implements Serializable {
  private final List<Pair<double[], Double>> _data;

  public InterpolatorNDDataBundle(final List<Pair<double[], Double>> data) {
    validateData(data);
    _data = data;
  }

  public List<Pair<double[], Double>> getData() {
    return _data;
  }

  private void validateData(final List<Pair<double[], Double>> data) {
    Validate.notEmpty(data, "no data");
    final Iterator<Pair<double[], Double>> iter = data.iterator();
    final int dim = iter.next().getFirst().length;
    Validate.isTrue(dim > 0, "no actual data");
    while (iter.hasNext()) {
      Validate.isTrue(iter.next().getFirst().length == dim, "different dimensions in data");
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_data == null) ? 0 : _data.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InterpolatorNDDataBundle)) {
      return false;
    }
    final InterpolatorNDDataBundle other = (InterpolatorNDDataBundle) obj;
    if (!ObjectUtils.equals(_data, other._data)) {
      return false;
    }
    return true;
  }
}
