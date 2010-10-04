/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;

/**
 * @param <S> Type of additional data needed for the calculation (this can be a null object if not needed) 
 *  @param <T> Type of visitor
 */
public interface InterestRateDerivativeVisitor<S, T> {

  T getValue(InterestRateDerivative ird, S data);

  T visitCash(Cash cash, S data);

  T visitForwardRateAgreement(ForwardRateAgreement fra, S data);

  T visitInterestRateFuture(InterestRateFuture future, S data);

  T visitSwap(final Swap<?, ?> swap, S data);

  T visitFixedFloatSwap(final FixedFloatSwap swap, S data);

  T visitTenorSwap(final TenorSwap swap, S data);

  T visitFloatingRateNote(final FloatingRateNote frn, S data);

  T visitBond(final Bond bond, S data);

  // T visitConstantCouponAnnuity(final FixedCouponAnnuity annuity, S data);
  //
  // T visitVariableAnnuity(final ForwardLiborAnnuity annuity, S data);

  T visitFixedPayment(FixedPayment payment, S data);

  T visitForwardLiborPayment(ForwardLiborPayment payment, S data);

  T visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, S data);
}
