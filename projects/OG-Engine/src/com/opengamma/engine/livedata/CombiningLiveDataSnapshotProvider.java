/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.livedata.PendingCombinedLiveDataSubscription.PendingCombinedSubscriptionState;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Combines snapshots from multiple sources to form a requested snapshot. Due to the way in which such an
 * implementation must work, it is designed for use only with a small number of providers.
 */
public class CombiningLiveDataSnapshotProvider implements LiveDataSnapshotProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(CombiningLiveDataSnapshotProvider.class);
  
  private final Set<LiveDataSnapshotProvider> _providers;
  private final Map<Long, Collection<Pair<Long, LiveDataSnapshotProvider>>> _providerSnapshots = new ConcurrentHashMap<Long, Collection<Pair<Long, LiveDataSnapshotProvider>>>();
  private final Set<LiveDataSnapshotListener> _listeners = new CopyOnWriteArraySet<LiveDataSnapshotListener>();
  private final Map<ValueRequirement, PendingCombinedLiveDataSubscription> _pendingSubscriptions = new ConcurrentHashMap<ValueRequirement, PendingCombinedLiveDataSubscription>();

  private class CombinedLiveDataSnapshotListener implements LiveDataSnapshotListener {
    
    private final LiveDataSnapshotProvider _provider;
    
    public CombinedLiveDataSnapshotListener(LiveDataSnapshotProvider provider) {
      _provider = provider;
    }

    @Override
    public void subscriptionFailed(ValueRequirement requirement, String msg) {
      PendingCombinedLiveDataSubscription pendingSubscription = _pendingSubscriptions.get(requirement);
      if (pendingSubscription == null) {
        return;
      }
      processState(pendingSubscription.subscriptionFailed(_provider, msg), pendingSubscription, requirement);
    }

    @Override
    public void subscriptionStopped(ValueRequirement requirement) {
      CombiningLiveDataSnapshotProvider.this.subscriptionStopped(requirement);
    }

    @Override
    public void subscriptionSucceeded(ValueRequirement requirement) {
      PendingCombinedLiveDataSubscription pendingSubscription = _pendingSubscriptions.get(requirement);
      if (pendingSubscription == null) {
        return;
      }
      processState(pendingSubscription.subscriptionSucceeded(_provider), pendingSubscription, requirement);
    }

    @Override
    public void valueChanged(ValueRequirement requirement) {
      CombiningLiveDataSnapshotProvider.this.valueChanged(requirement);
    }
    
    private void processState(PendingCombinedSubscriptionState state, PendingCombinedLiveDataSubscription pendingSubscription, ValueRequirement requirement) {
      switch (state) {
        case FAILURE:
          String msg = StringUtils.join(pendingSubscription.getFailureMessages(), ", ");
          CombiningLiveDataSnapshotProvider.this.subscriptionFailed(requirement, msg);
          break;
        case SUCCESS:
          CombiningLiveDataSnapshotProvider.this.subscriptionSucceeded(requirement);
          break;
      }
    }
    
  }
  
  /**
   * Constructs an instance using the specified providers. The providers must be specified in order of precedence;
   * if a particular provider can satisfy a requested value then this will be returned before falling back to providers
   * specified later.
   * 
   * @param providers  the list of snapshot providers, in order of precedence (i.e. most important first), not null
   */
  public CombiningLiveDataSnapshotProvider(List<LiveDataSnapshotProvider> providers) {
    ArgumentChecker.notNull(providers, "providers");
    _providers = new LinkedHashSet<LiveDataSnapshotProvider>(providers);
    for (LiveDataSnapshotProvider provider : _providers) {
      // We subscribe to the providers on behalf of our listeners, collect the subscription results, and propagate only
      // an overall result.
      provider.addListener(new CombinedLiveDataSnapshotListener(provider));
    }
  }
  
  //--------------------------------------------------------------------------
  @Override
  public void addListener(LiveDataSnapshotListener listener) {
    _listeners.add(listener);
  }

  @Override
  public void addSubscription(UserPrincipal user, ValueRequirement valueRequirement) {
    addSubscription(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    for (ValueRequirement requirement : valueRequirements) {
      _pendingSubscriptions.put(requirement, new PendingCombinedLiveDataSubscription(_providers));
    }
    for (LiveDataSnapshotProvider provider : _providers) {
      provider.addSubscription(user, valueRequirements);
    }
  }

  @Override
  public Object querySnapshot(long snapshot, ValueRequirement requirement) {
    Collection<Pair<Long, LiveDataSnapshotProvider>> providerSnapshots = _providerSnapshots.get(snapshot);
    
    if (providerSnapshots == null) {
      for (LiveDataSnapshotProvider provider : _providers) {
        Object result = provider.querySnapshot(snapshot, requirement);
        if (result != null) {
          return result;
        }
      }
      return null;
    }
    
    for (Pair<Long, LiveDataSnapshotProvider> providerSnapshot : providerSnapshots) {
      Long snapshotTimestamp = providerSnapshot.getFirst();
      LiveDataSnapshotProvider provider = providerSnapshot.getSecond();
      Object result = provider.querySnapshot(snapshotTimestamp, requirement);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public void releaseSnapshot(long snapshot) {
    Collection<Pair<Long, LiveDataSnapshotProvider>> providerSnapshots = _providerSnapshots.remove(snapshot);
    if (providerSnapshots == null) {
      s_logger.warn("Attempted to release snapshot {} which does not exist", snapshot);
      return;
    }
    for (Pair<Long, LiveDataSnapshotProvider> providerSnapshot : providerSnapshots) {
      Long snapshotTimestamp = providerSnapshot.getFirst();
      LiveDataSnapshotProvider provider = providerSnapshot.getSecond();
      provider.releaseSnapshot(snapshotTimestamp);
    }
  }

  @Override
  public long snapshot() {
    Collection<Pair<Long, LiveDataSnapshotProvider>> providerSnapshots = new ArrayList<Pair<Long, LiveDataSnapshotProvider>>();
    for (LiveDataSnapshotProvider provider : _providers) {
      long snapshotTimestamp = provider.snapshot();
      providerSnapshots.add(Pair.of(snapshotTimestamp, provider));
    }
    long overallTimestamp = System.currentTimeMillis();
    _providerSnapshots.put(overallTimestamp, providerSnapshots);
    return overallTimestamp;
  }
  
  @Override
  public long snapshot(long snapshot) {
    Collection<Pair<Long, LiveDataSnapshotProvider>> providerSnapshots = new ArrayList<Pair<Long, LiveDataSnapshotProvider>>();
    for (LiveDataSnapshotProvider provider : _providers) {
      long snapshotTimestamp = provider.snapshot(snapshot);
      providerSnapshots.add(Pair.of(snapshotTimestamp, provider));
    }
    long overallTimestamp = snapshot;
    _providerSnapshots.put(overallTimestamp, providerSnapshots);
    return overallTimestamp;
  }
  
  //--------------------------------------------------------------------------
  private void subscriptionSucceeded(ValueRequirement requirement) {
    for (LiveDataSnapshotListener listener : _listeners) {
      listener.subscriptionSucceeded(requirement);
    }
  }
  
  private void subscriptionFailed(ValueRequirement requirement, String msg) {
    for (LiveDataSnapshotListener listener : _listeners) {
      listener.subscriptionFailed(requirement, msg);
    }
  }
  
  private void subscriptionStopped(ValueRequirement requirement) {
    for (LiveDataSnapshotListener listener : _listeners) {
      listener.subscriptionStopped(requirement);
    }
  }
  
  private void valueChanged(ValueRequirement requirement) {
    for (LiveDataSnapshotListener listener : _listeners) {
      listener.valueChanged(requirement);
    }
  }

}
