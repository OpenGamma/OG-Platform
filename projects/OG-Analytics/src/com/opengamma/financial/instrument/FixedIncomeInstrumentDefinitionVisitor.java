/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.instrument.bond.BondForwardDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.fra.FRADefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.instrument.swap.FixedFloatSwapDefinition;
import com.opengamma.financial.instrument.swap.FixedSwapLegDefinition;
import com.opengamma.financial.instrument.swap.FloatingSwapLegDefinition;
import com.opengamma.financial.instrument.swap.TenorSwapDefinition;

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

  U visitFixedSwapLegDefinition(FixedSwapLegDefinition fixedSwapLeg, T data);

  U visitFixedSwapLegDefinition(FixedSwapLegDefinition fixedSwapLeg);

  U visitFloatingSwapLegDefinition(FloatingSwapLegDefinition floatingSwapLeg, T data);

  U visitFloatingSwapLegDefinition(FloatingSwapLegDefinition floatingSwapLeg);

  U visitFRADefinition(FRADefinition fra, T data);

  U visitFRADefinition(FRADefinition fra);

  U visitFixedFloatSwapDefinition(FixedFloatSwapDefinition swap, T data);

  U visitFixedFloatSwapDefinition(FixedFloatSwapDefinition swap);

  U visitTenorSwapDefinition(TenorSwapDefinition swap, T data);

  U visitTenorSwapDefinition(TenorSwapDefinition swap);

  U visitPaymentFixed(PaymentFixedDefinition payment, T data);

  U visitPaymentFixed(PaymentFixedDefinition payment);

  U visitCouponFixed(CouponFixedDefinition payment, T data);

  U visitCouponFixed(CouponFixedDefinition payment);

  U visitCouponIbor(CouponIborDefinition payment, T data);

  U visitCouponIbor(CouponIborDefinition payment);
}
