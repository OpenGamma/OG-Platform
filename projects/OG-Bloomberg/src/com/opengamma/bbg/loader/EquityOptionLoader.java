/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_EQUITY_OPTION_SECURITY_TYPE;
import static com.opengamma.bbg.BloombergConstants.FIELD_EXCH_CODE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_MARKET_SECTOR_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPTION_ROOT_TICKER;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_EXERCISE_TYP;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_EXPIRE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_PUT_CALL;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_STRIKE_PX;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_TICK_VAL;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_UNDERLYING_SECURITY_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_UNDL_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_DES;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergSecuritySource;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Loads the data for an Equity Option from Bloomberg.
 */
public class EquityOptionLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_EQUITY_OPTION_FIELDS = ImmutableSet.of(
      FIELD_OPTION_ROOT_TICKER,
      FIELD_EXCH_CODE,
      FIELD_SECURITY_DES,
      FIELD_MARKET_SECTOR_DES,
      FIELD_OPT_EXERCISE_TYP,
      FIELD_OPT_STRIKE_PX,
      FIELD_OPT_PUT_CALL,
      FIELD_OPT_UNDERLYING_SECURITY_DES,
      FIELD_OPT_UNDL_CRNCY,
      FIELD_OPT_EXPIRE_DT,
      FIELD_ID_BBG_UNIQUE,
      FIELD_OPT_TICK_VAL);
  
  /**
   * The valid Bloomberg security types for EquityOption
   */
  public static final Set<String> VALID_SECURITY_TYPES = ImmutableSet.of(BLOOMBERG_EQUITY_OPTION_SECURITY_TYPE);
  
  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public EquityOptionLoader(final ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.EQUITY_OPTION);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    String rootTicker = fieldData.getString(FIELD_OPTION_ROOT_TICKER);
    String exchange = fieldData.getString(FIELD_EXCH_CODE);
    String optionExerciseType = fieldData.getString(FIELD_OPT_EXERCISE_TYP);
    double optionStrikePrice = fieldData.getDouble(FIELD_OPT_STRIKE_PX);
    double pointValue = fieldData.getDouble(FIELD_OPT_TICK_VAL);
    String putOrCall = fieldData.getString(FIELD_OPT_PUT_CALL);
    String underlingTicker = fieldData.getString(FIELD_OPT_UNDERLYING_SECURITY_DES);
    String currency = fieldData.getString(FIELD_OPT_UNDL_CRNCY);
    String expiryDate = fieldData.getString(FIELD_OPT_EXPIRE_DT);
    String bbgUniqueID = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    String secDes = fieldData.getString(FIELD_SECURITY_DES);
    String marketSector = fieldData.getString(FIELD_MARKET_SECTOR_DES);
    
    if (!BloombergDataUtils.isValidField(bbgUniqueID)) {
      s_logger.warn("bloomberg UniqueID is missing, cannot construct equityOption security");
      return null;
    }
    if (!BloombergDataUtils.isValidField(rootTicker)) {
      s_logger.warn("option root ticker is missing, cannot construct equityOption security");
      return null;
    }
    if (!BloombergDataUtils.isValidField(putOrCall)) {
      s_logger.warn("option type is missing, cannot construct equityOption security");
      return null;
    }
    if (!BloombergDataUtils.isValidField(exchange)) {
      s_logger.warn("exchange is missing, cannot construct equityOption security");
      return null;
    }
    if (!BloombergDataUtils.isValidField(expiryDate)) {
      s_logger.warn("option expiry date is missing, cannot construct equityOption security");
      return null;
    }
    if (!BloombergDataUtils.isValidField(underlingTicker)) {
      s_logger.warn("option underlying ticker is missing, cannot construct equityOption security");
      return null;
    }
    if (!BloombergDataUtils.isValidField(currency)) {
      s_logger.warn("option currency is missing, cannot construct equityOption security");
      return null;
    }
    if (!BloombergDataUtils.isValidField(optionExerciseType)) {
      s_logger.warn("option exercise type is missing, cannot construct equityOption security");
      return null;
    }
    OptionType optionType = getOptionType(putOrCall);
    //get year month day from expiryDate in the yyyy-mm-dd format
    LocalDate expiryLocalDate = null;
    try {
      expiryLocalDate = LocalDate.parse(expiryDate);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException(expiryDate + " returned from bloomberg not in format yyyy-mm-dd", e);
    }
    int year = expiryLocalDate.getYear();
    int month = expiryLocalDate.getMonthOfYear().getValue();
    int day = expiryLocalDate.getDayOfMonth();
    Expiry expiry = new Expiry(DateUtils.getUTCDate(year, month, day));

    Currency ogCurrency = Currency.parse(currency);
    
    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId(bbgUniqueID));
    if (BloombergDataUtils.isValidField(secDes) && BloombergDataUtils.isValidField(marketSector)) {
      identifiers.add(SecurityUtils.bloombergTickerSecurityId(secDes + " " + marketSector));
    }
    
    final ExerciseType exerciseType = getExerciseType(optionExerciseType);
    
    final EquityOptionSecurity security = new EquityOptionSecurity(
        optionType, 
        optionStrikePrice, 
        ogCurrency, 
        SecurityUtils.bloombergTickerSecurityId(underlingTicker),
        exerciseType, 
        expiry, 
        pointValue, 
        exchange);
    security.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    security.setUniqueId(BloombergSecuritySource.createUniqueId(bbgUniqueID));
    //build option display name
    StringBuilder buf = new StringBuilder(rootTicker);
    buf.append(" ");
    buf.append(expiryDate);
    if (optionType == OptionType.CALL) {
      buf.append(" C ");
    } else {
      buf.append(" P ");
    }
    buf.append(optionStrikePrice);
    security.setName(buf.toString());
    return security;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_EQUITY_OPTION_FIELDS;
  }

}
