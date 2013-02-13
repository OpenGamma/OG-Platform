/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Wrapper to provide RESTful access to a {@link MarketDataInjector}.
 */
public class DataLiveDataInjectorResource extends AbstractDataResource {

  //CSOFF: just constants
  public static final String PATH_ADD = "add";
  public static final String PATH_REMOVE = "remove";
  //CSON: just constants

  private final MarketDataInjector _injector;

  public DataLiveDataInjectorResource(final MarketDataInjector injector) {
    ArgumentChecker.notNull(injector, "injector");
    _injector = injector;
  }

  private ValueRequirement plat3044Hack(final ValueRequirement req) {
    if (req.getTargetReference().getType().isTargetType(ComputationTargetType.PRIMITIVE) && (req.getTargetReference() instanceof ComputationTargetSpecification)) {
      final String valueName = req.getValueName();
      final UniqueId uid = req.getTargetReference().getSpecification().getUniqueId();
      final String scheme = uid.getScheme();
      final String ticker = uid.getValue();
      final ValueProperties constraints = req.getConstraints();
      return new ValueRequirement(valueName, new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of(scheme, ticker)), constraints);
    } else {
      return null;
    }
  }

  @POST
  @Path(PATH_ADD)
  public void put(final AddValueRequest request) {
    ArgumentChecker.notNull(request.getValue(), "value");
    if (request.getValueRequirement() != null) {
      _injector.addValue(request.getValueRequirement(), request.getValue());
      // [PLAT-3044] Tempoary measure to make sure both the unique ID form and external ID form are injected into the value store for primitives until the identifiers are handled correctly
      final ValueRequirement temp = plat3044Hack(request.getValueRequirement());
      if (temp != null) {
        _injector.addValue(temp, request.getValue());
      }
    } else if (request.getIdentifier() != null && request.getValueName() != null) {
      _injector.addValue(request.getIdentifier(), request.getValueName(), request.getValue());
    } else {
      throw new OpenGammaRuntimeException("Invalid request: " + request);
    }
  }

  @POST
  @Path(PATH_REMOVE)
  public void remove(final RemoveValueRequest request) {
    if (request.getValueRequirement() != null) {
      _injector.removeValue(request.getValueRequirement());
      // [PLAT-3044] Tempoary measure to make sure both the unique ID form and external ID form are injected into the value store for primitives until the identifiers are handled correctly
      final ValueRequirement temp = plat3044Hack(request.getValueRequirement());
      if (temp != null) {
        _injector.removeValue(temp);
      }
    } else if (request.getIdentifier() != null && request.getValueName() != null) {
      _injector.removeValue(request.getIdentifier(), request.getValueName());
    } else {
      throw new OpenGammaRuntimeException("Invalid request: " + request);
    }
  }

}
