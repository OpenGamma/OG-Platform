/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.Paging;

/**
 * An immutable list of portfolio summaries with paging.
 */
public final class SearchPortfoliosResult {

  /**
   * The paging information.
   */
  private final Paging _paging;
  /**
   * The paged list of summaries.
   */
  private final List<PortfolioSummary> _portfolios;

  /**
   * Creates an instance.
   * @param paging  the paging information, not null
   * @param portfolios  the portfolios, not null
   */
  public SearchPortfoliosResult(final Paging paging, final List<PortfolioSummary> portfolios) {
    ArgumentChecker.notNull(paging, "paging");
    ArgumentChecker.noNulls(portfolios, "portfolios");
    _paging = paging;
    _portfolios = ImmutableList.copyOf(portfolios);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the paging information.
   * @return the paging information, not null
   */
  public Paging getPaging() {
    return _paging;
  }

  /**
   * Gets the list of portfolios.
   * @return the list of portfolios, unmodifiable, not null
   */
  public List<PortfolioSummary> getPortfolioSummaries() {
    return _portfolios;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[portfolios=" + _portfolios.size() + ", paging=" + _paging + "]";
  }

}
