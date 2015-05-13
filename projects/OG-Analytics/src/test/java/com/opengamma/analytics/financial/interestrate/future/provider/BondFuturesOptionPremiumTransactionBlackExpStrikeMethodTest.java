/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.BondFuturesDataSets;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.StandardDataSetsBlack;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesExpStrikeProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing methods for bond future options transaction with up-front premium payment.
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesOptionPremiumTransactionBlackExpStrikeMethodTest {

  /** Bond future option: JGB */
  private static final Currency JPY = Currency.JPY;
  private static final BondFuturesSecurityDefinition JBM5_DEFINITION = BondFuturesDataSets.JBM5_DEFINITION;
  private static final double NOTIONAL = JBM5_DEFINITION.getNotional();
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2015, 5, 12);
  private static final double STRIKE_147 = 1.47; // To be close to ATM for the data set used.
  private static final ZonedDateTime EXPIRY_DATE_OPT = DateUtils.getUTCDate(2015, 5, 31);
  private static final boolean IS_CALL = true;
  private static final BondFuturesOptionPremiumSecurityDefinition CALL_JB_147_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(JBM5_DEFINITION, EXPIRY_DATE_OPT, STRIKE_147, IS_CALL);
  private static final BondFuturesOptionPremiumSecurityDefinition PUT_JB_147_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(JBM5_DEFINITION, EXPIRY_DATE_OPT, STRIKE_147, !IS_CALL);
  private static final BondFuturesOptionPremiumSecurity CALL_JB_147 = CALL_JB_147_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesOptionPremiumSecurity PUT_JB_147 = PUT_JB_147_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final int QUANTITY = 1234;
  private static final ZonedDateTime PREMIUM_DATE = DateUtils.getUTCDate(2015, 5, 13);
  private static final double PREMIUM_UNIT_CALL = 10.5;
  private static final double PREMIUM_UNIT_PUT = 15.6;
  private static final BondFuturesOptionPremiumTransactionDefinition BOND_FUTURE_OPTION_TRA_CALL_DEFINITION =
      new BondFuturesOptionPremiumTransactionDefinition(CALL_JB_147_DEFINITION, QUANTITY, PREMIUM_DATE,
          -QUANTITY * PREMIUM_UNIT_CALL);
  private static final BondFuturesOptionPremiumTransaction BOND_FUTURE_OPTION_TRA_CALL =
      BOND_FUTURE_OPTION_TRA_CALL_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesOptionPremiumTransactionDefinition BOND_FUTURE_OPTION_TRA_PUT_DEFINITION =
      new BondFuturesOptionPremiumTransactionDefinition(PUT_JB_147_DEFINITION, QUANTITY, PREMIUM_DATE,
          -QUANTITY * PREMIUM_UNIT_PUT);
  private static final BondFuturesOptionPremiumTransaction BOND_FUTURE_OPTION_TRA_PUT =
      BOND_FUTURE_OPTION_TRA_PUT_DEFINITION.toDerivative(REFERENCE_DATE);
  
  /** Black surface expiry/log-moneyness */
  final private static InterpolatedDoublesSurface BLACK_SURFACE_EXP_STRIKE = StandardDataSetsBlack.BLACK_SURFACE_BND_EXP_STRIKE;
  /** Curves for a specific issuer name */
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVES = 
      IssuerProviderDiscountDataSets.ISSUER_SPECIFIC_MULTICURVE_JP;
  /** The legal entity */
  private static final LegalEntity LEGAL_ENTITY_JAPAN = IssuerProviderDiscountDataSets.JP_GOVT;
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
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;
  
  @Test
  public void presentValueFromCurves() {
    MultipleCurrencyAmount pvCallComputed = 
        METHOD_OPT_TRA.presentValue(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT);
    double priceCall = METHOD_OPT_SEC.price(BOND_FUTURE_OPTION_TRA_CALL.getUnderlyingOption(), BLACK_EXP_STRIKE_BNDFUT);
    MultipleCurrencyAmount pvCallPremium = METHOD_PAY_FIXED.presentValue(BOND_FUTURE_OPTION_TRA_CALL.getPremium(), 
        ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider());
    assertEquals("BondFuturesOptionPremiumTransactionBlackExpStrikeMethod: present value", 
        pvCallComputed.getAmount(JPY), 
        pvCallPremium.getAmount(JPY) + priceCall * QUANTITY * JBM5_DEFINITION.getNotional(), TOLERANCE_PV);
    MultipleCurrencyAmount pvPutComputed = 
        METHOD_OPT_TRA.presentValue(BOND_FUTURE_OPTION_TRA_PUT, BLACK_EXP_STRIKE_BNDFUT);
    MultipleCurrencyAmount pvPutPremium = 
        METHOD_PAY_FIXED.presentValue(BOND_FUTURE_OPTION_TRA_PUT.getPremium(), BLACK_EXP_STRIKE_BNDFUT.getMulticurveProvider());
    double priceFut = METHOD_FUTURES.price(BOND_FUTURE_OPTION_TRA_CALL.getUnderlyingOption().getUnderlyingFuture(), 
            ISSUER_SPECIFIC_MULTICURVES);
    double df = ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider().getDiscountFactor(JBM5_DEFINITION.getCurrency(), 
        BOND_FUTURE_OPTION_TRA_CALL.getUnderlyingOption().getExpirationTime());
    assertEquals("BondFuturesOptionPremiumTransactionBlackExpStrikeMethod: option price from future price", 
        df * (priceFut - STRIKE_147) * QUANTITY * JBM5_DEFINITION.getNotional(), 
        (pvCallComputed.getAmount(JPY) - pvCallPremium.getAmount(JPY)) - 
        (pvPutComputed.getAmount(JPY) - pvPutPremium.getAmount(JPY)), TOLERANCE_PV);
  }

  @Test
  public void presentValueFromFuturesPrice() {
    final MultipleCurrencyAmount pvComputed = 
        METHOD_OPT_TRA.presentValue(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT);
    final double priceCall = 
        METHOD_OPT_SEC.price(BOND_FUTURE_OPTION_TRA_CALL.getUnderlyingOption(), BLACK_EXP_STRIKE_BNDFUT);
    final MultipleCurrencyAmount pvPremium = METHOD_PAY_FIXED.presentValue(BOND_FUTURE_OPTION_TRA_CALL.getPremium(), 
        BLACK_EXP_STRIKE_BNDFUT.getMulticurveProvider());
    assertEquals("BondFuturesOptionPremiumTransactionBlackExpStrikeMethod: present value", 
        pvComputed.getAmount(JPY), pvPremium.getAmount(JPY) + priceCall * QUANTITY * JBM5_DEFINITION.getNotional(), 
        TOLERANCE_PV);
    double priceFut = METHOD_FUTURES.price(BOND_FUTURE_OPTION_TRA_CALL.getUnderlyingOption().getUnderlyingFuture(), 
        ISSUER_SPECIFIC_MULTICURVES);
    MultipleCurrencyAmount pv2 = METHOD_OPT_TRA
        .presentValueFromUnderlyingPrice(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT, priceFut);
    assertEquals("BondFuturesOptionPremiumTransactionBlackExpStrikeMethod: present value", pv2.getAmount(JPY), 
        pvComputed.getAmount(JPY), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivityFromCurves() {
    MultipleCurrencyMulticurveSensitivity pvcsCallComputed = 
        METHOD_OPT_TRA.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT).cleaned();
    MulticurveSensitivity pcsCall = METHOD_OPT_SEC
        .priceCurveSensitivity(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    MultipleCurrencyMulticurveSensitivity pvcsCallPremium = METHOD_PAY_FIXED.presentValueCurveSensitivity(
        BOND_FUTURE_OPTION_TRA_CALL.getPremium(), BLACK_EXP_STRIKE_BNDFUT.getMulticurveProvider());
    MultipleCurrencyMulticurveSensitivity pvcsCallExpected = pvcsCallPremium.plus(
        MultipleCurrencyMulticurveSensitivity.of(JPY, pcsCall.multipliedBy(JBM5_DEFINITION.getNotional() * QUANTITY)))
        .cleaned();
    AssertSensitivityObjects.assertEquals(
        "BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value curve sensitivity", 
        pvcsCallExpected, pvcsCallComputed, TOLERANCE_PV_DELTA);
    MultipleCurrencyMulticurveSensitivity pvcsPutComputed = 
        METHOD_OPT_TRA.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_PUT, BLACK_EXP_STRIKE_BNDFUT).cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsPutPremium = 
        METHOD_PAY_FIXED.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_PUT.getPremium(), 
            BLACK_EXP_STRIKE_BNDFUT.getMulticurveProvider());
    double df = ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider().getDiscountFactor(JBM5_DEFINITION.getCurrency(), 
        BOND_FUTURE_OPTION_TRA_CALL.getUnderlyingOption().getExpirationTime());
    MultipleCurrencyMulticurveSensitivity pvcsFutQu = MultipleCurrencyMulticurveSensitivity.of(JPY,
        METHOD_FUTURES.priceCurveSensitivity(
            PUT_JB_147.getUnderlyingFuture(), BLACK_EXP_STRIKE_BNDFUT)
            .multipliedBy(QUANTITY * df * NOTIONAL).cleaned(TOLERANCE_PV_DELTA));    
    double priceFut = METHOD_FUTURES.price(BOND_FUTURE_OPTION_TRA_CALL.getUnderlyingOption().getUnderlyingFuture(), 
        ISSUER_SPECIFIC_MULTICURVES);
    Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(BOND_FUTURE_OPTION_TRA_PUT.getUnderlyingOption().getExpirationTime(), 
        -BOND_FUTURE_OPTION_TRA_PUT.getUnderlyingOption().getExpirationTime() * df));
    mapDsc.put(ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider().getName(JPY), listDiscounting);
    MultipleCurrencyMulticurveSensitivity dfDr = 
        MultipleCurrencyMulticurveSensitivity.of(JPY, MulticurveSensitivity.ofYieldDiscounting(mapDsc));
    MultipleCurrencyMulticurveSensitivity pvcsCallPut =
        pvcsCallComputed.plus(pvcsCallPremium.multipliedBy(-1))
            .plus(pvcsPutPremium.plus(pvcsPutComputed.multipliedBy(-1)))
            .cleaned(TOLERANCE_PV_DELTA);
    AssertSensitivityObjects.assertEquals(
        "BondFuturesOptionPremiumTransactionBlackExpStrikeMethod: present value curve sensitivity",
        (pvcsFutQu.plus(dfDr.multipliedBy((priceFut - STRIKE_147) * QUANTITY * NOTIONAL))).cleaned(),
        pvcsCallPut.cleaned(), TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueDelta() {
    double delta = METHOD_OPT_SEC.delta(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    double pvDelta = METHOD_OPT_TRA.presentValueDelta(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(delta * QUANTITY * NOTIONAL, pvDelta, TOLERANCE_PV);    
  }

  @Test
  public void presentValueGamma() {
    double gamma = METHOD_OPT_SEC.gamma(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    double pvGamma = METHOD_OPT_TRA.presentValueGamma(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(gamma * QUANTITY * NOTIONAL, pvGamma, TOLERANCE_PV);    
  }

  @Test
  public void presentValueVega() {
    double vega = METHOD_OPT_SEC.vega(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    double pvVega = METHOD_OPT_TRA.presentValueVega(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals(vega * QUANTITY * NOTIONAL, pvVega, TOLERANCE_PV);    
  }

  @Test
  public void expansionDeltaGamma() {
    double pvDelta = METHOD_OPT_TRA.presentValueDelta(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT);
    double pvGamma = METHOD_OPT_TRA.presentValueGamma(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT);
    double shift = 2.0E-5;
    double price = METHOD_FUTURES.price(CALL_JB_147.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);    
    MultipleCurrencyAmount pvInit = METHOD_OPT_TRA
        .presentValueFromUnderlyingPrice(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT, price);    
    MultipleCurrencyAmount pvShifted = METHOD_OPT_TRA
        .presentValueFromUnderlyingPrice(BOND_FUTURE_OPTION_TRA_CALL, BLACK_EXP_STRIKE_BNDFUT, price + shift);
    double pvExpanded = pvDelta * shift + 0.5 * pvGamma * (shift * shift);
    assertEquals(pvShifted.getAmount(JPY) - pvInit.getAmount(JPY), pvExpanded, 1.0E+0);    
  }  

}
