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
public class ReferenceRateSearchResult {
  private Collection<ReferenceRateDocument> _results;
  public ReferenceRateSearchResult(ReferenceRateDocument singleResult) {
    _results = Collections.singletonList(singleResult);
  }
  public ReferenceRateSearchResult(Collection<ReferenceRateDocument> results) {
    _results = results;
  }
  
  public Collection<ReferenceRateDocument> getResults() {
    return _results;
  }
}
