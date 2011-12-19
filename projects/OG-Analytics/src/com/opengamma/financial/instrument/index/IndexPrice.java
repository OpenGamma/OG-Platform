/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import javax.time.calendar.Period;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.money.Currency;

/**
 * Class describing an price index, like the one used in inflation instruments.
 */
public class IndexPrice {

  /**
   * Name of the index.
   */
  private final String _name;
  /**
   * The currency in which the index is computed.
   */
  private final Currency _currency;
  /**
   * The reference region for the price index.
   */
  private final Currency _region; // FIXME: to be changed to Region
  /**
   * The lag between the month end and the index publication.
   */
  private final Period _publicationLag;

  /**
   * Constructor of the price index.
   * @param name The index name. Not null.
   * @param ccy The currency in which the index is computed. Not null.
   * @param region The reference region for the price index. Not null.
   * @param publicationLag The lag between the month end and the index publication.
   */
  public IndexPrice(final String name, final Currency ccy, final Currency region, final Period publicationLag) {
    Validate.notNull(name, "Name");
    Validate.notNull(ccy, "Currency");
    Validate.notNull(region, "Region");
    Validate.notNull(publicationLag, "Publication lag");
    _name = name;
    _currency = ccy;
    _region = region;
    _publicationLag = publicationLag;
  }

  /**
   * Gets the name of the price index.
   * @return The name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the currency in which the index is computed.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the region associated to the price index.
   * @return The region.
   */
  public Currency getRegion() {
    return _region;
  }

  /**
   * Gets the publication lag of the price index.
   * @return The lag.
   */
  public Period getPublicationLag() {
    return _publicationLag;
  }

  @Override
  public String toString() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _name.hashCode();
    result = prime * result + _region.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    IndexPrice other = (IndexPrice) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (!ObjectUtils.equals(_region, other._region)) {
      return false;
    }
    return true;
  }

}
