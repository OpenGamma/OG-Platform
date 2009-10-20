/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;


import com.opengamma.engine.analytics.AnalyticValueDefinitionImpl;
import com.opengamma.engine.viewer.VisitableValueDefinition;
import com.opengamma.financial.Currency;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class DiscountCurveValueDefinition extends
    AnalyticValueDefinitionImpl<DiscountCurve> implements VisitableValueDefinition {
  @SuppressWarnings("unchecked")
  public DiscountCurveValueDefinition(Currency currency) {
     super(new KeyValuePair<String, Object>("TYPE", "DISCOUNT_CURVE"),
           new KeyValuePair<String, Object>("CURRENCY", currency.getISOCode()));
  }

  @SuppressWarnings("unchecked")
  public DiscountCurveValueDefinition() {
     super(new KeyValuePair<String, Object>("TYPE", "DISCOUNT_CURVE"));
  }
  
  public <E> E accept(FinancialValueDefinitionVisitor<E> visitor) {
    return visitor.visitDiscountCurveValueDefinition(this);
  }
}
