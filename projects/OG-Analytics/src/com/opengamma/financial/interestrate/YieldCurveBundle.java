/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class YieldCurveBundle {

  private final Map<String, YieldAndDiscountCurve> _curves;

  public YieldCurveBundle(final String[] names, final YieldAndDiscountCurve[] curves) {
    this();
    Validate.notNull(names);
    Validate.notNull(curves);
    Validate.isTrue(names.length == curves.length, "Different number of names and curves");
    Validate.noNullElements(names);
    Validate.noNullElements(curves);
    final int n = names.length;
    for (int i = 0; i < n; i++) {
      _curves.put(names[i], curves[i]);
    }
  }

  public YieldCurveBundle(final Map<String, ? extends YieldAndDiscountCurve> curvesMap) {
    this();
    if (curvesMap != null) {
      Validate.noNullElements(curvesMap.keySet());
      Validate.noNullElements(curvesMap.values());
      _curves.putAll(curvesMap);
    }
  }

  /**
   * Constructor from a bundle.
   * @param bundle A bundle.
   */
  public YieldCurveBundle(YieldCurveBundle bundle) {
    Validate.notNull(bundle);
    _curves = new LinkedHashMap<String, YieldAndDiscountCurve>(bundle._curves);
  }

  public YieldCurveBundle() {
    _curves = new LinkedHashMap<String, YieldAndDiscountCurve>();
  }

  /**
   * 
   * @param name curve name
   * @param curve curve name
   * @throws IllegalArgumentException if curve name already present 
   */
  public void setCurve(final String name, final YieldAndDiscountCurve curve) {
    Validate.notNull(name, "name");
    Validate.notNull(curve, "curve");
    if (_curves.containsKey(name)) {
      throw new IllegalArgumentException("Named yield curve already set: " + name);
    }
    _curves.put(name, curve);
  }

  /**
   * 
   * @param name curve name
   * @param curve curve name
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final String name, final YieldAndDiscountCurve curve) {
    Validate.notNull(name, "name");
    Validate.notNull(curve, "curve");
    if (!_curves.containsKey(name)) {
      throw new IllegalArgumentException("Named yield curve not in set" + name);
    }
    _curves.put(name, curve);
  }

  public void addAll(final YieldCurveBundle other) {
    _curves.putAll(other._curves);
  }

  public YieldAndDiscountCurve getCurve(final String name) {
    if (_curves.containsKey(name)) {
      return _curves.get(name);
    }
    throw new IllegalArgumentException("Named yield curve not found: " + name);
  }

  public int size() {
    return _curves.size();
  }

  public Set<String> getAllNames() {
    return _curves.keySet();
  }

  public Boolean containsName(final String curveName) {
    return _curves.containsKey(curveName);
  }

  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names.
   * @return The bundle.
   */
  public YieldCurveBundle copy() {
    return new YieldCurveBundle(_curves);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_curves == null) ? 0 : _curves.hashCode());
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
    final YieldCurveBundle other = (YieldCurveBundle) obj;
    return ObjectUtils.equals(_curves, other._curves);
  }

}
