/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collections;
import java.util.Set;

import com.opengamma.id.UniqueId;

/**
 * Implementation of an empty {@link FunctionBlacklistPolicy}.
 */
public class EmptyFunctionBlacklistPolicy extends AbstractFunctionBlacklistPolicy {

  public EmptyFunctionBlacklistPolicy() {
    super(UniqueId.of("com.opengamma.engine.function.blacklist", "EMPTY"));
  }

  @Override
  public Set<Entry> getEntries() {
    return Collections.emptySet();
  }

}
