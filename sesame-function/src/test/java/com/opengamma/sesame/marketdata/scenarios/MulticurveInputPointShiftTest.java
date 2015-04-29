/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.sesame.CurveNodeId;
import com.opengamma.sesame.TenorCurveNodeId;
import com.opengamma.util.time.Tenor;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.function.scenarios.curvedata.CurveTestUtils;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class MulticurveInputPointShiftTest {

  @Test
  public void noShift() {
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(CurveTestUtils.NODES), dataBundle());
    CurveTestUtils.checkValues(inputs.getNodeData(), 0.1, 0.2, 0.7, 0.4);
  }

  @Test
  public void shiftZeroAbsolute() {
    // shift 6M up 0 bp
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), 0.00
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.absolute(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(CurveTestUtils.NODES), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.2, 0.7, 0.4);
  }

  @Test
  public void shiftTenAbsolute() {
    // shift 6M up 5 bp
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), 0.05
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.absolute(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(CurveTestUtils.NODES), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.25, 0.7, 0.4);
  }

  @Test
  public void shiftZeroRelative() {
    // shift 6M up 0%
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), 0.00
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.relative(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(CurveTestUtils.NODES), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.2, 0.7, 0.4);
  }

  @Test
  public void shiftTenRelative() {
    // shift 6M up 10%
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), 0.1
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.relative(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(CurveTestUtils.NODES), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.22, 0.7, 0.4);
  }

  private static SnapshotDataBundle dataBundle() {
    SnapshotDataBundle dataBundle = new SnapshotDataBundle();

    for (Map.Entry<ExternalIdBundle, Double> entry : CurveTestUtils.VALUE_MAP.entrySet()) {
      dataBundle.setDataPoint(entry.getKey(), entry.getValue());
    }
    return dataBundle;
  }
}
