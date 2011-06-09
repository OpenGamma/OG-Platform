package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

import edu.emory.mathcs.backport.java.util.Collections;

public class InterpolatedYieldCurveSpecificationWithSecuritiesFudgeBuilderTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    @SuppressWarnings("unchecked")
    final IdentifierBundle bundle = IdentifierBundle.of(Collections.singleton(Identifier.of(SecurityUtils.BLOOMBERG_TICKER, "AAPL US Equity")));
    final EquitySecurity equity = new EquitySecurity(UniqueIdentifier.of("TEST", "TEST"), "Apple Inc", "EQUITY", bundle, "Apple Inc", "NASDAQ", "NSDQ", "Apple Inc", Currency.USD,
        GICSCode.getInstance(10203040));
    final FixedIncomeStripWithSecurity strip = new FixedIncomeStripWithSecurity(StripInstrumentType.CASH, Tenor.DAY, Tenor.TWO_DAYS, ZonedDateTime.now(), Identifier.of(SecurityUtils.BLOOMBERG_TICKER,
        "AAPL US Equity"), equity);

    final FutureSecurity future = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.now()), "XCSE", "XCSE", Currency.USD, 0, "LIBOR");
    final FixedIncomeStripWithSecurity futureStrip = new FixedIncomeStripWithSecurity(StripInstrumentType.FUTURE, Tenor.DAY, Tenor.TWO_DAYS, 2, ZonedDateTime.now(), Identifier.of(
        SecurityUtils.BLOOMBERG_TICKER, "AAPL US Equity"), future);

    final Collection<FixedIncomeStripWithSecurity> strips = new ArrayList<FixedIncomeStripWithSecurity>();
    strips.add(strip);
    strips.add(futureStrip);

    final InterpolatedYieldCurveSpecificationWithSecurities spec = new InterpolatedYieldCurveSpecificationWithSecurities(LocalDate.now(), "FUNDING", Currency.USD,
        Interpolator1DFactory.LINEAR_INSTANCE, strips);

    assertEquals(spec, cycleObject(InterpolatedYieldCurveSpecificationWithSecurities.class, spec));
  }
}
