/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureForward;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyForward;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalForward;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.AgricultureFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.AgricultureFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityPhysicalSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.EnergyFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.EnergyFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityPhysicalSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.MetalFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.MetalFutureTransaction;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.equity.Equity;
import com.opengamma.analytics.financial.equity.future.derivative.CashSettledFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.IndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.VolatilityIndexFuture;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.trs.EquityTotalReturnSwap;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwap;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.instrument.TestInstrumentDefinitionsAndDerivatives;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolationWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthlyWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSimpleSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCompoundingCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.financial.volatilityswap.FXVolatilitySwap;
import com.opengamma.analytics.financial.volatilityswap.VolatilitySwap;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class InstrumentDerivativeVisitorTest {
  private static final Set<InstrumentDerivative> ALL_DERIVATIVES = TestInstrumentDefinitionsAndDerivatives.getAllDerivatives();
  private static final MyVisitor<Object> VISITOR = new MyVisitor<>();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDerivative() {
    new InstrumentDerivativeVisitorDelegate<>(null);
  }

  @Test
  public void testNullVisitor() {
    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      if (derivative != null) {
        try {
          derivative.accept(null);
          fail();
        } catch (final IllegalArgumentException e) {
        } catch (final NullPointerException e) {
          throw new NullPointerException("accept(InstrumentDerivativeVisitor visitor) in " + derivative.getClass().getSimpleName() + " does not check that the visitor is not null");
        }
      } else {
        throw new NullPointerException("Derivative was null");
      }
    }
    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      try {
        derivative.accept(null, "");
        fail();
      } catch (final IllegalArgumentException e) {
      } catch (final NullPointerException e) {
        throw new NullPointerException("accept(InstrumentDerivativeVisitor visitor, S data) in " + derivative.getClass().getSimpleName() + " does not check that the visitor is not null");
      }
    }
  }

  @Test
  public void testVisitMethodsImplemented() {

  }

  @Test
  public void testDelegate() {
    final String s = "aaaa";
    final String result = s + " + data1";
    final BondFixedVisitor<Object> visitor = new BondFixedVisitor<>(VISITOR, s);
    for (final InstrumentDerivative definition : ALL_DERIVATIVES) {
      if (definition instanceof BondFixedSecurity) {
        assertEquals(definition.accept(visitor), s);
        assertEquals(definition.accept(visitor, ""), result);
      } else {
        assertEquals(definition.accept(visitor), definition.accept(VISITOR));
        assertEquals(definition.accept(visitor, ""), definition.accept(VISITOR, ""));
      }
    }
  }

  @Test
  public void testAdapter() {
    final DummyVisitor visitor = new DummyVisitor();
    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      try {
        derivative.accept(visitor);
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }

    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      try {
        derivative.accept(visitor, "");
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }

    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      try {
        derivative.accept(visitor, null);
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }
  }

  @Test
  public void testSameValueAdapter() {
    final Double value = Math.PI;
    final InstrumentDerivativeVisitor<Double, Double> visitor = new InstrumentDerivativeVisitorSameValueAdapter<>(value);
    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      assertEquals(value, derivative.accept(visitor));
      assertEquals(value, derivative.accept(visitor, Math.E));
    }
  }

  @Test
  public void testSameMethodAdapter() {
    final String data = "qwerty";
    final InstrumentDerivativeVisitor<String, String> visitor = new SameMethodAdapter();
    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      final String simpleName = derivative.getClass().getSimpleName();
      assertEquals(simpleName, derivative.accept(visitor));
      assertEquals(derivative.getClass().getSimpleName() + data, derivative.accept(visitor, data));
    }
  }

  private static class DummyVisitor extends InstrumentDerivativeVisitorAdapter<Object, Object> {

    public DummyVisitor() {
    }

  }

  private static class SameMethodAdapter extends InstrumentDerivativeVisitorSameMethodAdapter<String, String> {

    public SameMethodAdapter() {
    }

    @Override
    public String visit(final InstrumentDerivative instrument) {
      return instrument.getClass().getSimpleName();
    }

    @Override
    public String visit(final InstrumentDerivative instrument, final String data) {
      return instrument.getClass().getSimpleName() + data;
    }

  }

  private static class BondFixedVisitor<T> extends InstrumentDerivativeVisitorDelegate<T, String> {
    private final String _s;

    public BondFixedVisitor(final InstrumentDerivativeVisitor<T, String> delegate, final String s) {
      super(delegate);
      _s = s;
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond, final T data) {
      return _s + " + data1";
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond) {
      return _s;
    }

  }

  private static class MyVisitor<T> implements InstrumentDerivativeVisitor<T, String> {

    public MyVisitor() {
    }

    private String getValue(final InstrumentDerivative derivative, final boolean withData) {
      String result = derivative.getClass().getSimpleName();
      if (withData) {
        result += " + data";
      }
      return result;
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondFixedTransaction(final BondFixedTransaction bond, final T data) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondIborSecurity(final BondIborSecurity bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondIborTransaction(final BondIborTransaction bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBillSecurity(final BillSecurity bill, final T data) {
      return getValue(bill, true);
    }

    @Override
    public String visitBillTransaction(final BillTransaction bill, final T data) {
      return getValue(bill, true);
    }

    @Override
    public String visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity, final T data) {
      return getValue(genericAnnuity, true);
    }

    @Override
    public String visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final T data) {
      return getValue(fixedCouponAnnuity, true);
    }

    @Override
    public String visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final T data) {
      return getValue(annuity, true);
    }

    @Override
    public String visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final T data) {
      return getValue(swap, true);
    }

    @Override
    public String visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitForexForward(final ForexForward fx, final T data) {
      throw new NotImplementedException("Not implemented because derivative is deprecated");
    }

    @Override
    public String visitCash(final Cash cash, final T data) {
      return getValue(cash, true);
    }

    @Override
    public String visitFixedPayment(final PaymentFixed payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponCMS(final CouponCMS payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCapFloorIbor(final CapFloorIbor payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCapFloorCMS(final CapFloorCMS payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitForwardRateAgreement(final ForwardRateAgreement fra, final T data) {
      return getValue(fra, true);
    }

    @Override
    public String visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitCDSDerivative(final ISDACDSDerivative cds, final T data) {
      return getValue(cds, true);
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondFixedTransaction(final BondFixedTransaction bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondIborSecurity(final BondIborSecurity bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondIborTransaction(final BondIborTransaction bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBillSecurity(final BillSecurity bill) {
      return getValue(bill, false);
    }

    @Override
    public String visitBillTransaction(final BillTransaction bill) {
      return getValue(bill, false);
    }

    @Override
    public String visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity) {
      return getValue(genericAnnuity, false);
    }

    @Override
    public String visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
      return getValue(fixedCouponAnnuity, false);
    }

    @Override
    public String visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
      return getValue(annuity, false);
    }

    @Override
    public String visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
      return getValue(swap, false);
    }

    @Override
    public String visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitForexForward(final ForexForward fx) {
      throw new NotImplementedException("Not implemented because derivative is deprecated");
    }

    @Override
    public String visitCash(final Cash cash) {
      return getValue(cash, false);
    }

    @Override
    public String visitFixedPayment(final PaymentFixed payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponCMS(final CouponCMS payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCapFloorIbor(final CapFloorIbor payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCapFloorCMS(final CapFloorCMS payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitForwardRateAgreement(final ForwardRateAgreement fra) {
      return getValue(fra, false);
    }

    @Override
    public String visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitCDSDerivative(final ISDACDSDerivative cds) {
      return getValue(cds, false);
    }

    @Override
    public String visitCouponFixed(final CouponFixed payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponFixed(final CouponFixed payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitInterpolatedStubCoupon(final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitInterpolatedStubCoupon(final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIbor(final CouponIbor payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIbor(final CouponIbor payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborSpread(final CouponIborSpread payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborSpread(final CouponIborSpread payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborGearing(final CouponIborGearing payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborGearing(final CouponIborGearing payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborCompounding(final CouponIborCompounding payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponOIS(final CouponON payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponOIS(final CouponON payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitSwap(final Swap<?, ?> swap, final T data) {
      return getValue(swap, true);
    }

    @Override
    public String visitSwap(final Swap<?, ?> swap) {
      return getValue(swap, false);
    }

    @Override
    public String visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitBondFuture(final BondFuture bondFuture, final T data) {
      return getValue(bondFuture, true);
    }

    @Override
    public String visitBondFuture(final BondFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
      return getValue(future, false);
    }

    @Override
    public String visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future) {
      return getValue(future, false);
    }

    @Override
    public String visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future) {
      return getValue(future, false);
    }

    @Override
    public String visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures, final T data) {
      return getValue(futures, true);
    }

    @Override
    public String visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures) {
      return getValue(futures, false);
    }

    @Override
    public String visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option) {
      return getValue(option, false);
    }

    @Override
    public String visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option) {
      return getValue(option, false);
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
      return getValue(option, false);
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
      return getValue(option, false);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
      return getValue(option, false);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
      return getValue(option, false);
    }

    @Override
    public String visitDepositIbor(final DepositIbor deposit, final T data) {
      return getValue(deposit, true);
    }

    @Override
    public String visitDepositIbor(final DepositIbor deposit) {
      return getValue(deposit, false);
    }

    @Override
    public String visitDepositCounterpart(final DepositCounterpart deposit, final T data) {
      return getValue(deposit, true);
    }

    @Override
    public String visitDepositCounterpart(final DepositCounterpart deposit) {
      return getValue(deposit, false);
    }

    @Override
    public String visitDepositZero(final DepositZero deposit, final T data) {
      return getValue(deposit, true);
    }

    @Override
    public String visitDepositZero(final DepositZero deposit) {
      return getValue(deposit, false);
    }

    @Override
    public String visitForex(final Forex derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForex(final Forex derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexSwap(final ForexSwap derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexSwap(final ForexSwap derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexOptionVanilla(final ForexOptionVanilla derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexOptionVanilla(final ForexOptionVanilla derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexOptionDigital(final ForexOptionDigital derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexOptionDigital(final ForexOptionDigital derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitMetalForward(final MetalForward future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitMetalForward(final MetalForward future) {
      return getValue(future, false);
    }

    @Override
    public String visitMetalFuture(final MetalFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitMetalFuture(final MetalFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitMetalFutureOption(final MetalFutureOption future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitMetalFutureOption(final MetalFutureOption future) {
      return getValue(future, false);
    }

    @Override
    public String visitAgricultureForward(final AgricultureForward future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitAgricultureForward(final AgricultureForward future) {
      return getValue(future, false);
    }

    @Override
    public String visitAgricultureFuture(final AgricultureFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitAgricultureFuture(final AgricultureFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitAgricultureFutureOption(final AgricultureFutureOption future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitAgricultureFutureOption(final AgricultureFutureOption future) {
      return getValue(future, false);
    }

    @Override
    public String visitEnergyForward(final EnergyForward future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEnergyForward(final EnergyForward future) {
      return getValue(future, false);
    }

    @Override
    public String visitEnergyFuture(final EnergyFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEnergyFuture(final EnergyFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitEnergyFutureOption(final EnergyFutureOption future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEnergyFutureOption(final EnergyFutureOption future) {
      return getValue(future, false);
    }

    @Override
    public String visitCouponIborCompounding(final CouponIborCompounding payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitEquityFuture(final EquityFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitEquityFuture(final EquityFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEquityIndexDividendFuture(final EquityIndexDividendFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEquityIndexOption(final EquityIndexOption option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitEquityIndexOption(final EquityIndexOption option) {
      return getValue(option, false);
    }

    @Override
    public String visitEquityIndexFutureOption(final EquityIndexFutureOption option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitEquityIndexFutureOption(final EquityIndexFutureOption option) {
      return getValue(option, false);
    }

    @Override
    public String visitEquityOption(final EquityOption option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitEquityOption(final EquityOption option) {
      return getValue(option, false);
    }

    @Override
    public String visitVarianceSwap(final VarianceSwap varianceSwap) {
      return getValue(varianceSwap, false);
    }

    @Override
    public String visitVarianceSwap(final VarianceSwap varianceSwap, final T data) {
      return getValue(varianceSwap, true);
    }

    @Override
    public String visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap) {
      return getValue(varianceSwap, false);
    }

    @Override
    public String visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap, final T data) {
      return getValue(varianceSwap, true);
    }

    @Override
    public String visitVolatilitySwap(final VolatilitySwap volatilitySwap) {
      return getValue(volatilitySwap, false);
    }

    @Override
    public String visitVolatilitySwap(final VolatilitySwap volatilitySwap, final T data) {
      return getValue(volatilitySwap, true);
    }

    @Override
    public String visitFXVolatilitySwap(final FXVolatilitySwap volatilitySwap) {
      return getValue(volatilitySwap, false);
    }

    @Override
    public String visitFXVolatilitySwap(final FXVolatilitySwap volatilitySwap, final T data) {
      return getValue(volatilitySwap, true);
    }

    @Override
    public String visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIborAverage(final CouponIborAverage payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIborAverage(final CouponIborAverage payment) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final T data) {
      return null;
    }

    @Override
    public String visitCouponFixedCompounding(final CouponFixedCompounding payment, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity future) {
      return null;
    }

    @Override
    public String visitCouponFixedCompounding(final CouponFixedCompounding payment) {
      return null;
    }

    @Override
    public String visitFixedCompoundingCouponSwap(final SwapFixedCompoundingCoupon<?> swap, final T data) {
      return null;
    }

    @Override
    public String visitFixedCompoundingCouponSwap(final SwapFixedCompoundingCoupon<?> swap) {
      return null;
    }

    @Override
    public String visitCashSettledFuture(final CashSettledFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitCashSettledFuture(final CashSettledFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitIndexFuture(final IndexFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitIndexFuture(final IndexFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitEquityIndexFuture(final EquityIndexFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEquityIndexFuture(final EquityIndexFuture future) {
      return getValue(future, false);// TODO Auto-generated method stub
    }

    @Override
    public String visitVolatilityIndexFuture(final VolatilityIndexFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitVolatilityIndexFuture(final VolatilityIndexFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitCouponONArithmeticAverage(final CouponONArithmeticAverage payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverage(final CouponONArithmeticAverage payment) {
      return null;
    }

    @Override
    public String visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures, final T data) {
      return null;
    }

    @Override
    public String visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation coupon, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation coupon) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly coupon, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly coupon) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation coupon, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation coupon) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly coupon, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly coupon) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified payment) {
      return null;
    }

    @Override
    public String visitBondFuturesSecurity(final BondFuturesSecurity bondFutures, final T data) {
      return null;
    }

    @Override
    public String visitBondFuturesSecurity(final BondFuturesSecurity bondFutures) {
      return null;
    }

    @Override
    public String visitBondFuturesTransaction(final BondFuturesTransaction bondFutures, final T data) {
      return null;
    }

    @Override
    public String visitBondFuturesTransaction(final BondFuturesTransaction bondFutures) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMargin coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMargin coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMargin coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMargin coupon) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedSecurity(final BondInterestIndexedSecurity<?, ?> bond) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedSecurity(final BondInterestIndexedSecurity<?, ?> bond, final T data) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedTransaction(final BondInterestIndexedTransaction<?, ?> bond, final T data) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedTransaction(final BondInterestIndexedTransaction<?, ?> bond) {
      return null;
    }

    @Override
    public String visitCouponONSpread(final CouponONSpread payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponONSpread(final CouponONSpread payment) {
      return null;
    }

    @Override
    public String visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment) {
      return null;
    }

    @Override
    public String visitCouponONCompounded(final CouponONCompounded payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponONCompounded(final CouponONCompounded payment) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedCompoundedONCompounded(final SwaptionCashFixedCompoundedONCompounded swaption, final T data) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final T data) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedCompoundedONCompounded(final SwaptionCashFixedCompoundedONCompounded swaption) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment, final T data) {
      return null;
    }

    @Override
    public String visitSwapMultileg(final SwapMultileg swap, final T data) {
      return null;
    }

    @Override
    public String visitSwapMultileg(final SwapMultileg swap) {
      return null;
    }

    @Override
    public String visitMetalFutureSecurity(final MetalFutureSecurity future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitMetalFutureSecurity(final MetalFutureSecurity future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitEnergyFutureSecurity(final EnergyFutureSecurity future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitEnergyFutureSecurity(final EnergyFutureSecurity future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitAgricultureFutureSecurity(final AgricultureFutureSecurity future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitAgricultureFutureSecurity(final AgricultureFutureSecurity future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitMetalFutureTransaction(final MetalFutureTransaction future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitMetalFutureTransaction(final MetalFutureTransaction future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitEnergyFutureTransaction(final EnergyFutureTransaction future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitEnergyFutureTransaction(final EnergyFutureTransaction future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitAgricultureFutureTransaction(final AgricultureFutureTransaction future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitAgricultureFutureTransaction(final AgricultureFutureTransaction future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitCouponCommodityCashSettle(final CouponCommodityCashSettle coupon, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitCouponCommodityCashSettle(final CouponCommodityCashSettle coupon) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitCouponCommodityPhysicalSettle(final CouponCommodityPhysicalSettle coupon, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitCouponCommodityPhysicalSettle(final CouponCommodityPhysicalSettle coupon) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitForwardCommodityCashSettle(final ForwardCommodityCashSettle forward, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitForwardCommodityCashSettle(final ForwardCommodityCashSettle forward) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitForwardCommodityPhysicalSettle(final ForwardCommodityPhysicalSettle forward, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitForwardCommodityPhysicalSettle(final ForwardCommodityPhysicalSettle forward) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitBondFuturesYieldAverageSecurity(final BondFuturesYieldAverageSecurity bondFutures, final T data) {
      return null;
    }

    @Override
    public String visitBondFuturesYieldAverageSecurity(final BondFuturesYieldAverageSecurity bondFutures) {
      return null;
    }

    @Override
    public String visitYieldAverageBondFuturesTransaction(final BondFuturesYieldAverageTransaction bondFutures, final T data) {
      return null;
    }

    @Override
    public String visitYieldAverageBondFuturesTransaction(final BondFuturesYieldAverageTransaction bondFutures) {
      return null;
    }

    @Override
    public String visitTotalReturnSwap(final TotalReturnSwap totalReturnSwap) {
      return getValue(totalReturnSwap, false);
    }

    @Override
    public String visitTotalReturnSwap(final TotalReturnSwap totalReturnSwap, final T data) {
      return getValue(totalReturnSwap, true);
    }

    @Override
    public String visitBondTotalReturnSwap(final BondTotalReturnSwap totalReturnSwap) {
      return getValue(totalReturnSwap, false);
    }

    @Override
    public String visitBondTotalReturnSwap(final BondTotalReturnSwap totalReturnSwap, final T data) {
      return getValue(totalReturnSwap, true);
    }

    @Override
    public String visitEquityTotalReturnSwap(final EquityTotalReturnSwap totalReturnSwap) {
      return getValue(totalReturnSwap, false);
    }

    @Override
    public String visitEquityTotalReturnSwap(final EquityTotalReturnSwap totalReturnSwap, final T data) {
      return getValue(totalReturnSwap, true);
    }

    @Override
    public String visitEquity(final Equity equity) {
      return getValue(equity, false);
    }

    @Override
    public String visitEquity(final Equity equity, final T data) {
      return getValue(equity, true);
    }

    @Override
    public String visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option, T data) {
      return null;
    }

    @Override
    public String visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option) {
      return null;
    }

    @Override
    public String visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, T data) {
      return null;
    }

    @Override
    public String visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingSimpleSpread(CouponIborCompoundingSimpleSpread payment) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingSimpleSpread(CouponIborCompoundingSimpleSpread payment, T data) {
      return null;
    }
  }

}
