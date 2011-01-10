/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Utilities and constants for securities.
 */
public class SecurityUtils {

  /**
   * Identification scheme for the ISIN code.
   */
  public static final IdentificationScheme ISIN = IdentificationScheme.of("ISIN");
  /**
   * Identification scheme for the CUSIP code.
   */
  public static final IdentificationScheme CUSIP = IdentificationScheme.of("CUSIP");
  /**
   * Identification scheme for SEDOL1.
   */
  public static final IdentificationScheme SEDOL1 = IdentificationScheme.of("SEDOL1");
  /**
   * Identification scheme for Bloomberg BUIDs.
   */
  public static final IdentificationScheme BLOOMBERG_BUID = IdentificationScheme.of("BLOOMBERG_BUID");
  /**
   * Identification scheme for Bloomberg tickers.
   */
  public static final IdentificationScheme BLOOMBERG_TICKER = IdentificationScheme.of("BLOOMBERG_TICKER");
  /**
   * Identification scheme for Reuters RICs.
   */
  public static final IdentificationScheme RIC = IdentificationScheme.of("RIC");
  /**
   * Identification scheme for ActivFeed tickers.
   */
  public static final IdentificationScheme ACTIVFEED_TICKER = IdentificationScheme.of("ACTIVFEED_TICKER");

  /**
   * Restricted constructor.
   */
  protected SecurityUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an ISIN code.
   * <p>
   * This is the international standard securities identifying number.
   * The first two characters are the ISO country code, followed by a 9 character
   * alphanumeric national code and a single numeric check-digit.
   * Example might be {@code US0231351067}.
   * 
   * @param code  the ISIN code, not null
   * @return the security identifier, not null
   */
  public static Identifier isinSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("ISIN code is invalid: " + code);
    }
//    if (code.matches("[A-Z]{2}[A-Z0-9]{9}[0-9]") == false) {
//      throw new IllegalArgumentException("ISIN code is invalid: " + code);
//    }
    return Identifier.of(ISIN, code);
  }

  /**
   * Creates a CUSIP code.
   * <p>
   * This is the national securities identifying number for USA and Canada.
   * The code should be 8 alphanumeric characters followed by a single numeric check-digit.
   * Example might be {@code 023135106}.
   * 
   * @param code  the CUSIP code, not null
   * @return the security identifier, not null
   */
  public static Identifier cusipSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("CUSIP code is invalid: " + code);
    }
//    if (code.matches("[A-Z0-9]{8}[0-9]?") == false) {
//      throw new IllegalArgumentException("CUSIP code is invalid: " + code);
//    }
    return Identifier.of(CUSIP, code);
  }

  /**
   * Creates a SEDOL code.
   * <p>
   * This is the national securities identifying number for UK and Ireland.
   * The code should be 6 alphanumeric characters followed by a single numeric check-digit.
   * Example might be {@code 0263494}.
   * 
   * @param code  the SEDOL code, not null
   * @return the security identifier, not null
   */
  public static Identifier sedol1SecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("SEDOL1 code is invalid: " + code);
    }
//    if (code.matches("[A-Z0-9]{6}[0-9]?") == false) {
//      throw new IllegalArgumentException("SEDOL1 code is invalid: " + code);
//    }
    return Identifier.of(SEDOL1, code);
  }

  /**
   * Creates a Bloomberg BIUD code.
   * <p>
   * This is the BUID code supplied by Bloomberg.
   * Examples might be {@code EQ0010599500001000} or {@code IX6572023-0}.
   * 
   * @param code  the Bloomberg BIUD code, not null
   * @return the security identifier, not null
   */
  public static Identifier bloombergBuidSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("BUID code is invalid: " + code);
    }
    return Identifier.of(BLOOMBERG_BUID, code);
  }

  /**
   * Creates a Bloomberg ticker.
   * <p>
   * This is the ticker supplied by Bloomberg.
   * Examples might be {@code MCO US Equity} or {@code CSCO US 01/21/12 C30 Equity}.
   * 
   * @param ticker  the Bloomberg ticker, not null
   * @return the security identifier, not null
   */
  public static Identifier bloombergTickerSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "code");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return Identifier.of(BLOOMBERG_TICKER, ticker);
  }

  /**
   * Creates a Reuters RIC code.
   * <p>
   * This is the RIC code supplied by Reuters.
   * Example might be {@code MSFT.OQ}.
   * 
   * @param code  the BIUD code, not null
   * @return the security identifier, not null
   */
  public static Identifier ricSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("RIC code is invalid: " + code);
    }
    return Identifier.of(RIC, code);
  }
  
  /**
   * Creates an ActivFeed ticker.
   * <p>
   * This is the ticker used by ActivFeed.
   * Examples might be {@code IBM.N} or {@code C/04H.CB}.
   * 
   * @param ticker  the ActivFeed ticker, not null
   * @return the security identifier, not null
   */
  public static Identifier activFeedTickerSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "code");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return Identifier.of(ACTIVFEED_TICKER, ticker);
  }

}
