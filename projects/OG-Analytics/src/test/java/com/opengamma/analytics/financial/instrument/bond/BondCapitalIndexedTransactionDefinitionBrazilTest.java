/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.datasets.StandardTimeSeriesInflationDataSets;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.timeseries.DoubleTimeSeries;

public class BondCapitalIndexedTransactionDefinitionBrazilTest {

  public static final ZonedDateTime VALUATION_DATE = BondCapitalIndexedSecurityDefinitionBrazilTest.VALUATION_DATE;
  private static final ZonedDateTime SETTLE_DATE_STANDARD = VALUATION_DATE.plusDays(1);
  private static final ZonedDateTime SETTLE_DATE_PAST = VALUATION_DATE.minusDays(4);
  private static final ZonedDateTime SETTLE_DATE_FOWARD = VALUATION_DATE.plusDays(7);
  private static final double QUANTITY = 1234;
  private static final double TRADE_PRICE = 2500; // Dirty nominal not price;
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> 
      NTNB_SECURITY_DEFINITION = BondCapitalIndexedSecurityDefinitionBrazilTest.NTNB_SECURITY_DEFINITION;
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> 
      NTNB_TRANSACTION_STD_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(
          NTNB_SECURITY_DEFINITION, QUANTITY, SETTLE_DATE_STANDARD, TRADE_PRICE);
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> 
      NTNB_TRANSACTION_PAS_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(
          NTNB_SECURITY_DEFINITION, QUANTITY, SETTLE_DATE_PAST, TRADE_PRICE);
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> 
      NTNB_TRANSACTION_FWD_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(
          NTNB_SECURITY_DEFINITION, QUANTITY, SETTLE_DATE_FOWARD, TRADE_PRICE);
  public static final DoubleTimeSeries<ZonedDateTime> BR_IPCA = StandardTimeSeriesInflationDataSets.timeSeriesBrCpi(VALUATION_DATE);
  
  @Test
  public void getter() {
    assertEquals("BondCapitalIndexedTransactionDefinitionBrazil: getter", 
        NTNB_SECURITY_DEFINITION, NTNB_TRANSACTION_STD_DEFINITION.getUnderlyingBond());
    assertEquals("BondCapitalIndexedTransactionDefinitionBrazil: getter", 
        QUANTITY, NTNB_TRANSACTION_STD_DEFINITION.getQuantity());
    assertEquals("BondCapitalIndexedTransactionDefinitionBrazil: getter", 
        TRADE_PRICE, NTNB_TRANSACTION_STD_DEFINITION.getPrice());
    assertEquals("BondCapitalIndexedTransactionDefinitionBrazil: getter", 
        SETTLE_DATE_STANDARD, NTNB_TRANSACTION_STD_DEFINITION.getSettlementDate());
    assertEquals("BondCapitalIndexedTransactionDefinitionBrazil: getter", 
        SETTLE_DATE_PAST, NTNB_TRANSACTION_PAS_DEFINITION.getSettlementDate());
    assertEquals("BondCapitalIndexedTransactionDefinitionBrazil: getter", 
        SETTLE_DATE_FOWARD, NTNB_TRANSACTION_FWD_DEFINITION.getSettlementDate());
  }

  @Test
  public void toDerivative() {
    BondCapitalIndexedTransaction<Coupon> transactionStd = 
        NTNB_TRANSACTION_STD_DEFINITION.toDerivative(VALUATION_DATE, BR_IPCA);
    BondCapitalIndexedTransaction<Coupon> transactionPas = 
        NTNB_TRANSACTION_PAS_DEFINITION.toDerivative(VALUATION_DATE, BR_IPCA);
    BondCapitalIndexedTransaction<Coupon> transactionFwd = 
        NTNB_TRANSACTION_FWD_DEFINITION.toDerivative(VALUATION_DATE, BR_IPCA);
    assertEquals("BondCapitalIndexedTransactionDefinitionBrazil: toDerivative", 
        transactionStd.getBondStandard(), transactionStd.getBondTransaction());
    assertEquals("BondCapitalIndexedTransactionDefinitionBrazil: toDerivative", 
        transactionStd.getBondStandard(), transactionPas.getBondStandard());
    assertEquals("BondCapitalIndexedTransactionDefinitionBrazil: toDerivative", 
        transactionStd.getBondStandard(), transactionFwd.getBondStandard());    
  }
  
}
