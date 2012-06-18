/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ViewportResults {

  private final List<List<Object>> _allResults;

  // TODO does this need to contain the viewport spec?

  /* package */ ViewportResults(List<List<Object>> allResults) {
    ArgumentChecker.notNull(allResults, "allResults");
    _allResults = allResults;
  }

  public List<List<Object>> getResults() {
    return _allResults;
  }

  @Override
  public String toString() {
    return "ViewportResults [" +
        "_allResults=" + _allResults +
        "]";
  }
}
