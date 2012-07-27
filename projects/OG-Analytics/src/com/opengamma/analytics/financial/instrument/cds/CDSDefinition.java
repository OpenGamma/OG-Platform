/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cds;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.cds.CDSDerivative;

/**
 * InstrumentDefinition implementation for CDS securities
 * @author Martin Traverse (Riskcare)
 * @see CDSSecurity
 * @see CDSDerivative
 * @see InstrumentDefinition
 */
public class CDSDefinition implements InstrumentDefinition<CDSDerivative> {
  
  private AnnuityCouponFixedDefinition _premium;
  private AnnuityPaymentFixedDefinition _payout;
  
  private double _recoveryRate;
  
  public CDSDefinition(AnnuityCouponFixedDefinition premium, AnnuityPaymentFixedDefinition payout, double recoveryRate) {
    _premium = premium;
    _payout = payout;
    _recoveryRate = recoveryRate;
  }
  
  
  @Override
  public CDSDerivative toDerivative(ZonedDateTime pricingDate, String... yieldCurveNames) {
    return new CDSDerivative(
      _premium.toDerivative(pricingDate, yieldCurveNames),
      _payout.toDerivative(pricingDate, yieldCurveNames),
      _recoveryRate
    );
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitCDSDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCDSDefinition(this);
  }

}
