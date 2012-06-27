/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;


/**
 * Provides remote access to a {@link FunctionBlacklistPolicySource}.
 */
public class RemoteFunctionBlacklistPolicySource extends AbstractRemoteClient implements FunctionBlacklistPolicySource {

  public RemoteFunctionBlacklistPolicySource(final URI baseUri) {
    super(baseUri);
  }

  @Override
  public FunctionBlacklistPolicy getPolicy(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    try {
      final FudgeDeserializer fdc = new FudgeDeserializer(getFudgeContext());
      final FudgeMsg response = accessRemote(UriBuilder.fromUri(getBaseUri()).path("uid/{uniqueId}").build(uniqueId.toString())).get(FudgeMsg.class);
      return fdc.fudgeMsgToObject(FunctionBlacklistPolicy.class, response);
    } catch (UniformInterfaceException404NotFound e) {
      return null;
    }
  }

  @Override
  public FunctionBlacklistPolicy getPolicy(final String name) {
    ArgumentChecker.notNull(name, "name");
    try {
      final FudgeDeserializer fdc = new FudgeDeserializer(getFudgeContext());
      final FudgeMsg response = accessRemote(UriBuilder.fromUri(getBaseUri()).path("name/{name}").build(name)).get(FudgeMsg.class);
      return fdc.fudgeMsgToObject(FunctionBlacklistPolicy.class, response);
    } catch (UniformInterfaceException404NotFound e) {
      return null;
    }
  }

}
