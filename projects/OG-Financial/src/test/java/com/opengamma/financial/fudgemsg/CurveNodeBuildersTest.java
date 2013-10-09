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
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeBuildersTest extends AnalyticsTestBase {

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
  public void testRateFutureNodeBuilder() {
    RateFutureNode node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, ExternalId.of("convention", "future"), ExternalId.of("convention", "underlying"), "TEST");
    assertEquals(node, cycleObject(RateFutureNode.class, node));
    node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, ExternalId.of("convention", "future"), ExternalId.of("convention", "underlying"), "TEST",
        null);
    assertEquals(node, cycleObject(RateFutureNode.class, node));
    node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, ExternalId.of("convention", "future"), ExternalId.of("convention", "underlying"), "TEST",
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

}
