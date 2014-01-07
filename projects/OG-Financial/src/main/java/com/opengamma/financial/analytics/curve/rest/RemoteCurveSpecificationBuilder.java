/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.rest;

import java.net.URI;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.AbstractCurveSpecification;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides access to a remote {@link CurveSpecificationBuilder}.
 */
public class RemoteCurveSpecificationBuilder extends AbstractRemoteClient implements CurveSpecificationBuilder {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteCurveSpecificationBuilder(final URI baseUri) {
    super(baseUri);
  }

  @Override
  public CurveSpecification buildCurve(final Instant valuationTime, final LocalDate curveDate, final CurveDefinition curveDefinition) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(curveDate, "curveDate");
    ArgumentChecker.notNull(curveDefinition, "curveDefinition");

    final URI uri = DataCurveSpecificationBuilderResource.uriBuildCurve(getBaseUri(), valuationTime, curveDate);
    return accessRemote(uri).post(CurveSpecification.class, curveDefinition);
  }

  @Override
  public AbstractCurveSpecification buildSpecification(final Instant valuationTime, final LocalDate curveDate, final AbstractCurveDefinition curveDefinition) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(curveDate, "curveDate");
    ArgumentChecker.notNull(curveDefinition, "curveDefinition");

    final URI uri = DataCurveSpecificationBuilderResource.uriBuildCurve(getBaseUri(), valuationTime, curveDate);
    return accessRemote(uri).post(AbstractCurveSpecification.class, curveDefinition);
  }

}
