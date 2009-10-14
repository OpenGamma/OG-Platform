/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;


import com.opengamma.engine.viewer.ValueDefinitionVisitor;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.securities.Currency;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class DiscountCurveValueDefinition extends
    AnalyticValueDefinitionImpl<DiscountCurve> {
  @SuppressWarnings("unchecked")
  public DiscountCurveValueDefinition(Currency currency) {
     super(new KeyValuePair<String, Object>("TYPE", "DISCOUNT_CURVE"),
           new KeyValuePair<String, Object>("CURRENCY", currency.getISOCode()));
  }

  @SuppressWarnings("unchecked")
  public DiscountCurveValueDefinition() {
     super(new KeyValuePair<String, Object>("TYPE", "DISCOUNT_CURVE"));
  }
  
  @Override
  public <E> E accept(ValueDefinitionVisitor<E> visitor) {
    return visitor.visitDiscountCurveValueDefinition(this);
  }
}
