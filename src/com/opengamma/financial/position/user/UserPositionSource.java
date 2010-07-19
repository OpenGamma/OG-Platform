/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.user;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.financial.user.UserResourceDetails;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;
import com.opengamma.financial.user.rest.ClientResource;
import com.opengamma.financial.user.rest.UserResource;
import com.opengamma.financial.user.rest.UsersResource;
import com.opengamma.id.UniqueIdentifier;

/**
 * Implementation of {@link PositionSource} which delegates to individual user and client {@link PositionSource}s from
 * an underlying {@link UsersResource}.
 * <p>
 * When {@link UsersResource} is backed by a proper UserMaster, this should instead delegate to that.
 */
public class UserPositionSource implements PositionSource {

  private final UsersResource _underlying;
  
  public UserPositionSource(UsersResource underlying) {
    _underlying = underlying;
  }
  
  /**
   * Attempts to find the PositionMaster from which the specified {@link UniqueIdentifier} was created.
   * 
   * @param uid  the unique identifier
   * @return  the position master from which the specified {@link UniqueIdentifier} was created, null if not found
   */
  private PositionSource findPositionMaster(UniqueIdentifier uid) {
    // TODO: replace with searches starting from a UserMaster
    UserResourceDetails uidDetails = UserUniqueIdentifierUtils.getDetails(uid);
    UserResource userResource = _underlying.getUser(uidDetails.getUsername());
    if (userResource == null) {
      return null;
    }
    ClientResource clientResource = userResource.getClients().getClient(uidDetails.getClientId());
    if (clientResource == null) {
      return null;
    }
    return clientResource.getPortfolios().getPositionMaster();
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    PositionSource positionMaster = findPositionMaster(uid);
    if (positionMaster == null) {
      return null;
    }
    return positionMaster.getPortfolio(uid);
  }

  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    Set<UniqueIdentifier> result = new HashSet<UniqueIdentifier>();
    for (UserResource user : _underlying.getAllUsers()) {
      for (ClientResource client : user.getClients().getAllClients()) {
        PositionSource positionMaster = client.getPortfolios().getPositionMaster();
        result.addAll(positionMaster.getPortfolioIds());
      }
    }
    return Collections.unmodifiableSet(result);
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    PositionSource positionMaster = findPositionMaster(uid);
    if (positionMaster == null) {
      return null;
    }
    return positionMaster.getPortfolioNode(uid);
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    PositionSource positionMaster = findPositionMaster(uid);
    if (positionMaster == null) {
      return null;
    }
    return positionMaster.getPosition(uid);
  }
  
}
