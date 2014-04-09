/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import java.text.MessageFormat;

import com.opengamma.util.ArgumentChecker;

/**
 * Utilities for managing entitlements.
 * <p>
 * Entitlements are a set of patterns that control what a user can access.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class EntitlementUtils {

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

  /**
   * The separator for different regions in entitlement strings.
   * Put in a constant so that it's easy to change when religious wars break out.
   */
  private static final String SECTION_SEPARATOR = ":";
  /**
   * The entitlement format.
   */
  private static final String ENTITLEMENT_FORMAT = "{0}{1}" + SECTION_SEPARATOR + "{2}" + SECTION_SEPARATOR + "{3}";

  /**
   * Restricted constructor.
   */
  private EntitlementUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Generates an entitlement string from the standard arguments.
   * 
   * @param isPositive  true if the entitlement is allowed, false if not
   * @param operation  the operation, not null
   * @param module  the module, not null
   * @param entitlement  the entitlement, not null
   * @return the complete string, not null
   */
  public static String generateEntitlementString(boolean isPositive, String operation, String module, String entitlement) {
    String entitlementText =  MessageFormat.format(ENTITLEMENT_FORMAT, new Object[] {
      isPositive ? "" : "-",
      operation,
      module,
      entitlement
    });
    return entitlementText;
  }

  //-------------------------------------------------------------------------
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
   * @param userEntitlement  the entitlement held by the user, not null
   * @param required  the entitlement to be checked, not null
   * @return the result of checking the entitlement, not null
   */
  public static EntitlementMatchResult checkEntitlement(String userEntitlement, String required) {
    ArgumentChecker.notNull(userEntitlement, "userEntitlement");
    ArgumentChecker.notNull(required, "required");
    // TODO kirk 2012-08-21 -- http://jira.opengamma.com/browse/PLAT-2556
    return EntitlementMatchResult.MATCHES_ALLOWED;
  }

  /**
   * Checks if a user has the requirement.
   * 
   * @param user  the user, not null
   * @param requirement  the requirement needed, not null
   * @return true if access is permitted, false if not permitted
   */
  public static boolean userHasEntitlement(UserAccount user, String requirement) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(requirement, "requirement");
    for (String permission : user.getPermissions()) {
      switch (checkEntitlement(permission, requirement)) {
        case MATCHES_NOT_ALLOWED:
          return false;
        case MATCHES_ALLOWED:
          return true;
        default:
          continue;
      }
    }
    // TODO kirk 2012-08-21 -- http://jira.opengamma.com/browse/PLAT-2556
    return true;  // TODO: should default to false I assume...
  }

}
