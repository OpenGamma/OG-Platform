/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;

/**
 * Represent a CDS premium as understood by the ISDA standard model.
 * 
 * A fixed coupon annuity using {@link ISDACDSCoupon} to describe payments.
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see AnnuityCouponFixed
 * @deprecated Use classes from isdastandardmodel
 */
@Deprecated
public class ISDACDSPremium extends AnnuityCouponFixed {

  public ISDACDSPremium(final ISDACDSCoupon[] payments) {
    super(payments);
  }

  @Override
  public ISDACDSCoupon[] getPayments() {
    return (ISDACDSCoupon[]) super.getPayments();
  }

}
