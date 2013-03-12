/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.util.ArgumentChecker;

/**
 * Maintains a set of capabilities suitable for a {@link JobInvoker} implementation.
 */
public class CapabilitySet {

  private final Map<String, Capability> _capabilitiesById = new ConcurrentHashMap<String, Capability>();
  private final AtomicInteger _changeId = new AtomicInteger();
  private volatile Wrapper _wrapper;

  private static final class Wrapper implements Collection<Capability> {

    private final Collection<Capability> _underlying;
    private final int _changeId;

    private Wrapper(final Collection<Capability> underlying, final int changeId) {
      _underlying = underlying;
      _changeId = changeId;
    }

    private Collection<Capability> getUnderlying() {
      return _underlying;
    }

    private int getChangeId() {
      return _changeId;
    }

    @Override
    public boolean add(Capability e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Capability> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
      return getUnderlying().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return getUnderlying().containsAll(c);
    }

    @Override
    public boolean isEmpty() {
      return getUnderlying().isEmpty();
    }

    @Override
    public Iterator<Capability> iterator() {
      return getUnderlying().iterator();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
      return getUnderlying().size();
    }

    @Override
    public Object[] toArray() {
      return getUnderlying().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
      return getUnderlying().toArray(a);
    }

  };

  protected Wrapper createWrapper() {
    return new Wrapper(getCapabilitiesById().values(), _changeId.get());
  }

  protected Map<String, Capability> getCapabilitiesById() {
    return _capabilitiesById;
  }

  /**
   * Adds (or updates) a capability within the set. If there is an existing capability with the same identifier it will be replaced.
   * 
   * @param capability the new capability, not null
   */
  public void addCapability(final Capability capability) {
    ArgumentChecker.notNull(capability, "capability");
    final Capability previous = getCapabilitiesById().put(capability.getIdentifier(), capability);
    if (!capability.equals(previous)) {
      _changeId.incrementAndGet();
    }
  }

  public void setCapability(final Capability capability) {
    ArgumentChecker.notNull(capability, "capability");
    getCapabilitiesById().clear();
    getCapabilitiesById().put(capability.getIdentifier(), capability);
    _changeId.incrementAndGet();
  }

  public void addCapabilities(final Collection<Capability> capabilities) {
    ArgumentChecker.notNull(capabilities, "capabilities");
    for (Capability capability : capabilities) {
      getCapabilitiesById().put(capability.getIdentifier(), capability);
    }
    _changeId.incrementAndGet();
  }

  public void setCapabilities(final Collection<Capability> capabilities) {
    ArgumentChecker.notNull(capabilities, "capabilities");
    getCapabilitiesById().clear();
    for (Capability capability : capabilities) {
      getCapabilitiesById().put(capability.getIdentifier(), capability);
    }
    _changeId.incrementAndGet();
  }

  public void setParameterCapability(final String identifier, final double parameter) {
    ArgumentChecker.notNull(identifier, "identifier");
    Capability capability = getCapabilitiesById().get(identifier);
    if (capability != null) {
      if ((capability.getLowerBoundParameter() <= parameter) && (capability.getUpperBoundParameter() >= parameter)) {
        return;
      }
    }
    getCapabilitiesById().put(identifier, Capability.parameterInstanceOf(identifier, parameter));
    _changeId.incrementAndGet();
  }

  /**
   * Returns a read-only view of the capabilities, backed by the main set. Whenever capabilities change,
   * the wrapper is reallocated. It's hashCode and equals are identity based comparisons.
   * 
   * @return the capabilities
   */
  public Collection<Capability> getCapabilities() {
    if ((_wrapper == null) || (_wrapper.getChangeId() != _changeId.get())) {
      _wrapper = createWrapper();
    }
    return _wrapper;
  }

}
