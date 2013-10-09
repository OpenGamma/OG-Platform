/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscountingDecoratedIssuer;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

public class BondTransactionDiscountingMethodTest {

  private final static IssuerProviderDiscount ISSUER_MULTICURVES = IssuerProviderDiscountDataSets.createIssuerProvider();
  private final static String[] ISSUER_NAMES = IssuerProviderDiscountDataSets.getIssuerNames();

  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  // to derivatives: first coupon
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  //Fixed Coupon Semi-annual 5Y
  private static final Period PAYMENT_TENOR_FIXED = Period.ofMonths(6);
  private static final DayCount DAY_COUNT_FIXED = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY_FIXED = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_FIXED = false;
  private static final Period BOND_TENOR_FIXED = Period.ofYears(5);
  private static final int SETTLEMENT_DAYS_FIXED = 3;
  private static final ZonedDateTime START_ACCRUAL_DATE_FIXED = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE_FIXED = START_ACCRUAL_DATE_FIXED.plus(BOND_TENOR_FIXED);
  private static final double RATE_FIXED = 0.0325;
  private static final YieldConvention YIELD_CONVENTION_FIXED = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final BondFixedSecurityDefinition BOND_DESCRIPTION_DEFINITION_FIXED = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE_FIXED, START_ACCRUAL_DATE_FIXED, PAYMENT_TENOR_FIXED,
      RATE_FIXED, SETTLEMENT_DAYS_FIXED, CALENDAR, DAY_COUNT_FIXED, BUSINESS_DAY_FIXED, YIELD_CONVENTION_FIXED, IS_EOM_FIXED, ISSUER_NAMES[1]);
  // Transaction fixed
  private static final double PRICE_FIXED = 0.90; //clean price
  private static final double QUANTITY_FIXED = 100000000; //100m
  // Transaction past
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FIXED_1 = DateUtils.getUTCDate(2011, 8, 16);
  private static final AnnuityCouponFixed COUPON_TR_FIXED_1 = BOND_DESCRIPTION_DEFINITION_FIXED.getCoupons().toDerivative(REFERENCE_DATE);
  private static final AnnuityPaymentFixed NOMINAL_TR_FIXED_1 = (AnnuityPaymentFixed) BOND_DESCRIPTION_DEFINITION_FIXED.getNominal().toDerivative(REFERENCE_DATE);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION_FIXED_1 = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED,
      BOND_SETTLEMENT_DATE_FIXED_1, PRICE_FIXED);
  private static final BondFixedTransaction BOND_TRANSACTION_FIXED_1 = BOND_TRANSACTION_DEFINITION_FIXED_1.toDerivative(REFERENCE_DATE);
  // Transaction today
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FIXED_2 = DateUtils.getUTCDate(2011, 8, 18);
  private static final double BOND_SETTLEMENT_TIME_FIXED_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE, BOND_SETTLEMENT_DATE_FIXED_2);
  private static final AnnuityCouponFixed COUPON_TR_FIXED_2 = BOND_DESCRIPTION_DEFINITION_FIXED.getCoupons().toDerivative(REFERENCE_DATE).trimBefore(BOND_SETTLEMENT_TIME_FIXED_2);
  private static final AnnuityPaymentFixed NOMINAL_TR_FIXED_2 = (AnnuityPaymentFixed) BOND_DESCRIPTION_DEFINITION_FIXED.getNominal().toDerivative(REFERENCE_DATE)
      .trimBefore(BOND_SETTLEMENT_TIME_FIXED_2);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION_FIXED_2 = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED,
      BOND_SETTLEMENT_DATE_FIXED_2, PRICE_FIXED);
  private static final PaymentFixed BOND_SETTLEMENT_FIXED_2 = new PaymentFixed(CUR, BOND_SETTLEMENT_TIME_FIXED_2, -(PRICE_FIXED + BOND_TRANSACTION_DEFINITION_FIXED_2.getAccruedInterestAtSettlement())
      * QUANTITY_FIXED);
  private static final BondFixedTransaction BOND_TRANSACTION_FIXED_2 = BOND_TRANSACTION_DEFINITION_FIXED_2.toDerivative(REFERENCE_DATE);
  // Transaction future
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FIXED_3 = DateUtils.getUTCDate(2011, 8, 24);
  private static final double BOND_SETTLEMENT_TIME_FIXED_3 = TimeCalculator.getTimeBetween(REFERENCE_DATE, BOND_SETTLEMENT_DATE_FIXED_3);
  private static final AnnuityCouponFixed COUPON_TR_FIXED_3 = BOND_DESCRIPTION_DEFINITION_FIXED.getCoupons().toDerivative(REFERENCE_DATE).trimBefore(BOND_SETTLEMENT_TIME_FIXED_3);
  private static final AnnuityPaymentFixed NOMINAL_TR_FIXED_3 = (AnnuityPaymentFixed) BOND_DESCRIPTION_DEFINITION_FIXED.getNominal().toDerivative(REFERENCE_DATE)
      .trimBefore(BOND_SETTLEMENT_TIME_FIXED_3);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION_FIXED_3 = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED,
      BOND_SETTLEMENT_DATE_FIXED_3, PRICE_FIXED);
  private static final PaymentFixed BOND_SETTLEMENT_FIXED_3 = new PaymentFixed(CUR, BOND_SETTLEMENT_TIME_FIXED_3, -(PRICE_FIXED + BOND_TRANSACTION_DEFINITION_FIXED_3.getAccruedInterestAtSettlement())
      * QUANTITY_FIXED);
  private static final BondFixedTransaction BOND_TRANSACTION_FIXED_3 = BOND_TRANSACTION_DEFINITION_FIXED_3.toDerivative(REFERENCE_DATE);
  // Ibor coupon Quarterly 2Y
  private static final DayCount DAY_COUNT_FRN = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final BusinessDayConvention BUSINESS_DAY_FRN = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_FRN = false;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final int IBOR_SPOT_LAG = 2;
  private static final BusinessDayConvention IBOR_BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IBOR_IS_EOM = false;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SPOT_LAG, IBOR_DAY_COUNT, IBOR_BUSINESS_DAY, IBOR_IS_EOM, "Ibor");
  private static final Period BOND_TENOR_FRN = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS_FRN = 3; // Standard for euro-bonds.
  private static final ZonedDateTime START_ACCRUAL_DATE_FRN = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE_FRN = START_ACCRUAL_DATE_FRN.plus(BOND_TENOR_FRN);
  private static final BondIborSecurityDefinition BOND_DESCRIPTION_DEFINITION_FRN = BondIborSecurityDefinition.from(MATURITY_DATE_FRN, START_ACCRUAL_DATE_FRN, IBOR_INDEX, SETTLEMENT_DAYS_FRN,
      DAY_COUNT_FRN, BUSINESS_DAY_FRN, IS_EOM_FRN, ISSUER_NAMES[1], CALENDAR);
  // Transaction FRN
  private static final double FIRST_FIXING = 0.02;
  private static final double PRICE_FRN = 0.99;
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FRN = DateUtils.getUTCDate(2011, 8, 24);
  private static final double BOND_SETTLEMENT_TIME_FRN = TimeCalculator.getTimeBetween(REFERENCE_DATE, BOND_SETTLEMENT_DATE_FRN);
  private static final double QUANTITY_FRN = 100000000; //100m
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {BOND_DESCRIPTION_DEFINITION_FRN.getCoupons().getNthPayment(0)
      .getFixingDate()}, new double[] {FIRST_FIXING});
  private static final AnnuityPaymentFixed NOMINAL_TR_1_FRN = (AnnuityPaymentFixed) BOND_DESCRIPTION_DEFINITION_FRN.getNominal().toDerivative(REFERENCE_DATE)
      .trimBefore(BOND_SETTLEMENT_TIME_FRN);
  private static final Annuity<? extends Payment> COUPON_TR_1_FRN = BOND_DESCRIPTION_DEFINITION_FRN.getCoupons().toDerivative(REFERENCE_DATE, FIXING_TS)
      .trimBefore(BOND_SETTLEMENT_TIME_FRN);
  private static final BondIborTransactionDefinition BOND_TRANSACTION_DEFINITION_FRN = new BondIborTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FRN, QUANTITY_FRN, BOND_SETTLEMENT_DATE_FRN,
      PRICE_FRN);
  private static final PaymentFixed BOND_SETTLEMENT_FRN = new PaymentFixed(CUR, BOND_SETTLEMENT_TIME_FRN, -PRICE_FRN * QUANTITY_FRN);
  private static final BondIborTransaction BOND_TRANSACTION_FRN = BOND_TRANSACTION_DEFINITION_FRN.toDerivative(REFERENCE_DATE, FIXING_TS);
  // Calculators
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final BondTransactionDiscountingMethod METHOD_BOND_TR = BondTransactionDiscountingMethod.getInstance();
  private static final double TOLERANCE_PV_DELTA = 1.0E-2;

  @Test
  public void testPVFixedBondSettlePast() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValue(BOND_TRANSACTION_FIXED_1, ISSUER_MULTICURVES);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, BOND_TRANSACTION_FIXED_1.getBondTransaction().getIssuer());
    final MultipleCurrencyAmount pvNominal = NOMINAL_TR_FIXED_1.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = COUPON_TR_FIXED_1.accept(PVDC, multicurvesDecorated);
    assertEquals("Fixed bond present value", (pvNominal.getAmount(CUR) + pvCoupon.getAmount(CUR)) * QUANTITY_FIXED, pv.getAmount(CUR));
  }

  @Test
  public void testPVFixedBondSettleToday() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValue(BOND_TRANSACTION_FIXED_2, ISSUER_MULTICURVES);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, BOND_TRANSACTION_FIXED_1.getBondTransaction().getIssuer());
    final MultipleCurrencyAmount pvNominal = NOMINAL_TR_FIXED_2.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = COUPON_TR_FIXED_2.accept(PVDC, multicurvesDecorated);
    final double pvSettlement = BOND_SETTLEMENT_FIXED_2.getAmount();
    assertEquals("Fixed bond present value", (pvNominal.getAmount(CUR) + pvCoupon.getAmount(CUR)) * QUANTITY_FIXED + pvSettlement, pv.getAmount(CUR));
  }

  @Test
  public void testPVFixedBondSettleFuture() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValue(BOND_TRANSACTION_FIXED_3, ISSUER_MULTICURVES);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, BOND_TRANSACTION_FIXED_1.getBondTransaction().getIssuer());
    final MultipleCurrencyAmount pvNominal = NOMINAL_TR_FIXED_3.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = COUPON_TR_FIXED_3.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvSettlement = BOND_SETTLEMENT_FIXED_3.accept(PVDC, ISSUER_MULTICURVES.getMulticurveProvider());
    assertEquals("Fixed bond present value", (pvNominal.getAmount(CUR) + pvCoupon.getAmount(CUR)) * QUANTITY_FIXED + pvSettlement.getAmount(CUR), pv.getAmount(CUR));
  }

  @Test
  public void testPVSFixedBond() {
    final MultipleCurrencyMulticurveSensitivity pvs = METHOD_BOND_TR.presentValueSensitivity(BOND_TRANSACTION_FIXED_3, ISSUER_MULTICURVES);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, BOND_TRANSACTION_FIXED_1.getBondTransaction().getIssuer());
    final MultipleCurrencyMulticurveSensitivity pvsNominal = NOMINAL_TR_FIXED_3.accept(PVCSDC, multicurvesDecorated);
    final MultipleCurrencyMulticurveSensitivity pvsCoupon = COUPON_TR_FIXED_3.accept(PVCSDC, multicurvesDecorated);
    final MultipleCurrencyMulticurveSensitivity pvsSettlement = BOND_SETTLEMENT_FIXED_3.accept(PVCSDC, ISSUER_MULTICURVES.getMulticurveProvider());
    final MultipleCurrencyMulticurveSensitivity expectedPvs = pvsNominal.plus(pvsCoupon).multipliedBy(QUANTITY_FRN).plus(pvsSettlement).cleaned();
    assertEquals("Fixed bond present value sensitivity", expectedPvs, pvs.cleaned());
  }

  @Test(enabled = false)
  //FIXME change the test and the pv method with correct accrual interests mechanism.
  public void testFixedBondMethodCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_BOND_TR.presentValue(BOND_TRANSACTION_FIXED_3, ISSUER_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator =BOND_TRANSACTION_FIXED_3.accept(PVIC, ISSUER_MULTICURVES);
    assertEquals("Fixed bond present value: Method vs Calculator", pvMethod, pvCalculator);
    final MultipleCurrencyMulticurveSensitivity pvsMethod = METHOD_BOND_TR.presentValueSensitivity(BOND_TRANSACTION_FIXED_3, ISSUER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvsCalculator = BOND_TRANSACTION_FIXED_3.accept(PVCSIC, ISSUER_MULTICURVES);
    AssertSensivityObjects.assertEquals("Fixed bond present value sensitivity: Method vs Calculator", pvsMethod, pvsCalculator, TOLERANCE_PV_DELTA);
  }

  @Test(enabled = false)
  //FIXME change the test and the pv method with correct accrual interests mechanism.
  public void testPVIborBond() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValue(BOND_TRANSACTION_FRN, ISSUER_MULTICURVES);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, BOND_TRANSACTION_FIXED_1.getBondTransaction().getIssuer());
    final MultipleCurrencyAmount pvNominal = NOMINAL_TR_1_FRN.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = COUPON_TR_1_FRN.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvSettlement = BOND_SETTLEMENT_FRN.accept(PVDC, multicurvesDecorated);
    assertEquals("FRN present value", (pvNominal.getAmount(CUR) + pvCoupon.getAmount(CUR)) * QUANTITY_FRN + pvSettlement.getAmount(CUR), pv.getAmount(CUR));
  }

  @Test(enabled = false)
  //FIXME change the test and the pv method with correct accrual interests mechanism.
  public void testPVSIborBond() {
    final MultipleCurrencyMulticurveSensitivity pvs = METHOD_BOND_TR.presentValueSensitivity(BOND_TRANSACTION_FRN, ISSUER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvsNominal = NOMINAL_TR_1_FRN.accept(PVCSIC, ISSUER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvsCoupon = COUPON_TR_1_FRN.accept(PVCSIC, ISSUER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvsSettlement = BOND_SETTLEMENT_FRN.accept(PVCSIC, ISSUER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity expectedPvs = pvsNominal.plus(pvsCoupon).multipliedBy(QUANTITY_FRN).plus(pvsSettlement).cleaned();
    assertEquals("FRN present value sensitivity", expectedPvs, pvs.cleaned());
  }
}
