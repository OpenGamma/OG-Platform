/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.ObjectUtils;

/**
 * A collection of result documents containing conventions from a search.
 */
public class ConventionSearchResult {
  /** The collection of results */
  private final Collection<ConventionDocument> _results;

  /**
   * @param singleResult A single result
   */
  public ConventionSearchResult(final ConventionDocument singleResult) {
    _results = Collections.singletonList(singleResult);
  }

  /**
   * @param results A collection of results
   */
  public ConventionSearchResult(final Collection<ConventionDocument> results) {
    _results = results;
  }

  /**
   * Gets the collection of result documents.
   * @return The result documents
   */
  public Collection<ConventionDocument> getResults() {
    return _results;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_results == null) ? 0 : _results.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConventionSearchResult)) {
      return false;
    }
    final ConventionSearchResult other = (ConventionSearchResult) obj;
    return ObjectUtils.equals(_results, other._results);
  }

}
