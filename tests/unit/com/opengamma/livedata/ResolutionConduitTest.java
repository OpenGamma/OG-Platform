/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.livedata.client.DistributedSpecificationResolver;
import com.opengamma.livedata.client.IdentitySpecificationResolver;
import com.opengamma.livedata.server.SpecificationResolverServer;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;

/**
 * 
 *
 * @author pietari
 */
public class ResolutionConduitTest {
  
  @Test
  public void testRequestResponse() {
    
    IdentitySpecificationResolver delegate = new IdentitySpecificationResolver();
    SpecificationResolverServer server = new SpecificationResolverServer(delegate); 
    
    FudgeRequestDispatcher fudgeRequestDispatcher = new FudgeRequestDispatcher(server);
    InMemoryByteArrayRequestConduit inMemoryByteArrayRequestConduit = new InMemoryByteArrayRequestConduit(fudgeRequestDispatcher);
    ByteArrayFudgeRequestSender fudgeRequestSender = new ByteArrayFudgeRequestSender(inMemoryByteArrayRequestConduit);
    
    DistributedSpecificationResolver client = new DistributedSpecificationResolver(fudgeRequestSender);
    
    LiveDataSpecification testSpec = new LiveDataSpecification(new DomainSpecificIdentifier(new IdentificationDomain("test1"), "test1"));
    LiveDataSpecification resolvedSpec = client.resolve(testSpec);
    assertEquals(resolvedSpec, testSpec);
    
  }

}
