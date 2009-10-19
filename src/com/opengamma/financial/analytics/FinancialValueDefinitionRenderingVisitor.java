/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.financial.analytics.DiscountCurveValueDefinition;
import com.opengamma.financial.analytics.GreeksResultValueDefinition;
import com.opengamma.engine.viewer.ValueDefinitionRenderingVisitor;
import com.opengamma.financial.analytics.VolatilitySurfaceValueDefinition;

/**
 * 
 *
 * @author jim
 */
public class FinancialValueDefinitionRenderingVisitor extends
    ValueDefinitionRenderingVisitor implements
    FinancialValueDefinitionVisitor<String> {

  @Override
  public String visitDiscountCurveValueDefinition(
      DiscountCurveValueDefinition definition) {
    return "Build discount curve for "+definition.getValue("CURRENCY");
  }

  @Override
  public String visitGreeksResultValueDefinition(
      GreeksResultValueDefinition definition) {
    return "Calculate Greeks for "+definition.getValue("SECURITY");
  }

  @Override
  public String visitVolatilitySurfaceValueDefinition(VolatilitySurfaceValueDefinition definition) {
    return "Build volatility curve for "+definition.getValue("SECURITY");
  }
}
