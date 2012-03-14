/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilitySurfaceSnapshot;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.financial.analytics.volatility.surface.SurfacePropertyNames;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@SuppressWarnings("rawtypes")
public class VolatilitySurfaceSnapper extends
    StructuredSnapper<VolatilitySurfaceKey, VolatilitySurfaceData, VolatilitySurfaceSnapshot> {
  public VolatilitySurfaceSnapper() {
    super(ValueRequirementNames.VOLATILITY_SURFACE_DATA);
  }

  @Override
  VolatilitySurfaceKey getKey(ValueSpecification spec) {
    UniqueId uniqueId = spec.getTargetSpecification().getUniqueId();
    String surface = getSingleProperty(spec, ValuePropertyNames.SURFACE);
    String instrumentType = getSingleProperty(spec, "InstrumentType"); //TODO constant
    String quoteType = getSingleProperty(spec, SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE);
    String quoteUnits = getSingleProperty(spec, SurfacePropertyNames.PROPERTY_SURFACE_UNITS);
    return new VolatilitySurfaceKey(uniqueId, surface, instrumentType, quoteType, quoteUnits);
  }

  @SuppressWarnings("unchecked")
  @Override
  ManageableVolatilitySurfaceSnapshot buildSnapshot(ViewComputationResultModel resultModel, VolatilitySurfaceKey key,
      VolatilitySurfaceData volatilitySurfaceData) {
    Map<Pair<Object, Object>, ValueSnapshot> dict = new HashMap<Pair<Object, Object>, ValueSnapshot>();
    for (Object x : volatilitySurfaceData.getXs()) {
      for (Object y : volatilitySurfaceData.getYs()) {
        Double volatility = volatilitySurfaceData.getVolatility(x, y);
        ObjectsPair<Object, Object> volKey = Pair.of(x, y);
        dict.put(volKey, new ValueSnapshot(volatility));
      }
    }

    ManageableVolatilitySurfaceSnapshot ret = new ManageableVolatilitySurfaceSnapshot();
    ret.setValues(dict);
    return ret;
  }
}
