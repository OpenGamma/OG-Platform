/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.credit;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Stores the date required to uniquely identify a credit curve - the red code, currency, tenor, seniority and restructuring clause
 */
public final class CreditCurveIdentifier implements UniqueIdentifiable, ObjectIdentifiable {

  /**
   * The scheme to use in object identifiers
   */
  public static final String OBJECT_SCHEME = "CreditCurveIdentifier";
  private static final String SEPARATOR = "_";
  private final String _redCode;
  private final String _seniority;
  private final Currency _currency;
  private final String _term;
  private final String _restructuringClause;


  private final String _idValue;

  /**
   * Creates an {@code CreditCurveIdentifier} from issuer, seniority and restructuring clause data
   *
   * @param redCode the RED code, not null
   * @param currency the currency, not null
   * @param seniority the seniority, not null
   * @param restructuringClause the restructuring clause, not null
   * @return the credit curve identifier, not null
   */
  public static CreditCurveIdentifier of(final ExternalId redCode,
                                         final Currency currency,
                                         final String term,
                                         final String seniority,
                                         final String restructuringClause) {
    ArgumentChecker.notNull(redCode, "redCode");
    return new CreditCurveIdentifier(redCode.getValue(), currency, term, seniority, restructuringClause);
  }

  /**
   * Creates an {@code CreditCurveIdentifier} from issuer, seniority and restructuring clause data
   *
   * @param redCode the RED code, not null
   * @param currency the currency, not null
   * @param term the currency, not null
   * @param seniority the seniority, not null
   * @param restructuringClause the restructuring clause, not null
   * @return the credit curve identifier, not null
   */
  public static CreditCurveIdentifier of(final String redCode,
                                         final Currency currency,
                                         final String term,
                                         final String seniority,
                                         final String restructuringClause) {
    return new CreditCurveIdentifier(redCode, currency, term, seniority, restructuringClause);
  }

  /**
   * Creates an {@code CreditCurveIdentifier} from a unique id.
   *
   * @param uniqueId the unique id, not null
   * @return the credit curve identifier, not null
   */
  public static CreditCurveIdentifier of(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "unique id");
    if (uniqueId.getScheme().equals(OBJECT_SCHEME)) {
      final String[] sections = uniqueId.getValue().split(SEPARATOR);
      if (sections.length == 5) {
        return new CreditCurveIdentifier(sections[0], Currency.of(sections[1]), sections[2], sections[3], sections[4]);
      }
    }
    throw new UnsupportedOperationException(
        "Cannot create a CreditCurveIdentifier from this UniqueId; need an ObjectScheme of CreditCurveIdentifier, have " + uniqueId.getScheme());
  }

  /**
   * Constructs a new instance
   *
   * @param redCode the RED code, not null
   * @param currency the currency, not null
   * @param term the term, not null
   * @param seniority the seniority, not null
   * @param restructuringClause the restructuring clause, not null
   */
  private CreditCurveIdentifier(final String redCode,
                                final Currency currency,
                                final String term,
                                final String seniority,
                                final String restructuringClause) {
    ArgumentChecker.notNull(redCode, "redCode");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(seniority, "seniority");
    ArgumentChecker.notNull(restructuringClause, "restructuring clause");
    _redCode = redCode;
    _currency = currency;
    _seniority = seniority;
    _restructuringClause = restructuringClause;
    _term = term;
    _idValue = _redCode + SEPARATOR + _currency.getCode() + SEPARATOR + _seniority + SEPARATOR + _restructuringClause + SEPARATOR + _term;
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
   * @deprecated
   * Gets the term;
   *
   * @return the term
   */
  @Deprecated
  public String getTerm() {
    return _term;
  }

  /**
   * Gets the object identifier.
   * <p/>
   * This uses the scheme {@link #OBJECT_SCHEME CreditCurveIdentifier}.
   *
   * @return the object identifier, not null
   */
  @Override
  public ObjectId getObjectId() {
    return ObjectId.of(OBJECT_SCHEME, _idValue);
  }

  /**
   * Gets the unique identifier.
   * <p/>
   * The uses the scheme {@link #OBJECT_SCHEME CreditCurveIdentifier}
   *
   * @return the unique identifier, not null
   */
  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(OBJECT_SCHEME, _idValue);
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
    if (obj instanceof CreditCurveIdentifier) {
      return _idValue.equals(((CreditCurveIdentifier) obj)._idValue);
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
