/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.client.DistributedSpecificationResolver;
import com.opengamma.livedata.resolver.IdResolverServer;
import com.opengamma.livedata.resolver.IdentityIdResolver;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;

/**
 * 
 *
 * @author pietari
 */
public class DistributedSpecificationResolverTest {
  
  @Test
  public void testRequestResponse() {
    
    IdentityIdResolver delegate = new IdentityIdResolver();
    IdResolverServer server = new IdResolverServer(delegate); 
    
    FudgeRequestDispatcher fudgeRequestDispatcher = new FudgeRequestDispatcher(server);
    InMemoryByteArrayRequestConduit inMemoryByteArrayRequestConduit = new InMemoryByteArrayRequestConduit(fudgeRequestDispatcher);
    ByteArrayFudgeRequestSender fudgeRequestSender = new ByteArrayFudgeRequestSender(inMemoryByteArrayRequestConduit);
    
    DistributedSpecificationResolver client = new DistributedSpecificationResolver(fudgeRequestSender);
    
    LiveDataSpecification testSpec = new LiveDataSpecification("test1", new Identifier(new IdentificationScheme("test1"), "test1"));
    LiveDataSpecification resolvedSpec = client.resolve(testSpec);
    assertEquals(resolvedSpec, testSpec);
    
  }

}
