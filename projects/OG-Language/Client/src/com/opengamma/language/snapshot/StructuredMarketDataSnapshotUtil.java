/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.id.UniqueId;
import com.opengamma.language.text.CompositeStringUtil;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/* package */final class StructuredMarketDataSnapshotUtil {

  private static final CompositeStringUtil s_cubeName = new CompositeStringUtil(2, false);
  private static final CompositeStringUtil s_curveName = new CompositeStringUtil(2, false);
  private static final CompositeStringUtil s_surfaceName = new CompositeStringUtil(5, false);

  private StructuredMarketDataSnapshotUtil() {
  }

  public static String toString(final Object key) {
    if (key.getClass().isEnum()) {
      return ((Enum<?>) key).name();
    } else if (key instanceof Tenor) {
      return ((Tenor) key).getPeriod().toString();
    } else if (key instanceof String) {
      return (String) key;
    } else if (key instanceof Pair<?, ?>) {
      return toString(((Pair<?, ?>) key).getFirst()) + ", " + toString(((Pair<?, ?>) key).getSecond());
    } else {
      return key.toString();
    }
  }

  public static YieldCurveKey toYieldCurveKey(final String name) {
    final String[] parsed = s_curveName.parse(name);
    if (!s_curveName.validate(parsed)) {
      return null;
    }
    return new YieldCurveKey(Currency.of(parsed[0]), parsed[1]);
  }

  public static String fromYieldCurveKey(final YieldCurveKey key) {
    return s_curveName.create(key.getCurrency().getCode(), key.getName());
  }

  public static VolatilitySurfaceKey toVolatilitySurfaceKey(final String name) {
    final String[] parsed = s_surfaceName.parse(name);
    if (!s_surfaceName.validate(parsed)) {
      return null;
    }
    return new VolatilitySurfaceKey(UniqueId.parse(parsed[0]), parsed[1], parsed[4], parsed[2], parsed[3]);
  }

  public static String fromVolatilitySurfaceKey(final VolatilitySurfaceKey key) {
    return s_surfaceName.create(key.getTarget().toString(), key.getName(), key.getQuoteType(), key.getQuoteUnits(), key.getInstrumentType());
  }

  public static VolatilityCubeKey toVolatilityCubeKey(final String name) {
    final String[] parsed = s_cubeName.parse(name);
    if (!s_cubeName.validate(parsed)) {
      return null;
    }
    return new VolatilityCubeKey(Currency.of(parsed[0]), parsed[1]);
  }

  public static String fromVolatilityCubeKey(final VolatilityCubeKey key) {
    return s_cubeName.create(key.getCurrency().getCode(), key.getName());
  }

}
