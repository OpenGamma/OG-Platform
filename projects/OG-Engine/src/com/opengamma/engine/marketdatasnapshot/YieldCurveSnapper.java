/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdatasnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class YieldCurveSnapper extends
    StructuredSnapper<YieldCurveKey, SnapshotDataBundle, YieldCurveSnapshot> {

  public YieldCurveSnapper() {
    super(ValueRequirementNames.YIELD_CURVE_MARKET_DATA);
  }

  @Override
  YieldCurveKey getKey(ValueSpecification spec) {
    Currency currency = Currency.parse(spec.getTargetSpecification().getUniqueId().getValue());
    Set<String> curves = spec.getProperties().getValues("Curve");
    if (curves.size() != 1) {
      throw new IllegalArgumentException("Couldn't find curve property from " + spec);
    }
    String curve = Iterables.get(curves, 0);
    return new YieldCurveKey(currency, curve);
  }

  @Override
  ManageableYieldCurveSnapshot buildSnapshot(ViewComputationResultModel resultModel, YieldCurveKey key,
      SnapshotDataBundle bundle) {
    ManageableUnstructuredMarketDataSnapshot values = getUnstructured(bundle);
    ManageableYieldCurveSnapshot ret = new ManageableYieldCurveSnapshot();
    ret.setValues(values);
    ret.setValuationTime(resultModel.getValuationTime());
    return ret;
  }

  private ManageableUnstructuredMarketDataSnapshot getUnstructured(SnapshotDataBundle bundle) {
    Set<Entry<UniqueId, Double>> bundlePoints = bundle.getDataPoints().entrySet();
    ImmutableMap<MarketDataValueSpecification, Entry<UniqueId, Double>> bySpec =
      Maps.uniqueIndex(bundlePoints, new Function<Entry<UniqueId, Double>, MarketDataValueSpecification>() {
        @Override
        public MarketDataValueSpecification apply(Entry<UniqueId, Double> from) {
          return new MarketDataValueSpecification(MarketDataValueType.PRIMITIVE, from.getKey());
        }
      });
    Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> data = Maps.transformValues(bySpec,
        new Function<Entry<UniqueId, Double>, Map<String, ValueSnapshot>>() {

          @Override
          public Map<String, ValueSnapshot> apply(Entry<UniqueId, Double> from) {
            HashMap<String, ValueSnapshot> ret = new HashMap<String, ValueSnapshot>();
            ret.put(MarketDataRequirementNames.MARKET_VALUE, new ValueSnapshot(from.getValue()));
            return ret;
          }
        });
    ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();
    snapshot.setValues(data);
    return snapshot;
  }

}
