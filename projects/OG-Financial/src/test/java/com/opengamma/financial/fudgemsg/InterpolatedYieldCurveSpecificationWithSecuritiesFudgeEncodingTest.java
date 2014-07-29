package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterpolatedYieldCurveSpecificationWithSecuritiesFudgeEncodingTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    ExternalId dummyId = ExternalSchemes.bloombergTickerSecurityId("USDRC Curncy");
    ExternalIdBundle bundle = ExternalIdBundle.of(dummyId);
    final ZonedDateTime start = DateUtils.getUTCDate(2011, 9, 30);
    final ZonedDateTime maturity = DateUtils.getUTCDate(2011, 10, 1);
    final DayCount dayCount = DayCounts.ACT_365;
    final CashSecurity cash = new CashSecurity(Currency.USD, ExternalSchemes.financialRegionId("US"), start, maturity, dayCount, 0.05, 1);
    cash.setUniqueId(UniqueId.of("TEST", "TEST"));
    cash.setName("1m deposit rate");
    cash.setExternalIdBundle(bundle);
    final FixedIncomeStripWithSecurity cashStrip = new FixedIncomeStripWithSecurity(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ONE_MONTH, "DEFAULT"), Tenor.ONE_MONTH, maturity, dummyId, cash);

    dummyId = ExternalSchemes.bloombergTickerSecurityId("EDZ2 Comdty");
    bundle = ExternalIdBundle.of(dummyId);
    final FutureSecurity future = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.now()), "XCSE", "XCSE", Currency.USD, 0, dummyId, "Interest Rate");
    future.setExternalIdBundle(bundle);
    final FixedIncomeStripWithSecurity futureStrip = new FixedIncomeStripWithSecurity(new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.THREE_MONTHS, 2, "DEFAULT"), Tenor.THREE_MONTHS, DateUtils.getUTCDate(2011, 12, 1), dummyId, future);

    dummyId = ExternalSchemes.bloombergTickerSecurityId("USFR0BE Curncy");
    bundle = ExternalIdBundle.of(dummyId);
    final ZonedDateTime startDate = DateUtils.getUTCDate(2011, 11, 1);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2012, 2, 1);
    final ExternalId underlyingIdentifier = ExternalSchemes.bloombergTickerSecurityId("US0003M Index");
    final ZonedDateTime fixingDate = startDate.minusDays(2);
    final FRASecurity fra = new FRASecurity(Currency.USD, ExternalSchemes.financialRegionId("US"), startDate, endDate, 0.05, 1, underlyingIdentifier, fixingDate);
    fra.setExternalIdBundle(bundle);
    final FixedIncomeStripWithSecurity fraStrip = new FixedIncomeStripWithSecurity(new FixedIncomeStrip(StripInstrumentType.FRA_3M, Tenor.FIVE_MONTHS, "DEFAULT"), Tenor.FIVE_MONTHS, endDate, dummyId, fra);

    final Collection<FixedIncomeStripWithSecurity> strips = new ArrayList<FixedIncomeStripWithSecurity>();
    strips.add(cashStrip);
    strips.add(futureStrip);
    strips.add(fraStrip);

    final InterpolatedYieldCurveSpecificationWithSecurities spec = new InterpolatedYieldCurveSpecificationWithSecurities(
        LocalDate.now(), "FUNDING", Currency.USD, Interpolator1DFactory.LINEAR_INSTANCE, true, strips);
    assertEquals(spec, cycleObject(InterpolatedYieldCurveSpecificationWithSecurities.class, spec));
  }

}
