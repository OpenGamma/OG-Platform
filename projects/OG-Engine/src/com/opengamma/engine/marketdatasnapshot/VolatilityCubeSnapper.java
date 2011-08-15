/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdatasnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class VolatilityCubeSnapper extends
    StructuredSnapper<VolatilityCubeKey, VolatilityCubeData, VolatilityCubeSnapshot> {

  public VolatilityCubeSnapper() {
    super(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA);
  }

  @Override
  VolatilityCubeKey getKey(ValueSpecification spec) {
    Currency currency = Currency.parse(spec.getTargetSpecification().getUniqueId().getValue());
    String cube = getSingleProperty(spec, ValuePropertyNames.CUBE);
    return new VolatilityCubeKey(currency, cube);
  }

  @Override
  VolatilityCubeSnapshot buildSnapshot(ViewComputationResultModel resultModel, VolatilityCubeKey key,
      VolatilityCubeData volatilityCubeData) {
    /*
     * TODO: null placeholders for whole of defn
     foreach (var ycp in volatilityCubeDefinition.AllPoints)
    {
        ret.SetPoint(ycp, new ValueSnapshot(null));
    }
    */

    ManageableVolatilityCubeSnapshot ret = new ManageableVolatilityCubeSnapshot();
    ManageableUnstructuredMarketDataSnapshot otherValues = getUnstructured(volatilityCubeData.getOtherData());

    Map<VolatilityPoint, ValueSnapshot> values = new HashMap<VolatilityPoint, ValueSnapshot>();
    for (Entry<VolatilityPoint, Double> ycp : volatilityCubeData.getDataPoints().entrySet()) {
      values.put(ycp.getKey(), new ValueSnapshot(ycp.getValue()));
    }

    Map<Pair<Tenor, Tenor>, ValueSnapshot> strikes = new HashMap<Pair<Tenor, Tenor>, ValueSnapshot>();
    for (Entry<Pair<Tenor, Tenor>, Double> strike : volatilityCubeData.getStrikes().entrySet()) {
      strikes.put(strike.getKey(), new ValueSnapshot(strike.getValue()));
    }

    ret.setOtherValues(otherValues);
    ret.setValues(values);
    ret.setStrikes(strikes);
    return ret;
  }

}
