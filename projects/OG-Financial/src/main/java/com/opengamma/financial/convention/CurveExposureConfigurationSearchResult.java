/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Collection;
import java.util.Collections;

/**
 * A collections of curve exposure configuration documents from a search.
 */
public class CurveExposureConfigurationSearchResult {
  private final Collection<CurveExposureConfigurationDocument> _results;

  /**
   *
   */
  public CurveExposureConfigurationSearchResult(final CurveExposureConfigurationDocument singleResult) {
    _results = Collections.singletonList(singleResult);
  }

  public CurveExposureConfigurationSearchResult(final Collection<CurveExposureConfigurationDocument> results) {
    _results = results;
  }

  public Collection<CurveExposureConfigurationDocument> getResults() {
    return _results;
  }
}
