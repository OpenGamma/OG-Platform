/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.financial.instrument.future.BondFutureDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponFloatingDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.financial.instrument.payment.CouponOISSimplifiedDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;

/**
 * Tests the visitor of Forex definitions.
 */
public class ForexDefinitionVisitorTest {

  private static final ForexDefinition FX_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexDefinition();
  private static final ForexSwapDefinition FX_SWAP_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexSwapDefinition();
  private static final ForexOptionVanillaDefinition FX_OPTION_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexOptionVanillaDefinition();
  private static final ForexOptionSingleBarrierDefinition FX_SINGLE_BARRIER_OPTION_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexOptionSingleBarrierDefinition();
  private static final ForexNonDeliverableForwardDefinition NDF_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexNonDeliverableForwardDefinition();
  private static final ForexNonDeliverableOptionDefinition NDO_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexNonDeliverableOptionDefinition();

  @SuppressWarnings("synthetic-access")
  private static final MyVisitor<Object, String> VISITOR = new MyVisitor<Object, String>();

  @Test
  public void testVisitor() {
    final Object o = "G";
    assertEquals(FX_DEFINITION.accept(VISITOR), "Forex1");
    assertEquals(FX_DEFINITION.accept(VISITOR, o), "Forex2");
    assertEquals(FX_SWAP_DEFINITION.accept(VISITOR), "ForexSwap1");
    assertEquals(FX_SWAP_DEFINITION.accept(VISITOR, o), "ForexSwap2");
    assertEquals(FX_OPTION_DEFINITION.accept(VISITOR), "ForexOptionVanilla1");
    assertEquals(FX_OPTION_DEFINITION.accept(VISITOR, o), "ForexOptionVanilla2");
    assertEquals(FX_SINGLE_BARRIER_OPTION_DEFINITION.accept(VISITOR, o), "ForexOptionSingleBarrier2");
    assertEquals(FX_SINGLE_BARRIER_OPTION_DEFINITION.accept(VISITOR), "ForexOptionSingleBarrier1");
    assertEquals(NDF_DEFINITION.accept(VISITOR), "ForexNonDeliverableForwardDefinition1");
    assertEquals(NDF_DEFINITION.accept(VISITOR, o), "ForexNonDeliverableForwardDefinition2");
    assertEquals(NDO_DEFINITION.accept(VISITOR), "ForexNonDeliverableOptionDefinition1");
    assertEquals(NDO_DEFINITION.accept(VISITOR, o), "ForexNonDeliverableOptionDefinition2");
  }

  private static class MyVisitor<T, U> implements InstrumentDefinitionVisitor<T, String> {

    @Override
    public String visit(final InstrumentDefinition<?> definition, final T data) {
      return null;
    }

    @Override
    public String visit(final InstrumentDefinition<?> definition) {
      return null;
    }

    @Override
    public String visitForexDefinition(final ForexDefinition fx, final T data) {
      return "Forex2";
    }

    @Override
    public String visitForexDefinition(final ForexDefinition fx) {
      return "Forex1";
    }

    @Override
    public String visitForexSwapDefinition(final ForexSwapDefinition fx, final T data) {
      return "ForexSwap2";
    }

    @Override
    public String visitForexSwapDefinition(final ForexSwapDefinition fx) {
      return "ForexSwap1";
    }

    @Override
    public String visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx, final T data) {
      return "ForexOptionVanilla2";
    }

    @Override
    public String visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx) {
      return "ForexOptionVanilla1";
    }

    @Override
    public String visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx, final T data) {
      return "ForexOptionSingleBarrier2";
    }

    @Override
    public String visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx) {
      return "ForexOptionSingleBarrier1";
    }

    @Override
    public String visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf, T data) {
      return "ForexNonDeliverableForwardDefinition2";
    }

    @Override
    public String visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf) {
      return "ForexNonDeliverableForwardDefinition1";
    }

    @Override
    public String visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo, T data) {
      return "ForexNonDeliverableOptionDefinition2";
    }

    @Override
    public String visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo) {
      return "ForexNonDeliverableOptionDefinition1";
    }

    @Override
    public String visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond, T data) {
      return null;
    }

    @Override
    public String visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond) {
      return null;
    }

    @Override
    public String visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond, T data) {
      return null;
    }

    @Override
    public String visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond) {
      return null;
    }

    @Override
    public String visitBondFutureSecurityDefinition(BondFutureDefinition bond, T data) {
      return null;
    }

    @Override
    public String visitBondFutureSecurityDefinition(BondFutureDefinition bond) {
      return null;
    }

    @Override
    public String visitBondIborTransactionDefinition(BondIborTransactionDefinition bond, T data) {
      return null;
    }

    @Override
    public String visitBondIborTransactionDefinition(BondIborTransactionDefinition bond) {
      return null;
    }

    @Override
    public String visitBondIborSecurityDefinition(BondIborSecurityDefinition bond, T data) {
      return null;
    }

    @Override
    public String visitBondIborSecurityDefinition(BondIborSecurityDefinition bond) {
      return null;
    }

    @Override
    public String visitCashDefinition(CashDefinition cash, T data) {
      return null;
    }

    @Override
    public String visitCashDefinition(CashDefinition cash) {
      return null;
    }

    @Override
    public String visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra, T data) {
      return null;
    }

    @Override
    public String visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurityDefinition(InterestRateFutureDefinition future, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurityDefinition(InterestRateFutureDefinition future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurityDefinition(InterestRateFutureOptionMarginSecurityDefinition future, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurityDefinition(InterestRateFutureOptionMarginSecurityDefinition future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginTransactionDefinition future, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginTransactionDefinition future) {
      return null;
    }

    @Override
    public String visitPaymentFixed(PaymentFixedDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitPaymentFixed(PaymentFixedDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponFixed(CouponFixedDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponFixed(CouponFixedDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponFloating(CouponFloatingDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponFloating(CouponFloatingDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponIbor(CouponIborDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIbor(CouponIborDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponIborSpread(CouponIborDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIborSpread(CouponIborDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponOISSimplified(CouponOISSimplifiedDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponOISSimplified(CouponOISSimplifiedDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponOIS(CouponOISDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponOIS(CouponOISDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponCMS(CouponCMSDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponCMS(CouponCMSDefinition payment) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(CapFloorCMSDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(CapFloorCMSDefinition payment) {
      return null;
    }

    @Override
    public String visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity, T data) {
      return null;
    }

    @Override
    public String visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity) {
      return null;
    }

    @Override
    public String visitSwapDefinition(SwapDefinition swap, T data) {
      return null;
    }

    @Override
    public String visitSwapDefinition(SwapDefinition swap) {
      return null;
    }

    @Override
    public String visitSwapFixedIborDefinition(SwapFixedIborDefinition swap, T data) {
      return null;
    }

    @Override
    public String visitSwapFixedIborDefinition(SwapFixedIborDefinition swap) {
      return null;
    }

    @Override
    public String visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap, T data) {
      return null;
    }

    @Override
    public String visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap) {
      return null;
    }

    @Override
    public String visitSwapIborIborDefinition(SwapIborIborDefinition swap, T data) {
      return null;
    }

    @Override
    public String visitSwapIborIborDefinition(SwapIborIborDefinition swap) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIborDefinition(SwaptionCashFixedIborDefinition swaption, T data) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIborDefinition(SwaptionCashFixedIborDefinition swaption) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIborDefinition(SwaptionPhysicalFixedIborDefinition swaption, T data) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIborDefinition(SwaptionPhysicalFixedIborDefinition swaption) {
      return null;
    }

    @Override
    public String visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption, T data) {
      return null;
    }

    @Override
    public String visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond, T data) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond, T data) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond) {
      return null;
    }

  }

}
