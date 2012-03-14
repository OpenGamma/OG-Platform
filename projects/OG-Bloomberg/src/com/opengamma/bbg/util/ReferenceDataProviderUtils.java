/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Utilities for {@link ReferenceDataProvider}.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class ReferenceDataProviderUtils {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ReferenceDataProviderUtils.class);

  /**
   * Restricted constructor.
   */
  private ReferenceDataProviderUtils() {
  }

  /**
   * Gets the BUID field.
   * @param securityIDs  the security IDs
   * @param refDataProvider  the reference data provider
   * @return bbgUniqueID
   */
  public static Map<String, String> getBloombergUniqueIDs(final Set<String> securityIDs, final ReferenceDataProvider refDataProvider) {
    if (securityIDs.isEmpty()) {
      return Collections.emptyMap();
    }
    return singleFieldMultiSecuritiesSearch(securityIDs, FIELD_ID_BBG_UNIQUE, refDataProvider);
  }

  /**
   * Search for a single field.
   * @param securityID  the security ID
   * @param fieldID  the field ID
   * @param refDataProvider  the reference data provider
   * @return the field value
   */
  public static String singleFieldSearch(final String securityID, final String fieldID, final ReferenceDataProvider refDataProvider) {
    ArgumentChecker.notNull(securityID, "securityID");
    ArgumentChecker.notNull(fieldID, "fieldID");
    ArgumentChecker.notNull(refDataProvider, "Reference Data Provider");
    FudgeMsg fieldData = getFields(securityID, Collections.singleton(fieldID), refDataProvider);
    if (fieldData == null) {
      s_logger.info("FudgeFieldContainer for security {} field {} cannot be null", securityID, fieldID);
      return null;
    }
    return fieldData.getString(fieldID);
  }

  public static Map<String, String> singleFieldMultiSecuritiesSearch(final Set<String> secIds, final String fieldID,
      ReferenceDataProvider refDataProvider) {
    ArgumentChecker.notNull(secIds, "secIds");
    ArgumentChecker.notNull(fieldID, "fieldID");
    ArgumentChecker.notNull(refDataProvider, "Reference Data Provider");

    Map<String, String> result = new HashMap<String, String>();

    ReferenceDataResult refDateResult = refDataProvider.getFields(Collections.unmodifiableSet(secIds), Collections
        .singleton(fieldID));
    if (refDateResult == null) {
      s_logger.info("Bloomberg Reference Data Provider returned NULL result for {}", secIds);
      return result;
    }

    for (String securityDes : secIds) {
      PerSecurityReferenceDataResult secResult = refDateResult.getResult(securityDes);
      if (secResult == null) {
        s_logger.info("PerSecurityReferenceDataResult for {} cannot be null", securityDes);
        continue;
      }
      //check no exceptions
      List<String> exceptions = secResult.getExceptions();
      if (exceptions != null && exceptions.size() > 0) {
        for (String msg : exceptions) {
          s_logger.warn("Exception looking up {}/{} - {}", new Object[] {securityDes, fieldID, msg });
        }
        continue;
      }
      // check same security was returned
      String refSec = secResult.getSecurity();
      if (!securityDes.equals(refSec)) {
        s_logger.warn("Returned security {} not the same as searched security {}", refSec, securityDes);
        continue;
      }
      FudgeMsg fieldData = secResult.getFieldData();
      if (fieldData == null) {
        s_logger.info("FudgeFieldContainer for security {} field {} cannot be null", securityDes, fieldID);
        continue;
      }
      result.put(securityDes, fieldData.getString(fieldID));
    }
    return result;
  }

  public static FudgeMsg getFields(String securityDes, Set<String> fields,
      ReferenceDataProvider refDataProvider) {
    ReferenceDataResult result = refDataProvider.getFields(Collections.singleton(securityDes), Collections
        .unmodifiableSet(fields));
    if (result == null) {
      s_logger.info("Bloomberg Reference Data Provider returned NULL result for {}", securityDes);
      return null;
    }
    PerSecurityReferenceDataResult secResult = result.getResult(securityDes);
    if (secResult == null) {
      s_logger.info("PerSecurityReferenceDataResult for {} cannot be null", securityDes);
      return null;
    }
    //check no exceptions
    List<String> exceptions = secResult.getExceptions();
    if (exceptions != null && exceptions.size() > 0) {
      for (String msg : exceptions) {
        s_logger.warn("Exception looking up {}/{} - {}", new Object[] {securityDes, fields, msg });
      }
      return null;
    }
    // check same security was returned
    String refSec = secResult.getSecurity();
    if (!securityDes.equals(refSec)) {
      s_logger.warn("Returned security {} not the same as searched security {}", refSec, securityDes);
      return null;
    }
    //get field data
    return secResult.getFieldData();
  }

  public static Map<String, FudgeMsg> getFields(Set<String> bloombergKeys, Set<String> fields,
      ReferenceDataProvider refDataProvider) {
    ArgumentChecker.notEmpty(bloombergKeys, "BloombeyKeys");
    ArgumentChecker.notEmpty(fields, "BloombergFields");
    ArgumentChecker.notNull(refDataProvider, "ReferenceDataProvider");

    Map<String, FudgeMsg> result = new HashMap<String, FudgeMsg>();
    ReferenceDataResult referenceDataResult = refDataProvider.getFields(bloombergKeys, Collections
        .unmodifiableSet(fields));
    for (String bloombergKey : bloombergKeys) {
      PerSecurityReferenceDataResult secResult = referenceDataResult.getResult(bloombergKey);
      if (secResult == null) {
        s_logger.warn("PerSecurityReferenceDataResult for {} cannot be null", bloombergKey);
        continue;
      }
      //check no exceptions
      List<String> exceptions = secResult.getExceptions();
      if (exceptions != null && exceptions.size() > 0) {
        for (String msg : exceptions) {
          s_logger.warn("Exception looking up {}/{} - {}", new Object[] {bloombergKey, fields, msg });
        }
        continue;
      }
      // check same security was returned
      String refSec = secResult.getSecurity();
      if (!bloombergKey.equals(refSec)) {
        s_logger.warn("Returned security {} not the same as searched security {}", refSec, bloombergKey);
        continue;
      }
      //get field data
      FudgeMsg fieldData = secResult.getFieldData();
      if (fieldData != null) {
        result.put(bloombergKey, fieldData);
      }
    }
    return result;
  }

}
