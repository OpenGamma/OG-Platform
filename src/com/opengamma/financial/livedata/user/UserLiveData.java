/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.user;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotListener;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.user.UserResourceDetails;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;
import com.opengamma.financial.user.rest.ClientResource;
import com.opengamma.financial.user.rest.UserResource;
import com.opengamma.financial.user.rest.UsersResource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * A Live Data provider for user supplied data.
 */
public class UserLiveData implements LiveDataAvailabilityProvider, LiveDataSnapshotProvider {

  private final UsersResource _userData;
  private final Set<LiveDataSnapshotListener> _listeners = new HashSet<LiveDataSnapshotListener>();

  public UserLiveData(final UsersResource userData) {
    _userData = userData;
  }

  private UsersResource getUserData() {
    return _userData;
  }

  private Set<LiveDataSnapshotListener> getListeners() {
    return _listeners;
  }

  private InMemoryUserSnapshotProvider findUserSnapshotProvider(final ValueRequirement valueRequirement) {
    return findUserSnapshotProvider(valueRequirement.getTargetSpecification().getUniqueIdentifier());
  }

  private InMemoryUserSnapshotProvider findUserSnapshotProvider(final UniqueIdentifier uid) {
    UserResourceDetails uidDetails = UserUniqueIdentifierUtils.getDetails(uid);
    UserResource userResource = getUserData().getUser(uidDetails.getUsername());
    if (userResource == null) {
      return null;
    }
    ClientResource clientResource = userResource.getClients().getClient(uidDetails.getClientId());
    if (clientResource == null) {
      return null;
    }
    return clientResource.getLiveDataResource().getLiveData();
  }

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    return findUserSnapshotProvider(requirement).isAvailable(requirement);
  }

  @Override
  public void addListener(LiveDataSnapshotListener listener) {
    getListeners().add(listener);
  }

  @Override
  public void addSubscription(UserPrincipal user, ValueRequirement valueRequirement) {
    // No action - all values provided externally
  }

  @Override
  public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    for (ValueRequirement valueRequirement : valueRequirements) {
      addSubscription(user, valueRequirement);
    }
  }

  @Override
  public Object querySnapshot(long snapshot, ValueRequirement requirement) {
    return findUserSnapshotProvider(requirement).querySnapshot(snapshot, requirement);
  }

  @Override
  public void releaseSnapshot(long snapshot) {
    // TODO: propogate to ALL user live data stores
  }

  @Override
  public long snapshot() {
    // TODO: propogate call to ALL user live data stores
    return 0;
  }

}
