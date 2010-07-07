/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.financial.security.ManageableSecurityMaster;
import com.opengamma.financial.user.UserResourceDetails;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;
import com.opengamma.financial.user.rest.ClientResource;
import com.opengamma.financial.user.rest.UserResource;
import com.opengamma.financial.user.rest.UsersResource;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Security master which delegates to individual user and client security masters from an underlying
 * {@link UsersResource}.
 */
public class UserSecurityMaster implements SecurityMaster {

  private final UsersResource _underlying;
  
  public UserSecurityMaster(UsersResource underlying) {
    _underlying = underlying;
  }
  
  @Override
  public Set<String> getAllSecurityTypes() {
    Set<String> result = new HashSet<String>();
    for (UserResource user : _underlying.getAllUsers()) {
      for (ClientResource client : user.getClients().getAllClients()) {
        ManageableSecurityMaster securityMaster = client.getSecurities().getSecurityMaster();
        result.addAll(securityMaster.getAllSecurityTypes());
      }
    }
    return result;
  }
  
  private SecurityMaster findSecurityMaster(UniqueIdentifier uid) {
    UserResourceDetails uidDetails = UserUniqueIdentifierUtils.getDetails(uid);
    UserResource userResource = _underlying.getUser(uidDetails.getUsername());
    if (userResource == null) {
      return null;
    }
    ClientResource clientResource = userResource.getClients().getClient(uidDetails.getClientId());
    if (clientResource == null) {
      return null;
    }
    return clientResource.getSecurities().getSecurityMaster();
  }
  
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    SecurityMaster secMaster = findSecurityMaster(uid);
    return secMaster.getSecurity(uid);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    // TODO: improve
    return Collections.singleton(getSecurity(secKey));
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    // TODO: improve
    UniqueIdentifier uid = getUid(secKey);
    return getSecurity(uid);
  }

  private UniqueIdentifier getUid(IdentifierBundle secKey) {
    // TODO: improve
    final String userScheme = UserUniqueIdentifierUtils.getUserScheme();
    String idValue = secKey.getIdentifier(new IdentificationScheme(userScheme));
    return UniqueIdentifier.of(userScheme, idValue);
  }
  
}
