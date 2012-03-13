/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_INTEREST_RATE_TYPE;
import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LAST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LONG_NAME;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_TRADING_HRS;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_VAL_PT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_MIC_PRIM_EXCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_DES;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Loads the data for an Interest Rate Future from Bloomberg.
 */
public class InterestRateFutureLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureLoader.class);
  
  private static final Pattern s_rateTenorPattern = Pattern.compile("(\\d+)\\s*(DAY|MO)");
  
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_INTEREST_RATE_FUTURE_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_FUT_LONG_NAME,
      FIELD_FUT_LAST_TRADE_DT,
      FIELD_FUT_TRADING_HRS,
      FIELD_ID_MIC_PRIM_EXCH, // trading exchange
      FIELD_CRNCY,
      FIELD_SECURITY_DES,
      FIELD_PARSEKYABLE_DES,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_FUT_VAL_PT));

//  private static final Map<Currency, String> s_currency2BBGRateCode = Maps.newHashMap();
//  static {
//    s_currency2BBGRateCode.put(Currency.USD, "US");
//    s_currency2BBGRateCode.put(Currency.GBP, "BP");
//    s_currency2BBGRateCode.put(Currency.EUR, "EU");
//    s_currency2BBGRateCode.put(Currency.CHF, "SF");
//    s_currency2BBGRateCode.put(Currency.JPY, "JY");
//  }
  
  private static final Map<String, String> BBGCODE_UNDERLYING = Maps.newHashMap();
  static {
    BBGCODE_UNDERLYING.put("ED", "US0003M Index");
    BBGCODE_UNDERLYING.put("EM", "US0001M Index");
    BBGCODE_UNDERLYING.put("ER", "EUR003M Index");
    BBGCODE_UNDERLYING.put("0R", "EUR003M Index");
    BBGCODE_UNDERLYING.put("2R", "EUR003M Index");
    BBGCODE_UNDERLYING.put("FP", "EUR003M Index");
    BBGCODE_UNDERLYING.put("L ", "BP0003M Index");
    BBGCODE_UNDERLYING.put("0L", "BP0003M Index");
    BBGCODE_UNDERLYING.put("2L", "BP0003M Index");
    BBGCODE_UNDERLYING.put("ES", "SF0003M Index");
    BBGCODE_UNDERLYING.put("EF", "JY0003M Index");
    // TODO: Add EY - 3M Tibor
  }
  
  /**
   * The valid Bloomberg future categories for IR Futures
   */
  public static final Set<String> VALID_FUTURE_CATEGORIES = ImmutableSet.of(BLOOMBERG_INTEREST_RATE_TYPE);

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public InterestRateFutureLoader(ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.INTEREST_RATE_FUTURE);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    String expiryDate = fieldData.getString(FIELD_FUT_LAST_TRADE_DT);
    String futureTradingHours = fieldData.getString(FIELD_FUT_TRADING_HRS);
    String micExchangeCode = fieldData.getString(FIELD_ID_MIC_PRIM_EXCH);
    String currencyStr = fieldData.getString(FIELD_CRNCY);
    String name = fieldData.getString(FIELD_FUT_LONG_NAME);
    String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    double unitAmount = Double.valueOf(fieldData.getString(FIELD_FUT_VAL_PT));
    

    if (!isValidField(bbgUnique)) {
      s_logger.warn("bbgUnique is null, cannot construct interest rate future security");
      return null;
    }
    if (!isValidField(expiryDate)) {
      s_logger.warn("expiry date is null, cannot construct interest rate future security");
      return null;
    }
    if (!isValidField(futureTradingHours)) {
      s_logger.warn("futures trading hours is null, cannot construct interest rate future security");
      return null;
    }
    if (!isValidField(micExchangeCode)) {
      s_logger.warn("settlement exchange is null, cannot construct interest rate future security");
      return null;
    }
    if (!isValidField(currencyStr)) {
      s_logger.info("currency is null, cannot construct interest rate future security");
      return null;
    }

    Expiry expiry = decodeExpiry(expiryDate, futureTradingHours);
    if (expiry == null) {
      s_logger.info("expiry is null, cannot construct interest rate future security");
      return null;
    }
    
    if (!isValidField(name)) {
      s_logger.info("name is null, cannot construct interest rate future security");
      return null;
    }
    
    Currency currency = Currency.parse(currencyStr);
    String bbgCode = fieldData.getString(FIELD_PARSEKYABLE_DES);
    String bbgCode2 = bbgCode.substring(0, 2); // 2 first char
    String id = BBGCODE_UNDERLYING.get(bbgCode2);
    if (id == null) {
      throw new OpenGammaRuntimeException("Cannot get underlying for future " + bbgCode2);
    }
    ExternalId underlyingIdentifier = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, id);
    InterestRateFutureSecurity security = new InterestRateFutureSecurity(expiry, micExchangeCode, micExchangeCode, currency, unitAmount, underlyingIdentifier);
    security.setName(BloombergDataUtils.removeDuplicateWhiteSpace(name, " "));
    // set identifiers
    parseIdentifiers(fieldData, security);
    return security;
  }

//  private ExternalId parseUnderlyingIdentifier(Currency currency, FudgeMsg fieldData) {
//    ArgumentChecker.notNull(currency, "currency");
//    String name = fieldData.getString(FIELD_FUT_LONG_NAME);
//    String tenor = extractRateTenor(name);
//    String identifierValue = null;
//    if (tenor != null) {
//      identifierValue = s_currency2BBGRateCode.get(currency);
//      if (identifierValue == null) {
//        throw new OpenGammaRuntimeException("cannot parse underlying from currency=" + currency + " name=" + name + " sec des=" + fieldData.getString(FIELD_SECURITY_DES));
//      }
//      if (tenor.startsWith("3MO") || tenor.startsWith("90DAY")) {
//        identifierValue += "0003M";
//      } else if (tenor.startsWith("1MO") || tenor.startsWith("30DAY")) {
//        identifierValue += "0001M";
//      } else {
//        throw new OpenGammaRuntimeException("cannot parse underlying from currency=" + currency + " name=" + name + " sec des=" + fieldData.getString(FIELD_SECURITY_DES));
//      }
//      identifierValue += " Index";
//    } else {
//      throw new OpenGammaRuntimeException("tenor can not be extracted from name = " + name + " for SecDes=" + fieldData.getString(FIELD_SECURITY_DES));
//    }
//    return ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, identifierValue);
//  }
//
//  private String extractRateTenor(String name) {
//    name = name.toUpperCase();
//    Matcher matcher = s_rateTenorPattern.matcher(name);
//    String result = null;
//    if (matcher.find()) {
//      result = BloombergDataUtils.removeDuplicateWhiteSpace(matcher.group(), "");
//    } 
//    return result;
//  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_INTEREST_RATE_FUTURE_FIELDS;
  }

}
