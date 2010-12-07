/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import static org.junit.Assert.assertEquals;

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
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.BondFutureCalculator;
import com.opengamma.financial.interestrate.bond.BondPriceCalculator;
import com.opengamma.financial.interestrate.bond.BondYieldCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.payments.FixedPayment;
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
  private static final BondYieldCalculator YIELD_CALCULATOR = new BondYieldCalculator();
  
  private static final HolidaySource HOLIDAY_SOURCE = new MyHolidaySource();
  private static final ConventionBundleSource CONVENTION_SOURCE = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());
  private static final BondSecurityToBondConverter CONVERTER = new BondSecurityToBondConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE);
  private static final BondForwardCalculator CALCULATOR = new BondForwardCalculator(HOLIDAY_SOURCE, CONVENTION_SOURCE);
  
  private static final ZonedDateTime TRADE_DATE = DateUtil.getUTCDate(2001, 12, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2001, 12, 10);
  
  private static final double FUTURE_PRICE = 104.1406;
  private static final ZonedDateTime FIRST_DELIVERY_DATE = DateUtil.getUTCDate(2002,3,01);
  private static final ZonedDateTime LAST_DELIVERY_DATE = DateUtil.getUTCDate(2002,3,28);
  private static final double ACTUAL_REPO = 1.73;
  
  private static final int N_BONDS = 7;
  
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[]{DateUtil.getUTCDate(2010, 2, 15), DateUtil.getUTCDate(2009, 8, 15), DateUtil.getUTCDate(2009, 5, 15),
    DateUtil.getUTCDate(2010, 8, 15),DateUtil.getUTCDate(2008, 11, 15),DateUtil.getUTCDate(2011, 2, 15), DateUtil.getUTCDate(2011, 8, 15)};
  
  
  
  private static double[] COUPON = new double[]{6.5, 6, 5.5, 5.75, 4.75, 5, 5};
 // private static final double[] CLEAN_PRICE = new double[]{109.0391, 105.7891, 102.8672, 104.1797, 98.8203, 98.8828, 98.9688};
  private static final double[] CLEAN_PRICE = new double[]{109.0390625, 105.7890625, 102.8671875, 104.1796875, 98.8203125, 98.8828125, 98.96875};
  private static final double[] CONV_YIELD = new double[]{5.132, 5.079, 5.032, 5.146, 4.953, 5.153,5.136};
  private static final double[] C_FACTOR = new double[]{1.0305, 0.9999, 0.9718, 0.9838, 0.9335, 0.9326, 0.9297};
  private static final double[] GROSS_BASIS = new double[]{1.722, 1.659, 1.663, 1.726, 1.605, 1.761, 2.149};
  private static final double[] NET_BASIS = new double[]{0.373, 0.440, 0.558, 0.573, 0.702, 0.801, 1.190};
  private static final double[] IMPLIED_REPO = new double[]{0.60, 0.35, -0.07, -0.09, -0.63, -0.95, -2.25};

  private static final BondSecurity[] BOND = new BondSecurity[N_BONDS]; 
  
  
  static{
  for(int i = 0; i< N_BONDS; i++){
    
    ZonedDateTime accrualDate = MATURITY_DATE[i].minusYears(11);
    ZonedDateTime firstCouponDate = MATURITY_DATE[i].minusYears(11);
    BOND[i] = new GovernmentBondSecurity("US",
        "Government",
        "US",
        "Treasury",
        Currency.getInstance("USD"),
        YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"),
        new Expiry(MATURITY_DATE[i]),
        "",
        COUPON[i],
        SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME),
        DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"),
        new DateTimeWithZone(accrualDate),
        new DateTimeWithZone(accrualDate),
        new DateTimeWithZone(firstCouponDate),
        100,
        100000000,
        5000,
        1000,
        100,
        100);
    } 
  }
  
  
  
  @Test
  public void TestYield(){
    for(int i=0;i<N_BONDS;i++){
    final Bond bond = CONVERTER.getBond(BOND[i], "some curve", TRADE_DATE);
    double dirtyPrice = BondPriceCalculator.dirtyPrice(bond, CLEAN_PRICE[i]/100.0);
    double yield = YIELD_CALCULATOR.calculate(bond, dirtyPrice);
    yield = 2 * (Math.exp(yield / 2) - 1.0);
    assertEquals(CONV_YIELD[i],100*yield,1e-2); //TODO should have accuracy to 3 dp
    System.out.println("BBG yield: "+CONV_YIELD[i]+", Cal yield: " +100*yield);
    }
  }
  
  @Test
  public void TestGrossBasis(){
    for(int i=0;i<N_BONDS;i++){
    double grossBasis = BondFutureCalculator.grossBasis(CLEAN_PRICE[i]/100.0, FUTURE_PRICE/100.0, C_FACTOR[i]);
    assertEquals(GROSS_BASIS[i], 100.0*grossBasis,1e-3);
    }
  }
  
 
  @Test
  public void testNetBasis() {
    
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("actual/360"); //TODO this needs to be pulled from a convention    
    double deliveryDate = dayCount.getDayCountFraction(SETTLEMENT_DATE, LAST_DELIVERY_DATE);
    
    for(int i=0;i<N_BONDS;i++){
    final Bond bond = CONVERTER.getBond(BOND[i], "some curve", TRADE_DATE);
    final Bond fwdBond = CONVERTER.getBond(BOND[i], "some curve", LAST_DELIVERY_DATE, false);
    
    
    final double fwdDP = CALCULATOR.getForwardDirtyPrice(BOND[i], CLEAN_PRICE[i], SETTLEMENT_DATE, LAST_DELIVERY_DATE, ACTUAL_REPO/100);
    
    final double netBasisFromDates =  fwdDP - (C_FACTOR[i]*FUTURE_PRICE + fwdBond.getAccruedInterest()*100);
    
    double netBasis = BondFutureCalculator.netBasis(bond, deliveryDate, CLEAN_PRICE[i]/100., FUTURE_PRICE/100., C_FACTOR[i], 
        fwdBond.getAccruedInterest(), ACTUAL_REPO/100.0);
    
    assertEquals(NET_BASIS[i],100*netBasis,1e-3);
    
    System.out.println("BBG net basis: "+NET_BASIS[i]+", Cal net basis: " +100*netBasis + " with dates: "+ netBasisFromDates);
    }
  }
  
  
  @Test
  public void testImpliedRepo() {
    
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/360"); //TODO this needs to be pulled from a convention        
    double deliveryDate = dayCount.getDayCountFraction(SETTLEMENT_DATE, LAST_DELIVERY_DATE);
    
    for(int i=0;i<N_BONDS;i++){
    final Bond bond = CONVERTER.getBond(BOND[i], "some curve", TRADE_DATE);
    final Bond fwdBond = CONVERTER.getBond(BOND[i], "some curve", LAST_DELIVERY_DATE, false);
    
    double irr = BondFutureCalculator.impliedRepoRate(bond, deliveryDate, CLEAN_PRICE[i]/100., FUTURE_PRICE/100., C_FACTOR[i], 
        fwdBond.getAccruedInterest());
    
    assertEquals(IMPLIED_REPO[i],100*irr,1e-2);
    }
  }
  
  /**
   * The number in this test are from Bond & Money Markets p586
   */
  @Test
  public void testNetBasis2() {
    
    final ZonedDateTime settlementDate = DateUtil.getUTCDate(2000, 3, 16);
    final ZonedDateTime deliveryDate = DateUtil.getUTCDate(2000, 6, 30);
    final ZonedDateTime maturityDate = DateUtil.getUTCDate(2011, 7, 12);
    final double cleanPrice = 131.4610;
    final double futuresPrice = 112.98;
    final double conversionFactor = 1.1525705;
    final double coupon = 9;
    final int daysToDelivery = 106;
    final double accruedInterest = 1.5780822;
    final double accruedToDelivery = 4.1917808;
    final double repoRate = 0.0624;
    
    
    final ZonedDateTime firstCouponDate = settlementDate.minusDays(64);
    
    assertEquals(daysToDelivery,DateUtil.getDaysBetween(settlementDate, deliveryDate),0);
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/365"); //TODO this needs to be pulled from a convention    
    
   assertEquals(accruedInterest, dayCount.getAccruedInterest(firstCouponDate, settlementDate, firstCouponDate.plusMonths(6), coupon, 2),1e-8);
    
    
   BondSecurity bondSec = new GovernmentBondSecurity("UK",
       "Government",
       "UK",
       "Treasury",
       Currency.getInstance("GBP"),
       YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"),
       new Expiry(maturityDate),
       "",
       coupon,
       SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME),
       DayCountFactory.INSTANCE.getDayCount("Actual/365"),
       new DateTimeWithZone(firstCouponDate),
       new DateTimeWithZone(firstCouponDate),
       new DateTimeWithZone(firstCouponDate),
       100,
       100000000,
       5000,
       1000,
       100,
       100);
   
   
    double t = dayCount.getDayCountFraction(settlementDate, deliveryDate); 

    final Bond bond = CONVERTER.getBond(bondSec, "some curve", settlementDate, false);
    final Bond fwdBond = CONVERTER.getBond(bondSec, "some curve", deliveryDate, false);
    
    assertEquals(accruedInterest, 100*bond.getAccruedInterest(),1e-8);
    assertEquals(accruedToDelivery, 100*fwdBond.getAccruedInterest(),1e-7);
    
    double grossBasis = BondFutureCalculator.grossBasis(cleanPrice, futuresPrice, conversionFactor);
    assertEquals(1.24358491, grossBasis, 1e-8);
    
    double netBasis = BondFutureCalculator.netBasis(bond, t, cleanPrice/100, futuresPrice/100, conversionFactor, accruedToDelivery/100, repoRate);
    assertEquals(1.0407732, 100*netBasis, 1e-7); //NOTE the calculated number in the book is wrong!!!!
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
    final double accruedInterest = (dirtyPrice - cleanPrice);
    final double accruedToDelivery = 2.139946;
    final double repoRate = 0.049;
       
    assertEquals(daysToDelivery,DateUtil.getDaysBetween(settlementDate, deliveryDate),0);
    
    DayCount repoDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/365"); //TODO this needs to be pulled from a convention    
    DayCount accuredInterestDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); //TODO this needs to be pulled from a convention   
    
   assertEquals(accruedInterest, accuredInterestDayCount.getAccruedInterest(firstCouponDate, settlementDate, firstCouponDate.plusMonths(6), coupon, 2),1e-6);
       
   BondSecurity bondSec = new GovernmentBondSecurity("UK",
       "Government",
       "UK",
       "Treasury",
       Currency.getInstance("GBP"),
       YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"),
       new Expiry(maturityDate),
       "",
       coupon,
       SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME),
       accuredInterestDayCount,
       new DateTimeWithZone(firstCouponDate),
       new DateTimeWithZone(firstCouponDate),
       new DateTimeWithZone(firstCouponDate),
       100,
       100000000,
       5000,
       1000,
       100,
       100);
   
    double t = repoDayCount.getDayCountFraction(settlementDate, deliveryDate); 

    final Bond bond = CONVERTER.getBond(bondSec, "some curve", settlementDate, false);
    final Bond fwdBond = CONVERTER.getBond(bondSec, "some curve", deliveryDate, false);
    
    assertEquals(accruedInterest, 100*bond.getAccruedInterest(),1e-6);
    assertEquals(accruedToDelivery, 100*fwdBond.getAccruedInterest(),1e-6);
    
    assertEquals(dirtyPrice, 100*BondPriceCalculator.dirtyPrice(bond, cleanPrice/100.0),1e-7);
    
    double yield = YIELD_CALCULATOR.calculate(bond, dirtyPrice/100);
    yield = 2 * (Math.exp(yield / 2) - 1.0); //convert to semi-annual compounding 
    assertEquals(4.870,100*yield,1e-3);
    
    double grossBasis = BondFutureCalculator.grossBasis(cleanPrice, futuresPrice, conversionFactor);
    assertEquals(0.1154801, grossBasis, 1e-7);
    
    double netBasis = BondFutureCalculator.netBasis(bond, t, cleanPrice/100, futuresPrice/100, conversionFactor, accruedToDelivery/100, repoRate);
    assertEquals(0.0231429, 100*netBasis, 1e-7); //book is slightly out
    
    double irr = BondFutureCalculator.impliedRepoRate(bond, t, cleanPrice/100, futuresPrice/100, conversionFactor, accruedToDelivery/100);
    assertEquals(4.7353923, 100*irr, 1e-7); //again book is slightly out
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
    final int daysToDelivery = 46;
    final double repoRate = 0.049;

    
    DayCount repoDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/365"); //TODO this needs to be pulled from a convention    
    DayCount accuredInterestDayCount = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); //TODO this needs to be pulled from a convention   
    
 
       
   BondSecurity bondSec = new GovernmentBondSecurity("UK",
       "Government",
       "UK",
       "Treasury",
       Currency.getInstance("GBP"),
       YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"),
       new Expiry(maturityDate),
       "",
       coupon,
       SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME),
       accuredInterestDayCount,
       new DateTimeWithZone(firstCouponDate),
       new DateTimeWithZone(firstCouponDate),
       new DateTimeWithZone(firstCouponDate),
       100,
       100000000,
       5000,
       1000,
       100,
       100);
   
    double t = repoDayCount.getDayCountFraction(settlementDate, deliveryDate); 

    final Bond bond = CONVERTER.getBond(bondSec, "some curve", settlementDate, false);
    final Bond fwdBond = CONVERTER.getBond(bondSec, "some curve", deliveryDate, false);
    

    final double dirtyPrice = BondPriceCalculator.dirtyPrice(bond, cleanPrice/100.0);
    
    double yield = YIELD_CALCULATOR.calculate(bond, dirtyPrice);
    yield = 2 * (Math.exp(yield / 2) - 1.0); //convert to semi-annual compounding 
    assertEquals(4.895,100*yield,1e-3);
    
    double grossBasis = BondFutureCalculator.grossBasis(cleanPrice, futuresPrice, conversionFactor);
    assertEquals(2.856, grossBasis, 1e-3);
    
    double netBasis = BondFutureCalculator.netBasis(bond, t, cleanPrice/100, futuresPrice/100, conversionFactor, fwdBond.getAccruedInterest(), repoRate);
    assertEquals(2.665, 100*netBasis, 1e-3);
    
    double irr = BondFutureCalculator.impliedRepoRate(bond, t, cleanPrice/100, futuresPrice/100, conversionFactor, fwdBond.getAccruedInterest());
    assertEquals(-11.23, 100*irr, 1e-2);
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
