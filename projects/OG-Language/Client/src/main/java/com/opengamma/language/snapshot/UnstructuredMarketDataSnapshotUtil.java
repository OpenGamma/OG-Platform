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

import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/* package */final class UnstructuredMarketDataSnapshotUtil {

  private UnstructuredMarketDataSnapshotUtil() {
  }

  public static List<Double> getValue(final UnstructuredMarketDataSnapshot snapshot, final String valueName, final ExternalId identifier) {
    final Map<ExternalIdBundle, Map<String, ValueSnapshot>> globalValues = snapshot.getValues();
    // TODO [PLAT-3044] Querying by bundle won't work
    final Map<String, ValueSnapshot> values = globalValues.get(identifier.toBundle());
    if (values != null) {
      final ValueSnapshot value = values.get(valueName);
      if (value != null) {
        return Arrays.asList(value.getOverrideValue(), value.getMarketValue());
      }
    }
    return null;
  }

  private static Map<String, ValueSnapshot> getValueMap(final UnstructuredMarketDataSnapshot snapshot, final ExternalId identifier, final boolean addIfMissing) {
    final Map<ExternalIdBundle, Map<String, ValueSnapshot>> globalValues = snapshot.getValues();
    // TODO [PLAT-3044] Querying by bundle won't work
    Map<String, ValueSnapshot> values = globalValues.get(identifier.toBundle());
    if ((values == null) && addIfMissing) {
      values = new HashMap<String, ValueSnapshot>();
      globalValues.put(identifier.toBundle(), values);
    }
    return values;
  }

  public static void setValue(final UnstructuredMarketDataSnapshot snapshot, final String valueName, final ExternalId identifier, final Double overrideValue, final Double marketValue) {
    if ((overrideValue != null) || (marketValue != null)) {
      final Map<String, ValueSnapshot> values = getValueMap(snapshot, identifier, true);
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
      final Map<String, ValueSnapshot> values = getValueMap(snapshot, identifier, false);
      if (values != null) {
        values.remove(valueName);
      }
    }
  }

  public static UnstructuredMarketDataSnapshot create() {
    final ManageableUnstructuredMarketDataSnapshot values = new ManageableUnstructuredMarketDataSnapshot();
    values.setValues(new HashMap<ExternalIdBundle, Map<String, ValueSnapshot>>());
    return values;
  }

}
