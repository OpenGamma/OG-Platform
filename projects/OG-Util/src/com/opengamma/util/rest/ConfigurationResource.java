/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.transport.jaxrs.FudgeFieldContainerBrowser;

/**
 * Generic container for configuration objects - e.g. variant properties of a component that need
 * exposure to remote items such as network port numbers or other allocated resources.
 */
@Path("configuration")
public class ConfigurationResource {

  private final FudgeContext _fudgeContext;
  private final Map<Object, Object> _resources;

  public ConfigurationResource(final FudgeContext fudgeContext) {
    this(fudgeContext, new ConcurrentHashMap<Object, Object>());
  }

  public ConfigurationResource(final FudgeContext fudgeContext, final Map<Object, Object> resources) {
    _fudgeContext = fudgeContext;
    _resources = resources;
  }

  protected Map<Object, Object> getResources() {
    return _resources;
  }

  public void addResource(final String name, final Object value) {
    getResources().put(name, value);
  }

  public void addResources(final Map<String, Object> map) {
    getResources().putAll(map);
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  @SuppressWarnings("unchecked")
  private FudgeMsg mapToMessage(final FudgeSerializationContext context, final Map<?, ?> value) {
    final MutableFudgeMsg message = context.newMessage();
    for (Map.Entry<Object, Object> config : ((Map<Object, Object>) value).entrySet()) {
      if (config.getValue() instanceof Map) {
        message.add(config.getKey().toString(), mapToMessage(context, (Map<?, ?>) config.getValue()));
      } else {
        context.addToMessage(message, config.getKey().toString(), null, config.getValue());
      }
    }
    return message;
  }

  @GET
  public FudgeMsgEnvelope getResource() {
    return new FudgeMsgEnvelope(mapToMessage(getFudgeSerializationContext(), getResources()));
  }

  @SuppressWarnings("unchecked")
  @Path("{entry}")
  public Object getResource(@PathParam("entry") final String entry) {
    final Object object = getResources().get(entry);
    if (object == null) {
      return null;
    }
    if (object instanceof Map<?, ?>) {
      return new ConfigurationResource(getFudgeContext(), (Map<Object, Object>) object);
    } else {
      return new FudgeFieldContainerBrowser(getFudgeSerializationContext().objectToFudgeMsg(object));
    }
  }

}
