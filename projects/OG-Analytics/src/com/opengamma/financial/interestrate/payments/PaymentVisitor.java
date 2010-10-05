/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

/**
 *  @param <S> Type of additional data needed for the calculation (this can be a null object if not needed) 
 *  @param <T> Type of visitor 
 */
public interface PaymentVisitor<S, T> {

  T calculate(Payment p, S data);

  T visitFixedPayment(FixedPayment payment, S data);

  T visitForwardLiborPayment(ForwardLiborPayment payment, S data);

}
