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
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CurveNodeBuildersTest extends AnalyticsTestBase {

  @Test
  public void testCreditCurveNodeBuilder() {
    final CreditSpreadNode node = new CreditSpreadNode("TEST", Tenor.EIGHT_MONTHS);
    assertEquals(node, cycleObject(CreditSpreadNode.class, node));
  }

  @Test
  public void testCurveNodeWithIdentifiers() {
    final CreditSpreadNode node = new CreditSpreadNode("TEST", Tenor.EIGHT_YEARS);
    final CurveNodeWithIdentifier nodeWithId = new CurveNodeWithIdentifier(node, ExternalSchemes.bloombergTickerSecurityId("AAA"));
    assertEquals(nodeWithId, cycleObject(CurveNodeWithIdentifier.class, nodeWithId));
  }
}
