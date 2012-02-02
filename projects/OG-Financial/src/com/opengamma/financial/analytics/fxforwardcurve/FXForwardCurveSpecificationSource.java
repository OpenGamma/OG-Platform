/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import javax.time.Instant;

/**
 * 
 */
public interface FXForwardCurveSpecificationSource {

  FXForwardCurveSpecification getSpecification(String name);

  FXForwardCurveSpecification getSpecification(String name, Instant version);
}
