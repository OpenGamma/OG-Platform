/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.BondFuturesDataSets;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesExpStrikeProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * End-to-end test used for demo and integration checks. Previous run hard-coded numbers. 
 */
public class BondFuturesOptionPremiumTransactionBlackExpStrikeE2ETest {

  /** Bond future option: JGB */
  private static final Currency JPY = Currency.JPY;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2015, 5, 12);
  private static final BondFuturesSecurityDefinition JBM5_DEFINITION = BondFuturesDataSets.JBM5_DEFINITION;
  private static final BondFuturesSecurity JBM5 = JBM5_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesSecurityDefinition JBU5_DEFINITION = BondFuturesDataSets.JBU5_DEFINITION;
  private static final BondFuturesSecurity JBU5 = JBU5_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final double NOTIONAL = JBM5_DEFINITION.getNotional();
  private static final double STRIKE_147 = 1.47; // To be close to ATM for the data set used.
  private static final double STRIKE_146_5 = 1.465; // To be close to ATM for the data set used.
  private static final ZonedDateTime EXPIRY_DATE_M_OPT = DateUtils.getUTCDate(2015, 5, 31);
  private static final ZonedDateTime EXPIRY_DATE_N_OPT = DateUtils.getUTCDate(2015, 6, 30);
  private static final boolean IS_CALL = true;
  private static final BondFuturesOptionPremiumSecurityDefinition CALL_JBM_147_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(JBM5_DEFINITION, EXPIRY_DATE_M_OPT, STRIKE_147, IS_CALL);
  private static final BondFuturesOptionPremiumSecurityDefinition PUT_JBM_147_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(JBM5_DEFINITION, EXPIRY_DATE_M_OPT, STRIKE_147, !IS_CALL);
  private static final BondFuturesOptionPremiumSecurityDefinition PUT_JBM_146_5_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(JBM5_DEFINITION, EXPIRY_DATE_N_OPT, STRIKE_146_5, !IS_CALL);
  private static final BondFuturesOptionPremiumSecurity CALL_JBM_147 = CALL_JBM_147_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesOptionPremiumSecurity PUT_JBM_147 = PUT_JBM_147_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesOptionPremiumSecurity PUT_JBM_146_5 = PUT_JBM_146_5_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final int QUANTITY = 100;
  private static final ZonedDateTime PREMIUM_DATE = DateUtils.getUTCDate(2015, 5, 13);
  private static final ZonedDateTime PREMIUM_DATE_2 = DateUtils.getUTCDate(2015, 5, 6);
  private static final double PREMIUM_UNIT_CALL_JBM_147 = 0.0030;
  private static final double PREMIUM_UNIT_PUT_JBM_147 = 0.0046;
  
  private static final BondFuturesOptionPremiumTransactionDefinition CALL_JBM_147_TRA_DEFINITION =
      new BondFuturesOptionPremiumTransactionDefinition(CALL_JBM_147_DEFINITION, QUANTITY, PREMIUM_DATE,
          -QUANTITY * NOTIONAL * PREMIUM_UNIT_CALL_JBM_147);
  private static final BondFuturesOptionPremiumTransaction CALL_JBM_147_TRA =
      CALL_JBM_147_TRA_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesOptionPremiumTransactionDefinition PUT_JBM_147_TRA_DEFINITION =
      new BondFuturesOptionPremiumTransactionDefinition(PUT_JBM_147_DEFINITION, QUANTITY, PREMIUM_DATE,
          -QUANTITY * NOTIONAL * PREMIUM_UNIT_PUT_JBM_147);
  private static final BondFuturesOptionPremiumTransaction PUT_JBM_147_TRA =
      PUT_JBM_147_TRA_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesOptionPremiumTransactionDefinition PUT_JBM_146_5_TRA_DEFINITION =
      new BondFuturesOptionPremiumTransactionDefinition(PUT_JBM_146_5_DEFINITION, -QUANTITY, PREMIUM_DATE_2, 0);
  private static final BondFuturesOptionPremiumTransaction PUT_JBM_146_5_TRA =
      PUT_JBM_146_5_TRA_DEFINITION.toDerivative(REFERENCE_DATE);
  
  /** Black surface expiry/strike */
  final private static InterpolatedDoublesSurface BLACK_SURFACE_EXP_STRIKE = 
      BondFuturesOptionPremiumE2EDataSet.BLACK_SURFACE_BND_EXP_STRIKE;
  /** Curves for a specific issuer name */
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVES = 
      BondFuturesOptionPremiumE2EDataSet.ISSUER_SPECIFIC_MULTICURVE_JP;
  /** The legal entity */
  private static final LegalEntity LEGAL_ENTITY_JAPAN = BondFuturesOptionPremiumE2EDataSet.JP_GOVT;
  /** The Black bond futures provider **/
  private static final BlackBondFuturesExpStrikeProvider BLACK_EXP_STRIKE_BNDFUT =
      new BlackBondFuturesExpStrikeProvider(ISSUER_SPECIFIC_MULTICURVES, BLACK_SURFACE_EXP_STRIKE, LEGAL_ENTITY_JAPAN);
  /** Methods and calculators */
  private static final BondFuturesOptionPremiumSecurityBlackBondFuturesMethod METHOD_OPT_SEC = 
      BondFuturesOptionPremiumSecurityBlackBondFuturesMethod.getInstance();
  private static final BondFuturesOptionPremiumTransactionBlackBondFuturesMethod METHOD_OPT_TRA = 
      BondFuturesOptionPremiumTransactionBlackBondFuturesMethod.getInstance();
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURES = 
      BondFuturesSecurityDiscountingMethod.getInstance();
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = 
      PaymentFixedDiscountingMethod.getInstance();
  
  /** Tolerances */
  private static final double TOLERANCE_PRICE = 1.0E-6;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;


  @Test
  public void futurePrice() {
    double futuresPriceM5Expected = 1.46883113;
    double futuresPriceM5 = METHOD_FUTURES.price(JBM5, ISSUER_SPECIFIC_MULTICURVES);
    assertEquals(futuresPriceM5Expected, futuresPriceM5, TOLERANCE_PRICE);
    double futuresPriceU5Expected = 1.46480431;
    final double futuresPriceU5 = METHOD_FUTURES.price(JBU5, ISSUER_SPECIFIC_MULTICURVES);
    assertEquals(futuresPriceU5Expected, futuresPriceU5, TOLERANCE_PV);
  }
  
  /* Present value for each option. */
  @Test
  public void presentValue() {
    double pvCallM147Expected = 5878108.4498; // PV includes premium and option value
    MultipleCurrencyAmount pvCallM147Computed = METHOD_OPT_TRA.presentValue(CALL_JBM_147_TRA, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(pvCallM147Expected, pvCallM147Computed.getAmount(JPY), TOLERANCE_PV);
    double pvPutM147Expected = 1566468.8540;
    MultipleCurrencyAmount pvPutM147Computed = METHOD_OPT_TRA.presentValue(PUT_JBM_147_TRA, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(pvPutM147Expected, pvPutM147Computed.getAmount(JPY), TOLERANCE_PV);    
    double pvPutU1465Expected = -49059917.8775; // Short the option
    MultipleCurrencyAmount pvPutU1465Computed = METHOD_OPT_TRA.presentValue(PUT_JBM_146_5_TRA, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(pvPutU1465Expected, pvPutU1465Computed.getAmount(JPY), TOLERANCE_PV);    
  }
  
}
