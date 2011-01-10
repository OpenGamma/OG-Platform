/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;

import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper to provide RESTful access to a particular {@link ValueRequirement} from a {@link LiveDataInjector}.
 */
public class LiveDataInjectorValueResource {

  private final LiveDataInjector _injector;
  private final ValueRequirement _valueRequirement;
  
  public LiveDataInjectorValueResource(LiveDataInjector injector, ValueRequirement valueRequirement) {
    ArgumentChecker.notNull(injector, "injector");
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    _injector = injector;
    _valueRequirement = valueRequirement;
  }
  
  @PUT
  @Consumes(FudgeRest.MEDIA)
  public void put(Double value) {
    ArgumentChecker.notNull(value, "value");
    _injector.addValue(_valueRequirement, value);
  }
  
  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public void delete() {
    _injector.removeValue(_valueRequirement);
  }
  
}
