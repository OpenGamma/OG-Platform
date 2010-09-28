/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;


/**
 * 
 */
public interface Payment {

  double getPaymentTime();

  <S, T> T accept(PaymentVisitor<S, T> visitor, S data);

}
