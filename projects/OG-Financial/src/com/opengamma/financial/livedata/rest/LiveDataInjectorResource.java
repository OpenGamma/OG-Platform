/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper to provide RESTful access to a {@link LiveDataInjector}.
 */
public class LiveDataInjectorResource {
  
  //CSOFF: just constants
  public static final String PATH_ADD = "add";
  public static final String PATH_REMOVE = "remove";
  //CSON: just constants
  
  private final LiveDataInjector _injector;
  
  public LiveDataInjectorResource(LiveDataInjector injector) {
    ArgumentChecker.notNull(injector, "injector");
    _injector = injector;
  }

  @POST
  @Path(PATH_ADD)
  public void put(AddValueRequest request) {
    ArgumentChecker.notNull(request.getValue(), "value");
    if (request.getValueRequirement() != null) {
      _injector.addValue(request.getValueRequirement(), request.getValue());
    } else if (request.getIdentifier() != null && request.getValueName() != null) {
      _injector.addValue(request.getIdentifier(), request.getValueName(), request.getValue());
    } else {
      throw new OpenGammaRuntimeException("Invalid request: " + request);
    }
  }
  
  @POST
  @Path(PATH_REMOVE)
  public void remove(RemoveValueRequest request) {
    if (request.getValueRequirement() != null) {
      _injector.removeValue(request.getValueRequirement());
    } else if (request.getIdentifier() != null && request.getValueName() != null) {
      _injector.removeValue(request.getIdentifier(), request.getValueName());
    } else {
      throw new OpenGammaRuntimeException("Invalid request: " + request);
    }
  }
  
}
