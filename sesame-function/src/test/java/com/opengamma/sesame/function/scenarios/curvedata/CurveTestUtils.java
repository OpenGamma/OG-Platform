/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios.curvedata;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.util.time.Tenor;

public class CurveTestUtils {

  public static final ExternalId ID1 = ExternalId.of("scheme", "1");
  public static final ExternalId ID2 = ExternalId.of("scheme", "2");
  public static final ExternalId ID3 = ExternalId.of("scheme", "3");
  public static final ExternalId ID4 = ExternalId.of("scheme", "4");
  public static final FieldName FIELD_NAME = FieldName.of(MarketDataRequirementNames.MARKET_VALUE);
  public static final ImmutableMap<ExternalIdBundle, Double> VALUE_MAP =
      ImmutableMap.of(
          ID1.toBundle(), 0.1,
          ID2.toBundle(), 0.2,
          ID3.toBundle(), 0.7,
          ID4.toBundle(), 0.4);
  public static final CurveNode NODE1 = swapNode(Tenor.ofMonths(1), Tenor.ofMonths(2)); // 3M point
  public static final CurveNode NODE2 = swapNode(Tenor.ofMonths(3), Tenor.ofMonths(3)); // 6M point
  public static final CurveNode NODE3 = futureNode();                                   // 9M point
  public static final CurveNode NODE4 = swapNode(Tenor.ofYears(0), Tenor.ofYears(1));   // 1Y point
  public static final List<CurveNodeWithIdentifier> NODES =
      Lists.newArrayList(
          nodeWithId(ID1, NODE1),
          nodeWithId(ID2, NODE2),
          nodeWithId(ID3, NODE3),
          nodeWithId(ID4, NODE4));
  public static final String CURVE_NAME = "curveName";
  public static final CurveSpecification CURVE_SPEC = new CurveSpecification(LocalDate.now(), CURVE_NAME, NODES);
  public static final double DELTA = 1e-8;

  public static CurveNode swapNode(Tenor start, Tenor maturity) {
    return new SwapNode(
        start,
        maturity,
        ExternalId.of("convention", "payLeg"),
        ExternalId.of("convention", "receiveLeg"),
        "nodeMapper");
  }

  public static CurveNode futureNode() {
    return new RateFutureNode(
        3,
        Tenor.ofMonths(3),
        Tenor.ofMonths(1),
        Tenor.ofMonths(3),
        ExternalId.of("convention", "foo"),
        "nodeMapper");
  }

  public static CurveNodeWithIdentifier nodeWithId(ExternalId id, CurveNode node) {
    return new CurveNodeWithIdentifier(node, id, FIELD_NAME.getName(), DataFieldType.OUTRIGHT);
  }

  public static void checkValues(Map<ExternalIdBundle, Double> shiftedValues,
                                 double node1ExpectedValue,
                                 double node2ExpectedValue,
                                 double node3ExpectedValue,
                                 double node4ExpectedValue) {
    assertEquals(node1ExpectedValue, shiftedValues.get(ID1.toBundle()), DELTA);
    assertEquals(node2ExpectedValue, shiftedValues.get(ID2.toBundle()), DELTA);
    assertEquals(node3ExpectedValue, shiftedValues.get(ID3.toBundle()), DELTA);
    assertEquals(node4ExpectedValue, shiftedValues.get(ID4.toBundle()), DELTA);
  }

  public static void checkValues(SnapshotDataBundle shiftedValues,
                                 double node1ExpectedValue,
                                 double node2ExpectedValue,
                                 double node3ExpectedValue,
                                 double node4ExpectedValue) {
    assertEquals(node1ExpectedValue, shiftedValues.getDataPoint(ID1.toBundle()), DELTA);
    assertEquals(node2ExpectedValue, shiftedValues.getDataPoint(ID2.toBundle()), DELTA);
    assertEquals(node3ExpectedValue, shiftedValues.getDataPoint(ID3.toBundle()), DELTA);
    assertEquals(node4ExpectedValue, shiftedValues.getDataPoint(ID4.toBundle()), DELTA);
  }
}
