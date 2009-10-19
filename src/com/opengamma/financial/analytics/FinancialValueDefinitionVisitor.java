/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.financial.analytics.DiscountCurveValueDefinition;
import com.opengamma.financial.analytics.GreeksResultValueDefinition;
import com.opengamma.engine.viewer.ValueDefinitionVisitor;
import com.opengamma.financial.analytics.VolatilitySurfaceValueDefinition;

/**
 * 
 *
 * @author jim
 */
public interface FinancialValueDefinitionVisitor<T> extends
    ValueDefinitionVisitor<T> {
  public T visitDiscountCurveValueDefinition(DiscountCurveValueDefinition definition);
  public T visitGreeksResultValueDefinition(GreeksResultValueDefinition definition);
  public T visitVolatilitySurfaceValueDefinition(VolatilitySurfaceValueDefinition definition);
}
