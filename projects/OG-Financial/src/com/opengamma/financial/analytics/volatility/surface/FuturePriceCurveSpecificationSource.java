/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.Instant;

/**
 * 
 */
public interface FuturePriceCurveSpecificationSource {

  FuturePriceCurveSpecification getSpecification(String name, String instrumentType);

  FuturePriceCurveSpecification getSpecification(String name, String instrumentType, Instant version);
}
