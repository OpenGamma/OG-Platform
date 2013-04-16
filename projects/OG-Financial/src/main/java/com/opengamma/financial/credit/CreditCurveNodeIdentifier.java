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

public class CreditCurveNodeIdentifier {

  /**
   * The separator used in the id construction.
   */
  private static final String SEPARATOR = "_";

  private static final ExternalScheme CDS_INDEX_SCHEME = ExternalScheme.of("CDS_INDEX_SCHEME");

  private static final ExternalScheme SAMEDAY_CDS_SCHEME = ExternalScheme.of("SAMEDAY_CREDIT_CURVE_NODE");
  /**
   * The scheme to use in external identifiers
   */
  private static final ExternalScheme COMPOSITE_CDS_SCHEME = ExternalScheme.of("COMPOSITE_CREDIT_CURVE_NODE");

  /**
   * The external id for this curve.
   */
  private final ExternalId _externalId;

  /**
   * The generated id for this curve.
   */
  protected final String _idValue;

  public static CreditCurveNodeIdentifier forCdsIndex(final String indexName, final String redCode, final Period term) {

    String idValue = indexName + SEPARATOR + convertRed(redCode) + SEPARATOR + term.toString();
    return new CreditCurveNodeIdentifier(CDS_INDEX_SCHEME, idValue);
  }

  private static String convertRed(String redCode) {
    return redCode.replace("_", "-");
  }

  public static CreditCurveNodeIdentifier forSamedayCds(final String ticker,
                                                        final String redCode,
                                                        final Currency currency,
                                                        final Period term,
                                                        final String seniority,
                                                        final String restructuringClause) {

    String idValue = generateCdsId(ticker, redCode, currency, term, seniority, restructuringClause);
    return new CreditCurveNodeIdentifier(SAMEDAY_CDS_SCHEME, idValue);
  }

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


  public CreditCurveNodeIdentifier(ExternalScheme creditCurveScheme, String idValue) {

    ArgumentChecker.notNull(creditCurveScheme, "creditCurveScheme");
    ArgumentChecker.notNull(idValue, "idValue");

    _idValue = idValue;
    _externalId = ExternalId.of(creditCurveScheme, idValue);
  }

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