/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.beancompare.BeanCompare;
import com.opengamma.util.beancompare.BeanDifference;

/**
 * Static utility methods for using a security master
 */
public class SecurityMasterUtils {
  
  private static final Logger s_logger = LoggerFactory.getLogger(SecurityMasterUtils.class);
  
  /**
   * Adds a security if not present, otherwise creates a new version of an existing security if it's changed (by comparing all fields except the UniqueId
   * @param securityMaster the security master to use when performing the operation
   * @param security the security to add or update
   * @return the persisted security
   */
  public static ManageableSecurity addOrUpdateSecurity(SecurityMaster securityMaster, ManageableSecurity security) {
    return addOrUpdateSecurity(securityMaster, security, false);
  }
  
  /**
   * Adds a security if not present, otherwise creates a new version of an existing security if it's changed (by comparing all fields except the UniqueId
   * @param securityMaster the security master to use when performing the operation
   * @param security the security to add or update
   * @param deleteAndReAdd optionally delete any existing security and start a new version
   * @return the persisted security
   */
  public static ManageableSecurity addOrUpdateSecurity(SecurityMaster securityMaster, ManageableSecurity security, boolean deleteAndReAdd) {
    
    ArgumentChecker.notNull(security, "security");
    BeanCompare beanCompare = new BeanCompare();
    SecuritySearchRequest searchReq = new SecuritySearchRequest();
    ExternalIdSearch idSearch = ExternalIdSearch.of(security.getExternalIdBundle());  // match any one of the IDs
    searchReq.setVersionCorrection(VersionCorrection.ofVersionAsOf(Instant.now())); // valid now
    searchReq.setExternalIdSearch(idSearch);
    searchReq.setFullDetail(true);
    searchReq.setSortOrder(SecuritySearchSortOrder.VERSION_FROM_INSTANT_DESC);
    SecuritySearchResult searchResult = securityMaster.search(searchReq);
    if (deleteAndReAdd) {
      for (ManageableSecurity foundSecurity : searchResult.getSecurities()) {
        securityMaster.remove(foundSecurity.getUniqueId());
      }
    } else {
      for (ManageableSecurity foundSecurity : searchResult.getSecurities()) {
        List<BeanDifference<?>> differences = null;
        if (foundSecurity.getClass().equals(security.getClass())) {
          try {
            differences = beanCompare.compare(foundSecurity, security);
          } catch (Exception e) {
            s_logger.error("Error comparing securities with ID bundle " + security.getExternalIdBundle(), e);
            return null;
          }
        }
        if (differences != null && (differences.isEmpty() || (differences.size() == 1 && differences.get(0).getProperty().propertyType() == UniqueId.class))) {
          // It's already there, don't update or add it
          return foundSecurity;
        } else {
          SecurityDocument updateDoc = new SecurityDocument(security);
          updateDoc.setVersionFromInstant(Instant.now());
          try {
            //updateDoc.setUniqueId(foundSecurity.getUniqueId());
            //return _securityMaster.update(updateDoc).getSecurity();
            UniqueId newId = securityMaster.addVersion(foundSecurity.getUniqueId().getObjectId(), updateDoc);
            security.setUniqueId(newId);
            return security;
          } catch (Throwable t) {
            s_logger.error("Unable to update security " + security.getUniqueId() + ": " + t.getMessage(), t);
            return null;
          }
        }
      }
    }

    // Not found, so add it
    SecurityDocument addDoc = new SecurityDocument(security);
    try {
      SecurityDocument result = securityMaster.add(addDoc);
      return result.getSecurity();
    } catch (Exception e) {
      s_logger.error("Failed to write security " + security + " to the security master", e);
      return null;
    }
  }
}
