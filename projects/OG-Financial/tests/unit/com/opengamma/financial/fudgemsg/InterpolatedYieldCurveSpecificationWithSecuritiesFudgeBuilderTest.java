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
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
public class InterpolatedYieldCurveSpecificationWithSecuritiesFudgeBuilderTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    final ExternalId dummyId = SecurityUtils.bloombergTickerSecurityId("AAPL US Equity");
    final ExternalIdBundle bundle = ExternalIdBundle.of(dummyId);
    final EquitySecurity equity = new EquitySecurity("NASDAQ", "NSDQ", "Apple Inc", Currency.USD);
    equity.setUniqueId(UniqueId.of("TEST", "TEST"));
    equity.setName("Apple Inc");
    equity.setShortName("Apple Inc");
    equity.setExternalIdBundle(bundle);
    equity.setGicsCode(GICSCode.of("10203040"));
    
    final FixedIncomeStripWithSecurity strip = new FixedIncomeStripWithSecurity(StripInstrumentType.CASH, Tenor.DAY, Tenor.TWO_DAYS,
        ZonedDateTime.now(), dummyId, equity);

    final FutureSecurity future = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.now(), ExpiryAccuracy.DAY_MONTH_YEAR), "XCSE", "XCSE",
        Currency.USD, 0, SecurityUtils.bloombergTickerSecurityId("US0003M Index"));
    final FixedIncomeStripWithSecurity futureStrip = new FixedIncomeStripWithSecurity(StripInstrumentType.FUTURE, Tenor.DAY, Tenor.TWO_DAYS, 2,
        ZonedDateTime.now(), SecurityUtils.bloombergTickerSecurityId("US0003M Index"), future);

    final Collection<FixedIncomeStripWithSecurity> strips = new ArrayList<FixedIncomeStripWithSecurity>();
    strips.add(strip);
    strips.add(futureStrip);

    final InterpolatedYieldCurveSpecificationWithSecurities spec = new InterpolatedYieldCurveSpecificationWithSecurities(
        LocalDate.now(), "FUNDING", Currency.USD, Interpolator1DFactory.LINEAR_INSTANCE, strips);
    assertEquals(spec, cycleObject(InterpolatedYieldCurveSpecificationWithSecurities.class, spec));
  }

}
