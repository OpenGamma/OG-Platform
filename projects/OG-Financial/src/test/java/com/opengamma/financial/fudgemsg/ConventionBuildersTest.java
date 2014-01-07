/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class ConventionBuildersTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void testCMSLegConvention() {
    final CMSLegConvention convention = new CMSLegConvention("EUR CMS", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR CMS")),
        ExternalId.of("Test", "EUR 6m Swap Index"), Tenor.SIX_MONTHS, true);
    convention.setUniqueId(UniqueId.of("Test", "123"));
    assertEncodeDecodeCycle(CMSLegConvention.class, convention);
  }

  @Test
  public void testCompoundingIborLegConvention() {
    final CompoundingIborLegConvention convention = new CompoundingIborLegConvention("EUR Compounded", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR Compounded")),
        ExternalId.of("Test", "EURIBOR 3M"), Tenor.SIX_MONTHS, CompoundingType.FLAT_COMPOUNDING, Tenor.THREE_MONTHS, StubType.SHORT_START, 2, false, StubType.LONG_START, true, 1);
    convention.setUniqueId(UniqueId.of("Test", "12345"));
    assertEncodeDecodeCycle(CompoundingIborLegConvention.class, convention);
  }

  @Test
  public void testDepositConvention() {
    final DepositConvention convention = new DepositConvention("EUR Deposit", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR Deposit")),
        DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 2, true,
        Currency.EUR, ExternalId.of("Test", "EU"));
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEncodeDecodeCycle(DepositConvention.class, convention);
  }

  @Test
  public void testFXForwardAndSwapConvention() {
    final FXForwardAndSwapConvention convention = new FXForwardAndSwapConvention("USD/CAD", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("USD/CAD")),
        ExternalId.of("Test", "FX"), BusinessDayConventions.FOLLOWING, true, ExternalId.of("Test", "US"));
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEncodeDecodeCycle(FXForwardAndSwapConvention.class, convention);
  }

  @Test
  public void testFXSpotConvention() {
    final FXSpotConvention convention = new FXSpotConvention("USD/CAD", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("USD/CAD")),
        1, ExternalId.of("Test", "US"));
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEncodeDecodeCycle(FXSpotConvention.class, convention);
  }

  @Test
  public void testIborIndexConvention() {
    final IborIndexConvention convention = new IborIndexConvention("EUR Deposit", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR Deposit")),
        DayCounts.ACT_365, BusinessDayConventions.FOLLOWING, 2, true,
        Currency.EUR, LocalTime.of(11, 0), "EU", ExternalId.of("Test", "EU"), ExternalId.of("Test", "EU"), "Page");
    convention.setUniqueId(UniqueId.of("Test", "1234567"));
    assertEncodeDecodeCycle(IborIndexConvention.class, convention);
  }

  @Test
  public void testInterestRateFutureConvention() {
    final InterestRateFutureConvention convention = new InterestRateFutureConvention("ER", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("ER")),
        ExternalId.of("Test", "3rd Wednesday"), ExternalId.of("Test", "EUX"), ExternalId.of("Test", "3m Euribor"));
    convention.setUniqueId(UniqueId.of("Test", "123456"));
    assertEncodeDecodeCycle(InterestRateFutureConvention.class, convention);
  }

  @Test
  public void testFederalFundsFutureConvention() {
    final FederalFundsFutureConvention convention = new FederalFundsFutureConvention("FF", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("FF")),
        ExternalId.of("Test", "EOM"), ExternalId.of("Test", "CME"), ExternalId.of("Test", "FF Rate"), 5000000);
    convention.setUniqueId(UniqueId.of("Test", "123456"));
    assertEncodeDecodeCycle(FederalFundsFutureConvention.class, convention);
  }

  @Test
  public void testDeliverablePriceQuotedSwapFutureConvention() {
    final DeliverablePriceQuotedSwapFutureConvention convention = new DeliverablePriceQuotedSwapFutureConvention("T1U", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("T1U")),
        ExternalId.of("Test", "3rd Wednesday"), ExternalId.of("Test", "CME"), ExternalId.of("Test", "Swap"), 100000);
    convention.setUniqueId(UniqueId.of("Test", "123456"));
    assertEncodeDecodeCycle(DeliverablePriceQuotedSwapFutureConvention.class, convention);
  }

  @Test
  public void testIMMFRAConvention() {
    final RollDateFRAConvention convention = new RollDateFRAConvention("IMM FRA", InMemoryConventionBundleMaster.simpleNameSecurityId("IMM FRA").toBundle(),
        ExternalId.of("Test", "Ibor"), ExternalId.of("Test", "IMM dates"));
    convention.setUniqueId(UniqueId.of("Test", "3577"));
    assertEncodeDecodeCycle(RollDateFRAConvention.class, convention);
  }

  @Test
  public void testIMMSwapConvention() {
    final RollDateSwapConvention convention = new RollDateSwapConvention("IMM Swap", InMemoryConventionBundleMaster.simpleNameSecurityId("IMM Swap").toBundle(),
        ExternalId.of("Test", "Pay"), ExternalId.of("Test", "Receive"), ExternalId.of("Test", "IMM dates"));
    convention.setUniqueId(UniqueId.of("Test", "9836"));
    assertEncodeDecodeCycle(RollDateSwapConvention.class, convention);
  }

  @Test
  public void testOISLegConvention() {
    final OISLegConvention convention = new OISLegConvention("EUR OIS", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR OIS")),
        ExternalId.of("Test", "EONIA"), Tenor.SIX_MONTHS, BusinessDayConventions.MODIFIED_FOLLOWING, 1, true, StubType.LONG_START, false, 4);
    convention.setUniqueId(UniqueId.of("Test", "123"));
    assertEncodeDecodeCycle(OISLegConvention.class, convention);
  }

  @Test
  public void testOvernightIndexConvention() {
    final OvernightIndexConvention convention = new OvernightIndexConvention("EONIA", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EONIA")),
        DayCounts.ACT_360, 2, Currency.EUR, ExternalId.of("Test", "EU"));
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEncodeDecodeCycle(OvernightIndexConvention.class, convention);
  }

  @Test
  public void testPriceIndexConvention() {
    final PriceIndexConvention convention = new PriceIndexConvention("CPI", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("CPI")), Currency.USD, ExternalId.of("Region", "US"),
        ExternalId.of("Test", "CPI"));
    convention.setUniqueId(UniqueId.of("Test", "9385"));
    assertEncodeDecodeCycle(PriceIndexConvention.class, convention);
  }

  @Test
  public void testSwapConvention() {
    final SwapConvention convention = new SwapConvention("EUR Swap", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR Swap")),
        ExternalId.of("Test", "EUR Pay Leg"), ExternalId.of("Test", "EUR Receive Leg"));
    convention.setUniqueId(UniqueId.of("Test", "123"));
    assertEncodeDecodeCycle(SwapConvention.class, convention);
  }

  @Test
  public void testSwapFixedLegConvention() {
    final SwapFixedLegConvention convention = new SwapFixedLegConvention("EUR Fixed Leg", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR Fixed Leg")),
        Tenor.THREE_MONTHS, DayCounts.THIRTY_U_360, BusinessDayConventions.FOLLOWING,
        Currency.EUR, ExternalId.of("Test", "EU"), 2, true, StubType.LONG_END, false, 3);
    convention.setUniqueId(UniqueId.of("Test", "123"));
    assertEncodeDecodeCycle(SwapFixedLegConvention.class, convention);
  }

  @Test
  public void testSwapIndexConvention() {
    final SwapIndexConvention convention = new SwapIndexConvention("EUR 3m Swap", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR 3m Swap")),
        LocalTime.of(11, 0), ExternalId.of("Test", "EUR 3m Swap"));
    convention.setUniqueId(UniqueId.of("Test", "12345"));
    assertEncodeDecodeCycle(SwapIndexConvention.class, convention);
  }

  @Test
  public void testVanillaIborLegConvention() {
    final VanillaIborLegConvention convention = new VanillaIborLegConvention("EUR 3m Swap", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR 3m Swap")),
        ExternalId.of("Test", "3m Euribor"), true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, true, StubType.SHORT_END, false, 7);
    convention.setUniqueId(UniqueId.of("Test", "12345"));
    assertEncodeDecodeCycle(VanillaIborLegConvention.class, convention);
  }

  @Test
  public void testInflationLegConvention() {
    final InflationLegConvention convention = new InflationLegConvention("CPI", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("CPI")),
        BusinessDayConventions.FOLLOWING, DayCounts.ACT_360, true, 3, 1, ExternalId.of("Test", "Price"));
    convention.setUniqueId(UniqueId.of("Test", "98657"));
    assertEncodeDecodeCycle(InflationLegConvention.class, convention);
  }
}
