/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.rest;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper to provide RESTful access to a {@link LiveDataInjector}.
 */
public class LiveDataInjectorResource {
  
  private final LiveDataInjector _injector;
  
  public LiveDataInjectorResource(LiveDataInjector injector) {
    ArgumentChecker.notNull(injector, "injector");
    _injector = injector;
  }

  @Path("{valueName}/{targetType}/{uniqueIdentifier}")
  public LiveDataInjectorValueResource get(
      @PathParam("valueName") String valueName,
      @PathParam("targetType") String targetTypeName,
      @PathParam("uniqueIdentifier") String uidString) {
    ComputationTargetType targetType = Enum.valueOf(ComputationTargetType.class, targetTypeName);
    UniqueIdentifier uid = UniqueIdentifier.parse(uidString);
    ValueRequirement valueRequirement = new ValueRequirement(valueName, new ComputationTargetSpecification(targetType, uid));
    return new LiveDataInjectorValueResource(_injector, valueRequirement);
  }
  
}
