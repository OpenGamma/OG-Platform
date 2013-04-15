/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.credit;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveSpecification;

/**
 * 
 */
public interface CurveSpecificationBuilder {

  CurveSpecification buildCurve(Instant valuationTime, LocalDate curveDate, CurveDefinition curveDefinition);
}
