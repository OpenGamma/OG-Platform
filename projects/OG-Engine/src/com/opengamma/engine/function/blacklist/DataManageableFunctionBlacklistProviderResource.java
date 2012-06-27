/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import org.fudgemsg.FudgeContext;


/**
 * Publishes a {@link ManageableFunctionBlacklistProvider} to remote clients
 */
public class DataManageableFunctionBlacklistProviderResource extends DataFunctionBlacklistProviderResource {

  public DataManageableFunctionBlacklistProviderResource(final ManageableFunctionBlacklistProvider underlying, final FudgeContext fudgeContext) {
    super(underlying, fudgeContext);
  }

  @Override
  protected DataManageableFunctionBlacklistResource createResource(final FunctionBlacklist blacklist) {
    return new DataManageableFunctionBlacklistResource((ManageableFunctionBlacklist) blacklist, getFudgeContext());
  }

}
