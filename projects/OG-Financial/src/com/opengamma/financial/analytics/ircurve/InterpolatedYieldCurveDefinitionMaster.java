/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.master.AbstractMaster;

/**
 * A master for yield curve definitions.
 */
public interface InterpolatedYieldCurveDefinitionMaster extends AbstractMaster<YieldCurveDefinitionDocument> {

  /**
   * Stores the document in the master, updating if it already exists, adding if not.
   * 
   * @param document  the document to add or update, not null
   * @return the updated document details, not null
   */
  YieldCurveDefinitionDocument addOrUpdate(YieldCurveDefinitionDocument document);

}
