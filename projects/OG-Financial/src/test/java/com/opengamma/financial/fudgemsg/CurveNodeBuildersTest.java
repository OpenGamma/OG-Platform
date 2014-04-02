/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CalendarSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.InflationNodeType;
import com.opengamma.financial.analytics.ircurve.strips.PeriodicallyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests builders for curve nodes.
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeBuildersTest extends AnalyticsTestBase {

  @Test
  public void testBillNodeBuilder() {
    BillNode node = new BillNode(Tenor.ONE_YEAR, "TEST");
    assertEquals(node, cycleObject(BillNode.class, node));
    node = new BillNode(Tenor.TWO_YEARS, "TEST", "name");
    assertEquals(node, cycleObject(BillNode.class, node));
  }

  @Test
  public void testBondNodeBuilder() {
    BondNode node = new BondNode(Tenor.ONE_YEAR, "TEST");
    assertEquals(node, cycleObject(BondNode.class, node));
    node = new BondNode(Tenor.TWO_YEARS, "TEST", "name");
    assertEquals(node, cycleObject(BondNode.class, node));
  }

  @Test
  public void testCashNodeBuilder() {
    CashNode node = new CashNode(Tenor.ONE_DAY, Tenor.ONE_YEAR, ExternalId.of("convention", "name"), "TEST");
    assertEquals(node, cycleObject(CashNode.class, node));
    node = new CashNode(Tenor.ONE_DAY, Tenor.ONE_YEAR, ExternalId.of("convention", "name"), "TEST", null);
    assertEquals(node, cycleObject(CashNode.class, node));
    node = new CashNode(Tenor.ONE_DAY, Tenor.ONE_YEAR, ExternalId.of("convention", "name"), "TEST", "Name");
    assertEquals(node, cycleObject(CashNode.class, node));
  }

  @Test
  public void testContinuouslyCompoundedRateNodeBuilder() {
    ContinuouslyCompoundedRateNode node = new ContinuouslyCompoundedRateNode("TEST", Tenor.EIGHT_MONTHS);
    assertEquals(node, cycleObject(ContinuouslyCompoundedRateNode.class, node));
    node = new ContinuouslyCompoundedRateNode("TEST", Tenor.EIGHT_MONTHS, null);
    assertEquals(node, cycleObject(ContinuouslyCompoundedRateNode.class, node));
    node = new ContinuouslyCompoundedRateNode("TEST", Tenor.EIGHT_MONTHS, "Name");
    assertEquals(node, cycleObject(ContinuouslyCompoundedRateNode.class, node));
  }

  @Test
  public void testPeriodicallyCompoundedRateNodeBuilder() {
    PeriodicallyCompoundedRateNode node = new PeriodicallyCompoundedRateNode("TEST", Tenor.EIGHT_MONTHS, 4);
    assertEquals(node, cycleObject(PeriodicallyCompoundedRateNode.class, node));
    node = new PeriodicallyCompoundedRateNode("TEST", Tenor.EIGHT_MONTHS, null, 2);
    assertEquals(node, cycleObject(PeriodicallyCompoundedRateNode.class, node));
    node = new PeriodicallyCompoundedRateNode("TEST", Tenor.EIGHT_MONTHS, "Name", 1);
    assertEquals(node, cycleObject(PeriodicallyCompoundedRateNode.class, node));
  }

  @Test
  public void testCreditCurveNodeBuilder() {
    CreditSpreadNode node = new CreditSpreadNode("TEST", Tenor.EIGHT_MONTHS);
    assertEquals(node, cycleObject(CreditSpreadNode.class, node));
    node = new CreditSpreadNode("TEST", Tenor.EIGHT_MONTHS, null);
    assertEquals(node, cycleObject(CreditSpreadNode.class, node));
    node = new CreditSpreadNode("TEST", Tenor.EIGHT_MONTHS, "Name");
    assertEquals(node, cycleObject(CreditSpreadNode.class, node));
  }

  @Test
  public void testDeliverableSwapFutureNodeBuilder() {
    DeliverableSwapFutureNode node = new DeliverableSwapFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, ExternalId.of("convention", "future"),
        ExternalId.of("convention", "swap"), "TEST");
    assertEquals(node, cycleObject(DeliverableSwapFutureNode.class, node));
    node = new DeliverableSwapFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, ExternalId.of("convention", "future"),
        ExternalId.of("convention", "swap"), "TEST", null);
    assertEquals(node, cycleObject(DeliverableSwapFutureNode.class, node));
    node = new DeliverableSwapFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, ExternalId.of("convention", "future"),
        ExternalId.of("convention", "swap"), "TEST", "Name");
    assertEquals(node, cycleObject(DeliverableSwapFutureNode.class, node));
  }

  @Test
  public void testDiscountFactorNodeBuilder() {
    DiscountFactorNode node = new DiscountFactorNode("TEST", Tenor.EIGHT_MONTHS);
    assertEquals(node, cycleObject(DiscountFactorNode.class, node));
    node = new DiscountFactorNode("TEST", Tenor.EIGHT_MONTHS, null);
    assertEquals(node, cycleObject(DiscountFactorNode.class, node));
    node = new DiscountFactorNode("TEST", Tenor.EIGHT_MONTHS, "Name");
    assertEquals(node, cycleObject(DiscountFactorNode.class, node));
  }

  @Test
  public void testFRANodeBuilder() {
    FRANode node = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, ExternalId.of("convention", "name"), "TEST");
    assertEquals(node, cycleObject(FRANode.class, node));
    node = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, ExternalId.of("convention", "name"), "TEST", null);
    assertEquals(node, cycleObject(FRANode.class, node));
    node = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, ExternalId.of("convention", "name"), "TEST", "Name");
    assertEquals(node, cycleObject(FRANode.class, node));
  }

  @Test
  public void testFXForwardNodeBuilder() {
    FXForwardNode node = new FXForwardNode(Tenor.ONE_DAY, Tenor.TWO_YEARS, ExternalId.of("convention", "name"), Currency.USD, Currency.JPY, "TEST");
    assertEquals(node, cycleObject(FXForwardNode.class, node));
    node = new FXForwardNode(Tenor.ONE_DAY, Tenor.TWO_YEARS, ExternalId.of("convention", "name"), Currency.USD, Currency.JPY, "TEST", null);
    assertEquals(node, cycleObject(FXForwardNode.class, node));
    node = new FXForwardNode(Tenor.ONE_DAY, Tenor.TWO_YEARS, ExternalId.of("convention", "name"), Currency.USD, Currency.JPY, "TEST", "Name");
    assertEquals(node, cycleObject(FXForwardNode.class, node));
  }

  @Test
  public void testIMMFRANodeBuilder() {
    RollDateFRANode node = new RollDateFRANode(Tenor.ONE_DAY, Tenor.ONE_MONTH, 4, 40, ExternalId.of("convention", "ibor"), "TEST");
    assertEquals(node, cycleObject(RollDateFRANode.class, node));
    node = new RollDateFRANode(Tenor.ONE_DAY, Tenor.ONE_MONTH, 4, 40, ExternalId.of("convention", "ibor"), "TEST", "name");
    assertEquals(node, cycleObject(RollDateFRANode.class, node));
  }

  @Test
  public void testIMMSwapNodeBuilder() {
    RollDateSwapNode node = new RollDateSwapNode(Tenor.ONE_DAY, 4, 40, ExternalId.of("convention", "swap"), "TEST");
    assertEquals(node, cycleObject(RollDateSwapNode.class, node));
    node = new RollDateSwapNode(Tenor.ONE_DAY, 4, 40, ExternalId.of("convention", "swap"), true, "TEST");
    assertEquals(node, cycleObject(RollDateSwapNode.class, node));
    node = new RollDateSwapNode(Tenor.ONE_DAY, 4, 40, ExternalId.of("convention", "swap"), "TEST", "name");
    assertEquals(node, cycleObject(RollDateSwapNode.class, node));
    node = new RollDateSwapNode(Tenor.ONE_DAY, 4, 40, ExternalId.of("convention", "swap"), true, "TEST", "name");
    assertEquals(node, cycleObject(RollDateSwapNode.class, node));
  }

  @Test
  public void testRateFutureNodeBuilder() {
    RateFutureNode node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, ExternalId.of("convention", "future"), "TEST");
    assertEquals(node, cycleObject(RateFutureNode.class, node));
    node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, ExternalId.of("convention", "future"), "TEST",
        null);
    assertEquals(node, cycleObject(RateFutureNode.class, node));
    node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, ExternalId.of("convention", "future"), "TEST",
        "Name");
    assertEquals(node, cycleObject(RateFutureNode.class, node));
  }

  @Test
  public void testSwapNodeBuilder() {
    SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"), "TEST");
    assertEquals(node, cycleObject(SwapNode.class, node));
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"), "TEST", null);
    assertEquals(node, cycleObject(SwapNode.class, node));
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"), "TEST", "Name");
    assertEquals(node, cycleObject(SwapNode.class, node));
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"), false, "TEST");
    assertEquals(node, cycleObject(SwapNode.class, node));
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"), false, "TEST", null);
    assertEquals(node, cycleObject(SwapNode.class, node));
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"), false, "TEST", "Name");
    assertEquals(node, cycleObject(SwapNode.class, node));
  }

  @Test
  public void testThreeLegBasisSwapNodeBuilder() {
    ThreeLegBasisSwapNode node = new ThreeLegBasisSwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"),
        ExternalId.of("convention", "spread"), "TEST");
    assertEquals(node, cycleObject(ThreeLegBasisSwapNode.class, node));
    node = new ThreeLegBasisSwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"),
        ExternalId.of("convention", "spread"), "TEST", null);
    assertEquals(node, cycleObject(ThreeLegBasisSwapNode.class, node));
    node = new ThreeLegBasisSwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"),
        ExternalId.of("convention", "spread"), "TEST", "Name");
    assertEquals(node, cycleObject(ThreeLegBasisSwapNode.class, node));
    node = new ThreeLegBasisSwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"),
        ExternalId.of("convention", "spread"), false, "TEST");
    assertEquals(node, cycleObject(ThreeLegBasisSwapNode.class, node));
    node = new ThreeLegBasisSwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"),
        ExternalId.of("convention", "spread"), false, "TEST", null);
    assertEquals(node, cycleObject(ThreeLegBasisSwapNode.class, node));
    node = new ThreeLegBasisSwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, ExternalId.of("convention", "pay"), ExternalId.of("convention", "receive"),
        ExternalId.of("convention", "spread"), false, "TEST", "Name");
    assertEquals(node, cycleObject(ThreeLegBasisSwapNode.class, node));
  }

  @Test
  public void testZeroCouponInflationNodeBuilder() {
    ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.EIGHT_MONTHS, ExternalId.of("convention", "CPI"), ExternalId.of("convention", "Fixed"), InflationNodeType.MONTHLY, "TEST");
    assertEquals(node, cycleObject(ZeroCouponInflationNode.class, node));
    node = new ZeroCouponInflationNode(Tenor.EIGHT_MONTHS, ExternalId.of("convention", "CPI"), ExternalId.of("convention", "Fixed"), InflationNodeType.MONTHLY, "TEST", null);
    assertEquals(node, cycleObject(ZeroCouponInflationNode.class, node));
    node = new ZeroCouponInflationNode(Tenor.EIGHT_MONTHS, ExternalId.of("convention", "CPI"), ExternalId.of("convention", "Fixed"), InflationNodeType.MONTHLY, "TEST", "Name");
    assertEquals(node, cycleObject(ZeroCouponInflationNode.class, node));
  }

  @Test
  public void testCurveNodeWithIdentifiers() {
    final CreditSpreadNode node = new CreditSpreadNode("TEST", Tenor.EIGHT_YEARS);
    final CurveNodeWithIdentifier nodeWithId = new CurveNodeWithIdentifier(node, ExternalSchemes.bloombergTickerSecurityId("AAA"), "Market_Close", DataFieldType.OUTRIGHT);
    assertEquals(nodeWithId, cycleObject(CurveNodeWithIdentifier.class, nodeWithId));
  }

  @Test
  public void testCalendarSwapNode() {
    final CalendarSwapNode node = new CalendarSwapNode("ECB", Tenor.ONE_MONTH, 2, 3, ExternalId.of("id", "swap"), "mapper", "a name");
    assertEquals(node, cycleObject(CalendarSwapNode.class, node));
    final CalendarSwapNode node2 = new CalendarSwapNode("ECB", Tenor.ONE_MONTH, 2, 3, ExternalId.of("id", "swap"), "mapper");
    assertEquals(node2, cycleObject(CalendarSwapNode.class, node2));
  }

}
