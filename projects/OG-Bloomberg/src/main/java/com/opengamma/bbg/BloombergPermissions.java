/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import org.apache.shiro.authz.Permission;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;

/**
 * Permissions used to access Bloomberg.
 */
public final class BloombergPermissions {

  /**
   * The EID permission string prefix.
   * The suffix is a Bloomberg EID authorization identifier.
   */
  public static final String BLOOMBERG_PREFIX = "Data:Bloomberg:";
  /**
   * The EID permission string prefix.
   * The suffix is a Bloomberg EID authorization identifier.
   */
  public static final String EID_PREFIX = BLOOMBERG_PREFIX + "EID:";
  /**
   * The live data permission string prefix.
   * The suffix is a Bloomberg identifier, such as a ticker.
   */
  public static final String LIVE_PREFIX = BLOOMBERG_PREFIX + "Live:";
  /**
   * Permission granted to users that allows checking against Bloomberg.
   */
  public static final Permission PERMISSION_BLOOMBERG = AuthUtils.getPermissionResolver().resolvePermission(BLOOMBERG_PREFIX + "view");

  /**
   * Restricted constructor.
   */
  private BloombergPermissions() {
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a permission for an EID.
   * 
   * @param eid  the Bloomberg EID code
   * @return the permission, not null
   */
  public static Permission createEidPermission(int eid) {
    String perm = createEidPermissionString(eid);
    return AuthUtils.getPermissionResolver().resolvePermission(perm);
  }

  /**
   * Creates a permission string for an EID.
   * 
   * @param eid  the Bloomberg EID code
   * @return the permission string, not null
   */
  public static String createEidPermissionString(int eid) {
    return EID_PREFIX + eid;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if a permission string represents an EID.
   * 
   * @param permissionString  the permission string, null returns false
   * @return true if the permission string is a Bloomberg EID permission
   */
  public static boolean isEid(String permissionString) {
    return permissionString != null && permissionString.startsWith(EID_PREFIX);
  }

  /**
   * Extracts the EID from the permission string.
   * 
   * @param permissionString  the permission string, not null
   * @return the Bloomberg EID code
   * @throws IllegalArgumentException if the permission string is not an EID permission
   */
  public static int extractEid(String permissionString) {
    ArgumentChecker.notNull(permissionString, "permissionString");
    if (isEid(permissionString) == false) {
      throw new IllegalArgumentException("Permission string does not represent an EID: " + permissionString);
    }
    String eidStr = permissionString.substring(EID_PREFIX.length());
    try {
      return Integer.parseInt(eidStr);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("Permission string does not represent a valid EID: " + permissionString);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a permission for a live data identifier.
   * 
   * @param bloombergIdentifier  the Bloomberg identifier, such as a ticker, not null
   * @return the permission, not null
   */
  public static Permission createLiveDataIdPermission(String bloombergIdentifier) {
    String perm = createLiveDataIdPermissionString(bloombergIdentifier);
    return AuthUtils.getPermissionResolver().resolvePermission(perm);
  }

  /**
   * Creates a permission string for a live data identifier.
   * 
   * @param bloombergIdentifier  the Bloomberg identifier, such as a ticker, not null
   * @return the permission string, not null
   */
  public static String createLiveDataIdPermissionString(String bloombergIdentifier) {
    ArgumentChecker.notNull(bloombergIdentifier, "bloombergIdentifier");
    return LIVE_PREFIX + bloombergIdentifier;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if a permission string represents live data.
   * 
   * @param permissionString  the permission string, null returns false
   * @return true if the permission string is a Bloomberg live data permission
   */
  public static boolean isLiveDataId(String permissionString) {
    return permissionString != null && permissionString.startsWith(LIVE_PREFIX);
  }

  /**
   * Extracts the live data identifier from the permission string.
   * 
   * @param permissionString  the permission string, not null
   * @return the Bloomberg live data identifier, not null
   * @throws IllegalArgumentException if the permission string is not a live data permission
   */
  public static String extractLiveDataId(String permissionString) {
    ArgumentChecker.notNull(permissionString, "permissionString");
    if (isLiveDataId(permissionString) == false) {
      throw new IllegalArgumentException("Permission string does not represent an EID: " + permissionString);
    }
    String id = permissionString.substring(LIVE_PREFIX.length());
    if (id.isEmpty()) {
      throw new IllegalArgumentException("Permission string does not represent a valid EID: " + permissionString);
    }
    return id;
  }

}
