/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import com.opengamma.core.convention.Convention;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * Helper for per-currency conventions.
 */
public class PerCurrencyConventionHelper {

  /** The convention scheme name string **/
  public static final String SCHEME_NAME = "CONVENTION";
  /** Overnight Index string **/
  public static final String OVERNIGHT = "Overnight";
  /** Ibor (interbank offered rate) index string **/
  public static final String IBOR = "Ibor";
  /** Libor (London interbank offered rate) index string **/
  public static final String LIBOR = "Libor";
  /** Libor (London interbank offered rate) index string **/
  public static final String LIBOR_CONV = "LIBOR Convention";
  /** Jibar (Johannesburg interbank agreed rate) index string */
  public static final String JIBOR = "Jibar";
  /** Deposit convention string **/
  public static final String DEPOSIT = "Deposit";
  /** Deposit Overnight convention string **/
  public static final String DEPOSIT_ON = "DepositON";
  /** FRA convention string **/
  public static final String FRA = "FRA";
  /** OIS fixed leg convention string **/
  public static final String OIS_FIXED_LEG = "OIS Fixed Leg";
  /** IRS fixed leg convention string **/
  public static final String IRS_FIXED_LEG = "IRS Fixed Leg";
  /** IRS fixed leg convention string **/
  public static final String FIXED_LEG = "Fixed Leg";
  /** OIS float leg convention string **/
  public static final String OIS_ON_LEG = "OIS Overnight Leg";
  /** Overnight with composition, i.e. OIS-like **/
  public static final String ON_CMP_LEG = "ON Comp Leg";
  /** Overnight with arithmetic average, i.e. FF-like **/
  public static final String ON_AA_LEG = "ON AA Leg";
  /** Suffix to indicate that a leg as a unnatural payment lag to match the other leg. **/
  public static final String PAY_LAG = "Pay Lag ";
  /** IRS Ibor leg convention string **/
  public static final String IRS_IBOR_LEG = "IRS Ibor Leg";
  /** Ibor leg convention string **/
  public static final String IBOR_LEG = "Ibor Leg";
  /** Libor leg convention string **/
  public static final String LIBOR_LEG = "Libor Leg";
  /** Compounding Ibor leg convention string **/
  public static final String IBOR_CMP_LEG = "Comp Ibor Leg";
  /** Flat Compounding Ibor leg convention string **/
  public static final String IBOR_CMP_FLAT_LEG = "Comp Flat Ibor Leg";
  /** Swap string */
  public static final String SWAP = "Swap";
  /** Quarterly Eurodollar futures string */
  public static final String EURODOLLAR_FUTURE = "Quarterly ED, 3M Libor";
  /** Fed fund futures string */
  public static final String FED_FUNDS_FUTURE = "Fed Funds Future";
  /** CME deliverable swap future string */
  public static final String CME_DELIVERABLE_SWAP_FUTURE = "CME Deliverable Swap Future";
  /** Inflation swap leg string */
  public static final String INFLATION_LEG = "Inflation Swap Leg";
  /** Price index string */
  public static final String PRICE_INDEX = "Price Index";
  /** Swap index string */
  public static final String SWAP_INDEX = "Swap Index";
  /** FX Spot string */
  public static final String FX_SPOT = "FX Spot";
  /** FX Forward string */
  public static final String FX_FORWARD = "FX Forward";

  /** Tenor string: 1M **/
  public static final String TENOR_STR_1M = "1M";
  /** Tenor string: 3M **/
  public static final String TENOR_STR_3M = "3M";
  /** Tenor string: 6M **/
  public static final String TENOR_STR_6M = "6M";
  /** Tenor string: 12M **/
  public static final String TENOR_STR_12M = "12M";
  /** Tenor string: 1Y **/
  public static final String TENOR_STR_1Y = "1Y";
  /** Tenor string: short period instruments (usually for 1w or 2w) **/
  public static final String TENOR_STR_SHORT = "Short";
  /** STIR Futures (i.e. futures on Ibor) **/
  public static final String STIR_FUTURES = "STIR Futures ";
  /** Serial (i.e. monthly) futures **/
  public static final String SERIAL = "Serial";
  /** Quarterly (i.e. March, June, September, December) futures **/
  public static final String QUARTERLY = "Quarterly";
  /** Monthly **/
  public static final String MONTHLY = "Monthly";
  /** IMM dates **/
  public static final String IMM = "IMM";
  /** Government (Simplified bond description) **/
  public static final String GOVT = "Govt ";
  
  /** Quarterly IMM roll dates **/
  public static final ExternalId QUARTERLY_IMM_DATES = ExternalId.of(SCHEME_NAME, RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
  /** Monthly IMM roll dates **/
  public static final ExternalId MONTHLY_IMM_DATES = ExternalId.of(SCHEME_NAME, RollDateAdjusterFactory.MONTHLY_IMM_ROLL_STRING);

  public static ExternalIdBundle getIds(final Currency currency, final String instrumentName) {
    final String idName = getConventionName(currency, instrumentName);
    return ExternalIdBundle.of(simpleNameId(idName));
  }

  public static ExternalId getId(final Currency currency, final String instrumentName) {
    final String idName = getConventionName(currency, instrumentName);
    return simpleNameId(idName);
  }

  public static String getConventionName(final Currency currency, final String instrumentName) {
    return currency.getCode() + " " + instrumentName;
  }

  public static ExternalIdBundle getIds(final Currency currency, final String tenorString, final String instrumentName) {
    final String idName = getConventionName(currency, tenorString, instrumentName);
    return ExternalIdBundle.of(simpleNameId(idName));
  }

  public static ExternalId getId(final Currency currency, final String tenorString, final String instrumentName) {
    final String idName = getConventionName(currency, tenorString, instrumentName);
    return simpleNameId(idName);
  }

  public static ExternalIdBundle getIds(final String idName) {
    return ExternalIdBundle.of(simpleNameId(idName));
  }

  public static String getConventionName(final Currency currency, final String tenorString, final String instrumentName) {
    return currency.getCode() + " " + tenorString + " " + instrumentName;
  }

  public static ExternalId simpleNameId(final String name) {
    return ExternalId.of(SCHEME_NAME, name);
  }

  public static ConventionLink<Convention> getConventionLink(Currency ccy, String instrumentName) {
    return ConventionLink.of(Convention.class, getId(ccy, instrumentName));
  }
}
