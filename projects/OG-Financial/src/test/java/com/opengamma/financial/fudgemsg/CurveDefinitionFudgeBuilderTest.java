/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.id.UniqueId;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CurveDefinitionFudgeBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
    final Set<CurveNode> nodes = new TreeSet<>();
    nodes.add(new CreditSpreadNode("X", Tenor.DAY));
    nodes.add(new CreditSpreadNode("X", Tenor.EIGHT_YEARS));
    nodes.add(new CreditSpreadNode("Y", Tenor.ONE_MONTH));
    CurveDefinition definition = new CurveDefinition("NAME", nodes);
    assertEquals(definition, cycleObject(CurveDefinition.class, definition));
    definition = new CurveDefinition("NAME", nodes);
    definition.setUniqueId(UniqueId.of("test", "id"));
    assertEquals(definition, cycleObject(CurveDefinition.class, definition));
  }
}
