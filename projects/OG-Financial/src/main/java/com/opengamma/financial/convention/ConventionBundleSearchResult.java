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
public class ConventionBundleSearchResult {
  private Collection<ConventionBundleDocument> _results;
  public ConventionBundleSearchResult(ConventionBundleDocument singleResult) {
    _results = Collections.singletonList(singleResult);
  }
  public ConventionBundleSearchResult(Collection<ConventionBundleDocument> results) {
    _results = results;
  }
  
  public Collection<ConventionBundleDocument> getResults() {
    return _results;
  }
}
