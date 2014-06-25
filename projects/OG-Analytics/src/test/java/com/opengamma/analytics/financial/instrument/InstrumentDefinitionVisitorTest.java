/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.commodity.definition.AgricultureForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.AgricultureFutureSecurityDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.AgricultureFutureTransactionDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.CouponCommodityCashSettleDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.CouponCommodityPhysicalSettleDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.EnergyFutureSecurityDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.EnergyFutureTransactionDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.ForwardCommodityCashSettleDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.ForwardCommodityPhysicalSettleDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.MetalFutureSecurityDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.MetalFutureTransactionDefinition;
import com.opengamma.analytics.financial.equity.EquityDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityIndexDividendFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityIndexFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.IndexFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.VolatilityIndexFutureDefinition;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOptionDefinition;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;
import com.opengamma.analytics.financial.equity.option.EquityOptionDefinition;
import com.opengamma.analytics.financial.equity.trs.definition.EquityTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwapDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondInterestIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondInterestIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesYieldAverageSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesYieldAverageTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationYearOnYearInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearInterpolationWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSimpleSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborFlatCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapMultilegDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.TotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedCompoundedONCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.varianceswap.VarianceSwapDefinition;
import com.opengamma.analytics.financial.instrument.volatilityswap.FXVolatilitySwapDefinition;
import com.opengamma.analytics.financial.instrument.volatilityswap.VolatilitySwapDefinition;
import com.opengamma.util.test.TestGroup;

/**
 * Class testing the instrument definition visitor.
 */
@Test(groups = TestGroup.UNIT)
public class InstrumentDefinitionVisitorTest {

  private static final Set<InstrumentDefinition<?>> ALL_INSTRUMENTS = TestInstrumentDefinitionsAndDerivatives.getAllInstruments();
  private static final MyVisitor<Object> VISITOR = new MyVisitor<>();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDelegate() {
    new InstrumentDefinitionVisitorDelegate<>(null);
  }

  @Test
  public void testNullVisitor() {
    for (final InstrumentDefinition<?> definition : ALL_INSTRUMENTS) {
      try {
        definition.accept(null);
        fail();
      } catch (final IllegalArgumentException e) {
      } catch (final NullPointerException e) {
        throw new NullPointerException("accept(InstrumentDefinitionVisitor visitor) in " + definition.getClass().getSimpleName() + " does not check that the visitor is not null");
      }
    }
    for (final InstrumentDefinition<?> definition : ALL_INSTRUMENTS) {
      try {
        definition.accept(null, "");
        fail();
      } catch (final IllegalArgumentException e) {
      } catch (final NullPointerException e) {
        throw new NullPointerException("accept(InstrumentDefinitionVisitor visitor, S data) in " + definition.getClass().getSimpleName() + " does not check that the visitor is not null");
      }
    }
  }

  @Test
  public void testVisitMethodsImplemented() {
    final Object o = "G";
    final String s = " + data";
    @SuppressWarnings("unused")
    int count = 0;
    for (final InstrumentDefinition<?> definition : ALL_INSTRUMENTS) {
      assertEquals(definition.accept(VISITOR), definition.getClass().getSimpleName());
      assertEquals(definition.accept(VISITOR, o), definition.getClass().getSimpleName() + s);
      count += 2;
    }
    //    assertTrue("Have not tested all methods - need to make sure that the accept() method in the definition points to the correct method in the visitor:",
    //        InstrumentDefinitionVisitor.class.getMethods().length <= count);
  }

  @Test
  public void testDelegate() {
    final String s = "aaaa";
    final String result = s + " + data1";
    final BondFixedVisitor<Object> visitor = new BondFixedVisitor<>(VISITOR, s);
    for (final InstrumentDefinition<?> definition : ALL_INSTRUMENTS) {
      if (definition instanceof BondFixedSecurityDefinition) {
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
    for (final InstrumentDefinition<?> definition : ALL_INSTRUMENTS) {
      try {
        definition.accept(visitor);
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }

    for (final InstrumentDefinition<?> definition : ALL_INSTRUMENTS) {
      try {
        definition.accept(visitor, "");
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }

    for (final InstrumentDefinition<?> definition : ALL_INSTRUMENTS) {
      try {
        definition.accept(visitor, null);
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }
  }

  @Test
  public void testSameValueAdapter() {
    final Double value = Math.PI;
    final InstrumentDefinitionVisitor<Double, Double> visitor = new InstrumentDefinitionVisitorSameValueAdapter<>(value);
    for (final InstrumentDefinition<?> definition : ALL_INSTRUMENTS) {
      assertEquals(value, definition.accept(visitor));
      assertEquals(value, definition.accept(visitor, Math.E));
    }
  }

  @Test
  public void testSameMethodAdapter() {
    final String data = "qwerty";
    final InstrumentDefinitionVisitor<String, String> visitor = new SameMethodAdapter();
    for (final InstrumentDefinition<?> definition : ALL_INSTRUMENTS) {
      final String simpleName = definition.getClass().getSimpleName();
      assertEquals(simpleName, definition.accept(visitor));
      assertEquals(definition.getClass().getSimpleName() + data, definition.accept(visitor, data));
    }
  }

  private static class DummyVisitor extends InstrumentDefinitionVisitorAdapter<Object, Object> {

    public DummyVisitor() {
    }
  }

  private static class SameMethodAdapter extends InstrumentDefinitionVisitorSameMethodAdapter<String, String> {

    public SameMethodAdapter() {
    }

    @Override
    public String visit(final InstrumentDefinition<?> instrument) {
      return instrument.getClass().getSimpleName();
    }

    @Override
    public String visit(final InstrumentDefinition<?> instrument, final String data) {
      return instrument.getClass().getSimpleName() + data;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition coupon, final String data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition coupon) {
      return null;
    }

  }

  private static class BondFixedVisitor<T> extends InstrumentDefinitionVisitorDelegate<T, String> {
    private final String _s;

    public BondFixedVisitor(final InstrumentDefinitionVisitor<T, String> delegate, final String s) {
      super(delegate);
      _s = s;
    }

    @Override
    public String visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond, final T data) {
      return _s + " + data1";
    }

    @Override
    public String visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond) {
      return _s;
    }

  }

  private static class MyVisitor<T> implements InstrumentDefinitionVisitor<T, String> {

    public MyVisitor() {
    }

    private String getValue(final InstrumentDefinition<?> definition, final boolean withData) {
      String result = definition.getClass().getSimpleName();
      if (withData) {
        result += " + data";
      }
      return result;
    }

    @Override
    public String visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondFutureDefinition(final BondFutureDefinition bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBillSecurityDefinition(final BillSecurityDefinition bill, final T data) {
      return getValue(bill, true);
    }

    @Override
    public String visitBillSecurityDefinition(final BillSecurityDefinition bill) {
      return getValue(bill, false);
    }

    @Override
    public String visitBillTransactionDefinition(final BillTransactionDefinition bill, final T data) {
      return getValue(bill, true);
    }

    @Override
    public String visitBillTransactionDefinition(final BillTransactionDefinition bill) {
      return getValue(bill, false);
    }

    @Override
    public String visitCashDefinition(final CashDefinition cash, final T data) {
      return getValue(cash, true);
    }

    @Override
    public String visitCashDefinition(final CashDefinition cash) {
      return getValue(cash, false);
    }

    @Override
    public String visitDepositIborDefinition(final DepositIborDefinition deposit, final T data) {
      return getValue(deposit, true);
    }

    @Override
    public String visitDepositIborDefinition(final DepositIborDefinition deposit) {
      return getValue(deposit, false);
    }

    @Override
    public String visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit, final T data) {
      return getValue(deposit, true);
    }

    @Override
    public String visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit) {
      return getValue(deposit, false);
    }

    @Override
    public String visitDepositZeroDefinition(final DepositZeroDefinition deposit, final T data) {
      return getValue(deposit, true);
    }

    @Override
    public String visitDepositZeroDefinition(final DepositZeroDefinition deposit) {
      return getValue(deposit, false);
    }

    @Override
    public String visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final T data) {
      return getValue(fra, true);
    }

    @Override
    public String visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
      return getValue(fra, false);
    }

    @Override
    public String visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitBondFutureDefinition(final BondFutureDefinition bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitDeliverableSwapFuturesSecurityDefinition(final SwapFuturesPriceDeliverableSecurityDefinition futures, final T data) {
      return getValue(futures, true);
    }

    @Override
    public String visitDeliverableSwapFuturesSecurityDefinition(final SwapFuturesPriceDeliverableSecurityDefinition futures) {
      return getValue(futures, false);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitPaymentFixedDefinition(final PaymentFixedDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitPaymentFixedDefinition(final PaymentFixedDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponFixedDefinition(final CouponFixedDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponFixedDefinition(final CouponFixedDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborDefinition(final CouponIborDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborDefinition(final CouponIborDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCapFloorIborDefinition(final CapFloorIborDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCapFloorIborDefinition(final CapFloorIborDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponOISSimplifiedDefinition(final CouponONSimplifiedDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponOISSimplifiedDefinition(final CouponONSimplifiedDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponOISDefinition(final CouponONDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponOISDefinition(final CouponONDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponCMSDefinition(final CouponCMSDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponCMSDefinition(final CouponCMSDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCapFloorCMSDefinition(final CapFloorCMSDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCapFloorCMSDefinition(final CapFloorCMSDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final T data) {
      return getValue(annuity, true);
    }

    @Override
    public String visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
      return getValue(annuity, false);
    }

    @Override
    public String visitSwapDefinition(final SwapDefinition swap, final T data) {
      return getValue(swap, true);
    }

    @Override
    public String visitSwapDefinition(final SwapDefinition swap) {
      return getValue(swap, false);
    }

    @Override
    public String visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final T data) {
      return getValue(swap, true);
    }

    @Override
    public String visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
      return getValue(swap, false);
    }

    @Override
    public String visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final T data) {
      return getValue(swap, true);
    }

    @Override
    public String visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
      return getValue(swap, false);
    }

    @Override
    public String visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final T data) {
      return getValue(swap, true);
    }

    @Override
    public String visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
      return getValue(swap, false);
    }

    @Override
    public String visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap, final T data) {
      return getValue(swap, true);
    }

    @Override
    public String visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap) {
      return getValue(swap, false);
    }

    @Override
    public String visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitCDSDefinition(final ISDACDSDefinition cds, final T data) {
      return getValue(cds, true);
    }

    @Override
    public String visitCDSDefinition(final ISDACDSDefinition cds) {
      return getValue(cds, false);
    }

    @Override
    public String visitForexDefinition(final ForexDefinition fx, final T data) {
      return getValue(fx, true);
    }

    @Override
    public String visitForexDefinition(final ForexDefinition fx) {
      return getValue(fx, false);
    }

    @Override
    public String visitForexSwapDefinition(final ForexSwapDefinition fx, final T data) {
      return getValue(fx, true);
    }

    @Override
    public String visitForexSwapDefinition(final ForexSwapDefinition fx) {
      return getValue(fx, false);
    }

    @Override
    public String visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx, final T data) {
      return getValue(fx, true);
    }

    @Override
    public String visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx) {
      return getValue(fx, false);
    }

    @Override
    public String visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx, final T data) {
      return getValue(fx, true);
    }

    @Override
    public String visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx) {
      return getValue(fx, false);
    }

    @Override
    public String visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf, final T data) {
      return getValue(ndf, true);
    }

    @Override
    public String visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf) {
      return getValue(ndf, false);
    }

    @Override
    public String visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo, final T data) {
      return getValue(ndo, true);
    }

    @Override
    public String visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo) {
      return getValue(ndo, false);
    }

    @Override
    public String visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx, final T data) {
      return getValue(fx, true);
    }

    @Override
    public String visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx) {
      return getValue(fx, false);
    }

    @Override
    public String visitMetalForwardDefinition(final MetalForwardDefinition forward, final T data) {
      return getValue(forward, true);
    }

    @Override
    public String visitMetalForwardDefinition(final MetalForwardDefinition forward) {
      return getValue(forward, false);
    }

    @Override
    public String visitMetalFutureDefinition(final MetalFutureDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitMetalFutureDefinition(final MetalFutureDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitMetalFutureOptionDefinition(final MetalFutureOptionDefinition option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitMetalFutureOptionDefinition(final MetalFutureOptionDefinition option) {
      return getValue(option, false);
    }

    @Override
    public String visitAgricultureForwardDefinition(final AgricultureForwardDefinition forward, final T data) {
      return getValue(forward, true);
    }

    @Override
    public String visitAgricultureForwardDefinition(final AgricultureForwardDefinition forward) {
      return getValue(forward, false);
    }

    @Override
    public String visitAgricultureFutureDefinition(final AgricultureFutureDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitAgricultureFutureDefinition(final AgricultureFutureDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitAgricultureFutureOptionDefinition(final AgricultureFutureOptionDefinition option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitAgricultureFutureOptionDefinition(final AgricultureFutureOptionDefinition option) {
      return getValue(option, false);
    }

    @Override
    public String visitEnergyForwardDefinition(final EnergyForwardDefinition forward, final T data) {
      return getValue(forward, true);
    }

    @Override
    public String visitEnergyForwardDefinition(final EnergyForwardDefinition forward) {
      return getValue(forward, false);
    }

    @Override
    public String visitEnergyFutureDefinition(final EnergyFutureDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEnergyFutureDefinition(final EnergyFutureDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitEnergyFutureOptionDefinition(final EnergyFutureOptionDefinition option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitEnergyFutureOptionDefinition(final EnergyFutureOptionDefinition option) {
      return getValue(option, false);
    }

    @Override
    public String visitEquityFutureDefinition(final EquityFutureDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEquityFutureDefinition(final EquityFutureDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitEquityIndexDividendFutureDefinition(final EquityIndexDividendFutureDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEquityIndexDividendFutureDefinition(final EquityIndexDividendFutureDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitEquityIndexOptionDefinition(final EquityIndexOptionDefinition option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitEquityIndexOptionDefinition(final EquityIndexOptionDefinition option) {
      return getValue(option, false);
    }

    @Override
    public String visitEquityIndexFutureOptionDefinition(final EquityIndexFutureOptionDefinition option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitEquityIndexFutureOptionDefinition(final EquityIndexFutureOptionDefinition option) {
      return getValue(option, false);
    }

    @Override
    public String visitEquityOptionDefinition(final EquityOptionDefinition option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitEquityOptionDefinition(final EquityOptionDefinition option) {
      return getValue(option, false);
    }

    @Override
    public String visitVarianceSwapDefinition(final VarianceSwapDefinition varianceSwap) {
      return getValue(varianceSwap, false);
    }

    @Override
    public String visitVarianceSwapDefinition(final VarianceSwapDefinition varianceSwap, final T data) {
      return getValue(varianceSwap, true);
    }

    @Override
    public String visitEquityVarianceSwapDefinition(final EquityVarianceSwapDefinition varianceSwap) {
      return getValue(varianceSwap, false);
    }

    @Override
    public String visitEquityVarianceSwapDefinition(final EquityVarianceSwapDefinition varianceSwap, final T data) {
      return getValue(varianceSwap, true);
    }

    @Override
    public String visitVolatilitySwapDefinition(final VolatilitySwapDefinition volatilitySwap) {
      return getValue(volatilitySwap, false);
    }

    @Override
    public String visitVolatilitySwapDefinition(final VolatilitySwapDefinition volatilitySwap, final T data) {
      return getValue(volatilitySwap, true);
    }

    @Override
    public String visitFXVolatilitySwapDefinition(final FXVolatilitySwapDefinition volatilitySwap) {
      return getValue(volatilitySwap, false);
    }

    @Override
    public String visitFXVolatilitySwapDefinition(final FXVolatilitySwapDefinition volatilitySwap, final T data) {
      return getValue(volatilitySwap, true);
    }

    @Override
    public String visitTotalReturnSwapDefinition(final TotalReturnSwapDefinition totalReturnSwap) {
      return getValue(totalReturnSwap, false);
    }

    @Override
    public String visitTotalReturnSwapDefinition(final TotalReturnSwapDefinition totalReturnSwap, final T data) {
      return getValue(totalReturnSwap, true);
    }

    @Override
    public String visitBondTotalReturnSwapDefinition(final BondTotalReturnSwapDefinition totalReturnSwap) {
      return getValue(totalReturnSwap, false);
    }

    @Override
    public String visitBondTotalReturnSwapDefinition(final BondTotalReturnSwapDefinition totalReturnSwap, final T data) {
      return getValue(totalReturnSwap, true);
    }

    @Override
    public String visitEquityTotalReturnSwapDefinition(final EquityTotalReturnSwapDefinition totalReturnSwap) {
      return getValue(totalReturnSwap, false);
    }

    @Override
    public String visitEquityTotalReturnSwapDefinition(final EquityTotalReturnSwapDefinition totalReturnSwap, final T data) {
      return getValue(totalReturnSwap, true);
    }

    @Override
    public String visitEquityDefinition(final EquityDefinition equity) {
      return getValue(equity, false);
    }

    @Override
    public String visitEquityDefinition(final EquityDefinition equity, final T data) {
      return getValue(equity, true);
    }

    @Override
    public String visitCouponIborCompoundingSpreadDefinition(final CouponIborCompoundingSpreadDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborCompoundingSpreadDefinition(final CouponIborCompoundingSpreadDefinition payment) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborAverageDefinition(final CouponIborAverageIndexDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborAverageDefinition(final CouponIborAverageIndexDefinition payment) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponInflationYearOnYearFirstOfMonth(final CouponInflationYearOnYearMonthlyDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationYearOnYearFirstOfMonth(final CouponInflationYearOnYearMonthlyDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponFixedCompoundingDefinition(final CouponFixedCompoundingDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponFixedCompoundingDefinition(final CouponFixedCompoundingDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitIndexFutureDefinition(final IndexFutureDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitIndexFutureDefinition(final IndexFutureDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitEquityIndexFutureDefinition(final EquityIndexFutureDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEquityIndexFutureDefinition(final EquityIndexFutureDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitVolatilityIndexFutureDefinition(final VolatilityIndexFutureDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitVolatilityIndexFutureDefinition(final VolatilityIndexFutureDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitDeliverableSwapFuturesTransactionDefinition(final SwapFuturesPriceDeliverableTransactionDefinition futures, final T data) {
      return getValue(futures, true);
    }

    @Override
    public String visitDeliverableSwapFuturesTransactionDefinition(final SwapFuturesPriceDeliverableTransactionDefinition futures) {
      return getValue(futures, false);
    }

    @Override
    public String visitCapFloorInflationZeroCouponInterpolationDefinition(final CapFloorInflationZeroCouponInterpolationDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCapFloorInflationZeroCouponInterpolationDefinition(final CapFloorInflationZeroCouponInterpolationDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCapFloorInflationZeroCouponMonthlyDefinition(final CapFloorInflationZeroCouponMonthlyDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCapFloorInflationZeroCouponMonthlyDefinition(final CapFloorInflationZeroCouponMonthlyDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCapFloorInflationYearOnYearInterpolationDefinition(final CapFloorInflationYearOnYearInterpolationDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCapFloorInflationYearOnYearInterpolationDefinition(final CapFloorInflationYearOnYearInterpolationDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCapFloorInflationYearOnYearMonthlyDefinition(final CapFloorInflationYearOnYearMonthlyDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCapFloorInflationYearOnYearMonthlyDefinition(final CapFloorInflationYearOnYearMonthlyDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponArithmeticAverageONDefinition(final CouponONArithmeticAverageDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponArithmeticAverageONDefinition(final CouponONArithmeticAverageDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponArithmeticAverageONSpreadSimplifiedDefinition(final CouponONArithmeticAverageSpreadSimplifiedDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponArithmeticAverageONSpreadSimplifiedDefinition(final CouponONArithmeticAverageSpreadSimplifiedDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponONSpreadDefinition(final CouponONSpreadDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponONSpreadDefinition(final CouponONSpreadDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitBondFuturesSecurityDefinition(final BondFuturesSecurityDefinition bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondFuturesSecurityDefinition(final BondFuturesSecurityDefinition bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondFuturesTransactionDefinition(final BondFuturesTransactionDefinition bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondFuturesTransactionDefinition(final BondFuturesTransactionDefinition bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMarginDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMarginDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMarginDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMarginDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitBondInterestIndexedSecurity(final BondInterestIndexedSecurityDefinition<?, ?> bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondInterestIndexedSecurity(final BondInterestIndexedSecurityDefinition<?, ?> bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondInterestIndexedTransaction(final BondInterestIndexedTransactionDefinition<?, ?> bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondInterestIndexedTransaction(final BondInterestIndexedTransactionDefinition<?, ?> bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitCouponONSpreadSimplifiedDefinition(final CouponONSpreadSimplifiedDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponONSpreadSimplifiedDefinition(final CouponONSpreadSimplifiedDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponONCompoundedDefinition(final CouponONCompoundedDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponONCompoundedDefinition(final CouponONCompoundedDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitSwaptionCashFixedONCompoundingDefinition(final SwaptionCashFixedCompoundedONCompoundingDefinition swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitSwaptionCashFixedONCompoundingDefinition(final SwaptionCashFixedCompoundedONCompoundingDefinition swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitSwapMultilegDefinition(final SwapMultilegDefinition swap, final T data) {
      return getValue(swap, true);
    }

    @Override
    public String visitSwapMultilegDefinition(final SwapMultilegDefinition swap) {
      return getValue(swap, false);
    }

    @Override
    public String visitMetalFutureSecurityDefinition(final MetalFutureSecurityDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitMetalFutureSecurityDefinition(final MetalFutureSecurityDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitMetalFutureTransactionDefinition(final MetalFutureTransactionDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitMetalFuturTransactioneDefinition(final MetalFutureTransactionDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitAgricultureFutureSecurityDefinition(final AgricultureFutureSecurityDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitAgricultureFutureSecurityDefinition(final AgricultureFutureSecurityDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitAgricultureFutureTransactionDefinition(final AgricultureFutureTransactionDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitAgricultureFutureTransactionDefinition(final AgricultureFutureTransactionDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitEnergyFutureSecurityDefinition(final EnergyFutureSecurityDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEnergyFutureSecurityDefinition(final EnergyFutureSecurityDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitEnergyFutureTransactionDefinition(final EnergyFutureTransactionDefinition future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEnergyFutureTransactionDefinition(final EnergyFutureTransactionDefinition future) {
      return getValue(future, false);
    }

    @Override
    public String visitForwardCommodityCashSettleDefinition(final ForwardCommodityCashSettleDefinition forward, final T data) {
      return getValue(forward, true);
    }

    @Override
    public String visitForwardCommodityCashSettleDefinition(final ForwardCommodityCashSettleDefinition forward) {
      return getValue(forward, false);
    }

    @Override
    public String visitForwardCommodityPhysicalSettleDefinition(final ForwardCommodityPhysicalSettleDefinition forward, final T data) {
      return getValue(forward, true);
    }

    @Override
    public String visitForwardCommodityPhysicalSettleDefinition(final ForwardCommodityPhysicalSettleDefinition forward) {
      return getValue(forward, false);
    }

    @Override
    public String visitCouponCommodityCashSettleDefinition(final CouponCommodityCashSettleDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponCommodityCashSettleDefinition(final CouponCommodityCashSettleDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponCommodityPhysicalSettleDefinition(final CouponCommodityPhysicalSettleDefinition coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponCommodityPhysicalSettleDefinition(final CouponCommodityPhysicalSettleDefinition coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitBondFuturesYieldAverageSecurityDefinition(final BondFuturesYieldAverageSecurityDefinition bondFuture, final T data) {
      return getValue(bondFuture, true);
    }

    @Override
    public String visitBondFuturesYieldAverageSecurityDefinition(final BondFuturesYieldAverageSecurityDefinition bondFuture) {
      return getValue(bondFuture, false);
    }

    @Override
    public String visitYieldAverageBondFuturesTransactionDefinition(final BondFuturesYieldAverageTransactionDefinition bondFuture, final T data) {
      return getValue(bondFuture, true);
    }

    @Override
    public String visitYieldAverageBondFuturesTransactionDefinition(final BondFuturesYieldAverageTransactionDefinition bondFuture) {
      return getValue(bondFuture, false);
    }

    @Override
    public String visitBondFuturesOptionMarginSecurityDefinition(BondFuturesOptionMarginSecurityDefinition bondFutureOption, T data) {
      return null;
    }

    @Override
    public String visitBondFuturesOptionMarginSecurityDefinition(BondFuturesOptionMarginSecurityDefinition bondFutureOption) {
      return null;
    }

    @Override
    public String visitBondFuturesOptionMarginTransactionDefinition(BondFuturesOptionMarginTransactionDefinition bondFutureOption, T data) {
      return null;
    }

    @Override
    public String visitBondFuturesOptionMarginTransactionDefinition(BondFuturesOptionMarginTransactionDefinition bondFutureOption) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingSimpleSpreadDefinition(CouponIborCompoundingSimpleSpreadDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingSimpleSpreadDefinition(CouponIborCompoundingSimpleSpreadDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment, T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborAverageCompoundingDefinition(CouponIborAverageCompoundingDefinition payment, T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborAverageCompoundingDefinition(CouponIborAverageCompoundingDefinition payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborFlatCompoundingSpreadDefinition(CouponIborFlatCompoundingSpreadDefinition payment, T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborFlatCompoundingSpreadDefinition(CouponIborFlatCompoundingSpreadDefinition payment) {
      return getValue(payment, false);
    }
  }

}
