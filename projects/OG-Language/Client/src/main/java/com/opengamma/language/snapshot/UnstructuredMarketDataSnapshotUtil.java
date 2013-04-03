/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.id.ExternalId;

/* package */final class UnstructuredMarketDataSnapshotUtil {

  private UnstructuredMarketDataSnapshotUtil() {
  }

  public static void setValue(final ManageableUnstructuredMarketDataSnapshot snapshot, final String valueName, final ExternalId identifier, final Double overrideValue, final Double marketValue) {
    if (marketValue != null) {
      snapshot.putValue(identifier, valueName, new ValueSnapshot(marketValue, overrideValue));
    } else if (overrideValue != null) {
      final ValueSnapshot value = snapshot.getValue(identifier, valueName);
      if (value != null) {
        value.setOverrideValue(overrideValue);
      } else {
        snapshot.putValue(identifier, valueName, new ValueSnapshot(marketValue, overrideValue));
      }
    } else {
      snapshot.removeValue(identifier, valueName);
    }
  }

}
