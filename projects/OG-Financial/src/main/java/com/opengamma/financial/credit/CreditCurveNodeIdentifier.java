/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.credit;

import org.threeten.bp.Period;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;

/**
 * Stores the date required to uniquely identify a credit curve - the ticker, red code,
 * currency, tenor, seniority and restructuring clause
 */
public final class CreditCurveNodeIdentifier implements ExternalIdentifiable {

  /**
   * The scheme to use in external identifiers
   */
  public static final ExternalScheme CREDIT_CURVE_SCHEME = ExternalScheme.of("CREDIT_CURVE_NODE");

  /**
   * The separator used in the id construction.
   */
  private static final String SEPARATOR = "_";

  /**
   * The external id for this curve.
   */
  private final ExternalId _externalId;

  /**
   * The red code.
   */
  private final String _redCode;

  /**
   * The ticker.
   */
  private final String _ticker;

  /**
   * Seniority of the curve. E.g. LIEN1 (First Lien),
   * SNRFOR (Subordinated or Lower SeniorityLevel 2 Debt (Banks))
   */
  private final String _seniority;

  /**
   * The curve currency.
   */
  private final Currency _currency;

  /**
   * Term for the curve, 6m, 1y, 2y, ... 30y etc
   */
  private final Period _term;

  /**
   * The restructuring clause e.g. MR (Modified restructuring)
   */
  private final String _restructuringClause;

  /**
   * The generated id for this curve.
   */
  private final String _idValue;

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
     return new CreditCurveNodeIdentifier(ticker, redCode, currency, term, seniority, restructuringClause);
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
    return new CreditCurveNodeIdentifier(ticker, id.getRedCode(), id.getCurrency(), term, id.getSeniority(), id.getRestructuringClause());
  }

  /**
   * Constructs a new instance
   *
   * @param redCode the RED code, not null (underscores replaced with dashes)
   * @param currency the currency, not null
   * @param term the term, not null
   * @param seniority the seniority, not null
   * @param restructuringClause the restructuring clause, not null
   */
  private CreditCurveNodeIdentifier(final String ticker,
                                    final String redCode,
                                    final Currency currency,
                                    final Period term,
                                    final String seniority,
                                    final String restructuringClause) {
    ArgumentChecker.notNull(ticker, "ticker");
    ArgumentChecker.notNull(redCode, "redCode");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(term, "term");
    ArgumentChecker.notNull(seniority, "seniority");
    ArgumentChecker.notNull(restructuringClause, "restructuring clause");

    _ticker = ticker;
    _redCode = redCode.replace("_", "-");
    _currency = currency;
    _term = term;
    _seniority = seniority;
    _restructuringClause = restructuringClause;
    _idValue = _ticker + SEPARATOR +_redCode + SEPARATOR + _currency.getCode() + SEPARATOR +
        _seniority + SEPARATOR + _restructuringClause + SEPARATOR + _term.toString();
    _externalId = ExternalId.of(CREDIT_CURVE_SCHEME, _idValue);
  }

  @Override
  public ExternalId getExternalId() {
    return _externalId;
  }

  /**
   * Gets the ticker.
   *
   * @return the ticker
   */
  public String getTicker() {
    return _ticker;
  }

  /**
   * Gets the RED code.
   *
   * @return the RED code
   */
  public String getRedCode() {
    return _redCode;
  }

  /**
   * Gets the seniority.
   *
   * @return the seniority
   */
  public String getSeniority() {
    return _seniority;
  }

  /**
   * Gets the restructuring clause.
   *
   * @return the restructuring clause
   */
  public String getRestructuringClause() {
    return _restructuringClause;
  }

  /**
   * Gets the currency.
   *
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the term;
   *
   * @return the term
   */
  public Period getTerm() {
    return _term;
  }

  /**
   * Returns a suitable hash code for the identifier,
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return _idValue.hashCode();
  }

  /**
   * Checks if this identifier equals another identifier.
   *
   * @param obj the other identifier, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof CreditCurveNodeIdentifier) {
      return _idValue.equals(((CreditCurveNodeIdentifier) obj)._idValue);
    }
    return false;
  }

  /**
   * Gets the credit curve identifier as a string
   *
   * @return the string representing this identifier, not null
   */
  @Override
  public String toString() {
    return _idValue;
  }
}
