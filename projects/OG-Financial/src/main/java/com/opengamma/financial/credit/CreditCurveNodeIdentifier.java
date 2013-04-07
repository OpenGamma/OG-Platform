/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.credit;

import org.threeten.bp.Period;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

public abstract class CreditCurveNodeIdentifier implements ExternalIdentifiable {

  /**
   * The separator used in the id construction.
   */
  protected static final String SEPARATOR = "_";
  /**
   * The external id for this curve.
   */
  protected final ExternalId _externalId;
  /**
   * The red code.
   */
  protected final String _redCode;
  /**
   * The ticker.
   */
  protected final String _ticker;
  /**
   * Seniority of the curve. E.g. LIEN1 (First Lien),
   * SNRFOR (Subordinated or Lower SeniorityLevel 2 Debt (Banks))
   */
  protected final String _seniority;
  /**
   * The curve currency.
   */
  protected final Currency _currency;
  /**
   * Term for the curve, 6m, 1y, 2y, ... 30y etc
   */
  protected final Period _term;
  /**
   * The restructuring clause e.g. MR (Modified restructuring)
   */
  protected final String _restructuringClause;
  /**
   * The generated id for this curve.
   */
  protected final String _idValue;

  public CreditCurveNodeIdentifier(final ExternalScheme creditCurveScheme,
                                   final String ticker,
                                   final String redCode,
                                   final String seniority,
                                   final Currency currency,
                                   final Period term,
                                   final String restructuringClause) {

    ArgumentChecker.notNull(creditCurveScheme, "creditCurveScheme");
    ArgumentChecker.notNull(ticker, "ticker");
    ArgumentChecker.notNull(redCode, "redCode");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(seniority, "seniority");
    ArgumentChecker.notNull(term, "term");
    ArgumentChecker.notNull(restructuringClause, "restructuring clause");

    _currency = currency;
    _term = term;
    _redCode = redCode.replace("_", "-");
    _ticker = ticker;
    _restructuringClause = restructuringClause;
    _seniority = seniority;

    _idValue = _ticker + SEPARATOR +_redCode + SEPARATOR + _currency.getCode() + SEPARATOR +
        _seniority + SEPARATOR + _restructuringClause + SEPARATOR + _term.toString();
    _externalId = ExternalId.of(creditCurveScheme, _idValue);
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
    return _externalId.hashCode();
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

    return obj instanceof CreditCurveNodeIdentifier &&
        _externalId.equals(((CreditCurveNodeIdentifier) obj)._externalId);
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
