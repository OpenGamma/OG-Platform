/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.irs;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.FixedInterestRateSwapLegConvention;
import com.opengamma.financial.convention.FloatingInterestRateSwapLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * MGN-899, tests to confirm that legacy serialized objects can be imported using the latest code.
 * This latest code added new optional fields for effective date and termination date to the swap leg.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateSwapLegSerializationTest extends AbstractFudgeBuilderTestCase {

  private static HashSet<ExternalId> GBLO = Sets.newHashSet(ExternalSchemes.isdaHoliday("GBLO"));
  private static HashSet<ExternalId> USNYGBLO = Sets.newHashSet(ExternalSchemes.isdaHoliday("USNY,GBLO"));

  private static FixedInterestRateSwapLegConvention USD_FIXED_3M_EOM_CONVENTION;
  private static FixedInterestRateSwapLeg USD_FIX_LEG;
  private static FloatingInterestRateSwapLegConvention USD_LIBOR_3M_EOM_CONVENTION;
  private static FloatingInterestRateSwapLeg USD_FLOAT_LEG;

  private static InterestRateSwapSecurity USD_FIX_FLOAT_SWAP;

  private static final BusinessDayConvention MF = BusinessDayConventions.MODIFIED_FOLLOWING;

  static {
    USD_FIXED_3M_EOM_CONVENTION = new FixedInterestRateSwapLegConvention("Test1", ExternalIdBundle.of("Scheme", "TEST FIXED"));
    USD_FIXED_3M_EOM_CONVENTION.setDayCountConvention(DayCounts.ACT_360);
    USD_FIXED_3M_EOM_CONVENTION.setCalculationCalendars(USNYGBLO);
    USD_FIXED_3M_EOM_CONVENTION.setMaturityCalendars(USNYGBLO);
    USD_FIXED_3M_EOM_CONVENTION.setPaymentCalendars(USNYGBLO);
    USD_FIXED_3M_EOM_CONVENTION.setPaymentFrequency(SimpleFrequency.QUARTERLY);
    USD_FIXED_3M_EOM_CONVENTION.setPaymentRelativeTo(DateRelativeTo.END);
    USD_FIXED_3M_EOM_CONVENTION.setSettlementDays(2);
    USD_FIXED_3M_EOM_CONVENTION.setPaymentDayConvention(MF);
    USD_FIXED_3M_EOM_CONVENTION.setCalculationBusinessDayConvention(MF);
    USD_FIXED_3M_EOM_CONVENTION.setCalculationFrequency(SimpleFrequency.QUARTERLY);
    USD_FIXED_3M_EOM_CONVENTION.setMaturityBusinessDayConvention(MF);
    USD_FIXED_3M_EOM_CONVENTION.setRollConvention(RollConvention.EOM);
    USD_FIXED_3M_EOM_CONVENTION.setCompoundingMethod(CompoundingMethod.NONE);

    USD_LIBOR_3M_EOM_CONVENTION = new FloatingInterestRateSwapLegConvention("Test2", ExternalIdBundle.of("Scheme", "USD_LIBOR_3M FIXED"));
    USD_LIBOR_3M_EOM_CONVENTION.setDayCountConvention(DayCounts.ACT_360);
    USD_LIBOR_3M_EOM_CONVENTION.setCalculationCalendars(USNYGBLO);
    USD_LIBOR_3M_EOM_CONVENTION.setMaturityCalendars(USNYGBLO);
    USD_LIBOR_3M_EOM_CONVENTION.setPaymentCalendars(USNYGBLO);
    USD_LIBOR_3M_EOM_CONVENTION.setPaymentFrequency(SimpleFrequency.QUARTERLY);
    USD_LIBOR_3M_EOM_CONVENTION.setPaymentRelativeTo(DateRelativeTo.END);
    USD_LIBOR_3M_EOM_CONVENTION.setSettlementDays(2);
    USD_LIBOR_3M_EOM_CONVENTION.setPaymentDayConvention(MF);
    USD_LIBOR_3M_EOM_CONVENTION.setCalculationBusinessDayConvention(MF);
    USD_LIBOR_3M_EOM_CONVENTION.setCalculationFrequency(SimpleFrequency.QUARTERLY);
    USD_LIBOR_3M_EOM_CONVENTION.setMaturityBusinessDayConvention(MF);
    USD_LIBOR_3M_EOM_CONVENTION.setFixingCalendars(GBLO);
    USD_LIBOR_3M_EOM_CONVENTION.setFixingBusinessDayConvention(BusinessDayConventions.NONE);
    USD_LIBOR_3M_EOM_CONVENTION.setResetFrequency(SimpleFrequency.QUARTERLY);
    USD_LIBOR_3M_EOM_CONVENTION.setResetCalendars(USNYGBLO);
    USD_LIBOR_3M_EOM_CONVENTION.setResetBusinessDayConvention(MF);
    USD_LIBOR_3M_EOM_CONVENTION.setResetRelativeTo(DateRelativeTo.START);
    USD_LIBOR_3M_EOM_CONVENTION.setRollConvention(RollConvention.EOM);
    USD_LIBOR_3M_EOM_CONVENTION.setRateType(FloatingRateType.IBOR);
    USD_LIBOR_3M_EOM_CONVENTION.setCompoundingMethod(CompoundingMethod.NONE);

    USD_FIX_LEG = USD_FIXED_3M_EOM_CONVENTION.toLeg(InterestRateSwapNotional.of(Currency.USD, Lists.newArrayList(LocalDate.MIN, LocalDate.MAX), Lists.newArrayList(1e6, 1e5)), PayReceiveType.PAY, new Rate(0.01234), LocalDate.of(2014, 3, 1), LocalDate.of(2024, 3, 2));

    USD_FLOAT_LEG = USD_LIBOR_3M_EOM_CONVENTION.toLeg(new InterestRateSwapNotional(Currency.USD, 1e6), PayReceiveType.RECEIVE, LocalDate.of(2014, 2, 1), LocalDate.of(2024, 4, 2));

    USD_FIX_FLOAT_SWAP = new InterestRateSwapSecurity(ExternalIdBundle.EMPTY, "a swap",
                                                                           LocalDate.of(2014, 2, 1), LocalDate.of(2024, 4, 2),
                                                                           Sets.newHashSet(USD_FIX_LEG, USD_FLOAT_LEG));
  }

  @Test
  public void testFixedDeserializingLegacySwapLeg() {
    String legacySwapLegWithNoStartDateOrEndDatePath = "security/irs/legacyFixedSwapLegWithNoStartDateOrEndDatePath.xml";
    InputStream is = InterestRateSwapLegSerializationTest.class.getClassLoader().getResourceAsStream(legacySwapLegWithNoStartDateOrEndDatePath);
    FixedInterestRateSwapLeg swapLeg = JodaBeanSerialization.deserializer().xmlReader().read(is, FixedInterestRateSwapLeg.class);
    Assert.assertEquals(swapLeg.getNotional().getInitialAmount(), 1000000D);
    Assert.assertNull(swapLeg.getEffectiveDate());
    Assert.assertNull(swapLeg.getUnadjustedMaturityDate());
  }

  @Test
  public void testFixedDeserializingLatestSwapLeg() {
    String legacySwapLegWithNoStartDateOrEndDatePath = "security/irs/latestFixedSwapLegWithStartDateAndEndDatePath.xml";
    InputStream is = InterestRateSwapLegSerializationTest.class.getClassLoader().getResourceAsStream(legacySwapLegWithNoStartDateOrEndDatePath);
    FixedInterestRateSwapLeg swapLeg = JodaBeanSerialization.deserializer().xmlReader().read(is, FixedInterestRateSwapLeg.class);
    Assert.assertEquals(swapLeg.getNotional().getInitialAmount(), 1000000D);
    Assert.assertNotNull(swapLeg.getEffectiveDate());
    Assert.assertNotNull(swapLeg.getUnadjustedMaturityDate());
    Assert.assertEquals(swapLeg.getEffectiveDate(), LocalDate.of(2014, 3, 1));
    Assert.assertEquals(swapLeg.getUnadjustedMaturityDate(), LocalDate.of(2024, 3, 2));
  }

  @Test
  public void testFloatingDeserializingLegacySwapLeg() {
    String legacySwapLegWithNoStartDateOrEndDatePath = "security/irs/legacyFloatingSwapLegWithNoStartDateOrEndDatePath.xml";
    InputStream is = InterestRateSwapLegSerializationTest.class.getClassLoader().getResourceAsStream(legacySwapLegWithNoStartDateOrEndDatePath);
    FloatingInterestRateSwapLeg swapLeg = JodaBeanSerialization.deserializer().xmlReader().read(is, FloatingInterestRateSwapLeg.class);
    Assert.assertEquals(swapLeg.getNotional().getInitialAmount(), 1000000D);
    Assert.assertNull(swapLeg.getEffectiveDate());
    Assert.assertNull(swapLeg.getUnadjustedMaturityDate());
  }

  @Test
  public void testFloatingDeserializingLatestSwapLeg() {
    String legacySwapLegWithNoStartDateOrEndDatePath = "security/irs/latestFloatingSwapLegWithStartDateAndEndDatePath.xml";
    InputStream is = InterestRateSwapLegSerializationTest.class.getClassLoader().getResourceAsStream(legacySwapLegWithNoStartDateOrEndDatePath);
    FloatingInterestRateSwapLeg swapLeg = JodaBeanSerialization.deserializer().xmlReader().read(is, FloatingInterestRateSwapLeg.class);
    Assert.assertEquals(swapLeg.getNotional().getInitialAmount(), 1000000D);
    Assert.assertNotNull(swapLeg.getEffectiveDate());
    Assert.assertNotNull(swapLeg.getUnadjustedMaturityDate());
    Assert.assertEquals(swapLeg.getEffectiveDate(), LocalDate.of(2014, 2, 1));
    Assert.assertEquals(swapLeg.getUnadjustedMaturityDate(), LocalDate.of(2024, 4, 2));
  }

  @Test
  public void writeOutBean() {
    try {
      String beanToXml = JodaBeanSerialization.serializer(true).xmlWriter().write(USD_FIX_FLOAT_SWAP);
      Files.write(beanToXml, new File("/tmp/foo"), Charset.defaultCharset());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testLegacySwap() {
    String legacySwap = "security/irs/legacySwapWithStartAndEndDate.xml";
    InputStream is = InterestRateSwapLegSerializationTest.class.getClassLoader().getResourceAsStream(legacySwap);
    InterestRateSwapSecurity swap = JodaBeanSerialization.deserializer().xmlReader().read(is, InterestRateSwapSecurity.class);
    Assert.assertEquals(swap.getLegs().size(), 2);
    Assert.assertEquals(swap.getLegs().get(0).getNotional().getInitialAmount(), 1000000D);
    Assert.assertEquals(swap.getLegs().get(1).getNotional().getInitialAmount(), 1000000D);
    Assert.assertNotNull(swap.getEffectiveDate());
    Assert.assertNotNull(swap.getUnadjustedMaturityDate());
    Assert.assertEquals(swap.getEffectiveDate(), LocalDate.of(2014, 2, 1));
    Assert.assertEquals(swap.getUnadjustedMaturityDate(), LocalDate.of(2024, 4, 2));
  }

  @Test
  public void testLatestSwap() {
    String latestSwap = "security/irs/latestSwapWithoutStartOrEndDate.xml";
    InputStream is = InterestRateSwapLegSerializationTest.class.getClassLoader().getResourceAsStream(latestSwap);
    InterestRateSwapSecurity swap = JodaBeanSerialization.deserializer().xmlReader().read(is, InterestRateSwapSecurity.class);

    Assert.assertNotNull(swap.getEffectiveDate());
    Assert.assertNotNull(swap.getUnadjustedMaturityDate());
    Assert.assertEquals(swap.getEffectiveDate(), LocalDate.of(2014, 2, 1));
    Assert.assertEquals(swap.getUnadjustedMaturityDate(), LocalDate.of(2024, 4, 2));

    Assert.assertEquals(swap.getLegs().size(), 2);
    Assert.assertEquals(swap.getLegs().get(0).getNotional().getInitialAmount(), 1000000D);
    Assert.assertEquals(swap.getLegs().get(1).getNotional().getInitialAmount(), 1000000D);

    Assert.assertNotNull(swap.getLegs().get(0).getEffectiveDate());
    Assert.assertNotNull(swap.getLegs().get(0).getUnadjustedMaturityDate());
    Assert.assertEquals(swap.getLegs().get(0).getEffectiveDate(), LocalDate.of(2014, 3, 1));
    Assert.assertEquals(swap.getLegs().get(0).getUnadjustedMaturityDate(), LocalDate.of(2024, 3, 2));

    Assert.assertNotNull(swap.getLegs().get(1).getEffectiveDate());
    Assert.assertNotNull(swap.getLegs().get(1).getUnadjustedMaturityDate());
    Assert.assertEquals(swap.getLegs().get(1).getEffectiveDate(), LocalDate.of(2014, 2, 1));
    Assert.assertEquals(swap.getLegs().get(1).getUnadjustedMaturityDate(), LocalDate.of(2024, 4 , 2));
  }


}
