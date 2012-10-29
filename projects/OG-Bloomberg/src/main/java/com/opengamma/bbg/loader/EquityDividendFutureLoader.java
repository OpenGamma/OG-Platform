/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUTURES_CATEGORY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LAST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LONG_NAME;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_TRADING_HRS;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_VAL_PT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_MIC_PRIM_EXCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_MARKET_SECTOR_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SETTLE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_UNDL_SPOT_TICKER;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Loads the data for a equity dividend Future from Bloomberg.
 */
public class EquityDividendFutureLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityDividendFutureLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_EQUITY_DIVIDEND_FUTURE_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_MARKET_SECTOR_DES,
      FIELD_FUT_LONG_NAME,
      FIELD_FUT_LAST_TRADE_DT,
      FIELD_FUT_TRADING_HRS,
      FIELD_ID_MIC_PRIM_EXCH, // trading exchange
      FIELD_CRNCY,
//      FIELD_FUTURES_CATEGORY,
//      FIELD_FUT_TRADING_UNITS,
      FIELD_PARSEKYABLE_DES,
      FIELD_SETTLE_DT,
      FIELD_FUTURES_CATEGORY,
//      FIELD_FUT_CONT_SIZE,
      FIELD_UNDL_SPOT_TICKER,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_FUT_VAL_PT));
  
  /**
   * The valid Bloomberg future categories for Equity Dividend Futures
   */
  public static final Set<String> VALID_FUTURE_CATEGORIES = Collections.unmodifiableSet(Sets.newHashSet(
      BloombergConstants.BBG_STOCK_FUTURE_TYPE));

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public EquityDividendFutureLoader(ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.EQUITY_DIVIDEND_FUTURE);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    String marketSectorDes = fieldData.getString(FIELD_MARKET_SECTOR_DES);
    String expiryDate = fieldData.getString(FIELD_FUT_LAST_TRADE_DT);
    String futureTradingHours = fieldData.getString(FIELD_FUT_TRADING_HRS);
    String micExchangeCode = fieldData.getString(FIELD_ID_MIC_PRIM_EXCH);
    String currencyStr = fieldData.getString(FIELD_CRNCY);
    String settleDate = fieldData.getString(FIELD_SETTLE_DT);
    String category = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUTURES_CATEGORY), " ");
//    Double unitNumber = fieldData.getDouble(FIELD_FUT_CONT_SIZE);
//    String unitName = fieldData.getString(FIELD_FUT_TRADING_UNITS);
    String underlyingTicker = fieldData.getString(FIELD_UNDL_SPOT_TICKER);
    String name = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUT_LONG_NAME), " ");
    String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    double unitAmount = Double.valueOf(fieldData.getString(FIELD_FUT_VAL_PT));
    
    if (!isValidField(marketSectorDes)) {
      s_logger.warn("market sector description is null, cannot construct equity dividend future security");
      return null;
    }

    if (!isValidField(bbgUnique)) {
      s_logger.warn("bbgUnique is null, cannot construct equity dividend future security");
      return null;
    }
    if (!isValidField(expiryDate)) {
      s_logger.warn("expiry date is null, cannot construct equity dividend future security");
      return null;
    }
    if (!isValidField(settleDate)) {
      s_logger.warn("settle date is null, cannot construct equity dividend future security");
      return null;
    }
    if (!isValidField(category)) {
      s_logger.warn("futures category is null, cannot construct equity dividend index future security");
      return null;
    }
    if (!isValidField(futureTradingHours)) {
      s_logger.warn("futures trading hours is null, cannot construct equity dividend index future security");
      return null;
    }
    if (!isValidField(micExchangeCode)) {
      s_logger.warn("settlement exchange is null, cannot construct equity dividend future security");
      return null;
    }
    if (!isValidField(currencyStr)) {
      s_logger.info("currency is null, cannot construct equity dividend future security");
      return null;
    }
//    if (!isValidField(unitName)) {
//      s_logger.info("unitName is null, cannot construct equity dividend future security");
//      return null;
//    }
//    if (unitNumber == null) {
//      s_logger.info("unitNumber is null, cannot construct equity dividend future security");
//      return null;
//    }
    ExternalId underlying = null;
    if (underlyingTicker != null) {
      if (BloombergDataUtils.isValidBloombergTicker(underlyingTicker)) {
        underlying = ExternalSchemes.bloombergTickerSecurityId(underlyingTicker);
      } else {
        underlying = ExternalSchemes.bloombergTickerSecurityId(underlyingTicker + " " + marketSectorDes);
      }
    }
    Expiry expiry = decodeExpiry(expiryDate, futureTradingHours);
    if (expiry == null) {
      return null;
    }
    Expiry settle = decodeExpiry(settleDate, futureTradingHours);
    if (settle == null) {
      s_logger.info("Invalid settlement date, cannot construct equity dividend future security");
      return null;
    }
    Currency currency = Currency.parse(currencyStr);
    EquityIndexDividendFutureSecurity security = new EquityIndexDividendFutureSecurity(expiry, micExchangeCode, micExchangeCode, currency, unitAmount, settle.getExpiry(), underlying, category);
    security.setName(name);
    // set identifiers
    parseIdentifiers(fieldData, security);
    return security;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_EQUITY_DIVIDEND_FUTURE_FIELDS;
  }

}
