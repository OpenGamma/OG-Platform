/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * Create a structured snapshot of {@link VolatilityCubeData}.
 */
public class VolatilityCubeSnapper extends StructuredSnapper<VolatilityCubeKey, VolatilityCubeData<Tenor, Tenor, Double>, VolatilityCubeSnapshot> {

  /**
   * Sets the requirement name to {@link ValueRequirementNames#VOLATILITY_CUBE_MARKET_DATA}
   */
  public VolatilityCubeSnapper() {
    super(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA);
  }

  @Override
  VolatilityCubeKey getKey(final ValueSpecification spec) {
    final String definition = getSingleProperty(spec, PROPERTY_CUBE_DEFINITION);
    final String specification = getSingleProperty(spec, PROPERTY_CUBE_SPECIFICATION);
    final String quoteType = getSingleProperty(spec, PROPERTY_CUBE_QUOTE_TYPE);
    final String quoteUnits = getSingleProperty(spec, PROPERTY_CUBE_UNITS);
    return VolatilityCubeKey.of(definition, specification, quoteType, quoteUnits);
  }

  @Override
  VolatilityCubeSnapshot buildSnapshot(final ViewComputationResultModel resultModel, final VolatilityCubeKey key, final VolatilityCubeData<Tenor, Tenor, Double> volatilityCubeData) {

    final Map<Triple<Tenor, Tenor, Double>, ValueSnapshot> dict = new HashMap<>();
    for (final Tenor x : volatilityCubeData.getXs()) {
      for (final Tenor y : volatilityCubeData.getYs()) {
        for (final Double z : volatilityCubeData.getZs()) {
          final Double volatility = volatilityCubeData.getVolatility(x, y, z);
          final Triple<Tenor, Tenor, Double> volKey = Triple.of(x, y, z);
          dict.put(volKey, ValueSnapshot.of(volatility));
        }
      }
    }

    final ManageableVolatilityCubeSnapshot ret = new ManageableVolatilityCubeSnapshot();
    ret.setValues(dict);
    return ret;
  }

}
