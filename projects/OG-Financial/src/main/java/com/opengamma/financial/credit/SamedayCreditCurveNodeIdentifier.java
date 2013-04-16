/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.credit;

import org.threeten.bp.Period;

import com.opengamma.id.ExternalScheme;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;

/**
 * Stores the date required to uniquely identify a sameday credit curve - the ticker, red code,
 * currency, tenor, seniority and restructuring clause
 */
public final class SamedayCreditCurveNodeIdentifier extends CreditCurveNodeIdentifier {

  /**
   * The scheme to use in external identifiers
   */
  public static final ExternalScheme CREDIT_CURVE_SCHEME = ExternalScheme.of("SAMEDAY_CREDIT_CURVE_NODE");

  /**
   * Create a new identifier from constituent parts.
   *
   * @param ticker the ticker
   * @param redCode the red code
   * @param currency the currency
   * @param term the term
   * @param seniority the seniority
   * @param restructuringClause the restruturing clause
   * @return a new identifier
   */
  public static CreditCurveNodeIdentifier of(final String ticker,
                                             final String redCode,
                                             final Currency currency,
                                             final Period term,
                                             final String seniority,
                                             final String restructuringClause) {

     return new SamedayCreditCurveNodeIdentifier(ticker, redCode, currency, term, seniority, restructuringClause);
  }


  /**
   * Create a new identifier from an existing {@link CreditCurveIdentifier}.
   *
   * @param id the existing CreditCurveIdentifier
   * @param ticker the ticker
   * @param term the term
   * @return a new identifier
   */
  public static CreditCurveNodeIdentifier of(final CreditCurveIdentifier id, final String ticker, final Period term) {
    return of(ticker, id.getRedCode(), id.getCurrency(), term, id.getSeniority(), id.getRestructuringClause());
  }

  /**
   * Constructs a new instance
   *
   * @param redCode the RED code, not null (underscores will be replaced with dashes)
   * @param currency the currency, not null
   * @param term the term, not null
   * @param seniority the seniority, not null
   * @param restructuringClause the restructuring clause, not null
   */
  private SamedayCreditCurveNodeIdentifier(final String ticker,
                                           final String redCode,
                                           final Currency currency,
                                           final Period term,
                                           final String seniority,
                                           final String restructuringClause) {
    super(CREDIT_CURVE_SCHEME, ticker, redCode, seniority, currency, term, restructuringClause);
  }

}
