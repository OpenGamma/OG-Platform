/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.id.UniqueId;

/* package */final class UnstructuredMarketDataSnapshotUtil {

  private UnstructuredMarketDataSnapshotUtil() {
  }

  public static List<Double> getValue(final UnstructuredMarketDataSnapshot snapshot, final String valueName, final UniqueId identifier) {
    final Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> globalValues = snapshot.getValues();
    Map<String, ValueSnapshot> values = globalValues.get(new MarketDataValueSpecification(MarketDataValueType.SECURITY, identifier));
    if (values != null) {
      final ValueSnapshot value = values.get(valueName);
      if (value != null) {
        return Arrays.asList(value.getOverrideValue(), value.getMarketValue());
      }
    }
    values = globalValues.get(new MarketDataValueSpecification(MarketDataValueType.PRIMITIVE, identifier));
    if (values != null) {
      final ValueSnapshot value = values.get(valueName);
      if (value != null) {
        return Arrays.asList(value.getOverrideValue(), value.getMarketValue());
      }
    }
    return null;
  }

  private static Map<String, ValueSnapshot> getValueMap(final UnstructuredMarketDataSnapshot snapshot, final UniqueId identifier, final MarketDataValueType type, final boolean addIfMissing) {
    final Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> globalValues = snapshot.getValues();
    final MarketDataValueSpecification spec = new MarketDataValueSpecification(type, identifier);
    Map<String, ValueSnapshot> values = globalValues.get(spec);
    if ((values == null) && addIfMissing) {
      values = new HashMap<String, ValueSnapshot>();
      globalValues.put(spec, values);
    }
    return values;
  }

  public static void setValue(final UnstructuredMarketDataSnapshot snapshot, final String valueName, final UniqueId identifier,
      final Double overrideValue, final Double marketValue, final MarketDataValueType type) {
    if ((overrideValue != null) || (marketValue != null)) {
      final Map<String, ValueSnapshot> values = getValueMap(snapshot, identifier, type, true);
      final ValueSnapshot value = values.get(valueName);
      if (value != null) {
        if (marketValue != null) {
          values.put(valueName, new ValueSnapshot(marketValue, overrideValue));
        } else {
          value.setOverrideValue(overrideValue);
        }
      } else {
        values.put(valueName, new ValueSnapshot(marketValue, overrideValue));
      }
    } else {
      final Map<String, ValueSnapshot> values = getValueMap(snapshot, identifier, type, false);
      if (values != null) {
        values.remove(valueName);
      }
    }
  }

  public static UnstructuredMarketDataSnapshot create() {
    final ManageableUnstructuredMarketDataSnapshot values = new ManageableUnstructuredMarketDataSnapshot();
    values.setValues(new HashMap<MarketDataValueSpecification, Map<String, ValueSnapshot>>());
    return values;
  }

}
