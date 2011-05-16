/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;

/**
 * A wrapper class for a AnnuityDefinition containing CMS coupon Definition.
 */
public class AnnuityCouponCMSDefinition extends AnnuityDefinition<CouponCMSDefinition> {

  /**
   * Constructor from a list of CMS coupons.
   * @param payments The CMS coupons.
   */
  public AnnuityCouponCMSDefinition(final CouponCMSDefinition[] payments) {
    super(payments);
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitAnnuityCouponCMSDefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitAnnuityCouponCMSDefinition(this);
  }

}
