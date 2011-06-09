/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

import edu.emory.mathcs.backport.java.util.Collections;

public class FixedIncomeStripWithSecurityTest extends FinancialTestBase {
  @Test
  public void testCycle() {
    @SuppressWarnings("unchecked")
    final Identifier dummyId = Identifier.of(SecurityUtils.BLOOMBERG_TICKER, "AAPL US Equity");
    final IdentifierBundle bundle = IdentifierBundle.of(Collections.singleton(dummyId));
    final EquitySecurity equity = new EquitySecurity(UniqueIdentifier.of("TEST", "TEST"), "Apple Inc", "EQUITY", bundle, "Apple Inc", "NASDAQ", "NSDQ", "Apple Inc", Currency.USD,
        GICSCode.getInstance(10203040));
    final FixedIncomeStripWithSecurity strip = new FixedIncomeStripWithSecurity(StripInstrumentType.CASH, Tenor.DAY, Tenor.TWO_DAYS, ZonedDateTime.now(), dummyId, equity);
    assertEquals(strip, cycleObject(FixedIncomeStripWithSecurity.class, strip));
    final FutureSecurity future = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.now()), "XCSE", "XCSE", Currency.USD, 0, "LIBOR");
    final FixedIncomeStripWithSecurity futureStrip = new FixedIncomeStripWithSecurity(StripInstrumentType.FUTURE, Tenor.DAY, Tenor.TWO_DAYS, 2, ZonedDateTime.now(), dummyId, future);
    assertEquals(futureStrip, cycleObject(FixedIncomeStripWithSecurity.class, futureStrip));
  }
}
