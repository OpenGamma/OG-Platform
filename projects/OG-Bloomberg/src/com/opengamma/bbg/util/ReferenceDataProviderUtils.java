/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
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
      s_logger.info("Reference data for security {} field {} returned null", securityID, fieldID);
      return null;
    }
    return fieldData.getString(fieldID);
  }

  public static String singleFieldSearchIgnoreCache(String securityID, String fieldID, ReferenceDataProvider refDataProvider) {
    ArgumentChecker.notNull(securityID, "securityID");
    ArgumentChecker.notNull(fieldID, "fieldID");
    ArgumentChecker.notNull(refDataProvider, "Reference Data Provider");
    
    Map<String, FudgeMsg> map = refDataProvider.getReferenceDataIgnoreCache(Collections.singleton(securityID), Collections.singleton(fieldID));
    FudgeMsg fieldData = map.get(securityID);
    if (fieldData == null) {
      s_logger.info("Reference data for security {} field {} returned null", securityID, fieldID);
      return null;
    }
    return fieldData.getString(fieldID);
  }

  public static Map<String, String> singleFieldMultiSecuritiesSearch(final Set<String> secIds, final String fieldID,
      ReferenceDataProvider refDataProvider) {
    ArgumentChecker.notNull(secIds, "secIds");
    ArgumentChecker.notNull(fieldID, "fieldID");
    ArgumentChecker.notNull(refDataProvider, "refDataProvider");
    
    return refDataProvider.getReferenceDataValues(secIds, fieldID);
  }

  public static FudgeMsg getFields(String securityDes, Set<String> fields, ReferenceDataProvider refDataProvider) {
    Map<String, FudgeMsg> map = refDataProvider.getReferenceData(Collections.singleton(securityDes), fields);
    return map.get(securityDes);
  }

  public static Map<String, FudgeMsg> getFields(Set<String> bloombergKeys, Set<String> fields, ReferenceDataProvider refDataProvider) {
    ArgumentChecker.notEmpty(bloombergKeys, "bloombergKeys");
    ArgumentChecker.notEmpty(fields, "fields");
    ArgumentChecker.notNull(refDataProvider, "refDataProvider");

    return refDataProvider.getReferenceData(bloombergKeys, fields);
  }

}
