/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;

/**
 * @param <T> Type of visitor
 */
public interface InterestRateDerivativeVisitor<T> {

  T getValue(InterestRateDerivative ird, YieldCurveBundle curves);

  T visitCash(Cash cash, YieldCurveBundle curves);

  T visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves);

  T visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves);

  T visitSwap(final Swap swap, YieldCurveBundle curves);

  T visitFixedFloatSwap(final FixedFloatSwap swap, YieldCurveBundle curves);

  T visitBasisSwap(final BasisSwap swap, YieldCurveBundle curves);

  T visitBond(final Bond bond, YieldCurveBundle curves);

  T visitFixedAnnuity(final FixedAnnuity annuity, YieldCurveBundle curves);

  T visitConstantCouponAnnuity(final ConstantCouponAnnuity annuity, YieldCurveBundle curves);

  T visitVariableAnnuity(final VariableAnnuity annuity, YieldCurveBundle curves);
}
