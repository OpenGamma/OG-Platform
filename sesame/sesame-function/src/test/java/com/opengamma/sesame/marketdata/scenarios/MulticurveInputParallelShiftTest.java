/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.function.scenarios.curvedata.CurveTestUtils;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class MulticurveInputParallelShiftTest {

  @Test
  public void absolute() {
    double shiftAmount = 0.01;
    MulticurveInputParallelShift shift = MulticurveInputParallelShift.absolute(shiftAmount);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(CurveTestUtils.NODES), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.11, 0.21, 0.69, 0.41);
  }

  @Test
  public void relative() {
    double shiftAmount = 0.1; // shift up 10%
    MulticurveInputParallelShift shift = MulticurveInputParallelShift.relative(shiftAmount);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(CurveTestUtils.NODES), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.11, 0.22, 0.67, 0.44);
  }

  private static SnapshotDataBundle dataBundle() {
    SnapshotDataBundle dataBundle = new SnapshotDataBundle();

    for (Map.Entry<ExternalIdBundle, Double> entry : CurveTestUtils.VALUE_MAP.entrySet()) {
      dataBundle.setDataPoint(entry.getKey(), entry.getValue());
    }
    return dataBundle;
  }
}
