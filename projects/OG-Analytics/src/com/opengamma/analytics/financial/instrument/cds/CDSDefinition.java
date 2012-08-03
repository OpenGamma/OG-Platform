/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cds;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.credit.cds.CDSDerivative;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.util.time.TimeCalculator;

/**
 * InstrumentDefinition implementation for CDS securities
 * @author Martin Traverse (Riskcare)
 * @see CDSSecurity
 * @see CDSDerivative
 * @see InstrumentDefinition
 */
public class CDSDefinition implements InstrumentDefinition<CDSDerivative> {
  
  private final AnnuityCouponFixedDefinition _premium;
  private final AnnuityPaymentFixedDefinition _payout;
  
  private final ZonedDateTime _protectionStartDate;
  private final ZonedDateTime _maturity;
  
  private final double _notional;
  private final double _spread;
  private final double _recoveryRate;
  
  public CDSDefinition(AnnuityCouponFixedDefinition premium, AnnuityPaymentFixedDefinition payout,
    ZonedDateTime protectionStartDate, ZonedDateTime maturity,
    double notional, double spread, double recoveryRate) {
    _premium = premium;
    _payout = payout;
    _protectionStartDate = protectionStartDate;
    _maturity = maturity;
    _notional = notional;
    _spread = spread;
    _recoveryRate = recoveryRate;
  }

  /**
   * @param pricingDate Pricing point to offset t values
   * @param yieldCurveNames 0 = cdsCcyCurve, 1 = bondCcyCurve, 2 = spreadCurve
   * @return CDS InstrumentDerivative object ready for pricing
   */
  @Override
  public CDSDerivative toDerivative(ZonedDateTime pricingDate, String... yieldCurveNames) {
    
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length == 3, "precisely three curves required (CDS CCY, Bond CCY and credit spread)");
    
    final String cdsCcyCurveName = yieldCurveNames[0];
    final String bondCcyCurveName = yieldCurveNames[1];
    final String spreadCurveName = yieldCurveNames[2];
    
    // TODO: Time conversions all use TimeCalculator, which uses convention Actual / Actual ISDA
    // TODO: This is also embedded in the toDerivative methods for the annuity classes
    return new CDSDerivative(
      cdsCcyCurveName, bondCcyCurveName, spreadCurveName,
      _premium.toDerivative(pricingDate, cdsCcyCurveName),
      _payout.toDerivative(pricingDate, cdsCcyCurveName),
      TimeCalculator.getTimeBetween(pricingDate, _protectionStartDate),
      TimeCalculator.getTimeBetween(pricingDate, _maturity),
      _notional,
      _spread,
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
