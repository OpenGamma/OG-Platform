/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.user;

import java.util.Collection;
import java.util.Collections;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.user.UserResourceDetails;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;
import com.opengamma.financial.user.rest.ClientResource;
import com.opengamma.financial.user.rest.UserResource;
import com.opengamma.financial.user.rest.UsersResource;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.impl.MasterSecuritySource;

/**
 * Security source which delegates to individual user and client security masters from an underlying
 * {@link UsersResource}.
 */
public class UserSecuritySource implements SecuritySource {

  private final UsersResource _underlying;

  public UserSecuritySource(UsersResource underlying) {
    _underlying = underlying;
  }

  private SecuritySource findSecuritySource(UniqueIdentifier uid) {
    UserResourceDetails uidDetails = UserUniqueIdentifierUtils.getDetails(uid);
    UserResource userResource = _underlying.getUser(uidDetails.getUsername());
    if (userResource == null) {
      return null;
    }
    ClientResource clientResource = userResource.getClients().getClient(uidDetails.getClientId());
    if (clientResource == null) {
      return null;
    }
    return new MasterSecuritySource(clientResource.getSecurityMaster());
  }

  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    SecuritySource secMaster = findSecuritySource(uid);
    return secMaster.getSecurity(uid);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    // TODO: improve
    final Security security = getSecurity(secKey);
    if (security != null) {
      return Collections.singleton(security);
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    // TODO: improve
    UniqueIdentifier uid = getUid(secKey);
    if (uid != null) {
      return getSecurity(uid);
    } else {
      return null;
    }
  }

  public String getScheme() {
    return UserUniqueIdentifierUtils.getUserScheme();
  }

  private UniqueIdentifier getUid(IdentifierBundle secKey) {
    // TODO: improve
    final String userScheme = getScheme();
    String idValue = secKey.getIdentifier(IdentificationScheme.of(userScheme));
    if (idValue != null) {
      return UniqueIdentifier.of(userScheme, idValue);
    } else {
      return null;
    }
  }

}
