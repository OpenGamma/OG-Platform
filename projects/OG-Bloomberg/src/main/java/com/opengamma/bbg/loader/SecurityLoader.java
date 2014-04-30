/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_FIRST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LAST_TRADE_DT;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeParseException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.BloombergPermissions;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.security.BloombergSecurityProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Abstract base class for loading data from Bloomberg for a specific security type.
 */
public abstract class SecurityLoader {

  /**
   * The logger.
   */
  private final Logger _logger;
  /**
   * The reference data provider.
   */
  private final ReferenceDataProvider _referenceDataProvider;
  /**
   * The security type.
   */
  private final SecurityType _securityType;

  private final FudgeDeserializer _fudgeDeserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
  /**
   * Creates an instance.
   * @param logger  the logger, not null
   * @param referenceDataProvider  the referenceDataProvider, not null
   * @param securityType  the security type, not null
   */
  protected SecurityLoader(final Logger logger, final ReferenceDataProvider referenceDataProvider, final SecurityType securityType) {
    ArgumentChecker.notNull(logger, "logger");
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(referenceDataProvider, "referenceDataProvider");
    _logger = logger;
    _securityType = securityType;
    _referenceDataProvider = referenceDataProvider;
  }
  
  /**
   * Gets the referenceDataProvider.
   * @return the referenceDataProvider
   */
  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
  }

  /**
   * Gets the securityType.
   * @return the securityType
   */
  public SecurityType getSecurityType() {
    return _securityType;
  }

  //-------------------------------------------------------------------------
  /**
   * Parses a security from the response.
   * @param fieldData  the response, not null
   * @return the security, null if unable to create
   */
  protected abstract ManageableSecurity createSecurity(FudgeMsg fieldData);

  /**
   * Gets the fields to request from Bloomberg.
   * @return the set of fields, not null
   */
  protected abstract Set<String> getBloombergFields();

  //-------------------------------------------------------------------------
  /**
   * Loads a set of securities from Bloomberg specific keys.
   * @param bloombergKeys  the Bloomberg specific keys, not altered, not null
   * @return the set of securities, not null
   */
  public Map<String, ManageableSecurity> loadSecurities(Set<String> bloombergKeys) {
    ArgumentChecker.notNull(bloombergKeys, "bloombergKeys");
    validateReferenceDataProvider();
    _logger.info("loading securities for {}", bloombergKeys);
    bloombergKeys = Collections.unmodifiableSet(bloombergKeys); // defensive unmodifiable
    Set<String> bbgFields = getBloombergFields(); // subclasses supply unmodifiable collection

    Map<String, ManageableSecurity> result = new HashMap<String, ManageableSecurity>();
    if (bloombergKeys.isEmpty()) {
      return result;
    }
    
    Map<String, FudgeMsg> refData = _referenceDataProvider.getReferenceData(bloombergKeys, bbgFields);
    for (String securityDes : bloombergKeys) {
      FudgeMsg fieldData = refData.get(securityDes);
      if (fieldData == null) {
        _logger.warn("No reference data for {} cannot be null", securityDes);
        continue;
      }
      // get field data
      try {
        ManageableSecurity security = createSecurity(fieldData);
        if (security != null) {
          result.put(securityDes, security);
          String eidDataName = BloombergConstants.EID_DATA.toString();
          if (fieldData.hasField(eidDataName)) {
            for (FudgeField fudgeField : fieldData.getAllByName(eidDataName)) {
              try {
                Integer eidValue = _fudgeDeserializer.fieldValueToObject(Integer.class, fudgeField);
                security.getRequiredPermissions().add(BloombergPermissions.createEidPermissionString((int) eidValue));
              } catch (Exception ex) {
                _logger.warn("Error converting EID to Integer");
              }
            }
          }
        }
      } catch (Exception e) {
        _logger.error("Exception while trying to create security", e);
      }
    }
    return result;
  }

  private void validateReferenceDataProvider() {
    if (getReferenceDataProvider() == null) {
      throw new IllegalStateException("ReferenceDataProvider is null");
    }
  }

  /**
   * Parses a date string.
   * @param deliveryDateStr  the date string
   * @return the parsed ZonedDateTime, null if cannot parse
   */
  protected ZonedDateTime decodeDeliveryDate(String deliveryDateStr) {
    LocalDate deliveryDate = null;
    try {
      deliveryDate = LocalDate.parse(deliveryDateStr);
    } catch (DateTimeParseException ex) {
      _logger.warn("delivery date not in mm/dd/yyyy format - {}", deliveryDateStr);
      return null;
    }
    return deliveryDate.atStartOfDay(ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------  
  /**
   * Parses the expiry field.
   * @param expiryDate  the expiry string
   * @param futureTradingHours the future trading hours
   * @return the parsed expiry object, null if cannot parse
   */
  protected Expiry decodeExpiry(final String expiryDate, final String futureTradingHours) {
    _logger.debug("decodeExpiry expiryDate={} futureTradingHours={}", expiryDate, futureTradingHours);
    if (expiryDate == null || futureTradingHours == null) {
      _logger.warn("expirydate/futureTradingHours is null, cannot construct expiry");
      return null;
    }
    LocalDate expiryInLocalDate = null;
    try {
      expiryInLocalDate = LocalDate.parse(expiryDate);
    } catch (DateTimeParseException ex) {
      _logger.warn("expiry not in mm/dd/yyyy format - {}", expiryDate);
      return null;
    }
    //expects future trading hours in 07:00-21:00 OR 00:00-19:15 & 15:30-19:15 format
    String splitTradingHours = null;
    if (futureTradingHours.contains("&")) {
      String[] tokens = StringUtils.splitByWholeSeparator(futureTradingHours, "&");
      if (tokens.length != 2) {
        _logger.warn("futureTradingHours not in (hh:mm-hh:mm OR hh:mm-hh:mm & hh:mm-hh:mm) format - {}", futureTradingHours);
        return null;
      }
      splitTradingHours = tokens[1];
    } else {
      splitTradingHours = futureTradingHours;
    }
    int closeHr = 23;
    int closeMins = 59;
    String[] tradingHrsToken = StringUtils.splitByWholeSeparator(splitTradingHours, "-");
    if (tradingHrsToken.length != 2) {
      _logger.warn("futureTradingHours not in (hh:mm-hh:mm OR hh:mm-hh:mm & hh:mm-hh:mm) format - {}", futureTradingHours);
      return null;
    } else {
      String closeTime = tradingHrsToken[1];
      String[] closeTimeTokens = closeTime.split(":");
      if (closeTimeTokens.length != 2) {
        closeTimeTokens = closeTime.split("\\.");
        if (closeTimeTokens.length != 2) {
          _logger.warn("exchange close time not in hh:mm format - {}", futureTradingHours);
          return null;
        }
      } 
      
      try {
        closeHr = Integer.parseInt(closeTimeTokens[0]);
        closeMins = Integer.parseInt(closeTimeTokens[1]);
      } catch (NumberFormatException ex) {
        _logger.warn("Cannot parse futureTrading hours - {}", futureTradingHours);
      }
    }
    ZonedDateTime utcDate = DateUtils.getUTCDate(expiryInLocalDate.getYear(), expiryInLocalDate.getMonthValue(), expiryInLocalDate.getDayOfMonth(), closeHr, closeMins);
    return new Expiry(utcDate, ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
  }
  
  protected ExerciseType getExerciseType(String bbgExerciseType) {
    final ExerciseType result;
    if (bbgExerciseType.equalsIgnoreCase("American")) {
      result = new AmericanExerciseType();
    } else if (bbgExerciseType.equalsIgnoreCase("European")) {
      result = new EuropeanExerciseType();
    } else {
      // an option exercise type we don't support
      _logger.warn("option exercise type {} not currently supported", bbgExerciseType);
      throw new OpenGammaRuntimeException("option exercise type " + bbgExerciseType + " not currently supported");
    }
    return result;
  }

  /**
   * Parse the identifiers from the response.
   * @param fieldData  the response, not null
   * @param security  the security to populate, not null
   */
  protected void parseIdentifiers(final FudgeMsg fieldData, final ManageableSecurity security) {
    parseIdentifiers(fieldData, security, FIELD_FUT_FIRST_TRADE_DT, FIELD_FUT_LAST_TRADE_DT);
  }

  protected void parseIdentifiers(final FudgeMsg fieldData, final ManageableSecurity security, final String firstTradeDateField, final String lastTradeDateField) {
    ExternalIdBundle identifierBundle = BloombergDataUtils.parseIdentifiers(fieldData, firstTradeDateField, lastTradeDateField).toBundle();
    security.setUniqueId(BloombergSecurityProvider.createUniqueId(identifierBundle.getValue(ExternalSchemes.BLOOMBERG_BUID)));
    security.setExternalIdBundle(identifierBundle);
  }
  
  protected OptionType getOptionType(String putOrCall) {
    if (putOrCall.equalsIgnoreCase("Call") || putOrCall.equalsIgnoreCase("C")) {
      return OptionType.CALL;
    }
    return OptionType.PUT;
  }

  protected ExternalId buildUnderlyingTicker(String underlingTicker) {
    return ExternalSchemes.bloombergTickerSecurityId(underlingTicker.replaceFirst(" (?i)COMB ", " "));
  }

}
