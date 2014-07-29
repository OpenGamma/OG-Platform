/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.credit;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.AbstractCurveSpecification;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.InterpolatedCurveSpecification;

/**
 * Builds curve specifications from a definition, curve date and valuation time.
 */
public interface CurveSpecificationBuilder {

  /**
   * Builds either an {@link InterpolatedCurveSpecification} or {@link CurveSpecification}.
   * @param valuationTime The valuation time, not null
   * @param curveDate The curve date, not null
   * @param curveDefinition The curve definition, not null
   * @return The curve specification
   * @throws UnsupportedOperationException If the curve definition type is not {@link InterpolatedCurveSpecification} or
   * {@link CurveSpecification}
   * @deprecated This method is maintained for backwards compatibility. Use {@link #buildSpecification}, which should handle all
   * {@link AbstractCurveSpecification} types.
   */
  @Deprecated
  CurveSpecification buildCurve(Instant valuationTime, LocalDate curveDate, CurveDefinition curveDefinition);

  /**
   * Builds a curve specification from a curve definition, curve date and valuation time.
   * @param valuationTime The valuation time, not null
   * @param curveDate The curve date, not null
   * @param curveDefinition The curve definition, not null
   * @return The curve specification
   */
  AbstractCurveSpecification buildSpecification(Instant valuationTime, LocalDate curveDate, AbstractCurveDefinition curveDefinition);

}
