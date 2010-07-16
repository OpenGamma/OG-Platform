/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueIdentifierSchemeDelegator;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * A {@link LiveDataSnapshotProvider} implementation which allows the scheme of any requested {@link ValueRequirement}
 * to control which underlying {@code LiveDataSnapshotProvider} is used for subscriptions. If the scheme is not recognized,
 * a default is used. When a snapshot is taken, a composite of all underlying {@code LiveDataSnapshotProvider}s is taken.
 */
public class DelegatingLiveDataSnapshotProvider extends UniqueIdentifierSchemeDelegator<LiveDataSnapshotProvider> implements
    LiveDataSnapshotProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(DelegatingLiveDataSnapshotProvider.class);

  private class ProviderImpl extends AbstractLiveDataSnapshotProvider {

    private final Map<Long, Map<LiveDataSnapshotProvider, Long>> _snapshots = new ConcurrentHashMap<Long, Map<LiveDataSnapshotProvider, Long>>();

    @Override
    public void addSubscription(final UserPrincipal user, final ValueRequirement valueRequirement) {
      chooseDelegate(valueRequirement).addSubscription(user, valueRequirement);
    }

    @Override
    public void addSubscription(final UserPrincipal user, final Set<ValueRequirement> valueRequirements) {
      for (ValueRequirement valueRequirement : valueRequirements) {
        addSubscription(user, valueRequirement);
      }
    }

    @Override
    public Object querySnapshot(long snapshot, ValueRequirement requirement) {
      final Map<LiveDataSnapshotProvider, Long> providerMap = _snapshots.get(snapshot);
      if (providerMap == null) {
        return null;
      } else {
        final LiveDataSnapshotProvider delegate = chooseDelegate(requirement);
        final Long delegateSnapshotId = providerMap.get(delegate);
        return delegate.querySnapshot(delegateSnapshotId, requirement);
      }
    }

    @Override
    public void releaseSnapshot(long snapshot) {
      final Map<LiveDataSnapshotProvider, Long> providerMap = _snapshots.remove(snapshot);
      if (providerMap != null) {
        for (Map.Entry<LiveDataSnapshotProvider, Long> provider : providerMap.entrySet()) {
          provider.getKey().releaseSnapshot(provider.getValue());
        }
      }
    }

    @Override
    public long snapshot() {
      // The providerMap is created here and never modified so isn't the concurrent version 
      final Map<LiveDataSnapshotProvider, Long> providerMap = new HashMap<LiveDataSnapshotProvider, Long>();
      LiveDataSnapshotProvider defaultDelegate = getDefaultDelegate();
      providerMap.put(defaultDelegate, defaultDelegate.snapshot());
      for (LiveDataSnapshotProvider delegate : getDelegates().values()) {
        providerMap.put(delegate, delegate.snapshot());
      }
      final long snapshotTime = System.currentTimeMillis();
      _snapshots.put(snapshotTime, providerMap);
      return snapshotTime;
    }

  }

  private class ListenerImpl implements LiveDataSnapshotListener {

    @Override
    public void subscriptionFailed(final ValueRequirement requirement, final String msg) {
      getSnapshotProvider().subscriptionFailed(requirement, msg);
    }

    @Override
    public void subscriptionStopped(final ValueRequirement requirement) {
      getSnapshotProvider().subscriptionStopped(requirement);
    }

    @Override
    public void subscriptionSucceeded(final ValueRequirement requirement) {
      getSnapshotProvider().subscriptionSucceeded(requirement);
    }

    @Override
    public void valueChanged(final ValueRequirement requirement) {
      getSnapshotProvider().valueChanged(requirement);
    }

  }

  private final ProviderImpl _snapshotProvider = new ProviderImpl();
  private final ListenerImpl _snapshotListener = new ListenerImpl();

  /**
   * @param defaultDelegate default underlying provider
   */
  protected DelegatingLiveDataSnapshotProvider(final LiveDataSnapshotProvider defaultDelegate) {
    super(defaultDelegate);
    defaultDelegate.addListener(getSnapshotListener());
  }

  protected DelegatingLiveDataSnapshotProvider(final LiveDataSnapshotProvider defaultDelegate,
      final Map<String, LiveDataSnapshotProvider> delegates) {
    super(defaultDelegate, delegates);
    defaultDelegate.addListener(getSnapshotListener());
  }

  private ProviderImpl getSnapshotProvider() {
    return _snapshotProvider;
  }

  private ListenerImpl getSnapshotListener() {
    return _snapshotListener;
  }

  @Override
  public void registerDelegate(final String scheme, final LiveDataSnapshotProvider delegate) {
    super.registerDelegate(scheme, delegate);
    delegate.addListener(getSnapshotListener());
  }

  private LiveDataSnapshotProvider chooseDelegate(final ValueRequirement valueRequirement) {
    return chooseDelegate(valueRequirement.getTargetSpecification().getUniqueIdentifier());
  }

  @Override
  public void addListener(final LiveDataSnapshotListener listener) {
    getSnapshotProvider().addListener(listener);
  }

  @Override
  public void addSubscription(final UserPrincipal user, final ValueRequirement valueRequirement) {
    s_logger.debug("addSubscription {}, {}", user, valueRequirement);
    getSnapshotProvider().addSubscription(user, valueRequirement);
  }

  @Override
  public void addSubscription(final UserPrincipal user, final Set<ValueRequirement> valueRequirements) {
    s_logger.debug("addSubscription {}, {}", user, valueRequirements);
    getSnapshotProvider().addSubscription(user, valueRequirements);
  }

  @Override
  public Object querySnapshot(final long snapshot, final ValueRequirement requirement) {
    //s_logger.debug("querySnapshot {}, {}", snapshot, requirement);
    final Object o = getSnapshotProvider().querySnapshot(snapshot, requirement);
    s_logger.debug("querySnapshot {} = {}", requirement, o);
    return o;
  }

  @Override
  public void releaseSnapshot(final long snapshot) {
    s_logger.debug("releaseSnapshot {}", snapshot);
    getSnapshotProvider().releaseSnapshot(snapshot);
  }

  @Override
  public long snapshot() {
    long timestamp = getSnapshotProvider().snapshot();
    s_logger.debug("snapshot = {}", timestamp);
    return timestamp;
  }

}
