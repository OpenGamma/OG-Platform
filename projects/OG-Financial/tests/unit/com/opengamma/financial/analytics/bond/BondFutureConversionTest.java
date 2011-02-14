/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondConvention;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.instrument.bond.BondForwardDefinition;
import com.opengamma.financial.instrument.future.BondFutureDefinition;
import com.opengamma.financial.interestrate.bond.BondDirtyPriceCalculator;
import com.opengamma.financial.interestrate.bond.BondFutureGrossBasisCalculator;
import com.opengamma.financial.interestrate.bond.BondFutureImpliedRepoRateCalculator;
import com.opengamma.financial.interestrate.bond.BondFutureNetBasisCalculator;
import com.opengamma.financial.interestrate.bond.BondYieldCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureDeliverableBasketDataBundle;
import com.opengamma.financial.security.DateTimeWithZone;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * The data in this test is from a Bloomberg screen shot show on page 362 of Fixed-income Securities
 */
public class BondFutureConversionTest {
  private static final String CURVE_NAME = "A";
  private static final BondYieldCalculator YIELD_CALCULATOR = BondYieldCalculator.getInstance();
  private static final BondDirtyPriceCalculator DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final BondFutureImpliedRepoRateCalculator IMPLIED_REPO_CALCULATOR = BondFutureImpliedRepoRateCalculator.getInstance();
  private static final BondFutureGrossBasisCalculator GROSS_BASIS_CALCULATOR = BondFutureGrossBasisCalculator.getInstance();
  private static final BondFutureNetBasisCalculator NET_BASIS_CALCULATOR = BondFutureNetBasisCalculator.getInstance();
  private static final HolidaySource HOLIDAY_SOURCE = new MyHolidaySource();
  private static final ConventionBundleSource CONVENTION_SOURCE = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());
  private static final BondSecurityToBondDefinitionConverter BOND_CONVERTER = new BondSecurityToBondDefinitionConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE);
  private static final BondSecurityToBondForwardDefinitionConverter BOND_FORWARD_CONVERTER = new BondSecurityToBondForwardDefinitionConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE);

  private static final ZonedDateTime TRADE_DATE = DateUtil.getUTCDate(2001, 12, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2001, 12, 10);

  private static final double FUTURE_PRICE = 1.041406;
  private static final ZonedDateTime LAST_DELIVERY_DATE = DateUtil.getUTCDate(2002, 3, 28);
  private static final double ACTUAL_REPO = 0.0173;

  private static final int N_BONDS = 7;

  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[] {DateUtil.getUTCDate(2010, 2, 15), DateUtil.getUTCDate(2009, 8, 15), DateUtil.getUTCDate(2009, 5, 15),
      DateUtil.getUTCDate(2010, 8, 15), DateUtil.getUTCDate(2008, 11, 15), DateUtil.getUTCDate(2011, 2, 15), DateUtil.getUTCDate(2011, 8, 15)};

  private static double[] COUPON = new double[] {0.065, 0.06, 0.055, 0.0575, 0.0475, 0.05, 0.05};
  private static final double[] CLEAN_PRICE = new double[] {1.090390625, 1.057890625, 1.028671875, 1.041796875, .988203125, .988828125, .9896875};
  private static final double[] CONV_YIELD = new double[] {0.05132, 0.05079, 0.05032, 0.05146, 0.04953, 0.05153, 0.05136};
  private static final double[] C_FACTOR = new double[] {1.0305, 0.9999, 0.9718, 0.9838, 0.9335, 0.9326, 0.9297};
  private static final double[] GROSS_BASIS = new double[] {0.01722, 0.01659, 0.01663, 0.01726, 0.01605, 0.01761, 0.02149};
  private static final double[] NET_BASIS = new double[] {0.00373, 0.00440, 0.00558, 0.00573, 0.00702, 0.00801, 0.01190};
  private static final double[] IMPLIED_REPO = new double[] {0.0060, 0.0035, -0.0007, -0.0009, -0.0063, -0.0095, -0.0225};
  private static final BondSecurity[] DELIVERABLE_BONDS = new BondSecurity[N_BONDS];

  static {
    for (int i = 0; i < N_BONDS; i++) {
      final ZonedDateTime accrualDate = MATURITY_DATE[i].minusYears(11);
      final ZonedDateTime firstCouponDate = MATURITY_DATE[i].minusYears(11).plusMonths(6);
      DELIVERABLE_BONDS[i] = new GovernmentBondSecurity("US", "Government", "US", "Treasury", Currency.getInstance("USD"),
          YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(MATURITY_DATE[i]), "", COUPON[i] * 100,
          SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), new DateTimeWithZone(accrualDate),
          new DateTimeWithZone(accrualDate), new DateTimeWithZone(firstCouponDate), 100, 100000000, 5000, 1000, 100, 100);
    }
  }

  @Test
  public void testDeliverables() {
    final double[] repoRates = new double[N_BONDS];
    Arrays.fill(repoRates, ACTUAL_REPO);
    final BondDefinition[] deliverables = new BondDefinition[N_BONDS];
    for (int i = 0; i < N_BONDS; i++) {
      final BondForwardDefinition bondForwardDefinition = BOND_FORWARD_CONVERTER.getBondForward(DELIVERABLE_BONDS[i], LAST_DELIVERY_DATE);
      deliverables[i] = bondForwardDefinition.getUnderlyingBond();
      final Bond bond = BOND_CONVERTER.getBond(DELIVERABLE_BONDS[i], true).toDerivative(TRADE_DATE.toLocalDate(), CURVE_NAME);
      final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bond, CLEAN_PRICE[i]);
      double yield = YIELD_CALCULATOR.calculate(bond, dirtyPrice);
      yield = 2 * (Math.exp(yield / 2) - 1.0);
      assertEquals(CONV_YIELD[i], yield, 1e-3);
    }
    final Identifier id = Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_BOND_FUTURE_DELIVERABLE_CONVENTION");
    final ConventionBundle conventionBundle = CONVENTION_SOURCE.getConventionBundle(id);
    final BusinessDayConvention businessDayConvention = conventionBundle.getBusinessDayConvention();
    final Calendar calendar = new HolidaySourceCalendarAdapter(HOLIDAY_SOURCE, Currency.getInstance("USD"));
    final BondConvention convention = new BondConvention(conventionBundle.getSettlementDays(), conventionBundle.getDayCount(), businessDayConvention, calendar, conventionBundle.isEOMConvention(),
        "USD_BOND_FUTURE_DELIVERABLE", conventionBundle.getExDividendDays(), conventionBundle.getYieldConvention());
    final BondFutureDefinition bondFutureDefinition = new BondFutureDefinition(deliverables, C_FACTOR, convention, LAST_DELIVERY_DATE.toLocalDate());
    final BondFuture bondFuture = bondFutureDefinition.toDerivative(TRADE_DATE.toLocalDate(), FUTURE_PRICE, CURVE_NAME);
    final BondFutureDeliverableBasketDataBundle basketData = new BondFutureDeliverableBasketDataBundle(CLEAN_PRICE, repoRates);
    final double[] grossBasis = GROSS_BASIS_CALCULATOR.calculate(bondFuture, basketData);
    assertArrayEquals(grossBasis, GROSS_BASIS, 1e-5);
    final double[] netBasis = NET_BASIS_CALCULATOR.calculate(bondFuture, basketData);
    assertArrayEquals(netBasis, NET_BASIS, 1e-5);
    final double[] impliedRepo = IMPLIED_REPO_CALCULATOR.calculate(bondFuture, basketData);
    assertArrayEquals(impliedRepo, IMPLIED_REPO, 1e-4);
  }

  /**
    * The numbers in this test are from Bond & Money Markets p586
    */

  @Test
  public void testBondFuture1() {
    final ZonedDateTime date = DateUtil.getUTCDate(2000, 3, 15);
    final ZonedDateTime tradeDate = DateUtil.getUTCDate(2000, 3, 15);
    final ZonedDateTime settlementDate = DateUtil.getUTCDate(2000, 3, 16);
    final ZonedDateTime deliveryDate = DateUtil.getUTCDate(2000, 6, 30);
    final ZonedDateTime maturityDate = DateUtil.getUTCDate(2011, 7, 12);
    final double coupon = 0.09;
    final int daysToDelivery = 106;
    final ZonedDateTime firstCouponDate = settlementDate.minusDays(64);
    assertEquals(daysToDelivery, DateUtil.getDaysBetween(settlementDate, deliveryDate), 0);
    final DayCount bondDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    final BondSecurity bondSecurity = new GovernmentBondSecurity("UK", "Government", "UK", "Treasury", Currency.getInstance("GBP"),
        YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(maturityDate), "", coupon * 100,
        SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), bondDayCount, new DateTimeWithZone(firstCouponDate), new DateTimeWithZone(firstCouponDate),
        new DateTimeWithZone(firstCouponDate), 100, 100000000, 5000, 1000, 100, 100);
    final double accruedInterestForBond = 0.015780822;
    final double accruedToDelivery = 0.041917808;
    final double repoRate = 0.0624;
    final double cleanPrice = 1.314610;
    final double futuresPrice = 1.1298;
    final double conversionFactor = 1.1525705;
    assertEquals(accruedInterestForBond, bondDayCount.getAccruedInterest(firstCouponDate, settlementDate, firstCouponDate.plusMonths(6), coupon, 2), 1e-8);
    final BondForwardDefinition delivered = BOND_FORWARD_CONVERTER.getBondForward(bondSecurity, deliveryDate);
    assertEquals(accruedInterestForBond, delivered.getUnderlyingBond().toDerivative(tradeDate.toLocalDate(), CURVE_NAME).getAccruedInterest(), 1e-8);
    assertEquals(accruedToDelivery, delivered.toDerivative(settlementDate.toLocalDate(), CURVE_NAME).getAccruedInterestAtDelivery(), 1e-7);
    final double[] repoRates = new double[] {repoRate};
    final double[] cleanPrices = new double[] {cleanPrice};
    final BondFutureDeliverableBasketDataBundle basket = new BondFutureDeliverableBasketDataBundle(cleanPrices, repoRates);
    final Identifier id = Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_BOND_FUTURE_DELIVERABLE_CONVENTION");
    final ConventionBundle conventionBundle = CONVENTION_SOURCE.getConventionBundle(id);
    final BusinessDayConvention businessDayConvention = conventionBundle.getBusinessDayConvention();
    final Calendar calendar = new HolidaySourceCalendarAdapter(HOLIDAY_SOURCE, Currency.getInstance("GBP"));
    final BondConvention convention = new BondConvention(conventionBundle.getSettlementDays(), conventionBundle.getDayCount(), businessDayConvention, calendar, conventionBundle.isEOMConvention(),
        "GBP_BOND_FUTURE_DELIVERABLE", conventionBundle.getExDividendDays(), conventionBundle.getYieldConvention());
    final BondFutureDefinition bondFutureDefinition = new BondFutureDefinition(new BondDefinition[] {delivered.getUnderlyingBond()}, new double[] {conversionFactor}, convention,
        deliveryDate.toLocalDate());
    final BondFuture bondFuture = bondFutureDefinition.toDerivative(date.toLocalDate(), futuresPrice, CURVE_NAME);
    final double[] grossBasis = GROSS_BASIS_CALCULATOR.calculate(bondFuture, basket);
    final double[] netBasis = NET_BASIS_CALCULATOR.calculate(bondFuture, basket);
    assertEquals(grossBasis[0], 0.0124358491, 1e-10);
    assertEquals(netBasis[0], 0.010407732, 1e-9); //NOTE the calculated number in the book is wrong!!!!
  }

  /**
    * The number in this test are from The Repo Handbook  p426
  */

  @Test
  public void testBasis3() {
    final ZonedDateTime date = DateUtil.getUTCDate(2001, 8, 10);
    final ZonedDateTime tradeDate = DateUtil.getUTCDate(2001, 8, 12);
    final ZonedDateTime settlementDate = DateUtil.getUTCDate(2001, 8, 13);
    final ZonedDateTime deliveryDate = DateUtil.getUTCDate(2001, 9, 28);
    final ZonedDateTime firstCouponDate = DateUtil.getUTCDate(2001, 5, 25);
    final ZonedDateTime maturityDate = DateUtil.getUTCDate(2010, 11, 25);
    final double cleanPrice = 1.102;
    final double dirtyPrice = 1.115586957;
    final double futuresPrice = 1.1594;
    final double conversionFactor = 0.9494956;
    final double coupon = 0.0625;
    final int daysToDelivery = 46;
    final double accruedInterestForBond = dirtyPrice - cleanPrice;
    final double accruedToDelivery = 0.02139946;
    final double repoRate = 0.049;
    assertEquals(daysToDelivery, DateUtil.getDaysBetween(settlementDate, deliveryDate), 0);
    final DayCount accruedInterestDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
    assertEquals(accruedInterestForBond, accruedInterestDayCount.getAccruedInterest(firstCouponDate, settlementDate, firstCouponDate.plusMonths(6), coupon, 2), 1e-6);
    final BondSecurity bondSecurity = new GovernmentBondSecurity("UK", "Government", "UK", "Treasury", Currency.getInstance("GBP"),
        YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(maturityDate), "", coupon * 100,
        SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), accruedInterestDayCount, new DateTimeWithZone(firstCouponDate), new DateTimeWithZone(firstCouponDate),
        new DateTimeWithZone(firstCouponDate), 100, 100000000, 5000, 1000, 100, 100);
    final BondForwardDefinition delivered = BOND_FORWARD_CONVERTER.getBondForward(bondSecurity, deliveryDate);
    final Bond bond = delivered.getUnderlyingBond().toDerivative(tradeDate.toLocalDate(), CURVE_NAME);
    final BondForward bondForward = delivered.toDerivative(settlementDate.toLocalDate(), CURVE_NAME);
    assertEquals(accruedInterestForBond, bond.getAccruedInterest(), 1e-6);
    assertEquals(accruedToDelivery, bondForward.getAccruedInterestAtDelivery(), 1e-6);
    assertEquals(dirtyPrice, DIRTY_PRICE_CALCULATOR.calculate(bond, cleanPrice), 1e-7);
    double yield = YIELD_CALCULATOR.calculate(bond, dirtyPrice);
    yield = 2 * (Math.exp(yield / 2) - 1.0); //convert to semi-annual compounding
    assertEquals(0.04870, yield, 1e-5);
    final double[] repoRates = new double[] {repoRate};
    final double[] cleanPrices = new double[] {cleanPrice};
    final BondFutureDeliverableBasketDataBundle basket = new BondFutureDeliverableBasketDataBundle(cleanPrices, repoRates);
    final Identifier id = Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_BOND_FUTURE_DELIVERABLE_CONVENTION");
    final ConventionBundle conventionBundle = CONVENTION_SOURCE.getConventionBundle(id);
    final BusinessDayConvention businessDayConvention = conventionBundle.getBusinessDayConvention();
    final Calendar calendar = new HolidaySourceCalendarAdapter(HOLIDAY_SOURCE, Currency.getInstance("GBP"));
    final BondConvention convention = new BondConvention(conventionBundle.getSettlementDays(), conventionBundle.getDayCount(), businessDayConvention, calendar, conventionBundle.isEOMConvention(),
        "GBP_BOND_FUTURE_DELIVERABLE", conventionBundle.getExDividendDays(), conventionBundle.getYieldConvention());
    final BondFutureDefinition bondFutureDefinition = new BondFutureDefinition(new BondDefinition[] {delivered.getUnderlyingBond()}, new double[] {conversionFactor}, convention,
        deliveryDate.toLocalDate());
    final BondFuture bondFuture = bondFutureDefinition.toDerivative(date.toLocalDate(), futuresPrice, CURVE_NAME);
    final double[] grossBasis = GROSS_BASIS_CALCULATOR.calculate(bondFuture, basket);
    final double[] netBasis = NET_BASIS_CALCULATOR.calculate(bondFuture, basket);
    final double[] irr = IMPLIED_REPO_CALCULATOR.calculate(bondFuture, basket);
    assertEquals(grossBasis.length, 1);
    assertEquals(0.1154801, 100 * grossBasis[0], 1e-7);
    assertEquals(netBasis.length, 1);
    assertEquals(0.0231432, 100 * netBasis[0], 1e-7);
    assertEquals(irr.length, 1);
    assertEquals(0.04735390, irr[0], 1e-7);
  }

  /**
    * The number in this test are from The Repo Handbook  p426
    */
  @Test
  public void testBasis4() {
    final ZonedDateTime date = DateUtil.getUTCDate(2001, 8, 10);
    final ZonedDateTime settlementDate = DateUtil.getUTCDate(2001, 8, 13);
    final ZonedDateTime deliveryDate = DateUtil.getUTCDate(2001, 9, 28);
    final ZonedDateTime firstCouponDate = DateUtil.getUTCDate(2001, 3, 27);
    final ZonedDateTime maturityDate = DateUtil.getUTCDate(2013, 9, 27);
    final double cleanPrice = 1.2813;
    final double futuresPrice = 1.1594;
    final double conversionFactor = 1.0805114;
    final double coupon = 0.08;
    final double repoRate = 0.049;
    final DayCount accruedInterestDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
    final BondSecurity bondSecurity = new GovernmentBondSecurity("UK", "Government", "UK", "Treasury", Currency.getInstance("GBP"),
        YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(maturityDate), "", coupon * 100,
        SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), accruedInterestDayCount, new DateTimeWithZone(firstCouponDate), new DateTimeWithZone(firstCouponDate),
        new DateTimeWithZone(firstCouponDate), 100, 100000000, 5000, 1000, 100, 100);
    final BondForwardDefinition delivered = BOND_FORWARD_CONVERTER.getBondForward(bondSecurity, deliveryDate);
    final Bond bond = delivered.getUnderlyingBond().toDerivative(settlementDate.toLocalDate(), CURVE_NAME);
    final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bond, cleanPrice);
    double yield = YIELD_CALCULATOR.calculate(bond, dirtyPrice);
    yield = 2 * (Math.exp(yield / 2) - 1.0); //convert to semi-annual compounding
    assertEquals(0.04895, yield, 1e-5);
    final double[] repoRates = new double[] {repoRate};
    final double[] cleanPrices = new double[] {cleanPrice};
    final BondFutureDeliverableBasketDataBundle basket = new BondFutureDeliverableBasketDataBundle(cleanPrices, repoRates);
    final Identifier id = Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_BOND_FUTURE_DELIVERABLE_CONVENTION");
    final ConventionBundle conventionBundle = CONVENTION_SOURCE.getConventionBundle(id);
    final BusinessDayConvention businessDayConvention = conventionBundle.getBusinessDayConvention();
    final Calendar calendar = new HolidaySourceCalendarAdapter(HOLIDAY_SOURCE, Currency.getInstance("GBP"));
    final BondConvention convention = new BondConvention(conventionBundle.getSettlementDays(), conventionBundle.getDayCount(), businessDayConvention, calendar, conventionBundle.isEOMConvention(),
        "GBP_BOND_FUTURE_DELIVERABLE", conventionBundle.getExDividendDays(), conventionBundle.getYieldConvention());
    final BondFutureDefinition bondFutureDefinition = new BondFutureDefinition(new BondDefinition[] {delivered.getUnderlyingBond()}, new double[] {conversionFactor}, convention,
        deliveryDate.toLocalDate());
    final BondFuture bondFuture = bondFutureDefinition.toDerivative(date.toLocalDate(), futuresPrice, CURVE_NAME);
    final double[] grossBasis = GROSS_BASIS_CALCULATOR.calculate(bondFuture, basket);
    final double[] netBasis = NET_BASIS_CALCULATOR.calculate(bondFuture, basket);
    final double[] irr = IMPLIED_REPO_CALCULATOR.calculate(bondFuture, basket);
    assertEquals(grossBasis.length, 1);
    assertEquals(2.856, 100 * grossBasis[0], 1e-3);
    assertEquals(netBasis.length, 1);
    assertEquals(2.665, 100 * netBasis[0], 1e-3);
    assertEquals(irr.length, 1);
    assertEquals(-0.1123, irr[0], 1e-4);
  }

  private static class MyHolidaySource implements HolidaySource {

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final IdentifierBundle regionOrExchangeIds) {
      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final Identifier regionOrExchangeId) {
      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public Holiday getHoliday(final UniqueIdentifier uid) {
      return null;
    }
  }

}
