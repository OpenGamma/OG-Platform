/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

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

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String PAGING_FIELD_NAME = "paging";
  /** Field name. */
  private static final String PORTFOLIO_FIELD_NAME = "portfolio";

  /**
   * Serializes to a Fudge message.
   * @param context  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    MutableFudgeFieldContainer msg = context.newMessage();
    msg.add(PAGING_FIELD_NAME, _paging.toFudgeMsg(context));
    for (PortfolioSummary summary : _portfolios) {
      msg.add(PORTFOLIO_FIELD_NAME, summary.toFudgeMsg(context));
    }
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static SearchPortfoliosResult fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    Paging paging = Paging.fromFudgeMsg(context, msg.getMessage(PAGING_FIELD_NAME));
    List<PortfolioSummary> summaries = new ArrayList<PortfolioSummary>();
    for (FudgeField field : msg.getAllByName(PORTFOLIO_FIELD_NAME)) {
      summaries.add(PortfolioSummary.fromFudgeMsg(context, (FudgeFieldContainer) field.getValue()));
    }
    return new SearchPortfoliosResult(paging, summaries);
  }

}
