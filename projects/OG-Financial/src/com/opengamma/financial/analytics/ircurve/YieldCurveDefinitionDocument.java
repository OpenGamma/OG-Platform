/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDocument;

/**
 * 
 */
public class YieldCurveDefinitionDocument extends AbstractDocument {

  private UniqueId _uniqueId;

  private YieldCurveDefinition _yieldCurveDefinition;

  public YieldCurveDefinitionDocument(final UniqueId uniqueId, final YieldCurveDefinition yieldCurveDefinition) {
    setUniqueId(uniqueId);
    setYieldCurveDefinition(yieldCurveDefinition);
  }

  public YieldCurveDefinitionDocument() {
  }

  public YieldCurveDefinitionDocument(final YieldCurveDefinition yieldCurveDefinition) {
    _yieldCurveDefinition = yieldCurveDefinition;
  }

  public void setYieldCurveDefinition(final YieldCurveDefinition yieldcurveDefinition) {
    _yieldCurveDefinition = yieldcurveDefinition;
  }

  public YieldCurveDefinition getYieldCurveDefinition() {
    return _yieldCurveDefinition;
  }

  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  @Override
  public void setUniqueId(UniqueId uniqueId) {
    _uniqueId = uniqueId;
  }

}
