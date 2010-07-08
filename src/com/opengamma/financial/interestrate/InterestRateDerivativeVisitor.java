/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.Swap;

/**
 * @param <T> Type of visitor
 */
public interface InterestRateDerivativeVisitor<T> {

  T visitCash(Cash cash);

  T visitForwardRateAgreement(ForwardRateAgreement fra);

  T visitInterestRateFuture(InterestRateFuture future);

  T visitLibor(Libor libor);

  T visitSwap(final Swap swap);

}
