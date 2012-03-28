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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.CalendricalParseException;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergSecuritySource;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataResult;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
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

    ReferenceDataResult refDataResult = _referenceDataProvider.getFields(bloombergKeys, bbgFields);

    if (refDataResult == null) {
      _logger.warn("Bloomberg Reference Data Provider returned NULL result for {}", bloombergKeys);
      return result;
    }

    for (String securityDes : bloombergKeys) {
      PerSecurityReferenceDataResult secResult = refDataResult.getResult(securityDes);
      if (secResult == null) {
        _logger.warn("PerSecurityReferenceDataResult for {} cannot be null", securityDes);
        continue;
      }
      //check no exceptions
      List<String> exceptions = secResult.getExceptions();
      if (exceptions != null && exceptions.size() > 0) {
        for (String msg : exceptions) {
          _logger.warn("Exception looking up {}/{} - {}",
              new Object[] {securityDes, bbgFields, msg });
        }
        continue;
      }
      // check same security was returned
      String refSec = secResult.getSecurity();
      if (!securityDes.equals(refSec)) {
        _logger.warn("Returned security {} not the same as searched security {}", refSec, securityDes);
        continue;
      }
      //get field data
      FudgeMsg fieldData = secResult.getFieldData();
      ManageableSecurity security = createSecurity(fieldData);
      if (security != null) {
        result.put(securityDes, security);
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
    } catch (CalendricalParseException ex) {
      _logger.warn("delivery date not in mm/dd/yyyy format - {}", deliveryDateStr);
      return null;
    }
    return deliveryDate.atStartOfDayInZone(TimeZone.UTC);
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
    } catch (CalendricalParseException ex) {
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
    ZonedDateTime utcDate = DateUtils.getUTCDate(expiryInLocalDate.getYear(), expiryInLocalDate.getMonthOfYear().getValue(), expiryInLocalDate.getDayOfMonth(), closeHr, closeMins);
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
    security.setUniqueId(BloombergSecuritySource.createUniqueId(identifierBundle.getValue(SecurityUtils.BLOOMBERG_BUID)));
    security.setExternalIdBundle(identifierBundle);
  }
  
  protected OptionType getOptionType(String putOrCall) {
    if (putOrCall.equalsIgnoreCase("Call") || putOrCall.equalsIgnoreCase("C")) {
      return OptionType.CALL;
    }
    return OptionType.PUT;
  }

  protected ExternalId buildUnderlyingTicker(String underlingTicker) {
    return SecurityUtils.bloombergTickerSecurityId(underlingTicker.replaceFirst(" (?i)COMB ", " "));
  }

}
