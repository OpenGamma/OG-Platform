/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import java.net.URI;

import org.threeten.bp.Instant;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides access to a remote {@link FunctionCostsMaster}.
 */
public class RemoteFunctionCostsMaster extends AbstractRemoteClient implements FunctionCostsMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteFunctionCostsMaster(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public FunctionCostsDocument load(String configurationName, String functionId, Instant versionAsOf) {
    ArgumentChecker.notNull(configurationName, "configurationName");
    ArgumentChecker.notNull(functionId, "functionId");
    
    URI uri = DataFunctionCostsMasterResource.uriLoad(getBaseUri(), configurationName, functionId, versionAsOf);
    try {
      return accessRemote(uri).get(FunctionCostsDocument.class);
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public FunctionCostsDocument store(FunctionCostsDocument costs) {
    ArgumentChecker.notNull(costs, "costs");
    
    URI uri = DataFunctionCostsMasterResource.uriStore(getBaseUri());
    return accessRemote(uri).post(FunctionCostsDocument.class, costs);
  }

}
