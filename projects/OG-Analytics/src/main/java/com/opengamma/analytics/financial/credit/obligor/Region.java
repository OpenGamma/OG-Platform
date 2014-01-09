/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor;

/**
 * Enumerate the geographical region of the reference entity (doesn't have to be the same as the legal domicile).
 * @deprecated Regions have been promoted to objects. See (@link com.opengamma.analytics.financial.obligor.Region}.
 * These objects are creates with a name only and no currency or country information.
 */
@Deprecated
public enum Region {
  /**
   *
   */
  AFRICA("Africa"),
  /**
   *
   */
  ASIA("Asia"),
  /**
   *
   */
  CARIBBEAN("Caribbean"),
  /**
   *
   */
  EASTERNEUROPE("Eastern Europe"),
  /**
   *
   */
  EUROPE("Europe"),
  /**
   *
   */
  INDIA("India"),
  /**
   *
   */
  LATINAMERICA("Latin America"),
  /**
   *
   */
  MIDDLEEAST("Middle East"),
  /**
   *
   */
  NORTHAMERICA("North America"),
  /**
   *
   */
  OCEANIA("Oceania"),
  /**
   *
   */
  OFFSHORE("Offshore"),
  /**
   *
   */
  PACIFIC("Pacific"),
  /**
   *
   */
  SUPRA("Supra"),
  /**
   *
   */
  NONE("None");

  /** The region name */
  private final String _name;

  /**
   * @param name The region name
   */
  private Region(final String name) {
    _name = name;
  }

  /**
   * Delegates to {@link com.opengamma.analytics.financial.legalentity.Sector}, with
   * the name set to the name of the enum value and no classifications set.
   * @return A sector object
   */
  public com.opengamma.analytics.financial.legalentity.Region toRegion() {
    return com.opengamma.analytics.financial.legalentity.Region.of(_name);
  }

  @Override
  public String toString() {
    return _name;
  }
}
