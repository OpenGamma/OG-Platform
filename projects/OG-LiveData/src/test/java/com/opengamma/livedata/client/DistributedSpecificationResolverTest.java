/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.resolver.IdResolverServer;
import com.opengamma.livedata.resolver.IdentityIdResolver;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DistributedSpecificationResolverTest {

  public void testRequestResponse() {
    IdentityIdResolver delegate = new IdentityIdResolver();
    IdResolverServer server = new IdResolverServer(delegate);
    
    FudgeRequestDispatcher fudgeRequestDispatcher = new FudgeRequestDispatcher(server);
    InMemoryByteArrayRequestConduit inMemoryByteArrayRequestConduit = new InMemoryByteArrayRequestConduit(fudgeRequestDispatcher);
    ByteArrayFudgeRequestSender fudgeRequestSender = new ByteArrayFudgeRequestSender(inMemoryByteArrayRequestConduit);
    
    DistributedSpecificationResolver client = new DistributedSpecificationResolver(fudgeRequestSender);
    
    LiveDataSpecification testSpec = new LiveDataSpecification("test1", ExternalId.of("test1", "test1"));
    LiveDataSpecification resolvedSpec = client.resolve(testSpec);
    assertEquals(resolvedSpec, testSpec);
  }

}
