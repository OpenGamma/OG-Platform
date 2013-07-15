/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.CurveDefinitionAndSpecifications;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class YieldCurveDefinitionFudgeEncodingTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    final YieldCurveDefinition curveDefinition = new YieldCurveDefinition(Currency.USD, ExternalSchemes.countryRegionId(Country.US), "NAME", "LINEAR", "LEFT", "RIGHT", false);
    curveDefinition.addStrip(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.DAY, "Convention"));
    assertEquals(curveDefinition, cycleObject(YieldCurveDefinition.class, curveDefinition));
    curveDefinition.addStrip(new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.TWO_YEARS, 3, "CONVENTIONAL"));
    assertEquals(curveDefinition, cycleObject(YieldCurveDefinition.class, curveDefinition));
  }

  @Test
  public void testRealCycle() {
    final YieldCurveDefinition curveDefinition = CurveDefinitionAndSpecifications.buildUSDThreeMonthForwardCurveDefinition();
    assertEquals(curveDefinition, cycleObject(YieldCurveDefinition.class, curveDefinition));
    final YieldCurveDefinition curveDefinition2 = CurveDefinitionAndSpecifications.buildUSDFundingCurveDefinition();
    assertEquals(curveDefinition2, cycleObject(YieldCurveDefinition.class, curveDefinition2));

  }

}
