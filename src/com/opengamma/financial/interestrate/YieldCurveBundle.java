/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class YieldCurveBundle {

  private final Map<String, YieldAndDiscountCurve> _curves;

  public YieldCurveBundle(String[] names, YieldAndDiscountCurve[] curves) {
    this();
    Validate.notNull(names);
    Validate.notNull(curves);
    Validate.isTrue(names.length == curves.length, "Different number of names and curves");
    int n = names.length;
    for (int i = 0; i < n; i++) {
      _curves.put(names[i], curves[i]);
    }
  }

  public YieldCurveBundle(Map<String, YieldAndDiscountCurve> curvesMap) {
    this();
    if (curvesMap != null) {
      _curves.putAll(curvesMap);
    }
  }

  public void addAll(YieldCurveBundle other) {
    _curves.putAll(other._curves);
  }

  public YieldCurveBundle() {
    _curves = new LinkedHashMap<String, YieldAndDiscountCurve>();
  }

  public void setCurve(String name, YieldAndDiscountCurve curve) {
    if (_curves.containsKey(name)) {
      throw new IllegalArgumentException("Named yield curve already set");
    }
    _curves.put(name, curve);
  }

  public YieldAndDiscountCurve getCurve(String name) {
    if (_curves.containsKey(name)) {
      return _curves.get(name);
    }
    throw new IllegalArgumentException("Named yield curve not found");
  }

  public int size() {
    return _curves.size();
  }

  public Set<String> getAllNames() {
    return _curves.keySet();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_curves == null) ? 0 : _curves.hashCode());
    return result;
  }

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
    YieldCurveBundle other = (YieldCurveBundle) obj;
    if (_curves == null) {
      if (other._curves != null) {
        return false;
      }
    } else if (!_curves.equals(other._curves)) {
      return false;
    }
    return true;
  }

}
