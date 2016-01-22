/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * A bundle of data to use in some structured context.
 * <p>
 * This class is mutable and not thread-safe.
 */
public final class SnapshotDataBundle implements Serializable {

  /**
   * The market values in the bundle.
   */
  private final Map<ExternalIdBundle, Double> _dataPoints = new HashMap<ExternalIdBundle, Double>();

  /**
   * The index for lookup operations.
   */
  private final Map<ExternalId, ExternalIdBundle> _index = new HashMap<ExternalId, ExternalIdBundle>();

  /**
   * Creates an instance.
   */
  public SnapshotDataBundle() {
  }

  /**
   * Queries the data using an identifier bundle. Any data point matching one of the identifiers in the supplied bundle will be returned. If the identifier bundle is such that multiple points match
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
   * Sets a data point in the snapshot. Any previous points with matching identifiers will be replaced.
   * 
   * @param identifiers the identifiers to set, not null
   * @param value the value to set, not null
   */
  public void setDataPoint(final ExternalIdBundle identifiers, final Double value) {
    if (_dataPoints.put(identifiers, value) == null) {
      // Bundle not already set; remove anything previously defined for the bundle
      removeDataPoints(identifiers);
      for (final ExternalId identifier : identifiers) {
        _index.put(identifier, identifiers);
      }
    }
  }

  /**
   * Sets a data point in the snapshot. Any previous point with a matching identifier will be replaced.
   * 
   * @param identifier the identifier to set, not null
   * @param value the value to set
   */
  public void setDataPoint(final ExternalId identifier, final double value) {
    ExternalIdBundle key = _index.get(identifier);
    if (key == null) {
      // Identifier not already present; use a singleton bundle
      key = identifier.toBundle();
      _index.put(identifier, key);
    }
    _dataPoints.put(key, value);
  }

  /**
   * Removes data points from the snapshot. All points with matching identifiers will be removed.
   * 
   * @param identifiers the identifiers to remove, not null
   */
  public void removeDataPoints(final ExternalIdBundle identifiers) {
    for (final ExternalId identifier : identifiers) {
      removeDataPoint(identifier);
    }
  }

  /**
   * Removes a data point from the snapshot.
   * 
   * @param identifier the identifier to remove, not null
   */
  public void removeDataPoint(final ExternalId identifier) {
    final ExternalIdBundle key = _index.remove(identifier);
    if (key != null) {
      if (_dataPoints.remove(key) != null) {
        // There were points allocated against this key, to recursively remove those
        removeDataPoints(key);
      }
    }
  }

  /**
   * Returns the number of data points defined.
   * 
   * @return the number of data points
   */
  public int size() {
    return _dataPoints.size();
  }

  /**
   * Returns the data points as a set.
   * 
   * @return the data points
   */
  public Set<Map.Entry<ExternalIdBundle, Double>> getDataPointSet() {
    return Collections.unmodifiableSet(_dataPoints.entrySet());
  }

  /**
   * Returns the data points.
   *
   * @return the data points
   */
  public Map<ExternalIdBundle, Double> getDataPoints() {
    return Collections.unmodifiableMap(_dataPoints);
  }

  @Override
  public boolean equals(Object o) {
    // only check one id collection, assume both in sync
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SnapshotDataBundle that = (SnapshotDataBundle) o;
    if (_dataPoints.size() != that._dataPoints.size()) {
      return false;
    }
    return _dataPoints.entrySet().containsAll(that._dataPoints.entrySet());
  }

  @Override
  public int hashCode() {
    return _dataPoints.hashCode();
  }

}
