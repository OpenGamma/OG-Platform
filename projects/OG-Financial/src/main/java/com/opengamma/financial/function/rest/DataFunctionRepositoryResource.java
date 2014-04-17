/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the function repository intended for debugging.
 * <p>
 * This resource receives and processes RESTful calls to the source.
 */
@Path("functionRepository")
public class DataFunctionRepositoryResource extends AbstractDataResource {

  /**
   * The repository.
   */
  private final Supplier<FunctionRepository> _underlying;

  /**
   * Creates the resource, exposing the underlying repository over REST.
   * 
   * @param functionRepo the underlying repository, not null
   */
  public DataFunctionRepositoryResource(final FunctionRepository functionRepo) {
    ArgumentChecker.notNull(functionRepo, "functionRepo");
    _underlying = Suppliers.ofInstance(functionRepo);
  }

  public DataFunctionRepositoryResource(final Supplier<FunctionRepository> functionRepo) {
    ArgumentChecker.notNull(functionRepo, "functionRepo");
    _underlying = functionRepo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the repository.
   * 
   * @return the repository, not null
   */
  public FunctionRepository getFunctionRepository() {
    return _underlying.get();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("functionsByUniqueId")
  public Response getFunctionsByUniqueId() {
    final Collection<FunctionDefinition> allFunctions = getFunctionRepository().getAllFunctions();
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    for (FunctionDefinition function : allFunctions) {
      final MutableFudgeMsg submsg = serializer.newMessage();
      submsg.add("shortName", function.getShortName());
      serializer.addToMessageWithClassHeaders(submsg, "defaultParameters", null, function.getDefaultParameters(), FunctionParameters.class);
      msg.add(function.getUniqueId(), submsg);
    }
    return responseOk(new FudgeMsgEnvelope(msg));
  }

  @GET
  @Path("functionsByShortName")
  public Response getFunctionsByShortName() {
    final Collection<FunctionDefinition> allFunctions = getFunctionRepository().getAllFunctions();
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    for (FunctionDefinition function : allFunctions) {
      final MutableFudgeMsg submsg = serializer.newMessage();
      submsg.add("uniqueId", function.getUniqueId());
      serializer.addToMessageWithClassHeaders(submsg, "defaultParameters", null, function.getDefaultParameters(), FunctionParameters.class);
      msg.add(function.getShortName(), submsg);
    }
    return responseOk(new FudgeMsgEnvelope(msg));
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGetFunctionsByUniqueId(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/functionsByUniqueId");
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGetFunctionsByShortName(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/functionsByShortName");
    return bld.build();
  }

}
