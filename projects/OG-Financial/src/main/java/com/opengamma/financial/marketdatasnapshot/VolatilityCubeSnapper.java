/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilitySurfaceSnapshot;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class VolatilityCubeSnapper extends
    StructuredSnapper<VolatilityCubeKey, VolatilityCubeData, VolatilityCubeSnapshot> {

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
  VolatilityCubeSnapshot buildSnapshot(ViewComputationResultModel resultModel, VolatilityCubeKey key, VolatilityCubeData volatilityCubeData) {

    Map<Triple<Object, Object, Object>, ValueSnapshot> dict = new HashMap<>();
    for (Object x : volatilityCubeData.getXs()) {
      for (Object y : volatilityCubeData.getYs()) {
        for (Object z : volatilityCubeData.getZs()) {
          Double volatility = volatilityCubeData.getVolatility(x, y, z);
          Triple<Object, Object, Object> volKey = Triple.of(x, y, z);
          dict.put(volKey, ValueSnapshot.of(volatility));
        }
      }
    }

    ManageableVolatilityCubeSnapshot ret = new ManageableVolatilityCubeSnapshot();
    ret.setValues(dict);
    return ret;
  }

}
