/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.CurveNodeId;
import com.opengamma.sesame.TenorCurveNodeId;
import com.opengamma.util.time.Tenor;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.function.scenarios.curvedata.CurveTestUtils;
import com.opengamma.util.test.TestGroup;

import static com.opengamma.sesame.function.scenarios.curvedata.CurveTestUtils.nodeWithId;
import static com.opengamma.sesame.function.scenarios.curvedata.CurveTestUtils.swapNode;

@Test(groups = TestGroup.UNIT)
public class MulticurveInputPointShiftTest {

  @Test
  public void noShift() {
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(nodes()), dataBundle());
    CurveTestUtils.checkValues(inputs.getNodeData(), 0.1, 0.2, 0.7, 0.4);
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class})
  public void usingFuturesAsUnderlyingsNotSupportedAtTheMoment() {
    // shift 6M by 0 bp
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), 0.00
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.absolute(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(CurveTestUtils.NODES), dataBundle());
    // this will throw because the curve contains futures as underlyings. not yet supported.
    shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
  }

  @Test
  public void shiftOfZeroBpAbsoluteDoesNotChangeCurve() {
    // shift 6M by 0 bp
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), 0.00
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.absolute(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(nodes()), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.2, 0.7, 0.4);
  }

  @Test
  public void shiftUpOfTenBpAbsolute() {
    // shift 6M up 10 bp
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), 0.0010
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.absolute(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(nodes()), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.201, 0.7, 0.4);
  }

  @Test
  public void shiftDownOfTenBpAbsolute() {
    // shift 6M down 10 bp
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), -0.0010
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.absolute(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(nodes()), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.199, 0.7, 0.4);
  }

  @Test
  public void steepenCurveAbsolute() {
    // shift 6M down 10 bp, shift 1Y up 10 bp
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), -0.0010,
        TenorCurveNodeId.of(Tenor.ONE_YEAR), 0.0010

    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.absolute(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(nodes()), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.199, 0.7, 0.401);
  }

  @Test
  public void shiftOfZeroPctRelativeDoesNotChangeCurve() {
    // shift 6M by 0%
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), 0.00
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.relative(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(nodes()), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.2, 0.7, 0.4);
  }

  @Test
  public void shiftUpTenPctRelative() {
    // shift 6M up 10%
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), 0.1
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.relative(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(nodes()), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.22, 0.7, 0.4);
  }

  @Test
  public void shiftDownTenPctRelative() {
    // shift 6M up 10%
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), -0.1
    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.relative(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(nodes()), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.18, 0.7, 0.4);
  }

  @Test
  public void steepenCurveRelative() {
    // shift 6M down 10%, shift 1Y up 10%
    Map<CurveNodeId, Double> shifts = ImmutableMap.of(
        (CurveNodeId) TenorCurveNodeId.of(Tenor.SIX_MONTHS), -0.1,
        TenorCurveNodeId.of(Tenor.ONE_YEAR), 0.1

    );
    MulticurveInputPointShift shift = MulticurveInputPointShift.relative(shifts);
    CurveInputs inputs = new CurveInputs(ImmutableSet.copyOf(nodes()), dataBundle());
    CurveInputs shiftedInputs = shift.apply(inputs, StandardMatchDetails.multicurve("not used"));
    CurveTestUtils.checkValues(shiftedInputs.getNodeData(), 0.1, 0.18, 0.7, 0.44);
  }


  private static Set<CurveNodeWithIdentifier> nodes() {
    return Sets.newHashSet(
        nodeWithId(CurveTestUtils.ID1, swapNode(Tenor.ofMonths(0), Tenor.ofMonths(3))),
        nodeWithId(CurveTestUtils.ID2, swapNode(Tenor.ofMonths(3), Tenor.ofMonths(3))),
        nodeWithId(CurveTestUtils.ID3, swapNode(Tenor.ofMonths(6), Tenor.ofMonths(3))),
        nodeWithId(CurveTestUtils.ID4, swapNode(Tenor.ofYears(0), Tenor.ofYears(1)))
    );
  }

  private static SnapshotDataBundle dataBundle() {
    SnapshotDataBundle dataBundle = new SnapshotDataBundle();

    for (Map.Entry<ExternalIdBundle, Double> entry : CurveTestUtils.VALUE_MAP.entrySet()) {
      dataBundle.setDataPoint(entry.getKey(), entry.getValue());
    }
    return dataBundle;
  }
}
