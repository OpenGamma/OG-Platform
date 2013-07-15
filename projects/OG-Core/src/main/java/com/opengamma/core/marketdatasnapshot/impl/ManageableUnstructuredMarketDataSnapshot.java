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
  private final Map<ExternalId, Map<String, ExternalIdBundle>> _index = new HashMap<ExternalId, Map<String, ExternalIdBundle>>();

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
          Map<String, ExternalIdBundle> index = _index.get(identifier);
          if (index == null) {
            index = new HashMap<String, ExternalIdBundle>();
            _index.put(identifier, index);
          }
          for (final String value : values.keySet()) {
            index.put(value, target);
          }
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
    final Map<String, ExternalIdBundle> index = _index.get(identifier);
    if (index != null) {
      final ExternalIdBundle key = index.get(valueName);
      if (key != null) {
        return getImpl(key, valueName);
      }
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
    Map<String, ExternalIdBundle> index = _index.get(identifier);
    ExternalIdBundle key;
    if (index == null) {
      index = new HashMap<String, ExternalIdBundle>();
      _index.put(identifier, index);
      key = ExternalIdBundle.of(identifier);
      index.put(valueName, key);
    } else {
      key = index.get(valueName);
      if (key == null) {
        key = ExternalIdBundle.of(identifier);
        index.put(valueName, key);
      }
    }
    Map<String, ValueSnapshot> values = _values.get(key);
    if (values == null) {
      values = new HashMap<String, ValueSnapshot>();
      _values.put(key, values);
    }
    values.put(valueName, value);
  }

  /**
   * Stores a value against the target identifiers. Any values previously stored against any of the identifiers in the bundle will be replaced.
   * 
   * @param identifiers the target identifiers, not null
   * @param valueName the value name, not null
   * @param value the value to associate, not null
   */
  public void putValue(final ExternalIdBundle identifiers, final String valueName, final ValueSnapshot value) {
    Map<String, ValueSnapshot> values = _values.get(identifiers);
    if (values != null) {
      if (values.put(valueName, value) != null) {
        // Already have a value for this bundle/valueName pair so don't need to update the index
        return;
      }
    } else {
      values = new HashMap<String, ValueSnapshot>();
      _values.put(identifiers, values);
      values.put(valueName, value);
    }
    removeValue(identifiers, valueName);
    for (final ExternalId identifier : identifiers) {
      Map<String, ExternalIdBundle> index = _index.get(identifier);
      if (index == null) {
        index = new HashMap<String, ExternalIdBundle>();
        _index.put(identifier, index);
      }
      index.put(valueName, identifiers);
    }
  }

  /**
   * Removes a value held against a target identifier.
   * 
   * @param identifier the target identifier, not null
   * @param valueName the value name, not null
   */
  public void removeValue(final ExternalId identifier, final String valueName) {
    final Map<String, ExternalIdBundle> index = _index.get(identifier);
    if (index != null) {
      final ExternalIdBundle key = index.remove(valueName);
      if (key != null) {
        if (index.isEmpty()) {
          _index.remove(identifier);
        }
        final Map<String, ValueSnapshot> values = _values.get(key);
        if (values != null) {
          if (values.remove(valueName) != null) {
            if (values.isEmpty()) {
              _values.remove(key);
            }
          }
        }
        removeValue(key, valueName);
      }
    }
  }

  /**
   * Removes a value held against a target identifier bundle.
   * 
   * @param identifiers the target identifiers, not null
   * @param valueName the value name, not null
   */
  public void removeValue(final ExternalIdBundle identifiers, final String valueName) {
    for (final ExternalId identifier : identifiers) {
      removeValue(identifier, valueName);
    }
  }

}
