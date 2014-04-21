/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

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
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadRateCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadRateIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscountingDecoratedIssuer;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.SimpleParameterSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondTransactionDiscountingMethodTest {

  private final static IssuerProviderDiscount ISSUER_MULTICURVES = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  private final static String[] ISSUER_NAMES = IssuerProviderDiscountDataSets.getIssuerNames();

  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  // to derivatives: first coupon
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2012, 7, 12);
  //Fixed Coupon Semi-annual 5Y
  private static final Period PAYMENT_TENOR_FIXED = Period.ofMonths(6);
  private static final DayCount DAY_COUNT_FIXED = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY_FIXED = BusinessDayConventions.FOLLOWING;
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
  private static final double PRICE_CLEAN_FIXED = 0.90; //clean price
  private static final double QUANTITY_FIXED = 100000000; //100m
  // Transaction past
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FIXED_1 = DateUtils.getUTCDate(2011, 8, 16);
  private static final AnnuityCouponFixed COUPON_TR_FIXED_1 = BOND_DESCRIPTION_DEFINITION_FIXED.getCoupons().toDerivative(REFERENCE_DATE);
  private static final AnnuityPaymentFixed NOMINAL_TR_FIXED_1 = (AnnuityPaymentFixed) BOND_DESCRIPTION_DEFINITION_FIXED.getNominal().toDerivative(REFERENCE_DATE);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION_FIXED_1 = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED,
      BOND_SETTLEMENT_DATE_FIXED_1, PRICE_CLEAN_FIXED);
  private static final BondFixedTransaction BOND_TRANSACTION_FIXED_1 = BOND_TRANSACTION_DEFINITION_FIXED_1.toDerivative(REFERENCE_DATE);
  // Transaction today
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FIXED_2 = DateUtils.getUTCDate(2011, 8, 18);
  private static final double BOND_SETTLEMENT_TIME_FIXED_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE, BOND_SETTLEMENT_DATE_FIXED_2);
  private static final AnnuityCouponFixed COUPON_TR_FIXED_2 = BOND_DESCRIPTION_DEFINITION_FIXED.getCoupons().toDerivative(REFERENCE_DATE).trimBefore(BOND_SETTLEMENT_TIME_FIXED_2);
  private static final AnnuityPaymentFixed NOMINAL_TR_FIXED_2 = (AnnuityPaymentFixed) BOND_DESCRIPTION_DEFINITION_FIXED.getNominal().toDerivative(REFERENCE_DATE)
      .trimBefore(BOND_SETTLEMENT_TIME_FIXED_2);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION_FIXED_2 = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED,
      BOND_SETTLEMENT_DATE_FIXED_2, PRICE_CLEAN_FIXED);
  private static final PaymentFixed BOND_SETTLEMENT_FIXED_2 = new PaymentFixed(CUR, BOND_SETTLEMENT_TIME_FIXED_2,
      -(PRICE_CLEAN_FIXED + BOND_TRANSACTION_DEFINITION_FIXED_2.getAccruedInterestAtSettlement())
          * QUANTITY_FIXED);
  private static final BondFixedTransaction BOND_TRANSACTION_FIXED_2 = BOND_TRANSACTION_DEFINITION_FIXED_2.toDerivative(REFERENCE_DATE);
  // Transaction future
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FIXED_3 = DateUtils.getUTCDate(2011, 8, 24);
  private static final double BOND_SETTLEMENT_TIME_FIXED_3 = TimeCalculator.getTimeBetween(REFERENCE_DATE, BOND_SETTLEMENT_DATE_FIXED_3);
  private static final AnnuityCouponFixed COUPON_TR_FIXED_3 = BOND_DESCRIPTION_DEFINITION_FIXED.getCoupons().toDerivative(REFERENCE_DATE).trimBefore(BOND_SETTLEMENT_TIME_FIXED_3);
  private static final AnnuityPaymentFixed NOMINAL_TR_FIXED_3 = (AnnuityPaymentFixed) BOND_DESCRIPTION_DEFINITION_FIXED.getNominal().toDerivative(REFERENCE_DATE)
      .trimBefore(BOND_SETTLEMENT_TIME_FIXED_3);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION_FIXED_3 = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED,
      BOND_SETTLEMENT_DATE_FIXED_3, PRICE_CLEAN_FIXED);
  private static final PaymentFixed BOND_SETTLEMENT_FIXED_3 = new PaymentFixed(CUR, BOND_SETTLEMENT_TIME_FIXED_3,
      -(PRICE_CLEAN_FIXED + BOND_TRANSACTION_DEFINITION_FIXED_3.getAccruedInterestAtSettlement())
          * QUANTITY_FIXED);
  private static final BondFixedTransaction BOND_TRANSACTION_FIXED_3 = BOND_TRANSACTION_DEFINITION_FIXED_3.toDerivative(REFERENCE_DATE);
  private static final BondFixedTransaction BOND_TRANSACTION_FIXED_4 = BOND_TRANSACTION_DEFINITION_FIXED_3.toDerivative(REFERENCE_DATE_2);

  private static final ZonedDateTime SETTLE_DATE_STD = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BOND_DESCRIPTION_DEFINITION_FIXED.getSettlementDays(), CALENDAR);
  private static final BondFixedTransactionDefinition BOND_FIXED_STD_DEFINITION = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED, SETTLE_DATE_STD,
      PRICE_CLEAN_FIXED);
  private static final BondFixedTransaction BOND_FIXED_STD = BOND_FIXED_STD_DEFINITION.toDerivative(REFERENCE_DATE);

  // Ibor coupon Quarterly 2Y
  private static final DayCount DAY_COUNT_FRN = DayCounts.ACT_ACT_ISDA;
  private static final BusinessDayConvention BUSINESS_DAY_FRN = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_FRN = false;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final int IBOR_SPOT_LAG = 2;
  private static final BusinessDayConvention IBOR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
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
      .getFixingDate() }, new double[] {FIRST_FIXING });
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
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParSpreadRateIssuerDiscountingCalculator PSRIDC = ParSpreadRateIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadRateCurveSensitivityIssuerDiscountingCalculator PSRCSIDC = ParSpreadRateCurveSensitivityIssuerDiscountingCalculator.getInstance();

  private static final double SHIFT_FD = 1.0E-6;

  private static final SimpleParameterSensitivityIssuerCalculator<ParameterIssuerProviderInterface> PS_I_AD =
      new SimpleParameterSensitivityIssuerCalculator<ParameterIssuerProviderInterface>(PSRCSIDC);
  private static final SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator PS_I_FD = new SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator(PSRIDC, SHIFT_FD);

  private static final BondTransactionDiscountingMethod METHOD_BOND_TR = BondTransactionDiscountingMethod.getInstance();
  private static final BondSecurityDiscountingMethod METHOD_BOND_SEC = BondSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-8;

  @Test
  public void testPVFixedBondSettlePast() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValue(BOND_TRANSACTION_FIXED_1, ISSUER_MULTICURVES);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, BOND_TRANSACTION_FIXED_1.getBondTransaction().getIssuerEntity());
    final MultipleCurrencyAmount pvNominal = NOMINAL_TR_FIXED_1.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = COUPON_TR_FIXED_1.accept(PVDC, multicurvesDecorated);
    assertEquals("Fixed bond present value", (pvNominal.getAmount(CUR) + pvCoupon.getAmount(CUR)) * QUANTITY_FIXED, pv.getAmount(CUR));
  }

  @Test
  public void testPVCleanPriceFixedBondSettlePast() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValueFromCleanPrice(BOND_TRANSACTION_FIXED_1, ISSUER_MULTICURVES, PRICE_CLEAN_FIXED);
    final MultipleCurrencyAmount pvSec = METHOD_BOND_SEC.presentValueFromCleanPrice(BOND_TRANSACTION_FIXED_1.getBondStandard(),
        ISSUER_MULTICURVES.getMulticurveProvider(), PRICE_CLEAN_FIXED);
    final double df = ISSUER_MULTICURVES.getMulticurveProvider().getDiscountFactor(CUR, BOND_TRANSACTION_FIXED_1.getBondStandard().getSettlementTime());
    final double pvExpected = (PRICE_CLEAN_FIXED * BOND_TRANSACTION_FIXED_1.getNotionalStandard() + BOND_TRANSACTION_FIXED_1.getBondStandard().getAccruedInterest())
        * df * QUANTITY_FIXED;
    assertEquals("Fixed bond present value", pvSec.getAmount(CUR) * QUANTITY_FIXED, pv.getAmount(CUR), TOLERANCE_PV_DELTA);
    assertEquals("Fixed bond present value", pvExpected, pv.getAmount(CUR), TOLERANCE_PV_DELTA);
  }

  @Test
  public void testPVYieldFixedBondSettlePast() {
    final double yield = 0.05;
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValueFromYield(BOND_TRANSACTION_FIXED_1, ISSUER_MULTICURVES, yield);
    final double dirtyPrice = METHOD_BOND_SEC.dirtyPriceFromYield(BOND_TRANSACTION_FIXED_1.getBondStandard(), yield);
    final double df = ISSUER_MULTICURVES.getMulticurveProvider().getDiscountFactor(CUR, BOND_TRANSACTION_FIXED_1.getBondStandard().getSettlementTime());
    final double pvExpected = (dirtyPrice) * df * QUANTITY_FIXED;
    assertEquals("Fixed bond present value", pvExpected, pv.getAmount(CUR), TOLERANCE_PV_DELTA);
  }

  @Test
  public void testPVFixedBondSettleToday() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValue(BOND_TRANSACTION_FIXED_2, ISSUER_MULTICURVES);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, BOND_TRANSACTION_FIXED_1.getBondTransaction().getIssuerEntity());
    final MultipleCurrencyAmount pvNominal = NOMINAL_TR_FIXED_2.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = COUPON_TR_FIXED_2.accept(PVDC, multicurvesDecorated);
    final double pvSettlement = BOND_SETTLEMENT_FIXED_2.getAmount();
    assertEquals("Fixed bond present value", (pvNominal.getAmount(CUR) + pvCoupon.getAmount(CUR)) * QUANTITY_FIXED + pvSettlement, pv.getAmount(CUR));
  }

  @Test
  public void testPVFixedBondSettleFuture() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValue(BOND_TRANSACTION_FIXED_3, ISSUER_MULTICURVES);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, BOND_TRANSACTION_FIXED_1.getBondTransaction().getIssuerEntity());
    final MultipleCurrencyAmount pvNominal = NOMINAL_TR_FIXED_3.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = COUPON_TR_FIXED_3.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvSettlement = BOND_SETTLEMENT_FIXED_3.accept(PVDC, ISSUER_MULTICURVES.getMulticurveProvider());
    assertEquals("Fixed bond present value", (pvNominal.getAmount(CUR) + pvCoupon.getAmount(CUR)) * QUANTITY_FIXED + pvSettlement.getAmount(CUR), pv.getAmount(CUR));
  }

  @Test
  /**
   * Test the PV when a coupon payment is between today and standard settlement date and pv is computed from conventional clean price.
   */
  public void testPVCleanPriceFixedBondCouponBeforeSettle() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValueFromCleanPrice(BOND_TRANSACTION_FIXED_4, ISSUER_MULTICURVES, PRICE_CLEAN_FIXED);
    final MultipleCurrencyAmount pvSec = METHOD_BOND_SEC.presentValueFromCleanPrice(BOND_TRANSACTION_FIXED_4.getBondStandard(),
        ISSUER_MULTICURVES.getMulticurveProvider(), PRICE_CLEAN_FIXED);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR,
        BOND_TRANSACTION_FIXED_4.getBondTransaction().getIssuerEntity());
    final MultipleCurrencyAmount pvNominalStandard = BOND_TRANSACTION_FIXED_4.getBondStandard().getNominal().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCouponStandard = BOND_TRANSACTION_FIXED_4.getBondStandard().getCoupon().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvDiscountingStandard = pvNominalStandard.plus(pvCouponStandard);
    final MultipleCurrencyAmount pvNominalTransaction = BOND_TRANSACTION_FIXED_4.getBondTransaction().getNominal().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCouponTransaction = BOND_TRANSACTION_FIXED_4.getBondTransaction().getCoupon().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvDiscountingTransaction = pvNominalTransaction.plus(pvCouponTransaction);
    final double pvExpected = (pvDiscountingTransaction.getAmount(CUR) - pvDiscountingStandard.getAmount(CUR) + pvSec.getAmount(CUR)) * QUANTITY_FIXED;
    assertEquals("Fixed coupon bond present value", pvExpected, pv.getAmount(CUR), TOLERANCE_PV_DELTA);
    assertFalse("Fixed coupon bond present value", Math.abs(pvSec.getAmount(CUR) * QUANTITY_FIXED - pv.getAmount(CUR)) < TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the PV when a coupon payment is between today and standard settlement date and pv is computed from conventional yield.
   */
  public void testPVYieldFixedBondCouponBeforeSettle() {
    final double yield = 0.05;
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValueFromYield(BOND_TRANSACTION_FIXED_4, ISSUER_MULTICURVES, yield);
    final double dirtyPrice = METHOD_BOND_SEC.dirtyPriceFromYield(BOND_TRANSACTION_FIXED_4.getBondStandard(), yield);
    final double df = ISSUER_MULTICURVES.getMulticurveProvider().getDiscountFactor(CUR, BOND_TRANSACTION_FIXED_4.getBondStandard().getSettlementTime());
    final MultipleCurrencyAmount pvSec = MultipleCurrencyAmount.of(CUR, dirtyPrice * df);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR,
        BOND_TRANSACTION_FIXED_4.getBondTransaction().getIssuerEntity());
    final MultipleCurrencyAmount pvNominalStandard = BOND_TRANSACTION_FIXED_4.getBondStandard().getNominal().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCouponStandard = BOND_TRANSACTION_FIXED_4.getBondStandard().getCoupon().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvDiscountingStandard = pvNominalStandard.plus(pvCouponStandard);
    final MultipleCurrencyAmount pvNominalTransaction = BOND_TRANSACTION_FIXED_4.getBondTransaction().getNominal().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCouponTransaction = BOND_TRANSACTION_FIXED_4.getBondTransaction().getCoupon().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvDiscountingTransaction = pvNominalTransaction.plus(pvCouponTransaction);
    final double pvExpected = (pvDiscountingTransaction.getAmount(CUR) - pvDiscountingStandard.getAmount(CUR) + pvSec.getAmount(CUR)) * QUANTITY_FIXED;
    assertEquals("Fixed coupon bond present value", pvExpected, pv.getAmount(CUR), TOLERANCE_PV_DELTA);
    assertFalse("Fixed coupon bond present value", Math.abs(pvSec.getAmount(CUR) * QUANTITY_FIXED - pv.getAmount(CUR)) < TOLERANCE_PV_DELTA);
  }

  @Test
  public void testPVCSFixedBond() {
    final MultipleCurrencyMulticurveSensitivity pvs = METHOD_BOND_TR.presentValueCurveSensitivity(BOND_TRANSACTION_FIXED_3, ISSUER_MULTICURVES);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, BOND_TRANSACTION_FIXED_1.getBondTransaction().getIssuerEntity());
    final MultipleCurrencyMulticurveSensitivity pvsNominal = NOMINAL_TR_FIXED_3.accept(PVCSDC, multicurvesDecorated);
    final MultipleCurrencyMulticurveSensitivity pvsCoupon = COUPON_TR_FIXED_3.accept(PVCSDC, multicurvesDecorated);
    final MultipleCurrencyMulticurveSensitivity pvsSettlement = BOND_SETTLEMENT_FIXED_3.accept(PVCSDC, ISSUER_MULTICURVES.getMulticurveProvider());
    final MultipleCurrencyMulticurveSensitivity expectedPvs = pvsNominal.plus(pvsCoupon).multipliedBy(QUANTITY_FRN).plus(pvsSettlement).cleaned();
    assertEquals("Fixed bond present value sensitivity", expectedPvs, pvs.cleaned());
  }

  @Test
  public void testPVCSFixedBondMethodCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_BOND_TR.presentValue(BOND_TRANSACTION_FIXED_3, ISSUER_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = BOND_TRANSACTION_FIXED_3.accept(PVIC, ISSUER_MULTICURVES);
    assertEquals("Fixed bond present value: Method vs Calculator", pvMethod, pvCalculator);
    final MultipleCurrencyMulticurveSensitivity pvsMethod = METHOD_BOND_TR.presentValueCurveSensitivity(BOND_TRANSACTION_FIXED_3, ISSUER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvsCalculator = BOND_TRANSACTION_FIXED_3.accept(PVCSIC, ISSUER_MULTICURVES);
    AssertSensivityObjects.assertEquals("Fixed bond present value sensitivity: Method vs Calculator", pvsMethod, pvsCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  public void parSpreadMarketQuote() {
    final double parSpreadMarketQuote = METHOD_BOND_TR.parSpread(BOND_FIXED_STD, ISSUER_MULTICURVES);
    final BondFixedTransaction bond0 = new BondFixedTransaction(BOND_FIXED_STD.getBondTransaction(), QUANTITY_FIXED, PRICE_CLEAN_FIXED + parSpreadMarketQuote,
        BOND_FIXED_STD.getBondStandard(), BOND_FIXED_STD.getNotionalStandard());
    final MultipleCurrencyAmount pv0 = METHOD_BOND_TR.presentValue(bond0, ISSUER_MULTICURVES);
    assertEquals("Fixed bond: par spread market quote", pv0.getAmount(CUR), 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadYield() {
    final ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BOND_DESCRIPTION_DEFINITION_FIXED.getSettlementDays(), CALENDAR);
    final BondFixedTransactionDefinition bondTrDefinition = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED, settleDate, PRICE_CLEAN_FIXED);
    final BondFixedTransaction bondTr = bondTrDefinition.toDerivative(REFERENCE_DATE);
    final double parSpreadYield = METHOD_BOND_TR.parSpreadYield(bondTr, ISSUER_MULTICURVES);
    final double yield = METHOD_BOND_SEC.yieldFromCleanPrice(bondTr.getBondStandard(), PRICE_CLEAN_FIXED);
    final BondFixedTransactionDefinition bond0Definition = BondFixedTransactionDefinition.fromYield(BOND_DESCRIPTION_DEFINITION_FIXED,
        QUANTITY_FIXED, settleDate, yield + parSpreadYield);
    final BondFixedTransaction bond0 = bond0Definition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pv0 = METHOD_BOND_TR.presentValue(bond0, ISSUER_MULTICURVES);
    assertEquals("Fixed bond: par spread yield", pv0.getAmount(CUR), 0, TOLERANCE_PV);
    final Double parSpreadYieldCalculator = bondTr.accept(PSRIDC, ISSUER_MULTICURVES);
    assertEquals("Fixed bond: par spread yield", parSpreadYieldCalculator, parSpreadYield, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests parSpreadYield curve sensitivity: explicit formula versus finite difference.
   */
  public void parSpreadYieldCurveSensitivityMethodVsCalculator() {
    final SimpleParameterSensitivity pspsDepositExact = PS_I_AD.calculateSensitivity(BOND_FIXED_STD, ISSUER_MULTICURVES, ISSUER_MULTICURVES.getAllNames());
    final SimpleParameterSensitivity pspsDepositFD = PS_I_FD.calculateSensitivity(BOND_FIXED_STD, ISSUER_MULTICURVES);
    AssertSensivityObjects.assertEquals("BondTransactionDiscountingMethod: parSpreadYield curve sensitivity", pspsDepositExact, pspsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void parSpreadYieldCurveSensitivity() {
    final MulticurveSensitivity pscsyCalculator = BOND_FIXED_STD.accept(PSRCSIDC, ISSUER_MULTICURVES);
    final MulticurveSensitivity psycsMethod = METHOD_BOND_TR.parSpreadYieldCurveSensitivity(BOND_FIXED_STD, ISSUER_MULTICURVES);
    AssertSensivityObjects.assertEquals("BondTransactionDiscountingMethod: parSpreadYield curve sensitivity", psycsMethod, pscsyCalculator, TOLERANCE_PRICE_DELTA);
  }

  @Test(enabled = false)
  //FIXME change the test and the pv method with correct accrual interests mechanism.
  public void testPVIborBond() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TR.presentValue(BOND_TRANSACTION_FRN, ISSUER_MULTICURVES);
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, BOND_TRANSACTION_FIXED_1.getBondTransaction().getIssuerEntity());
    final MultipleCurrencyAmount pvNominal = NOMINAL_TR_1_FRN.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = COUPON_TR_1_FRN.accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvSettlement = BOND_SETTLEMENT_FRN.accept(PVDC, multicurvesDecorated);
    assertEquals("FRN present value", (pvNominal.getAmount(CUR) + pvCoupon.getAmount(CUR)) * QUANTITY_FRN + pvSettlement.getAmount(CUR), pv.getAmount(CUR));
  }

  @Test(enabled = false)
  //FIXME change the test and the pv method with correct accrual interests mechanism.
  public void testPVSIborBond() {
    final MultipleCurrencyMulticurveSensitivity pvs = METHOD_BOND_TR.presentValueCurveSensitivity(BOND_TRANSACTION_FRN, ISSUER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvsNominal = NOMINAL_TR_1_FRN.accept(PVCSIC, ISSUER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvsCoupon = COUPON_TR_1_FRN.accept(PVCSIC, ISSUER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvsSettlement = BOND_SETTLEMENT_FRN.accept(PVCSIC, ISSUER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity expectedPvs = pvsNominal.plus(pvsCoupon).multipliedBy(QUANTITY_FRN).plus(pvsSettlement).cleaned();
    assertEquals("FRN present value sensitivity", expectedPvs, pvs.cleaned());
  }
}
