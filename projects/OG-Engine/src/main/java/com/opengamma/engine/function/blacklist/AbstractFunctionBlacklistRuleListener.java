/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;

import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Partial implementation of {@link FunctionBlacklistRuleListener}. A full set of rules is queried initially and whenever the modification count changes unexpectedly. The listener can only be
 * associated with a single blacklist which must be specified at construction.
 */
public abstract class AbstractFunctionBlacklistRuleListener extends BaseFunctionBlacklistRuleListener {

  private final FunctionBlacklist _blacklist;

  protected AbstractFunctionBlacklistRuleListener(final FunctionBlacklist blacklist) {
    _blacklist = blacklist;
  }

  protected FunctionBlacklist getBlacklist() {
    return _blacklist;
  }

  protected void init() {
    init(getBlacklist().getModificationCount(), getBlacklist().getRules());
  }

  @Override
  protected Pair<Integer, ? extends Collection<FunctionBlacklistRule>> getUnderlyingRules(final int modificationCount) {
    final int mc = getBlacklist().getModificationCount();
    if (mc == modificationCount) {
      return null;
    }
    return Pairs.of((Integer) mc, getBlacklist().getRules());
  }

}
