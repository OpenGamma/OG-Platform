/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor;

/**
 * Enumerate the industrial sector classification of a reference entity.
 * @deprecated Sector has been promoted to an object. See {@link com.opengamma.analytics.financial.legalentity.Sector}
 */
@Deprecated
public enum Sector {
  /**
   * Basic materials
   */
  BASICMATERIALS,
  /**
   * Consumer goods
   */
  CONSUMERGOODS,
  /**
   * Consumer services
   */
  CONSUMERSERVICES,
  /**
   * Energy
   */
  ENERGY,
  /**
   * Financials
   */
  FINANCIALS,
  /**
   * Government
   */
  GOVERNMENT,
  /**
   * Healthcare
   */
  HEALTHCARE,
  /**
   * Industrials
   */
  INDUSTRIALS,
  /**
   * Technology
   */
  TECHNOLOGY,
  /**
   * Telecommunication services
   */
  TELECOMMUNICATIONSERVICES,
  /**
   * Utilities
   */
  UTILITIES,
  /**
   * The entity may be a municipal e.g. the state of California
   */
  MUNICIPAL,
  /**
   * The entity may be a sovereign e.g. the protection buyer has purchased protection on bonds issued by a sovereign
   */
  SOVEREIGN,
  /**
   * No sector
   */
  NONE;

  /**
   * Delegates to {@link com.opengamma.analytics.financial.legalentity.Sector}, with
   * the name set to the name of the enum value and no classifications set.
   * @return A sector object
   */
  public com.opengamma.analytics.financial.legalentity.Sector toSector() {
    return com.opengamma.analytics.financial.legalentity.Sector.of(name());
  }
}
