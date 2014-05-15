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

}
