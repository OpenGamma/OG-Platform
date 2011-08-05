/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.analytics.ircurve.CurveDefinitionAndSpecifications;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

public class YieldCurveDefinitionTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    final YieldCurveDefinition curveDefinition = new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId(Country.US), "ANNOYING", "STUPID");
    curveDefinition.addStrip(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.DAY, "Convention"));
    assertEquals(curveDefinition, cycleObject(YieldCurveDefinition.class, curveDefinition));
    curveDefinition.addStrip(new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.TWO_YEARS, 3, "CONVENTIONAL"));
    assertEquals(curveDefinition, cycleObject(YieldCurveDefinition.class, curveDefinition));
  }
  
  @Test
  public void testRealCycle() {
    final YieldCurveDefinition curveDefinition = CurveDefinitionAndSpecifications.buildUSDForwardCurveDefinition();
    assertEquals(curveDefinition, cycleObject(YieldCurveDefinition.class, curveDefinition));
    final YieldCurveDefinition curveDefinition2 = CurveDefinitionAndSpecifications.buildUSDFundingCurveDefinition();
    assertEquals(curveDefinition2, cycleObject(YieldCurveDefinition.class, curveDefinition2));
    
  }

}
