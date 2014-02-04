/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.auth;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.threeten.bp.Clock;
import com.opengamma.auth.master.portfolio.PortfolioCapability;
import com.opengamma.auth.master.portfolio.PortfolioEntitlement;
import com.opengamma.core.user.OGEntitlement;
import com.opengamma.core.user.OGUser;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.id.ObjectId;
import com.opengamma.master.user.ManageableOGRole;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public class UserManager {

  private final UserMaster _userMaster;
  private final RoleMaster _roleMaster;

  private Clock _timeSource = Clock.systemUTC();
  private static final Long CAPABILITY_REVOCATION_TIMEOUT = 60 * 60L; // one hour

  public UserManager(UserMaster userMaster, RoleMaster roleMaster) {
    _userMaster = userMaster;
    _roleMaster = roleMaster;
  }

  public boolean authenticateUser(String userId, String password) {
    UserSearchResult userSearchResult = _userMaster.search(UserSearchRequest.byUserId(userId));
    if (userSearchResult.getUsers().isEmpty()) {
      return false;
    } else {
      ManageableOGUser user = userSearchResult.getFirstUser();
      return authenticateUser(user, password);
    }
  }

  public boolean authenticateUser(OGUser user, String password) {
    return Signer.verifyPassword(password, user.getPasswordHash());
  }

  public PortfolioCapability getEntitledPortfolios(ManageableOGUser user) {

    List<ManageableOGRole> roles = _roleMaster.search(RoleSearchRequest.byUserUid(user.getUniqueId())).getRoles();

    Collection<SignedMessage<PortfolioEntitlement>> messages = newArrayList();

    for (ManageableOGRole role : roles) {
      Set<OGEntitlement> entitlements = role.getEntitlements();
      for (OGEntitlement entitlement : entitlements) {

        ResourceAccess access = entitlement.getAccess();
        final PortfolioEntitlement portfolioEntitlement;
        if (entitlement.getResourceId().equals(GLOBAL_PORTFOLIO)) {
          portfolioEntitlement = PortfolioEntitlement.globalPortfolioEntitlement(_timeSource.instant().plusSeconds(CAPABILITY_REVOCATION_TIMEOUT), access);
        } else {
          portfolioEntitlement = PortfolioEntitlement.singlePortfolioEntitlement(ObjectId.parse(entitlement.getResourceId()), _timeSource.instant().plusSeconds(CAPABILITY_REVOCATION_TIMEOUT), access);
        }

        SignedMessage<PortfolioEntitlement> message = Signer.sign(portfolioEntitlement);
        messages.add(message);
      }
    }
    return PortfolioCapability.of(messages);
  }

  private static final String GLOBAL_PORTFOLIO = "portfolio";

}

