/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains a yield curve specification and the market data required to build the curve.
 */
public class YieldCurveData {

  private final InterpolatedYieldCurveSpecificationWithSecurities _curveSpec;
  private final Map<ExternalIdBundle, Double> _dataPoints;
  private final Map<ExternalId, ExternalIdBundle> _index;

  public YieldCurveData(InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
                        Map<ExternalIdBundle, Double> dataPoints) {
    ArgumentChecker.notNull(curveSpec, "curveSpec");
    ArgumentChecker.notEmpty(dataPoints, "dataPoints");
    _curveSpec = curveSpec;
    _dataPoints = ImmutableMap.copyOf(dataPoints);
    Map<ExternalId, ExternalIdBundle> index = Maps.newHashMap();
    for (ExternalIdBundle bundle : dataPoints.keySet()) {
      for (ExternalId id : bundle) {
        index.put(id, bundle);
      }
    }
    _index = ImmutableMap.copyOf(index);
  }

  /**
   * Queries the data using an identifier bundle. Any data point matching one of the identifiers in the supplied bundle
   * will be returned. If the identifier bundle is such that multiple points match
   * then an arbitrary one will be returned.
   *
   * @param identifiers the identifier(s) to search for
   * @return the data point found, or null if none
   */
  public Double getDataPoint(final ExternalIdBundle identifiers) {
    Double value = _dataPoints.get(identifiers);
    if (value != null) {
      return value;
    }
    for (final ExternalId identifier : identifiers) {
      value = getDataPoint(identifier);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  /**
   * @return The data points in the curve keyed by ID bundle
   */
  public Map<ExternalIdBundle, Double> getDataPoints() {
    return _dataPoints;
  }

  /**
   * Queries the data using a single identifier.
   *
   * @param identifier the identifier to search for
   * @return the data point found, or null if none
   */
  public Double getDataPoint(final ExternalId identifier) {
    final ExternalIdBundle key = _index.get(identifier);
    if (key != null) {
      return _dataPoints.get(key);
    }
    return null;
  }

  /**
   * @return The specification that defines the yield curve
   */
  public InterpolatedYieldCurveSpecificationWithSecurities getCurveSpecification() {
    return _curveSpec;
  }

  public Map<ExternalId, ExternalIdBundle> getIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_curveSpec, _dataPoints);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final YieldCurveData other = (YieldCurveData) obj;
    return Objects.equals(_curveSpec, other._curveSpec) && Objects.equals(_dataPoints, other._dataPoints);
  }
}
