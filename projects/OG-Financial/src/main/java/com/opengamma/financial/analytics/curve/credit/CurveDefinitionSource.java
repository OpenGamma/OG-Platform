/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.credit;

import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.id.VersionCorrection;

/**
 * 
 */
public interface CurveDefinitionSource {

  CurveDefinition getCurveDefinition(String name);

  CurveDefinition getCurveDefinition(String name, VersionCorrection versionCorrection);
}
