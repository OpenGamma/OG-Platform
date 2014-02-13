/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeMsg;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;
import com.opengamma.util.test.ActiveMQTestUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterface;

/**
 * Tests the {@link RemoteManageableFunctionBlacklistProvider}, {@RemoteManageableFunctionBlacklist}, {@link DataManageableFunctionBlacklistProviderResource}, and
 * {@link DataManageableFunctionBlacklistResource} classes.
 */
@Test(groups = TestGroup.INTEGRATION)
public class RemoteManageableFunctionBlacklistProviderTest {

  private ManageableFunctionBlacklistProvider createClient(final ExecutorService executor, final JmsConnector jmsConnector, final DataManageableFunctionBlacklistProviderResource server) {
    final ManageableFunctionBlacklistProvider client = new RemoteManageableFunctionBlacklistProvider(URI.create("http://localhost/"), executor, jmsConnector) {
      @Override
      protected UniformInterface accessRemote(final URI uri) {
        final UniformInterface builder = Mockito.mock(UniformInterface.class);
        assertTrue(uri.getPath().startsWith("/name/"));
        final String[] s = uri.getPath().split("/");
        Mockito.when(builder.get(FudgeMsg.class)).thenAnswer(new Answer<Object>() {
          @Override
          public Object answer(final InvocationOnMock invocation) throws Throwable {
            try {
              return answerGet(server, s).getEntity();
            } catch (final WebApplicationException e) {
              assertEquals(e.getResponse().getStatus(), 404);
              throw new UniformInterfaceException404NotFound(new ClientResponse(404, null, null, null), false);
            }
          }
        });
        Mockito.doAnswer(new Answer<Object>() {
          @Override
          public Object answer(final InvocationOnMock invocation) throws Throwable {
            try {
              return answerPost(server, invocation.getArguments()[0], s).getEntity();
            } catch (final WebApplicationException e) {
              assertEquals(e.getResponse().getStatus(), 404);
              throw new UniformInterfaceException404NotFound(new ClientResponse(404, null, null, null), false);
            }
          }
        }).when(builder).post(Mockito.<UniformInterface>any());
        return builder;
      }
    };
    return client;
  }

  private Response answerGet(final DataManageableFunctionBlacklistProviderResource server, final String[] uri) {
    final DataFunctionBlacklistResource blacklist = server.get(uri[2]);
    if (uri.length == 3) {
      return blacklist.info();
    }
    assertEquals(uri[3], "mod");
    return blacklist.info(Integer.parseInt(uri[4]));
  }

  protected Response answerPost(final DataManageableFunctionBlacklistProviderResource server, final Object payload, final String[] uri) {
    final DataManageableFunctionBlacklistResource resource = (DataManageableFunctionBlacklistResource) server.get(uri[2]);
    assertEquals(uri.length, 4);
    if ("add".equals(uri[3])) {
      resource.add((FudgeMsg) payload);
    } else if ("remove".equals(uri[3])) {
      resource.remove((FudgeMsg) payload);
    } else {
      fail(uri[3]);
    }
    return Response.ok().build();
  }

  public void testGetBlacklist() {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    final JmsConnector jmsConnector = ActiveMQTestUtils.createTestJmsConnector("RemoteManageableFunctionBlacklistProviderTest.testGetBlacklist");
    try {
      final InMemoryFunctionBlacklistProvider underlying = new InMemoryFunctionBlacklistProvider(executor);
      final DataManageableFunctionBlacklistProviderResource server = new DataManageableFunctionBlacklistProviderResource(underlying, OpenGammaFudgeContext.getInstance(), jmsConnector);
      final ManageableFunctionBlacklistProvider client = createClient(executor, jmsConnector, server);
      final ManageableFunctionBlacklist foo = client.getBlacklist("Foo");
      assertNotNull(foo);
      assertEquals(foo.getName(), "Foo");
      assertEquals(foo.getModificationCount(), 0);
      final ManageableFunctionBlacklist bar = client.getBlacklist("Bar");
      assertNotNull(bar);
      assertEquals(bar.getName(), "Bar");
      assertEquals(bar.getModificationCount(), 0);
    } finally {
      executor.shutdown();
      jmsConnector.close();
    }
  }

  protected void waitForModificationCount(final FunctionBlacklist blacklist, final int expected) throws InterruptedException {
    for (long l = Timeout.standardTimeoutMillis(); l >= 0; l -= 250) {
      if (blacklist.getModificationCount() == expected) {
        break;
      }
      Thread.sleep(250);
    }
  }

  public void testReceiveUpdates() throws InterruptedException {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    final JmsConnector jmsConnector = ActiveMQTestUtils.createTestJmsConnector("RemoteManageableFunctionBlacklistProviderTest.testReceiveUpdates");
    try {
      final InMemoryFunctionBlacklistProvider underlying = new InMemoryFunctionBlacklistProvider(executor);
      final DataManageableFunctionBlacklistProviderResource server = new DataManageableFunctionBlacklistProviderResource(underlying, OpenGammaFudgeContext.getInstance(), jmsConnector);
      final ManageableFunctionBlacklistProvider client = createClient(executor, jmsConnector, server);
      final FunctionBlacklist clientBlacklist = client.getBlacklist("Test");
      final ManageableFunctionBlacklist serverBlacklist = underlying.getBlacklist("Test");
      RemoteFunctionBlacklistProviderTest.testRuleUpdates(serverBlacklist, clientBlacklist);
    } finally {
      executor.shutdown();
      jmsConnector.close();
    }
  }

  public void testPostUpdates() throws InterruptedException {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    final JmsConnector jmsConnector = ActiveMQTestUtils.createTestJmsConnector("RemoteManageableFunctionBlacklistProviderTest.testPostUpdates");
    try {
      final InMemoryFunctionBlacklistProvider underlying = new InMemoryFunctionBlacklistProvider(executor);
      final DataManageableFunctionBlacklistProviderResource server = new DataManageableFunctionBlacklistProviderResource(underlying, OpenGammaFudgeContext.getInstance(), jmsConnector);
      final ManageableFunctionBlacklistProvider client = createClient(executor, jmsConnector, server);
      final ManageableFunctionBlacklist clientBlacklist = client.getBlacklist("Test");
      final FunctionBlacklist serverBlacklist = underlying.getBlacklist("Test");
      RemoteFunctionBlacklistProviderTest.testRuleUpdates(clientBlacklist, serverBlacklist);
    } finally {
      executor.shutdown();
      jmsConnector.close();
    }
  }

}
