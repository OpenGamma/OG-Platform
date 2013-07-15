/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.Collections;

import javax.ws.rs.WebApplicationException;

import org.fudgemsg.FudgeMsg;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterface;

/**
 * Tests the {@link RemoteFunctionBlacklistPolicySource} and {@link DataFunctionBlacklistPolicySourceResource} classes.
 */
@Test(groups = TestGroup.INTEGRATION)
public class RemoteFunctionBlacklistPolicySourceTest {

  public void testGetPolicyByUniqueId() {
    final InMemoryFunctionBlacklistPolicySource underlying = new InMemoryFunctionBlacklistPolicySource();
    underlying.addPolicy(new DefaultFunctionBlacklistPolicy(UniqueId.of("Test", "Foo"), 10, Collections.singleton(FunctionBlacklistPolicy.Entry.PARAMETERIZED_FUNCTION)));
    final DataFunctionBlacklistPolicySourceResource server = new DataFunctionBlacklistPolicySourceResource(underlying, OpenGammaFudgeContext.getInstance());
    final FunctionBlacklistPolicySource client = new RemoteFunctionBlacklistPolicySource(URI.create("http://localhost/")) {
      @Override
      protected UniformInterface accessRemote(final URI uri) {
        assertTrue(uri.getPath().startsWith("/uid/"));
        final UniformInterface builder = Mockito.mock(UniformInterface.class);
        Mockito.when(builder.get(FudgeMsg.class)).thenAnswer(new Answer<FudgeMsg>() {
          @Override
          public FudgeMsg answer(final InvocationOnMock invocation) throws Throwable {
            try {
              return (FudgeMsg) server.getByUniqueId(uri.getPath().substring(5)).getEntity();
            } catch (WebApplicationException e) {
              assertEquals(e.getResponse().getStatus(), 404);
              throw new UniformInterfaceException404NotFound(new ClientResponse(404, null, null, null), false);
            }
          }
        });
        return builder;
      }
    };
    FunctionBlacklistPolicy policy = client.getPolicy(UniqueId.of("Test", "Foo"));
    assertNotNull(policy);
    assertEquals(policy.getName(), "Foo");
    assertEquals(policy.getUniqueId(), UniqueId.of("Test", "Foo"));
    policy = client.getPolicy(UniqueId.of("Test", "Bar"));
    assertNull(policy);
  }

  public void testGetPolicyByName() {
    final InMemoryFunctionBlacklistPolicySource underlying = new InMemoryFunctionBlacklistPolicySource();
    underlying.addPolicy(new DefaultFunctionBlacklistPolicy(UniqueId.of("Test", "Foo"), 10, Collections.singleton(FunctionBlacklistPolicy.Entry.PARAMETERIZED_FUNCTION)));
    final DataFunctionBlacklistPolicySourceResource server = new DataFunctionBlacklistPolicySourceResource(underlying, OpenGammaFudgeContext.getInstance());
    final FunctionBlacklistPolicySource client = new RemoteFunctionBlacklistPolicySource(URI.create("http://localhost/")) {
      @Override
      protected UniformInterface accessRemote(final URI uri) {
        assertTrue(uri.getPath().startsWith("/name/"));
        final UniformInterface builder = Mockito.mock(UniformInterface.class);
        Mockito.when(builder.get(FudgeMsg.class)).thenAnswer(new Answer<FudgeMsg>() {
          @Override
          public FudgeMsg answer(final InvocationOnMock invocation) throws Throwable {
            try {
              return (FudgeMsg) server.getByName(uri.getPath().substring(6)).getEntity();
            } catch (WebApplicationException e) {
              assertEquals(e.getResponse().getStatus(), 404);
              throw new UniformInterfaceException404NotFound(new ClientResponse(404, null, null, null), false);
            }
          }
        });
        return builder;
      }
    };
    FunctionBlacklistPolicy policy = client.getPolicy("Foo");
    assertNotNull(policy);
    assertEquals(policy.getName(), "Foo");
    assertEquals(policy.getUniqueId(), UniqueId.of("Test", "Foo"));
    policy = client.getPolicy("Bar");
    assertNull(policy);
  }
}
