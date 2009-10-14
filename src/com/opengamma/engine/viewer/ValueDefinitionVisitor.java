/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import com.opengamma.engine.analytics.AnalyticValueDefinitionImpl;
import com.opengamma.engine.analytics.DiscountCurveValueDefinition;
import com.opengamma.engine.analytics.GreeksResultValueDefinition;
import com.opengamma.engine.analytics.ResolveSecurityKeyToMarketDataFieldDefinition;
import com.opengamma.engine.analytics.ResolveSecurityKeyToMarketDataHeaderDefinition;
import com.opengamma.engine.analytics.ResolveSecurityKeyToSecurityDefinition;
import com.opengamma.engine.analytics.VolatilitySurfaceValueDefinition;

/**
 * 
 *
 * @author jim
 */
public interface ValueDefinitionVisitor<T> {
  public T visitDiscountCurveValueDefinition(DiscountCurveValueDefinition definition);
  public T visitGreeksResultValueDefinition(GreeksResultValueDefinition definition);
  public T visitResolveSecurityKeyToMarketDataFieldDefinition(ResolveSecurityKeyToMarketDataFieldDefinition definition);
  public T visitResolveSecurityKeyToMarketDataHeaderDefinition(ResolveSecurityKeyToMarketDataHeaderDefinition definition);
  public T visitResolveSecurityKeyToSecurityDescriptionDefinition(ResolveSecurityKeyToSecurityDefinition definition);
  public T visitVolatilitySurfaceValueDefinition(VolatilitySurfaceValueDefinition definition);
  public T visitAnalyticValueDefinitionImpl(AnalyticValueDefinitionImpl<?> definition);
}
