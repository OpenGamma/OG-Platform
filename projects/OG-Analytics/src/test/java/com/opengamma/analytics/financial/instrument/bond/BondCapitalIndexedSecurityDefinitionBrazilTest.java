/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.index.PriceIndexMaster;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.datasets.StandardTimeSeriesInflationDataSets;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.rolldate.EndOfMonthTemporalAdjuster;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the description of Brazil inflation linked bonds.
 */
public class BondCapitalIndexedSecurityDefinitionBrazilTest {
  
  /** Notas do Tesouro Nacional, B - ISIN: BRSTNCNTB096 - 15-Aug-2024 */
  private static final IndexPrice PRICE_INDEX = PriceIndexMaster.getInstance().getIndex("BRIPCA");
  private static final Calendar CALENDAR_BR = new MondayToFridayCalendar("Brazil");
  private static final String BR_GOVT_NAME = "BR GOVT";
  private static final Set<CreditRating> RATING = new HashSet<>();
  static {
    RATING.add(CreditRating.of("BBB", "OG_RATING", true));
  }
  private static final LegalEntity BR_GOVT_LEGAL_ENTITY = new LegalEntity("BRGOVT", BR_GOVT_NAME, RATING, null, null);
  private static final DayCount DAY_COUNT_BR = DayCounts.BUSINESS_252;
  private static final ZonedDateTime INTEREST_ACCRUED_DATE = DateUtils.getUTCDate(2010, 1, 15);
  private static final ZonedDateTime FIRST_COUPON_DATE = DateUtils.getUTCDate(2010, 2, 15);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2024, 8, 15);
  private static final YieldConvention YIELD_CONVENTION = SimpleYieldConvention.BRAZIL_IL_BOND;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final int SETTLEMENT_DAYS = 1;
  private static final double INDEX_START = 1614.62; // June-2000
  private static final double REAL_RATE = 0.06;
  private static final double NOTIONAL_NOTE = 1_000.00;
  private static final Period COUPON_PERIOD = Period.ofMonths(6);
  public static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> 
      NTNB_SECURITY_DEFINITION = BondCapitalIndexSecurityDefinitionUtils.fromBrazilType(PRICE_INDEX, 
          INTEREST_ACCRUED_DATE, INDEX_START, FIRST_COUPON_DATE, MATURITY_DATE, COUPON_PERIOD, NOTIONAL_NOTE, REAL_RATE, 
          BUSINESS_DAY, SETTLEMENT_DAYS, CALENDAR_BR, DAY_COUNT_BR, BR_GOVT_LEGAL_ENTITY);
  public static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 11, 3);
  public static final DoubleTimeSeries<ZonedDateTime> BR_IPCA = StandardTimeSeriesInflationDataSets.timeSeriesBrCpi(VALUATION_DATE);
  
  
  @Test
  public void getter() {
    assertEquals("BondCapitalIndexedSecurityBrazil: getter", 
        YIELD_CONVENTION, NTNB_SECURITY_DEFINITION.getYieldConvention());
    assertEquals("BondCapitalIndexedSecurityBrazil: getter", 
        1, NTNB_SECURITY_DEFINITION.getNominal().getNumberOfPayments());
    int nbCoupon = 30;
    assertEquals("BondCapitalIndexedSecurityBrazil: getter", 
        nbCoupon, NTNB_SECURITY_DEFINITION.getCoupons().getNumberOfPayments());
    for(int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      assertEquals("BondCapitalIndexedSecurityBrazil: getter", 
          NOTIONAL_NOTE, NTNB_SECURITY_DEFINITION.getCoupons().getNthPayment(loopcpn).getNotional());
      assertTrue("BondCapitalIndexedSecurityBrazil: getter", 
          NTNB_SECURITY_DEFINITION.getCoupons().getNthPayment(loopcpn) instanceof CouponInflationZeroCouponMonthlyGearingDefinition);
      ZonedDateTime currentCouponDate = BUSINESS_DAY.adjustDate(CALENDAR_BR, FIRST_COUPON_DATE.plus(COUPON_PERIOD.multipliedBy(loopcpn)));  
      assertEquals("BondCapitalIndexedSecurityBrazil: getter", currentCouponDate.toLocalDate(), 
          NTNB_SECURITY_DEFINITION.getCoupons().getNthPayment(loopcpn).getPaymentDate().toLocalDate());
      assertEquals("BondCapitalIndexedSecurityBrazil: getter", currentCouponDate.toLocalDate().minusMonths(1).
          with(EndOfMonthTemporalAdjuster.getAdjuster()), 
          NTNB_SECURITY_DEFINITION.getCoupons().getNthPayment(loopcpn).getReferenceEndDate().toLocalDate());
    }
  }

  /** Tests the toDerivative for a non-standard settlement date. */
  @Test
  public void toDerivativeNonStandardSettlement() {
    ZonedDateTime settleDate = VALUATION_DATE.plusDays(2);
    BondCapitalIndexedSecurity<Coupon> derivativeConverted = 
        NTNB_SECURITY_DEFINITION.toDerivative(VALUATION_DATE, settleDate, BR_IPCA);
    // TODO: Change time to BUS/252?
    assertEquals("BondCapitalIndexedSecurityBrazil: toDerivative", 
        TimeCalculator.getTimeBetween(VALUATION_DATE, settleDate), derivativeConverted.getSettlementTime());
    // Price is dirty nominal note price and is fixed at trade (not inflated)
    assertTrue("BondCapitalIndexedSecurityBrazil: toDerivative", 
        derivativeConverted.getSettlement() instanceof CouponFixed);
  }

  /** Tests the toDerivative for a standard settlement date (T+1). */
  @Test
  public void toDerivativeStandardSettlement() {
    BondCapitalIndexedSecurity<Coupon> derivativeConverted = 
        NTNB_SECURITY_DEFINITION.toDerivative(VALUATION_DATE, BR_IPCA);
    ZonedDateTime settleDateExpected = ScheduleCalculator.getAdjustedDate(VALUATION_DATE, SETTLEMENT_DAYS, CALENDAR_BR);
    assertEquals("BondCapitalIndexedSecurityBrazil: toDerivative", 
        TimeCalculator.getTimeBetween(VALUATION_DATE, settleDateExpected), derivativeConverted.getSettlementTime());
    // Price is dirty nominal note price and is fixed at trade (not inflated)
    assertTrue("BondCapitalIndexedSecurityBrazil: toDerivative", 
        derivativeConverted.getSettlement() instanceof CouponFixed);
  }
  
}
