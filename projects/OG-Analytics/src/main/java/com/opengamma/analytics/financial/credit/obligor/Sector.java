/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor;

/**
 * Enumerate the industrial sector classification of a reference entity
 */
public enum Sector {
  /**
   * 
   */
  BASICMATERIALS,
  /**
   * 
   */
  CONSUMERGOODS,
  /**
   * 
   */
  CONSUMERSERVICES,
  /**
   * 
   */
  ENERGY,
  /**
   * 
   */
  FINANCIALS,
  /**
   * 
   */
  GOVERNMENT,
  /**
   * 
   */
  HEALTHCARE,
  /**
   * 
   */
  INDUSTRIALS,
  /**
   * 
   */
  TECHNOLOGY,
  /**
   * 
   */
  TELECOMMUNICATIONSERVICES,
  /**
   * 
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
   * 
   */
  NONE;

  // TODO: Extend this list to include a comprehensive list of sector classifications (ICB classification)
}
