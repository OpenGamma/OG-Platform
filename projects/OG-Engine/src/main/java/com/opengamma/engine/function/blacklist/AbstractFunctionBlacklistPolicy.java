/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of {@link FunctionBlacklistPolicy}.
 */
public abstract class AbstractFunctionBlacklistPolicy implements FunctionBlacklistPolicy {

  private final UniqueId _uniqueId;
  private final String _name;
  private final int _ttl;

  public AbstractFunctionBlacklistPolicy(final UniqueId uniqueId) {
    this(uniqueId, uniqueId.getValue());
  }

  public AbstractFunctionBlacklistPolicy(final UniqueId uniqueId, final String name) {
    this(uniqueId, name, 3600);
  }

  public AbstractFunctionBlacklistPolicy(final UniqueId uniqueId, final String name, final int ttl) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNegativeOrZero(ttl, "ttl");
    _uniqueId = uniqueId;
    _name = name;
    _ttl = ttl;
  }

  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int getDefaultEntryActivationPeriod() {
    return _ttl;
  }

  @Override
  public boolean isEmpty() {
    return getEntries().isEmpty();
  }

  /**
   * Tests if two policies are equal based on their names, the entries and the default activation period.
   * 
   * @param a first policy to test, not null
   * @param b second policy to test, not null
   * @return true if the policies are equal, false otherwise
   */
  public static boolean equals(final FunctionBlacklistPolicy a, final FunctionBlacklistPolicy b) {
    if (a == b) {
      return true;
    }
    if (a.getDefaultEntryActivationPeriod() != b.getDefaultEntryActivationPeriod()) {
      return false;
    }
    if (!a.getName().equals(b.getName())) {
      return false;
    }
    return a.getEntries().equals(b.getEntries());
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof FunctionBlacklistPolicy)) {
      return false;
    }
    return equals(this, (FunctionBlacklistPolicy) o);
  }

  /**
   * Produces a hash code of the policy entries, name and default activation period.
   * 
   * @param policy the policy to hash, not null
   * @return the hash code
   */
  public static int hashCode(final FunctionBlacklistPolicy policy) {
    int hc = 1;
    hc += hc * 16 + policy.getDefaultEntryActivationPeriod();
    hc += hc * 16 + policy.getName().hashCode();
    hc += hc * 16 + policy.getEntries().hashCode();
    return hc;
  }

  @Override
  public int hashCode() {
    return hashCode(this);
  }

}
