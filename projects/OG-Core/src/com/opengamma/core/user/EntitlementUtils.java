/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import java.text.MessageFormat;

/**
 * Methods to work with entitlements.
 */
public final class EntitlementUtils {
  /**
   * The separator for different regions in entitlement strings.
   * Put in a constant so that it's easy to change when religious wars break out.
   */
  private static final String SECTION_SEPARATOR = ":";
  private static final String ENTITLEMENT_FORMAT = "{0}{1}" + SECTION_SEPARATOR + "{2}" + SECTION_SEPARATOR + "{3}";
  private EntitlementUtils() {
  }
  
  // --------------------------------------------------------------------------------------
  // COMMON ENTITLEMENT OPERATIONS
  // --------------------------------------------------------------------------------------
  /**
   * Whether a connection is allowed at all.
   */
  public static final String CONNECT = "connect";
  /**
   * Subscribe to a stream
   */
  public static final String SUBSCRIBE = "subscribe";
  /**
   * Snapshot the state of the world
   */
  public static final String SNAPSHOT = "snapshot";
  
  
  public static String generateEntitlementString(boolean isPositive, String operation, String module, String entitlement) {
    String entitlementText =  MessageFormat.format(ENTITLEMENT_FORMAT, new Object[] {
      isPositive ? "" : "-",
      operation,
      module,
      entitlement
    });
    return entitlementText;
  }
  
  /**
   * The result from a call to {@link EntitlementUtils#checkEntitlement(String, String)}.
   */
  public enum EntitlementMatchResult {
    /**
     * The line matches the requirement, and permits it.
     */
    MATCHES_ALLOWED,
    /**
     * The line matches the requirement, and forbids it (e.g. a negative entitlement).
     */
    MATCHES_NOT_ALLOWED,
    /**
     * The line doesn't match the requirement.
     */
    NOT_MATCHED;
  }
  
  /**
   * Check whether a line in a set of entitlements matches a required one.
   * 
   * @param granted The entitlement held by the user.
   * @param checked The entitlement to be checked
   * @return The result of checking the entitlement.
   */
  public static EntitlementMatchResult checkEntitlement(String granted, String checked) {
    // TODO kirk 2012-08-21 -- http://jira.opengamma.com/browse/PLAT-2556
    return EntitlementMatchResult.MATCHES_ALLOWED;
  }
  
  public static boolean userHasEntitlement(OGUser user, String requirement) {
    for (String entitlement : user.getEntitlements()) {
      switch (checkEntitlement(entitlement, requirement)) {
        case MATCHES_NOT_ALLOWED:
          return false;
        case MATCHES_ALLOWED:
          return true;
        default:
          continue;
      }
    }
    // TODO kirk 2012-08-21 -- http://jira.opengamma.com/browse/PLAT-2556
    return true;
  }

}
