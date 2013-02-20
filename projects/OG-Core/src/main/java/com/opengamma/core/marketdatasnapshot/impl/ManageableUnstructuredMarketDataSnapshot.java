/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Mutable snapshot of market data.
 * <p>
 * This class is mutable and not thread-safe.
 */
public class ManageableUnstructuredMarketDataSnapshot implements UnstructuredMarketDataSnapshot, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The values.
   * <p>
   * Note the use of {@link LinkedHashMap} to preserve the ordering of items. This is used by yield curve based logic as the ordering of the market data may have meaning to someone manipulating the
   * snapshot (PLAT-1889).
   */
  private final Map<ExternalIdBundle, Map<String, ValueSnapshot>> _values = new LinkedHashMap<ExternalIdBundle, Map<String, ValueSnapshot>>();

  /**
   * The index for lookup operations.
   */
  private final Map<ExternalId, ExternalIdBundle> _index = new HashMap<ExternalId, ExternalIdBundle>();

  /**
   * Creates an empty snapshot.
   */
  public ManageableUnstructuredMarketDataSnapshot() {
  }

  /**
   * Creates a snapshot initialised from a template.
   * 
   * @param copyFrom the template to initialise from
   */
  public ManageableUnstructuredMarketDataSnapshot(final UnstructuredMarketDataSnapshot copyFrom) {
    for (final ExternalIdBundle target : copyFrom.getTargets()) {
      final Map<String, ValueSnapshot> values = copyFrom.getTargetValues(target);
      if (values != null) {
        _values.put(target, new LinkedHashMap<String, ValueSnapshot>(values));
        for (final ExternalId identifier : target) {
          _index.put(identifier, target);
        }
      }
    }
  }

  protected ValueSnapshot getImpl(final ExternalIdBundle identifiers, final String valueName) {
    final Map<String, ValueSnapshot> values = _values.get(identifiers);
    if (values != null) {
      return values.get(valueName);
    } else {
      return null;
    }
  }

  @Override
  public boolean isEmpty() {
    return _values.isEmpty();
  }

  @Override
  public ValueSnapshot getValue(final ExternalId identifier, final String valueName) {
    final ExternalIdBundle key = _index.get(identifier);
    if (key != null) {
      return getImpl(key, valueName);
    }
    return null;
  }

  @Override
  public ValueSnapshot getValue(final ExternalIdBundle identifiers, final String valueName) {
    ValueSnapshot value = getImpl(identifiers, valueName);
    if (value != null) {
      return value;
    } else {
      for (final ExternalId identifier : identifiers) {
        value = getValue(identifier, valueName);
        if (value != null) {
          return value;
        }
      }
      return null;
    }
  }

  @Override
  public Set<ExternalIdBundle> getTargets() {
    return Collections.unmodifiableSet(_values.keySet());
  }

  @Override
  public Map<String, ValueSnapshot> getTargetValues(final ExternalIdBundle identifiers) {
    final Map<String, ValueSnapshot> values = _values.get(identifiers);
    if (values != null) {
      return Collections.unmodifiableMap(values);
    } else {
      return null;
    }
  }

  /**
   * Stores a value against the target identifier, replacing any previous association
   * 
   * @param identifier the target identifier, not null
   * @param valueName the value name, not null
   * @param value the value to associate, not null
   */
  public void putValue(final ExternalId identifier, final String valueName, final ValueSnapshot value) {
    throw new UnsupportedOperationException("[PLAT-3044] Update the snapshot");
  }

  /**
   * Stores a value against the target identifiers. Any values previously stored against any of the identifiers in the bundle will be replaced.
   * 
   * @param identifiers the target identifiers, not null
   * @param valueName the value name, not null
   * @param value the value to associate, not null
   */
  public void putValue(final ExternalIdBundle identifiers, final String valueName, final ValueSnapshot value) {
    throw new UnsupportedOperationException("[PLAT-3044] Update the snapshot");
  }

  /**
   * Removes a value held against a target identifier.
   * 
   * @param identifier the target identifier, not null
   * @param valueName the value name, not null
   */
  public void removeValue(final ExternalId identifier, final String valueName) {
    throw new UnsupportedOperationException("[PLAT-3044] Update the snapshot");
  }

  /**
   * Removes a value held against a target identifier bundle.
   * 
   * @param identifiers the target identifiers, not null
   * @param valueName the value name, not null
   */
  public void removeValue(final ExternalIdBundle identifiers, final String valueName) {
    throw new UnsupportedOperationException("[PLAT-3044] Update the snapshot");
  }

}
