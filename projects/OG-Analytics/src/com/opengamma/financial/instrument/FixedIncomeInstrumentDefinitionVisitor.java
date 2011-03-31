/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.instrument.bond.BondForwardDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.fra.FRADefinition;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.financial.instrument.swap.SwapIborIborDefinition;

/**
 * 
 * @param <T> Type of the data 
 * @param <U> Type of the result
 */
public interface FixedIncomeInstrumentDefinitionVisitor<T, U> {

  U visit(FixedIncomeInstrumentDefinition<?> definition, T data);

  U visit(FixedIncomeInstrumentDefinition<?> definition);

  U visitBondDefinition(BondDefinition bond, T data);

  U visitBondDefinition(BondDefinition bond);

  U visitBondForwardDefinition(BondForwardDefinition bondForward, T data);

  U visitBondForwardDefinition(BondForwardDefinition bondForward);

  U visitCashDefinition(CashDefinition cash, T data);

  U visitCashDefinition(CashDefinition cash);

  //  @Deprecated
  //  U visitFixedSwapLegDefinition(FixedSwapLegDefinition fixedSwapLeg, T data);
  //
  //  @Deprecated
  //  U visitFixedSwapLegDefinition(FixedSwapLegDefinition fixedSwapLeg);
  //
  //  @Deprecated
  //  U visitFloatingSwapLegDefinition(FloatingSwapLegDefinition floatingSwapLeg, T data);
  //
  //  @Deprecated
  //  U visitFloatingSwapLegDefinition(FloatingSwapLegDefinition floatingSwapLeg);

  U visitFRADefinition(FRADefinition fra, T data);

  U visitFRADefinition(FRADefinition fra);

  //  @Deprecated
  //  U visitFixedFloatSwapDefinition(FixedFloatSwapDefinition swap, T data);
  //
  //  @Deprecated
  //  U visitFixedFloatSwapDefinition(FixedFloatSwapDefinition swap);
  //
  //  @Deprecated
  //  U visitTenorSwapDefinition(TenorSwapDefinition swap, T data);
  //
  //  @Deprecated
  //  U visitTenorSwapDefinition(TenorSwapDefinition swap);

  U visitPaymentFixed(PaymentFixedDefinition payment, T data);

  U visitPaymentFixed(PaymentFixedDefinition payment);

  U visitCouponFixed(CouponFixedDefinition payment, T data);

  U visitCouponFixed(CouponFixedDefinition payment);

  U visitCouponIbor(CouponIborDefinition payment, T data);

  U visitCouponIbor(CouponIborDefinition payment);

  U visitCouponIborSpread(CouponIborDefinition payment, T data);

  U visitCouponIborSpread(CouponIborDefinition payment);

  U visitCouponCMS(CouponCMSDefinition payment, T data);

  U visitCouponCMS(CouponCMSDefinition payment);

  U visitAnnuityCouponCMSDefinition(AnnuityDefinition<CouponCMSDefinition> annuity, T data);

  U visitAnnuityCouponCMSDefinition(AnnuityDefinition<CouponCMSDefinition> annuity);

  U visitAnnuityCouponFixedDefinition(AnnuityDefinition<CouponFixedDefinition> annuity, T data);

  U visitAnnuityCouponFixedDefinition(AnnuityDefinition<CouponFixedDefinition> annuity);

  U visitAnnuityCouponIborDefinition(AnnuityDefinition<CouponIborDefinition> annuity, T data);

  U visitAnnuityCouponIborDefinition(AnnuityDefinition<CouponIborDefinition> annuity);

  U visitAnnuityCouponIborSpreadDefinition(AnnuityDefinition<CouponIborSpreadDefinition> annuity, T data);

  U visitAnnuityCouponIborSpreadDefinition(AnnuityDefinition<CouponIborSpreadDefinition> annuity);

  U visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity, T data);

  U visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity);

  U visitSwapDefinition(SwapDefinition<? extends PaymentDefinition, ? extends PaymentDefinition> swap, T data);

  U visitSwapDefinition(SwapDefinition<? extends PaymentDefinition, ? extends PaymentDefinition> swap);

  U visitSwapFixedIborDefinition(SwapFixedIborDefinition swap, T data);

  U visitSwapFixedIborDefinition(SwapFixedIborDefinition swap);

  U visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap, T data);

  U visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap);

  U visitSwapIborIborDefinition(SwapIborIborDefinition swap, T data);

  U visitSwapIborIborDefinition(SwapIborIborDefinition swap);
}
