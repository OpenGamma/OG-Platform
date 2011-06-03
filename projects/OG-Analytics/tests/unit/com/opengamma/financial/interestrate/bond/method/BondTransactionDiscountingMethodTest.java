/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

public class BondTransactionDiscountingMethodTest {

  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  // to derivatives: common
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final String[] COUPON_IBOR_CURVE_NAME = new String[] {CREDIT_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurvesBond1();
  // to derivatives: first coupon
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2011, 8, 18);
  private static final ZonedDateTime REFERENCE_DATE_Z = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
  //Fixed Coupon Semi-annual 5Y
  private static final Period PAYMENT_TENOR_FIXED = Period.ofMonths(6);
  private static final DayCount DAY_COUNT_FIXED = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY_FIXED = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_FIXED = false;
  private static final Period BOND_TENOR_FIXED = Period.ofYears(5);
  private static final int SETTLEMENT_DAYS_FIXED = 3;
  private static final ZonedDateTime START_ACCRUAL_DATE_FIXED = DateUtil.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE_FIXED = START_ACCRUAL_DATE_FIXED.plus(BOND_TENOR_FIXED);
  private static final double RATE_FIXED = 0.0325;
  private static final YieldConvention YIELD_CONVENTION_FIXED = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final BondFixedSecurityDefinition BOND_DESCRIPTION_DEFINITION_FIXED = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE_FIXED, START_ACCRUAL_DATE_FIXED, PAYMENT_TENOR_FIXED,
      RATE_FIXED, SETTLEMENT_DAYS_FIXED, CALENDAR, DAY_COUNT_FIXED, BUSINESS_DAY_FIXED, YIELD_CONVENTION_FIXED, IS_EOM_FIXED);
  // Transaction fixed
  private static final double PRICE_FIXED = 0.90;
  private static final double QUANTITY_FIXED = 100000000; //100m
  // Transaction past
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FIXED_1 = DateUtil.getUTCDate(2011, 8, 16);
  //  private static final double BOND_SETTLEMENT_TIME_FIXED_1 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_Z, BOND_SETTLEMENT_DATE_FIXED_1);
  private static final AnnuityCouponFixed COUPON_TR_FIXED_1 = BOND_DESCRIPTION_DEFINITION_FIXED.getCoupon().toDerivative(REFERENCE_DATE_Z, CURVES_NAME);
  private static final AnnuityPaymentFixed NOMINAL_TR_FIXED_1 = BOND_DESCRIPTION_DEFINITION_FIXED.getNominal().toDerivative(REFERENCE_DATE_Z, CURVES_NAME);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION_FIXED_1 = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED,
      BOND_SETTLEMENT_DATE_FIXED_1, PRICE_FIXED);
  private static final BondFixedTransaction BOND_TRANSACTION_FIXED_1 = BOND_TRANSACTION_DEFINITION_FIXED_1.toDerivative(REFERENCE_DATE_Z, CURVES_NAME);
  // Transaction today
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FIXED_2 = DateUtil.getUTCDate(2011, 8, 18);
  private static final double BOND_SETTLEMENT_TIME_FIXED_2 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_Z, BOND_SETTLEMENT_DATE_FIXED_2);
  private static final AnnuityCouponFixed COUPON_TR_FIXED_2 = BOND_DESCRIPTION_DEFINITION_FIXED.getCoupon().toDerivative(REFERENCE_DATE_Z, CURVES_NAME).trimBefore(BOND_SETTLEMENT_TIME_FIXED_2);
  private static final AnnuityPaymentFixed NOMINAL_TR_FIXED_2 = BOND_DESCRIPTION_DEFINITION_FIXED.getNominal().toDerivative(REFERENCE_DATE_Z, CURVES_NAME).trimBefore(BOND_SETTLEMENT_TIME_FIXED_2);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION_FIXED_2 = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED,
      BOND_SETTLEMENT_DATE_FIXED_2, PRICE_FIXED);
  private static final PaymentFixed BOND_SETTLEMENT_FIXED_2 = new PaymentFixed(CUR, BOND_SETTLEMENT_TIME_FIXED_2, -PRICE_FIXED * QUANTITY_FIXED, REPO_CURVE_NAME);
  private static final BondFixedTransaction BOND_TRANSACTION_FIXED_2 = BOND_TRANSACTION_DEFINITION_FIXED_2.toDerivative(REFERENCE_DATE_Z, CURVES_NAME);
  // Transaction future
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FIXED_3 = DateUtil.getUTCDate(2011, 8, 24);
  private static final double BOND_SETTLEMENT_TIME_FIXED_3 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_Z, BOND_SETTLEMENT_DATE_FIXED_3);
  private static final AnnuityCouponFixed COUPON_TR_FIXED_3 = BOND_DESCRIPTION_DEFINITION_FIXED.getCoupon().toDerivative(REFERENCE_DATE_Z, CURVES_NAME).trimBefore(BOND_SETTLEMENT_TIME_FIXED_3);
  private static final AnnuityPaymentFixed NOMINAL_TR_FIXED_3 = BOND_DESCRIPTION_DEFINITION_FIXED.getNominal().toDerivative(REFERENCE_DATE_Z, CURVES_NAME).trimBefore(BOND_SETTLEMENT_TIME_FIXED_3);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION_FIXED_3 = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FIXED, QUANTITY_FIXED,
      BOND_SETTLEMENT_DATE_FIXED_3, PRICE_FIXED);
  private static final PaymentFixed BOND_SETTLEMENT_FIXED_3 = new PaymentFixed(CUR, BOND_SETTLEMENT_TIME_FIXED_3, -PRICE_FIXED * QUANTITY_FIXED, REPO_CURVE_NAME);
  private static final BondFixedTransaction BOND_TRANSACTION_FIXED_3 = BOND_TRANSACTION_DEFINITION_FIXED_3.toDerivative(REFERENCE_DATE_Z, CURVES_NAME);
  // Ibor coupon Quarterly 2Y
  private static final DayCount DAY_COUNT_FRN = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final BusinessDayConvention BUSINESS_DAY_FRN = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_FRN = false;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final int IBOR_SPOT_LAG = 2;
  private static final BusinessDayConvention IBOR_BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IBOR_IS_EOM = false;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SPOT_LAG, CALENDAR, IBOR_DAY_COUNT, IBOR_BUSINESS_DAY, IBOR_IS_EOM);
  private static final Period BOND_TENOR_FRN = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS_FRN = 3; // Standard for euro-bonds.
  private static final ZonedDateTime START_ACCRUAL_DATE_FRN = DateUtil.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE_FRN = START_ACCRUAL_DATE_FRN.plus(BOND_TENOR_FRN);
  private static final BondIborSecurityDefinition BOND_DESCRIPTION_DEFINITION_FRN = BondIborSecurityDefinition.from(MATURITY_DATE_FRN, START_ACCRUAL_DATE_FRN, IBOR_INDEX, SETTLEMENT_DAYS_FRN,
      DAY_COUNT_FRN, BUSINESS_DAY_FRN, IS_EOM_FRN);
  // Transaction FRN
  private static final double FIRST_FIXING = 0.02;
  private static final double PRICE_FRN = 0.99;
  private static final ZonedDateTime BOND_SETTLEMENT_DATE_FRN = DateUtil.getUTCDate(2011, 8, 24);
  private static final double BOND_SETTLEMENT_TIME_FRN = ACT_ACT.getDayCountFraction(REFERENCE_DATE_Z, BOND_SETTLEMENT_DATE_FRN);
  private static final double QUANTITY_FRN = 100000000; //100m
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {BOND_DESCRIPTION_DEFINITION_FRN.getCoupon().getNthPayment(0)
      .getFixingDate()}, new double[] {FIRST_FIXING});
  private static final AnnuityPaymentFixed NOMINAL_TR_1_FRN = BOND_DESCRIPTION_DEFINITION_FRN.getNominal().toDerivative(REFERENCE_DATE_Z, CURVES_NAME).trimBefore(BOND_SETTLEMENT_TIME_FRN);
  private static final GenericAnnuity<? extends Payment> COUPON_TR_1_FRN = BOND_DESCRIPTION_DEFINITION_FRN.getCoupon().toDerivative(REFERENCE_DATE_Z, FIXING_TS, COUPON_IBOR_CURVE_NAME)
      .trimBefore(BOND_SETTLEMENT_TIME_FRN);
  private static final BondIborTransactionDefinition BOND_TRANSACTION_DEFINITION_FRN = new BondIborTransactionDefinition(BOND_DESCRIPTION_DEFINITION_FRN, QUANTITY_FRN, BOND_SETTLEMENT_DATE_FRN,
      PRICE_FRN);
  private static final PaymentFixed BOND_SETTLEMENT_FRN = new PaymentFixed(CUR, BOND_SETTLEMENT_TIME_FRN, -PRICE_FRN * QUANTITY_FRN, REPO_CURVE_NAME);
  private static final BondIborTransaction BOND_TRANSACTION_FRN = BOND_TRANSACTION_DEFINITION_FRN.toDerivative(REFERENCE_DATE_Z, FIXING_TS, CURVES_NAME);
  // Calculators
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueSensitivityCalculator PVSC = PresentValueSensitivityCalculator.getInstance();

  @Test
  public void testPVFixedBondSettlePast() {
    final BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    final double pv = method.presentValue(BOND_TRANSACTION_FIXED_1, CURVES);
    final double pvNominal = PVC.visit(NOMINAL_TR_FIXED_1, CURVES);
    final double pvCoupon = PVC.visit(COUPON_TR_FIXED_1, CURVES);
    assertEquals("Fixed bond present value", (pvNominal + pvCoupon) * QUANTITY_FIXED, pv);
  }

  @Test
  public void testPVFixedBondSettleToday() {
    final BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    final double pv = method.presentValue(BOND_TRANSACTION_FIXED_2, CURVES);
    final double pvNominal = PVC.visit(NOMINAL_TR_FIXED_2, CURVES);
    final double pvCoupon = PVC.visit(COUPON_TR_FIXED_2, CURVES);
    final double pvSettlement = BOND_SETTLEMENT_FIXED_2.getAmount();
    assertEquals("Fixed bond present value", (pvNominal + pvCoupon) * QUANTITY_FIXED + pvSettlement, pv);
  }

  @Test
  public void testPVFixedBondSettleFuture() {
    final BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    final double pv = method.presentValue(BOND_TRANSACTION_FIXED_3, CURVES);
    final double pvNominal = PVC.visit(NOMINAL_TR_FIXED_3, CURVES);
    final double pvCoupon = PVC.visit(COUPON_TR_FIXED_3, CURVES);
    final double pvSettlement = PVC.visit(BOND_SETTLEMENT_FIXED_3, CURVES);
    assertEquals("Fixed bond present value", (pvNominal + pvCoupon) * QUANTITY_FIXED + pvSettlement, pv);
  }

  @Test
  public void testPVSFixedBond() {
    final BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    final PresentValueSensitivity pvs = method.presentValueSensitivity(BOND_TRANSACTION_FIXED_3, CURVES);
    final PresentValueSensitivity pvsNominal = new PresentValueSensitivity(PVSC.visit(NOMINAL_TR_FIXED_3, CURVES));
    final PresentValueSensitivity pvsCoupon = new PresentValueSensitivity(PVSC.visit(COUPON_TR_FIXED_3, CURVES));
    final PresentValueSensitivity pvsSettlement = new PresentValueSensitivity(PVSC.visit(BOND_SETTLEMENT_FIXED_3, CURVES));
    final PresentValueSensitivity expectedPvs = pvsNominal.add(pvsCoupon).multiply(QUANTITY_FRN).add(pvsSettlement).clean();
    assertEquals("Fixed bond present value sensitivity", expectedPvs, pvs.clean());
  }

  @Test
  public void testFixedBondMethodCalculator() {
    final BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    final double pvMethod = method.presentValue(BOND_TRANSACTION_FIXED_3, CURVES);
    final double pvCalculator = PVC.visit(BOND_TRANSACTION_FIXED_3, CURVES);
    assertEquals("Fixed bond present value: Method vs Calculator", pvMethod, pvCalculator);
    final PresentValueSensitivity pvsMethod = method.presentValueSensitivity(BOND_TRANSACTION_FIXED_3, CURVES);
    final PresentValueSensitivity pvsCalculator = new PresentValueSensitivity(PVSC.visit(BOND_TRANSACTION_FIXED_3, CURVES));
    assertEquals("Fixed bond present value sensitivity: Method vs Calculator", pvsMethod, pvsCalculator);
  }

  @Test
  public void testPVIborBond() {
    final BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    final double pv = method.presentValue(BOND_TRANSACTION_FRN, CURVES);
    final double pvNominal = PVC.visit(NOMINAL_TR_1_FRN, CURVES);
    final double pvCoupon = PVC.visit(COUPON_TR_1_FRN, CURVES);
    final double pvSettlement = PVC.visit(BOND_SETTLEMENT_FRN, CURVES);
    assertEquals("FRN present value", (pvNominal + pvCoupon) * QUANTITY_FRN + pvSettlement, pv);
  }

  @Test
  public void testPVSIborBond() {
    final BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    final PresentValueSensitivity pvs = method.presentValueSensitivity(BOND_TRANSACTION_FRN, CURVES);
    final PresentValueSensitivity pvsNominal = new PresentValueSensitivity(PVSC.visit(NOMINAL_TR_1_FRN, CURVES));
    final PresentValueSensitivity pvsCoupon = new PresentValueSensitivity(PVSC.visit(COUPON_TR_1_FRN, CURVES));
    final PresentValueSensitivity pvsSettlement = new PresentValueSensitivity(PVSC.visit(BOND_SETTLEMENT_FRN, CURVES));
    final PresentValueSensitivity expectedPvs = pvsNominal.add(pvsCoupon).multiply(QUANTITY_FRN).add(pvsSettlement).clean();
    assertEquals("FRN present value sensitivity", expectedPvs, pvs.clean());
  }
}
