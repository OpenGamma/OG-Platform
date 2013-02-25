/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Collection;
import java.util.Collections;

/**
 * A collection of result documents from a search
 */
public class ConventionSearchResult {
  private final Collection<ConventionDocument> _results;
  public ConventionSearchResult(final ConventionDocument singleResult) {
    _results = Collections.singletonList(singleResult);
  }
  public ConventionSearchResult(final Collection<ConventionDocument> results) {
    _results = results;
  }

  public Collection<ConventionDocument> getResults() {
    return _results;
  }
}
