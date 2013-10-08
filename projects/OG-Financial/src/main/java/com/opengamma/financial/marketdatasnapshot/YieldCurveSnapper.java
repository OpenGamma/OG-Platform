/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
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
    String curve = getSingleProperty(spec, ValuePropertyNames.CURVE);
    return YieldCurveKey.of(currency, curve);
  }

  @Override
  ManageableYieldCurveSnapshot buildSnapshot(ViewComputationResultModel resultModel, YieldCurveKey key,
      SnapshotDataBundle bundle) {
    ManageableUnstructuredMarketDataSnapshot values = getUnstructured(bundle);
    return ManageableYieldCurveSnapshot.of(resultModel.getViewCycleExecutionOptions().getValuationTime(), values);
  }
}
