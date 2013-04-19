/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.credit;

import org.threeten.bp.Period;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Generates identifiers for CDS curve spreads whcih can be stored in the
 * HTS or snapshots.
 */
public final class CreditCurveNodeIdentifier {

  /**
   * The separator used in the id construction.
   */
  private static final String SEPARATOR = "_";

  /**
   * The scheme to use in external identifiers for CDS Index curve definition data.
   */
  private static final ExternalScheme CDS_INDEX_SCHEME = ExternalScheme.of("CDS_INDEX_CREDIT_CURVE_NODE");

  /**
   * The scheme to use in external identifiers for Same Day CDS curve definition data.
   */
  private static final ExternalScheme SAMEDAY_CDS_SCHEME = ExternalScheme.of("SAMEDAY_CREDIT_CURVE_NODE");

  /**
   * The scheme to use in external identifiers for Composite CDS curve definition data.
   */
  private static final ExternalScheme COMPOSITE_CDS_SCHEME = ExternalScheme.of("COMPOSITE_CREDIT_CURVE_NODE");

  /**
   * The external id for this curve.
   */
  private final ExternalId _externalId;

  /**
   * The generated id for this curve.
   */
  private final String _idValue;

  /**
   * Create an identifier for a CDS Index with the specified red code and term.
   *
   * @param redCode the red code of the CDS index
   * @param term the term required
   * @return a new identifier
   */
  public static CreditCurveNodeIdentifier forCdsIndex(final String redCode, final Period term) {

    String idValue = convertRed(redCode) + SEPARATOR + term.toString();
    return new CreditCurveNodeIdentifier(CDS_INDEX_SCHEME, idValue);
  }

  /**
   * Create an identifier for a Sameday CDS with the specified properties.
   *
   * @param ticker the ticker of the CDS
   * @param redCode the red code of the CDS
   * @param currency the currency of the CDS
   * @param term the term of the CDS
   * @param seniority the seniority of the CDS
   * @param restructuringClause the restructuring clause of the CDS
   * @return a new identifier
   */
  public static CreditCurveNodeIdentifier forSamedayCds(final String ticker,
                                                        final String redCode,
                                                        final Currency currency,
                                                        final Period term,
                                                        final String seniority,
                                                        final String restructuringClause) {

    String idValue = generateCdsId(ticker, redCode, currency, term, seniority, restructuringClause);
    return new CreditCurveNodeIdentifier(SAMEDAY_CDS_SCHEME, idValue);
  }

  /**
   * Create an identifier for a Composite CDS with the specified properties.
   *
   * @param ticker the ticker of the CDS
   * @param redCode the red code of the CDS
   * @param currency the currency of the CDS
   * @param term the term of the CDS
   * @param seniority the seniority of the CDS
   * @param restructuringClause the restructuring clause of the CDS
   * @return a new identifier
   */
  public static CreditCurveNodeIdentifier forCompositeCds(final String ticker,
                                                        final String redCode,
                                                        final Currency currency,
                                                        final Period term,
                                                        final String seniority,
                                                        final String restructuringClause) {

    String idValue = generateCdsId(ticker, redCode, currency, term, seniority, restructuringClause);
    return new CreditCurveNodeIdentifier(COMPOSITE_CDS_SCHEME, idValue);
  }

  private static String generateCdsId(String ticker,
                                      String redCode,
                                      Currency currency,
                                      Period term,
                                      String seniority,
                                      String restructuringClause) {
    return ticker + SEPARATOR + convertRed(redCode) + SEPARATOR + currency.getCode() + SEPARATOR +
        seniority + SEPARATOR + restructuringClause + SEPARATOR + term.toString();
  }

  private static String convertRed(String redCode) {
    return redCode.replace("_", "-");
  }

  private CreditCurveNodeIdentifier(ExternalScheme creditCurveScheme, String idValue) {

    ArgumentChecker.notNull(creditCurveScheme, "creditCurveScheme");
    ArgumentChecker.notNull(idValue, "idValue");

    _idValue = idValue;
    _externalId = ExternalId.of(creditCurveScheme, idValue);
  }

  /**
   * Return the external id.
   *
   * @return the external id
   */
  public ExternalId getExternalId() {
    return _externalId;
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
