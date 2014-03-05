/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.SURFACE_MARKET_DATA;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.SurfaceData;
import com.opengamma.core.marketdatasnapshot.SurfaceKey;
import com.opengamma.core.marketdatasnapshot.SurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableSurfaceSnapshot;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Create a structured snapshot of {@link SurfaceData}.
 */
public class SurfaceSnapper extends StructuredSnapper<SurfaceKey, SurfaceData<Object, Object>, SurfaceSnapshot> {

  /**
   * Sets the requirement name to {@link ValueRequirementNames#SURFACE_MARKET_DATA}
   */
  public SurfaceSnapper() {
    super(SURFACE_MARKET_DATA);
  }

  @Override
  SurfaceKey getKey(final ValueSpecification spec) {
    final String name = getSingleProperty(spec, SURFACE);
    return SurfaceKey.of(name);
  }

  @Override
  SurfaceSnapshot buildSnapshot(final ViewComputationResultModel resultModel, final SurfaceKey key, final SurfaceData<Object, Object> surfaceData) {
    final Map<Pair<Object, Object>, ValueSnapshot> dict = new HashMap<>();
    for (final Object x : surfaceData.getXs()) {
      for (final Object y : surfaceData.getYs()) {
        final Double value = surfaceData.getValue(x, y);
        final Pair<Object, Object> volKey = Pairs.of(x, y);
        dict.put(volKey, ValueSnapshot.of(value));
      }
    }

    final ManageableSurfaceSnapshot ret = new ManageableSurfaceSnapshot();
    ret.setValues(dict);
    return ret;
  }

}
