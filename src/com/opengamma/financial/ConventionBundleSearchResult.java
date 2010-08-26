/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

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
