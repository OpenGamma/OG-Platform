/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.interestrate.bond.BondDirtyPriceCalculator;
import com.opengamma.financial.interestrate.bond.BondForwardDirtyPriceCalculator;
import com.opengamma.financial.interestrate.bond.BondFutureGrossBasisCalculator;
import com.opengamma.financial.interestrate.bond.BondFutureImpliedRepoRateCalculator;
import com.opengamma.financial.interestrate.bond.BondFutureNetBasisCalculator;
import com.opengamma.financial.interestrate.bond.BondYieldCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;
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
public class BondFutureTest {
  private static final double EPS = 1e-12;
  private static final String CURVE_NAME = "A";
  private static final BondYieldCalculator YIELD_CALCULATOR = BondYieldCalculator.getInstance();
  private static final BondDirtyPriceCalculator DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final BondForwardDirtyPriceCalculator FORWARD_DIRTY_PRICE_CALCULATOR = BondForwardDirtyPriceCalculator.getInstance();
  private static final BondFutureImpliedRepoRateCalculator IMPLIED_REPO_CALCULATOR = BondFutureImpliedRepoRateCalculator.getInstance();
  private static final BondFutureGrossBasisCalculator GROSS_BASIS_CALCULATOR = BondFutureGrossBasisCalculator.getInstance();
  private static final BondFutureNetBasisCalculator NET_BASIS_CALCULATOR = BondFutureNetBasisCalculator.getInstance();
  private static final HolidaySource HOLIDAY_SOURCE = new MyHolidaySource();
  private static final ConventionBundleSource CONVENTION_SOURCE = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());
  private static final BondSecurityToBondConverter CONVERTER = new BondSecurityToBondConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE);
  private static final BondForwardCalculator BOND_FORWARD_CALCULATOR = new BondForwardCalculator(HOLIDAY_SOURCE, CONVENTION_SOURCE);

  private static final ZonedDateTime TRADE_DATE = DateUtil.getUTCDate(2001, 12, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2001, 12, 10);

  private static final double FUTURE_PRICE = 104.1406;
  //private static final ZonedDateTime FIRST_DELIVERY_DATE = DateUtil.getUTCDate(2002,3,01);
  private static final ZonedDateTime LAST_DELIVERY_DATE = DateUtil.getUTCDate(2002, 3, 28);
  private static final double ACTUAL_REPO = 1.73;

  private static final int N_BONDS = 7;

  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[] {DateUtil.getUTCDate(2010, 2, 15), DateUtil.getUTCDate(2009, 8, 15), DateUtil.getUTCDate(2009, 5, 15),
      DateUtil.getUTCDate(2010, 8, 15), DateUtil.getUTCDate(2008, 11, 15), DateUtil.getUTCDate(2011, 2, 15), DateUtil.getUTCDate(2011, 8, 15)};

  private static double[] COUPON = new double[] {6.5, 6, 5.5, 5.75, 4.75, 5, 5};
  private static final double[] CLEAN_PRICE = new double[] {109.0390625, 105.7890625, 102.8671875, 104.1796875, 98.8203125, 98.8828125, 98.96875};
  private static final double[] CONV_YIELD = new double[] {5.132, 5.079, 5.032, 5.146, 4.953, 5.153, 5.136};
  private static final double[] C_FACTOR = new double[] {1.0305, 0.9999, 0.9718, 0.9838, 0.9335, 0.9326, 0.9297};
  private static final double[] GROSS_BASIS = new double[] {0.01722, 0.01659, 0.01663, 0.01726, 0.01605, 0.01761, 0.02149};
  private static final double[] NET_BASIS = new double[] {0.00373, 0.00440, 0.00558, 0.00573, 0.00702, 0.00801, 0.01190};
  private static final double[] IMPLIED_REPO = new double[] {0.0060, 0.0035, -0.0007, -0.0009, -0.0063, -0.0095, -0.0225};
  private static final BondSecurity[] DELIVERABLE_BONDS = new BondSecurity[N_BONDS];

  static {
    for (int i = 0; i < N_BONDS; i++) {
      final ZonedDateTime accrualDate = MATURITY_DATE[i].minusYears(11);
      final ZonedDateTime firstCouponDate = MATURITY_DATE[i].minusYears(11);
      DELIVERABLE_BONDS[i] = new GovernmentBondSecurity("US", "Government", "US", "Treasury", Currency.getInstance("USD"),
          YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(MATURITY_DATE[i]), "", COUPON[i],
          SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), new DateTimeWithZone(accrualDate),
          new DateTimeWithZone(accrualDate), new DateTimeWithZone(firstCouponDate), 100, 100000000, 5000, 1000, 100, 100);
    }
  }

  //TODO need to remove dependence on BondForwardCalculator
  @Test
  public void testNetBasis() {
    /*final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("actual/360"); //TODO this needs to be pulled from a convention    
    final double deliveryDate = dayCount.getDayCountFraction(SETTLEMENT_DATE, LAST_DELIVERY_DATE);
    for (int i = 0; i < N_BONDS; i++) {
      final Bond bond = CONVERTER.getBond(DELIVERABLE_BONDS[i], CURVE_NAME, TRADE_DATE);
      final Bond fwdBond = CONVERTER.getBond(DELIVERABLE_BONDS[i], CURVE_NAME, LAST_DELIVERY_DATE, false);
      final double fwdDP = BOND_FORWARD_CALCULATOR.getForwardDirtyPrice(DELIVERABLE_BONDS[i], CLEAN_PRICE[i], SETTLEMENT_DATE, LAST_DELIVERY_DATE, ACTUAL_REPO / 100);
      final double netBasisFromDates = fwdDP - (C_FACTOR[i] * FUTURE_PRICE + fwdBond.getAccruedInterest() * 100);
      final double netBasis = BondFutureCalculator1.netBasis(bond, deliveryDate, CLEAN_PRICE[i] / 100., FUTURE_PRICE / 100., C_FACTOR[i], fwdBond.getAccruedInterest(), ACTUAL_REPO / 100.0);
      assertEquals(NET_BASIS[i], netBasis, 1e-3);*/
    //System.out.println("BBG net basis: " + NET_BASIS[i] + ", Cal net basis: " + 100 * netBasis + " with dates: " + netBasisFromDates);
    /*      BBG net basis: 0.00373, Cal net basis: 0.3725698227667662 with dates: 0.3727973031411693
          BBG net basis: 0.0044, Cal net basis: 0.4397521528904713 with dates: 0.4399621347745466
          BBG net basis: 0.00558, Cal net basis: 0.5583204850034384 with dates: 0.5583204850034491
          BBG net basis: 0.00573, Cal net basis: 0.5723618917023554 with dates: 0.5725631243412437
          BBG net basis: 0.00702, Cal net basis: 0.70251527077001 with dates: 0.7025152707700215
          BBG net basis: 0.00801, Cal net basis: 0.8010160356379026 with dates: 0.8011910205413102
          BBG net basis: 0.0119, Cal net basis: 1.1894072912629228 with dates: 1.1895822761663055
    */
    //}
  }

  @Test
  public void testDeliverables() {
    final List<Double> deliveryDates = new ArrayList<Double>();
    final List<Double> cleanPrices = new ArrayList<Double>();
    final List<Double> accruedInterest = new ArrayList<Double>();
    final List<Double> repoRates = new ArrayList<Double>();
    final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("actual/360"); //TODO this needs to be pulled from a convention    
    final double deliveryDate = dayCount.getDayCountFraction(SETTLEMENT_DATE, LAST_DELIVERY_DATE);
    final Bond[] deliverables = new Bond[N_BONDS];
    for (int i = 0; i < N_BONDS; i++) {
      deliveryDates.add(deliveryDate);
      cleanPrices.add(CLEAN_PRICE[i] / 100);
      final Bond fwdBond = CONVERTER.getBond(DELIVERABLE_BONDS[i], CURVE_NAME, LAST_DELIVERY_DATE, false);
      accruedInterest.add(fwdBond.getAccruedInterest());
      repoRates.add(ACTUAL_REPO / 100);
      deliverables[i] = CONVERTER.getBond(DELIVERABLE_BONDS[i], CURVE_NAME, TRADE_DATE);
      final Bond bond = CONVERTER.getBond(DELIVERABLE_BONDS[i], CURVE_NAME, TRADE_DATE);
      final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bond, CLEAN_PRICE[i] / 100.0);
      double yield = YIELD_CALCULATOR.calculate(bond, dirtyPrice);
      yield = 2 * (Math.exp(yield / 2) - 1.0);
      assertEquals(CONV_YIELD[i], 100 * yield, 1e-3);
    }
    final BondFuture bondFuture = new BondFuture(deliverables, C_FACTOR);
    final BondFutureDeliverableBasketDataBundle basket = new BondFutureDeliverableBasketDataBundle(deliveryDates, cleanPrices, accruedInterest, repoRates);
    final double[] grossBasis = GROSS_BASIS_CALCULATOR.calculate(bondFuture, basket, FUTURE_PRICE / 100);
    assertArrayEquals(grossBasis, GROSS_BASIS, 1e-5);
    final double[] netBasis = NET_BASIS_CALCULATOR.calculate(bondFuture, basket, FUTURE_PRICE / 100);
    assertArrayEquals(netBasis, NET_BASIS, 1e-5);
    final double[] irr = IMPLIED_REPO_CALCULATOR.calculate(bondFuture, basket, FUTURE_PRICE / 100);
    assertArrayEquals(irr, IMPLIED_REPO, 1e-4);
  }

  /**
   * The number in this test are from Bond & Money Markets p586
   */
  @Test
  public void testBondFuture1() {
    final ZonedDateTime settlementDate = DateUtil.getUTCDate(2000, 3, 16);
    final ZonedDateTime deliveryDate = DateUtil.getUTCDate(2000, 6, 30);
    final ZonedDateTime maturityDate = DateUtil.getUTCDate(2011, 7, 12);
    final double coupon = 9;
    final int daysToDelivery = 106;
    final ZonedDateTime firstCouponDate = settlementDate.minusDays(64);
    assertEquals(daysToDelivery, DateUtil.getDaysBetween(settlementDate, deliveryDate), 0);
    final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/365"); //TODO this needs to be pulled from a convention
    final BondSecurity bondSec = new GovernmentBondSecurity("UK", "Government", "UK", "Treasury", Currency.getInstance("GBP"),
        YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(maturityDate), "", coupon,
        SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), DayCountFactory.INSTANCE.getDayCount("Actual/365"), new DateTimeWithZone(firstCouponDate),
        new DateTimeWithZone(firstCouponDate), new DateTimeWithZone(firstCouponDate), 100, 100000000, 5000, 1000, 100, 100);
    final double accruedInterestForBond = 1.5780822;
    final double accruedToDelivery = 4.1917808;
    final double repoRate = 0.0624;
    final double cleanPrice = 131.4610;
    final double futuresPrice = 112.98;
    final double conversionFactor = 1.1525705;
    assertEquals(accruedInterestForBond, dayCount.getAccruedInterest(firstCouponDate, settlementDate, firstCouponDate.plusMonths(6), coupon, 2), 1e-8);
    final double t = dayCount.getDayCountFraction(settlementDate, deliveryDate);
    final Bond bond = CONVERTER.getBond(bondSec, CURVE_NAME, settlementDate, false);
    final Bond delivered = CONVERTER.getBond(bondSec, CURVE_NAME, deliveryDate, false);
    assertEquals(accruedInterestForBond, 100 * bond.getAccruedInterest(), 1e-8);
    assertEquals(accruedToDelivery, 100 * delivered.getAccruedInterest(), 1e-7);
    final List<Double> deliveryDates = Arrays.asList(t);
    final List<Double> cleanPrices = Arrays.asList(cleanPrice / 100);
    final List<Double> accruedInterest = Arrays.asList(accruedToDelivery / 100);
    final List<Double> repoRates = Arrays.asList(repoRate);
    final BondFuture bondFuture = new BondFuture(new Bond[] {bond}, new double[] {conversionFactor});
    final BondFutureDeliverableBasketDataBundle basket = new BondFutureDeliverableBasketDataBundle(deliveryDates, cleanPrices, accruedInterest, repoRates);
    final double[] grossBasis = GROSS_BASIS_CALCULATOR.calculate(bondFuture, basket, futuresPrice / 100);
    assertEquals(grossBasis.length, 1);
    assertEquals(1.24358491, 100 * grossBasis[0], 1e-8);
    final double[] netBasis = NET_BASIS_CALCULATOR.calculate(bondFuture, basket, futuresPrice / 100);
    assertEquals(netBasis.length, 1);
    assertEquals(1.0407732, 100 * netBasis[0], 1e-7); //NOTE the calculated number in the book is wrong!!!!
  }

  /**
   * The number in this test are from The Repo Handbook  p426
   */
  @Test
  public void testBasis3() {
    final ZonedDateTime settlementDate = DateUtil.getUTCDate(2001, 8, 13);
    final ZonedDateTime deliveryDate = DateUtil.getUTCDate(2001, 9, 28);
    final ZonedDateTime firstCouponDate = DateUtil.getUTCDate(2001, 5, 25);
    final ZonedDateTime maturityDate = DateUtil.getUTCDate(2010, 11, 25);
    final double cleanPrice = 110.2;
    final double dirtyPrice = 111.5586957;
    final double futuresPrice = 115.94;
    final double conversionFactor = 0.9494956;
    final double coupon = 6.25;
    final int daysToDelivery = 46;
    final double accruedInterestForBond = (dirtyPrice - cleanPrice);
    final double accruedToDelivery = 2.139946;
    final double repoRate = 0.049;
    assertEquals(daysToDelivery, DateUtil.getDaysBetween(settlementDate, deliveryDate), 0);
    final DayCount repoDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/365"); //TODO this needs to be pulled from a convention
    final DayCount accruedInterestDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); //TODO this needs to be pulled from a convention
    assertEquals(accruedInterestForBond, accruedInterestDayCount.getAccruedInterest(firstCouponDate, settlementDate, firstCouponDate.plusMonths(6), coupon, 2), 1e-6);
    final BondSecurity bondSec = new GovernmentBondSecurity("UK", "Government", "UK", "Treasury", Currency.getInstance("GBP"),
        YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(maturityDate), "", coupon,
        SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), accruedInterestDayCount, new DateTimeWithZone(firstCouponDate), new DateTimeWithZone(firstCouponDate),
        new DateTimeWithZone(firstCouponDate), 100, 100000000, 5000, 1000, 100, 100);
    final double t = repoDayCount.getDayCountFraction(settlementDate, deliveryDate);
    final Bond bond = CONVERTER.getBond(bondSec, CURVE_NAME, settlementDate, false);
    final Bond fwdBond = CONVERTER.getBond(bondSec, CURVE_NAME, deliveryDate, false);
    assertEquals(accruedInterestForBond, 100 * bond.getAccruedInterest(), 1e-6);
    assertEquals(accruedToDelivery, 100 * fwdBond.getAccruedInterest(), 1e-6);
    assertEquals(dirtyPrice, 100 * DIRTY_PRICE_CALCULATOR.calculate(bond, cleanPrice / 100.0), 1e-7);
    final List<Double> deliveryDates = Arrays.asList(t);
    final List<Double> cleanPrices = Arrays.asList(cleanPrice / 100);
    final List<Double> accruedInterest = Arrays.asList(accruedToDelivery / 100);
    final List<Double> repoRates = Arrays.asList(repoRate);
    final BondFuture bondFuture = new BondFuture(new Bond[] {bond}, new double[] {conversionFactor});
    double yield = YIELD_CALCULATOR.calculate(bond, dirtyPrice / 100);
    yield = 2 * (Math.exp(yield / 2) - 1.0); //convert to semi-annual compounding
    assertEquals(4.870, 100 * yield, 1e-3);
    final BondFutureDeliverableBasketDataBundle basket = new BondFutureDeliverableBasketDataBundle(deliveryDates, cleanPrices, accruedInterest, repoRates);
    final double[] grossBasis = GROSS_BASIS_CALCULATOR.calculate(bondFuture, basket, futuresPrice / 100);
    assertEquals(grossBasis.length, 1);
    assertEquals(0.1154801, 100 * grossBasis[0], 1e-7);
    final double[] netBasis = NET_BASIS_CALCULATOR.calculate(bondFuture, basket, futuresPrice / 100);
    assertEquals(0.0231429, 100 * netBasis[0], 1e-7); //book is slightly out
    final double[] irr = IMPLIED_REPO_CALCULATOR.calculate(bondFuture, basket, futuresPrice / 100);
    assertEquals(irr.length, 1);
    assertEquals(4.7353923, 100 * irr[0], 1e-7); //again book is slightly out
  }

  /**
   * The number in this test are from The Repo Handbook  p426
   */
  @Test
  public void testBasis4() {
    final ZonedDateTime settlementDate = DateUtil.getUTCDate(2001, 8, 13);
    final ZonedDateTime deliveryDate = DateUtil.getUTCDate(2001, 9, 28);
    final ZonedDateTime firstCouponDate = DateUtil.getUTCDate(2001, 3, 27);
    final ZonedDateTime maturityDate = DateUtil.getUTCDate(2013, 9, 27);
    final double cleanPrice = 128.13;
    final double futuresPrice = 115.94;
    final double conversionFactor = 1.0805114;
    final double coupon = 8;
    //final int daysToDelivery = 46; //TODO why is this here?
    final double repoRate = 0.049;
    final DayCount repoDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/365"); //TODO this needs to be pulled from a convention
    final DayCount accuredInterestDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); //TODO this needs to be pulled from a convention
    final BondSecurity bondSec = new GovernmentBondSecurity("UK", "Government", "UK", "Treasury", Currency.getInstance("GBP"),
        YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(maturityDate), "", coupon,
        SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), accuredInterestDayCount, new DateTimeWithZone(firstCouponDate), new DateTimeWithZone(firstCouponDate),
        new DateTimeWithZone(firstCouponDate), 100, 100000000, 5000, 1000, 100, 100);
    final double t = repoDayCount.getDayCountFraction(settlementDate, deliveryDate);
    final Bond bond = CONVERTER.getBond(bondSec, CURVE_NAME, settlementDate, false);
    final Bond fwdBond = CONVERTER.getBond(bondSec, CURVE_NAME, deliveryDate, false);
    final double dirtyPrice = DIRTY_PRICE_CALCULATOR.calculate(bond, cleanPrice / 100.0);
    double yield = YIELD_CALCULATOR.calculate(bond, dirtyPrice);
    yield = 2 * (Math.exp(yield / 2) - 1.0); //convert to semi-annual compounding
    assertEquals(4.895, 100 * yield, 1e-3);
    final List<Double> deliveryDates = Arrays.asList(t);
    final List<Double> cleanPrices = Arrays.asList(cleanPrice / 100);
    final List<Double> accruedInterest = Arrays.asList(fwdBond.getAccruedInterest());
    final List<Double> repoRates = Arrays.asList(repoRate);
    final BondFuture bondFuture = new BondFuture(new Bond[] {bond}, new double[] {conversionFactor});
    final BondFutureDeliverableBasketDataBundle basket = new BondFutureDeliverableBasketDataBundle(deliveryDates, cleanPrices, accruedInterest, repoRates);
    final double[] grossBasis = GROSS_BASIS_CALCULATOR.calculate(bondFuture, basket, futuresPrice / 100);
    assertEquals(grossBasis.length, 1);
    assertEquals(2.856, 100 * grossBasis[0], 1e-3);
    final double[] netBasis = NET_BASIS_CALCULATOR.calculate(bondFuture, basket, futuresPrice / 100);
    assertEquals(netBasis.length, 1);
    assertEquals(2.665, 100 * netBasis[0], 1e-3); //book is slightly out
    final double[] irr = IMPLIED_REPO_CALCULATOR.calculate(bondFuture, basket, futuresPrice / 100);
    assertEquals(irr.length, 1);
    assertEquals(-11.23, 100 * irr[0], 1e-2); //again book is slightly out
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
