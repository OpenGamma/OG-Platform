/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;

/**
 * Represent a CDS premium as understood by the ISDA standard model
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class ISDACDSPremium extends AnnuityCouponFixed {

  public ISDACDSPremium(ISDACDSCoupon[] payments) {
    super(payments);
  }
  
  @Override
  public ISDACDSCoupon[] getPayments() {
    return (ISDACDSCoupon[]) super.getPayments();
  }

}
