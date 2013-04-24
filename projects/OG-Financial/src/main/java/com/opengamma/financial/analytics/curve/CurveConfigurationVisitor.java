/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

/**
 * 
 */
public interface CurveConfigurationVisitor<RETURN_TYPE> {

  RETURN_TYPE visitDiscountingCurveConfiguration(DiscountingCurveConfiguration configuration);

  RETURN_TYPE visitOvernightCurveConfiguration(OvernightCurveConfiguration configuration);

  RETURN_TYPE visitForwardIborCurveConfiguration(ForwardIborCurveConfiguration configuration);
}
