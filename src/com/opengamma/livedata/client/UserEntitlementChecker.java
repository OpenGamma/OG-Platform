/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import com.opengamma.id.IdentificationDomain;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.security.user.User;
import com.opengamma.security.user.UserManager;
import com.opengamma.util.ArgumentChecker;

/**
 * This class checks user permissions against a user database (as represented by
 * {@link com.opengamma.security.user.UserManager}).
 * <p>
 * The permission to check is built as a combination of two strings:
 * <ol>
 * <li>Namespace - for example: MarketData/Bloomberg(</li>
 * <li>Identification domain - for example: BbgUniqueId</li>
 * </ol>
 * Say {@link #isEntitled(String, LiveDataSpecification)} is then called with a
 * <code>LiveDataSpecification</code> containing a mapping BbgUniqueId -&gt;
 * EQ0010169500001000 (which is the Bloomberg unique ID for AAPL). This class
 * combines the namespace and the unique id, and queries <code>UserManager<code> 
 * if the user has permission MarketData/Bloomberg/EQ0010169500001000.
 * <p>
 * If the user for example has <code>Authority</code>
 * MarketData/Bloomberg/EQ&#42;, access is granted. But if the user has no
 * compatible <code>Authority</code>, access is denied.
 * 
 * @author pietari
 */
public class UserEntitlementChecker implements LiveDataEntitlementChecker {

  private final UserManager _userManager;
  private final String _namespace;
  private final IdentificationDomain _identificationDomain;

  /**
   * @param userManager
   *          Used to load users (their permissions really)
   * @param namespace
   *          First part of <code>Authority</code> name to check. Example: 
   *          MarketData/Bloomberg
   * @param identificationDomain
   *          Identifier of securities within the namespace. In the Bloomberg
   *          example, you might pass in com.opengamma.bbg.BloombergConstants.BBG_UNIQUE_SECURITY_ID_KEY_NAME.
   */
  public UserEntitlementChecker(UserManager userManager, String namespace,
      IdentificationDomain identificationDomain) {
    ArgumentChecker.checkNotNull(userManager, "User manager");
    ArgumentChecker.checkNotNull(namespace, "Authority namespace");
    ArgumentChecker.checkNotNull(identificationDomain, "Identification domain");

    _userManager = userManager;

    _namespace = namespace;
    if (_namespace.endsWith("/")) {
      namespace = namespace.substring(0, namespace.length() - 1);
    }

    _identificationDomain = identificationDomain;
  }

  @Override
  public boolean isEntitled(String userName,
      LiveDataSpecification fullyQualifiedSpecification) {
    User user = _userManager.getUser(userName);
    if (user == null) {
      return false;
    }

    String identifier = fullyQualifiedSpecification
        .getIdentifier(_identificationDomain);

    String permission = _namespace + "/" + identifier;
    return user.hasPermission(permission);
  }

}
