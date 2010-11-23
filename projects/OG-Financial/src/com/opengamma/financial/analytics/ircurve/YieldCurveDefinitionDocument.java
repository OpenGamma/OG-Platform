/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractDocument;

/**
 * 
 */
public class YieldCurveDefinitionDocument extends AbstractDocument {

  private UniqueIdentifier _uniqueIdentifier;

  private YieldCurveDefinition _yieldCurveDefinition;

  protected YieldCurveDefinitionDocument(final UniqueIdentifier uniqueIdentifier, final YieldCurveDefinition yieldCurveDefinition) {
    setUniqueId(uniqueIdentifier);
    setYieldCurveDefinition(yieldCurveDefinition);
  }

  public void setYieldCurveDefinition(final YieldCurveDefinition yieldcurveDefinition) {
    _yieldCurveDefinition = yieldcurveDefinition;
  }

  public YieldCurveDefinition getYieldCurveDefinition() {
    return _yieldCurveDefinition;
  }

  @Override
  public UniqueIdentifier getUniqueId() {
    return _uniqueIdentifier;
  }

  @Override
  public void setUniqueId(UniqueIdentifier uniqueIdentifier) {
    _uniqueIdentifier = uniqueIdentifier;
  }

}
