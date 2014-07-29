/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity;

/** 
 * Seniority Levels 
 * 
 * Reference: https://www.markit.com/news/XMLGuide.pdf, 
 *            "XML User Guide - Markit Data",
 *            Version 10.3.8, November 2010.
 *
 */
public enum SeniorityLevel {
  /** Junior Subordinated or Upper Tier 2 Debt (Banks) */
  JRSUBUT2,
  /** First Lien – Secured Debt with a First Lien on a pool of assets. (Introduced in August 2006) */
  LIEN1,
  /** Second Lien – Secured Debt with a Second Lien on a pool of assets. (Introduced in August 2006) */
  LIEN2,
  /** Third Lien – Secured Debt with a Third Lien on a pool of assets. (Introduced in August 2006) */
  LIEN3,
  /**
   * Mezzanine – Contractually or Structurally Subordinated, Unsecured Debt falling between senior debt and equity.
   * Commonly used in leveraged buyouts or by middle-market companies. (Introduced in August 2006)
   */
  MEZZ,
  /** Preference Shares, or Tier 1 Capital (Banks) */
  PREFT1,
  /** Secured Debt (Corporate/Financial) or Domestic Currency Sovereign Debt (Government) */
  SECDOM,
  /** Senior Unsecured Debt (Corporate/Financial), Foreign Currency Sovereign Debt (Government) */
  SNRFOR,
  /** Subordinated or Lower Tier 2 Debt (Banks) */
  SUBLT2
}
