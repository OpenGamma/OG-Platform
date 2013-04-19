/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.credit;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Generates identifiers for CDS recovery rates whcih can be stored in the
 * HTS or snapshots.
 */
public final class CdsRecoveryRateIdentifier {

  /**
   * The separator used in the id construction.
   */
  private static final String SEPARATOR = "_";

  /**
   * The scheme to use in external identifiers for Same Day CDS recovery rate data.
   */
  private static final ExternalScheme SAMEDAY_CDS_SCHEME = ExternalScheme.of("SAMEDAY_CDS_RECOVERY_RATE");

  /**
   * The scheme to use in external identifiers for Composite CDS recovery rate data.
   */
  private static final ExternalScheme COMPOSITE_CDS_SCHEME = ExternalScheme.of("COMPOSITE_CDS_RECOVERY_RATE");

  /**
   * The external id for this recovery rate.
   */
  private final ExternalId _externalId;

  /**
   * The generated id for this recovery rate.
   */
  private final String _idValue;

  /**
   * Create an identifier for a Sameday recovery rate with the specified properties.
   *
   * @param redCode the red code of the CDS
   * @param currency the currency of the CDS
   * @param seniority the seniority of the CDS
   * @param restructuringClause the restructuring clause of the CDS
   * @return a new identifier
   */
  public static CdsRecoveryRateIdentifier forSamedayCds(final String redCode,
                                                        final Currency currency,
                                                        final String seniority,
                                                        final String restructuringClause) {

    String idValue = generateCdsId(redCode, currency, seniority, restructuringClause);
    return new CdsRecoveryRateIdentifier(SAMEDAY_CDS_SCHEME, idValue);
  }

  /**
   * Create an identifier for a Composite CDS with the specified properties.
   *
   * @param redCode the red code of the CDS
   * @param currency the currency of the CDS
   * @param seniority the seniority of the CDS
   * @param restructuringClause the restructuring clause of the CDS
   * @return a new identifier
   */
  public static CdsRecoveryRateIdentifier forCompositeCds(final String redCode,
                                                          final Currency currency,
                                                          final String seniority,
                                                          final String restructuringClause) {

    String idValue = generateCdsId(redCode, currency, seniority, restructuringClause);
    return new CdsRecoveryRateIdentifier(COMPOSITE_CDS_SCHEME, idValue);
  }

  private static String generateCdsId(String redCode,
                                      Currency currency,
                                      String seniority,
                                      String restructuringClause) {
    return convertRed(redCode) + SEPARATOR + currency.getCode() + SEPARATOR +
        seniority + SEPARATOR + restructuringClause;
  }

  private static String convertRed(String redCode) {
    return redCode.replace("_", "-");
  }

  private CdsRecoveryRateIdentifier(ExternalScheme recoveryRateScheme, String idValue) {

    ArgumentChecker.notNull(recoveryRateScheme, "creditCurveScheme");
    ArgumentChecker.notNull(idValue, "idValue");

    _idValue = idValue;
    _externalId = ExternalId.of(recoveryRateScheme, idValue);
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

    return obj instanceof CdsRecoveryRateIdentifier &&
        _externalId.equals(((CdsRecoveryRateIdentifier) obj)._externalId);
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
