/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Tenor;

public class FixedIncomeStripWithIdentifierTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    final FixedIncomeStripWithIdentifier strip = new FixedIncomeStripWithIdentifier(StripInstrumentType.CASH, Tenor.DAY, Identifier.of(SecurityUtils.BLOOMBERG_TICKER, "AAPL US Equity"));
    assertEquals(strip, cycleObject(FixedIncomeStripWithIdentifier.class, strip));
    final FixedIncomeStripWithIdentifier futureStrip = new FixedIncomeStripWithIdentifier(StripInstrumentType.FUTURE, Tenor.YEAR, 3, Identifier.of(SecurityUtils.BLOOMBERG_TICKER, "GOOG US Equity"));
    assertEquals(futureStrip, cycleObject(FixedIncomeStripWithIdentifier.class, futureStrip));
  }

}
