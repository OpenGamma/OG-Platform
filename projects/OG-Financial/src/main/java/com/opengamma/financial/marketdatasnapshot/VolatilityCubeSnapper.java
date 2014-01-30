/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class VolatilityCubeSnapper extends StructuredSnapper<VolatilityCubeKey, VolatilityCubeData<Tenor, Tenor, Double>, VolatilityCubeSnapshot> {

  private final VolatilityCubeDefinitionSource _cubeDefinitionSource;

  public VolatilityCubeSnapper(VolatilityCubeDefinitionSource cubeDefinitionSource) {
    super(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA);
    _cubeDefinitionSource = cubeDefinitionSource;
  }

  @Override
  VolatilityCubeKey getKey(ValueSpecification spec) {
    UniqueId uniqueId = spec.getTargetSpecification().getUniqueId();
    String surface = getSingleProperty(spec, ValuePropertyNames.SURFACE);
    String instrumentType = getSingleProperty(spec, SurfaceAndCubePropertyNames.INSTRUMENT_TYPE);
    String quoteType = getSingleProperty(spec, SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE);
    String quoteUnits = getSingleProperty(spec, SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS);
    return VolatilityCubeKey.of(uniqueId, surface, instrumentType, quoteType, quoteUnits);
  }

  @Override
  VolatilityCubeSnapshot buildSnapshot(ViewComputationResultModel resultModel, VolatilityCubeKey key, VolatilityCubeData<Tenor, Tenor, Double> volatilityCubeData) {

    Map<Triple<Tenor, Tenor, Double>, ValueSnapshot> dict = new HashMap<>();
    for (Tenor x : volatilityCubeData.getXs()) {
      for (Tenor y : volatilityCubeData.getYs()) {
        for (Double z : volatilityCubeData.getZs()) {
          Double volatility = volatilityCubeData.getVolatility(x, y, z);
          Triple<Tenor, Tenor, Double> volKey = Triple.of(x, y, z);
          dict.put(volKey, ValueSnapshot.of(volatility));
        }
      }
    }

    ManageableVolatilityCubeSnapshot ret = new ManageableVolatilityCubeSnapshot();
    ret.setValues(dict);
    return ret;
  }

}
