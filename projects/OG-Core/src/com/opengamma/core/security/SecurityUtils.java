/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import javax.time.CalendricalException;
import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Utilities and constants for {@code Security}.
 * <p>
 * This is a thread-safe static utility class.
 */
@PublicAPI
public class SecurityUtils {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SecurityUtils.class);

  /**
   * Identification scheme for the ISIN code.
   */
  public static final ExternalScheme ISIN = ExternalScheme.of("ISIN");
  /**
   * Identification scheme for the CUSIP code.
   */
  public static final ExternalScheme CUSIP = ExternalScheme.of("CUSIP");
  /**
   * Identification scheme for SEDOL1.
   */
  public static final ExternalScheme SEDOL1 = ExternalScheme.of("SEDOL1");
  /**
   * Identification scheme for Bloomberg BUIDs.
   */
  public static final ExternalScheme BLOOMBERG_BUID = ExternalScheme.of("BLOOMBERG_BUID");
  /**
   * Identification scheme for weak Bloomberg BUIDs.
   * A weak ID permits the underlying market data to return old data. 
   */
  public static final ExternalScheme BLOOMBERG_BUID_WEAK = ExternalScheme.of("BLOOMBERG_BUID_WEAK");
  /**
   * Identification scheme for Bloomberg tickers.
   */
  public static final ExternalScheme BLOOMBERG_TICKER = ExternalScheme.of("BLOOMBERG_TICKER");
  /**
   * Identification scheme for weak Bloomberg tickers.
   * A weak ID permits the underlying market data to return old data. 
   */
  public static final ExternalScheme BLOOMBERG_TICKER_WEAK = ExternalScheme.of("BLOOMBERG_TICKER_WEAK");
  /**
   * Identification scheme for Bloomberg tickers.
   */
  public static final ExternalScheme BLOOMBERG_TCM = ExternalScheme.of("BLOOMBERG_TCM");
  /**
   * Identification scheme for Reuters RICs.
   */
  public static final ExternalScheme RIC = ExternalScheme.of("RIC");
  /**
   * Identification scheme for ActivFeed tickers.
   */
  public static final ExternalScheme ACTIVFEED_TICKER = ExternalScheme.of("ACTIVFEED_TICKER");
  /**
   * Identification scheme for OpenGamma synthetic instruments.
   */
  public static final ExternalScheme OG_SYNTHETIC_TICKER = ExternalScheme.of("OG_SYNTHETIC_TICKER");
  /**
   * Identification scheme for Tullet-Prebon SURF tickers.
   */
  public static final ExternalScheme SURF = ExternalScheme.of("SURF");

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
  public static ExternalId isinSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("ISIN code is invalid: " + code);
    }
    //    if (code.matches("[A-Z]{2}[A-Z0-9]{9}[0-9]") == false) {
    //      throw new IllegalArgumentException("ISIN code is invalid: " + code);
    //    }
    return ExternalId.of(ISIN, code);
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
  public static ExternalId cusipSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("CUSIP code is invalid: " + code);
    }
    //    if (code.matches("[A-Z0-9]{8}[0-9]?") == false) {
    //      throw new IllegalArgumentException("CUSIP code is invalid: " + code);
    //    }
    return ExternalId.of(CUSIP, code);
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
  public static ExternalId sedol1SecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("SEDOL1 code is invalid: " + code);
    }
    //    if (code.matches("[A-Z0-9]{6}[0-9]?") == false) {
    //      throw new IllegalArgumentException("SEDOL1 code is invalid: " + code);
    //    }
    return ExternalId.of(SEDOL1, code);
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
  public static ExternalId bloombergBuidSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("BUID code is invalid: " + code);
    }
    return ExternalId.of(BLOOMBERG_BUID, code);
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
  public static ExternalId bloombergTickerSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "code");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(BLOOMBERG_TICKER, ticker);
  }

  /**
   * Creates a Bloomberg ticker coupon maturity identifier.
   * <p>
   * This is the ticker combined with a coupon and a maturity supplied by Bloomberg.
   * Example might be {@code T 4.75 15/08/43 Govt}.
   * 
   * @param tickerWithoutSector  the Bloomberg ticker without the sector, not null
   * @param coupon  the coupon, not null
   * @param maturity  the maturity date, not null
   * @param marketSector  the sector, not null
   * @return the security identifier, not null
   */
  public static ExternalId bloombergTCMSecurityId(final String tickerWithoutSector, final String coupon, final String maturity, final String marketSector) {
    ArgumentChecker.notNull(tickerWithoutSector, "tickerWithoutSector");
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(maturity, "maturity");
    ArgumentChecker.notNull(marketSector, "marketSector");
    if (StringUtils.isEmpty(tickerWithoutSector)) {
      throw new IllegalArgumentException("Ticker (without sector) must not be empty");
    }
    if (StringUtils.isEmpty(coupon)) {
      throw new IllegalArgumentException("Coupon must not be empty, ticker = " + tickerWithoutSector);
    }
    if (StringUtils.isEmpty(maturity)) {
      throw new IllegalArgumentException("Maturity must not be empty, ticker = " + tickerWithoutSector + ", coupon = " + coupon);
    }
    if (StringUtils.isEmpty(marketSector)) {
      throw new IllegalArgumentException("Market sector must not be empty, ticker = " + tickerWithoutSector + ", coupon = " + coupon + ", maturity = " + maturity);
    }
    Double couponDbl;
    try {
      couponDbl = Double.parseDouble(coupon);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Coupon must be a valid double, ticker=" + tickerWithoutSector + ", coupon=" + coupon, ex);
    }
    if (s_logger.isDebugEnabled()) {
      try {
        LocalDate.parse(maturity, DateTimeFormatters.pattern("MM/dd/YY"));
      } catch (UnsupportedOperationException uoe) {
        s_logger.warn("Problem parsing maturity " + maturity + " ticker=" + tickerWithoutSector + ", coupon=" + coupon);
      } catch (CalendricalException ce) {
        s_logger.warn("Problem parsing maturity " + maturity + " ticker=" + tickerWithoutSector + ", coupon=" + coupon);
      }
    }
    return ExternalId.of(BLOOMBERG_TCM, tickerWithoutSector + " " + couponDbl + " " + maturity + " " + marketSector);
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
  public static ExternalId ricSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("RIC code is invalid: " + code);
    }
    return ExternalId.of(RIC, code);
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
  public static ExternalId activFeedTickerSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "code");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(ACTIVFEED_TICKER, ticker);
  }
  
}
