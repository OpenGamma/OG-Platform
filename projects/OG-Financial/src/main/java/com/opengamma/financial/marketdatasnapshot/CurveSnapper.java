/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.impl.ManageableCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;

/**
 * Snapshotter that captures {@code ValueRequirementNames.CURVE_MARKET_DATA}
 */
public class CurveSnapper extends
    StructuredSnapper<CurveKey, SnapshotDataBundle, CurveSnapshot> {

  public CurveSnapper() {
    super(ValueRequirementNames.CURVE_MARKET_DATA);
  }

  @Override
  CurveKey getKey(final ValueSpecification spec) {
    final String curve = getSingleProperty(spec, ValuePropertyNames.CURVE);
    return CurveKey.of(curve);
  }

  @Override
  CurveSnapshot buildSnapshot(final ViewComputationResultModel resultModel, final CurveKey key,
      final SnapshotDataBundle bundle) {
    final ManageableUnstructuredMarketDataSnapshot values = getUnstructured(bundle);
    final ManageableCurveSnapshot ret = new ManageableCurveSnapshot();
    ret.setValues(values);
    ret.setValuationTime(resultModel.getViewCycleExecutionOptions().getValuationTime());
    return ret;
  }
}
