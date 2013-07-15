/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
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

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;
import com.opengamma.util.test.ActiveMQTestUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterface;

/**
 * Tests the {@link RemoteFunctionBlacklistProvider}, {@RemoteFunctionBlacklist}, {@link DataFunctionBlacklistProviderResource}, and {@link DataFunctionBlacklistResource}
 * classes.
 */
@Test(groups = TestGroup.INTEGRATION)
public class RemoteFunctionBlacklistProviderTest {

  private FunctionBlacklistProvider createClient(final ExecutorService executor, final JmsConnector jmsConnector, final DataFunctionBlacklistProviderResource server) {
    final FunctionBlacklistProvider client = new RemoteFunctionBlacklistProvider(URI.create("http://localhost/"), executor, jmsConnector) {
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
        return builder;
      }
    };
    return client;
  }

  private Response answerGet(final DataFunctionBlacklistProviderResource server, final String[] uri) {
    final DataFunctionBlacklistResource blacklist = server.get(uri[2]);
    if (uri.length == 3) {
      return blacklist.info();
    }
    assertEquals(uri[3], "mod");
    return blacklist.info(Integer.parseInt(uri[4]));
  }

  public void testGetBlacklist () {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    final JmsConnector jmsConnector = ActiveMQTestUtils.createTestJmsConnector("RemoteFunctionBlacklistProviderTest.testGetBlacklist");
    try {
      final InMemoryFunctionBlacklistProvider underlying = new InMemoryFunctionBlacklistProvider(executor);
      final DataFunctionBlacklistProviderResource server = new DataFunctionBlacklistProviderResource(underlying, OpenGammaFudgeContext.getInstance(), jmsConnector);
      final FunctionBlacklistProvider client = createClient(executor, jmsConnector, server);
      final FunctionBlacklist foo = client.getBlacklist("Foo");
      assertNotNull(foo);
      assertEquals(foo.getName(), "Foo");
      assertEquals(foo.getModificationCount(), 0);
      final FunctionBlacklist bar = client.getBlacklist("Bar");
      assertNotNull(bar);
      assertEquals(bar.getName(), "Bar");
      assertEquals(bar.getModificationCount(), 0);
    } finally {
      executor.shutdown();
      jmsConnector.close();
    }
  }

  protected static void waitForModificationCount(final FunctionBlacklist blacklist, final int expected) throws InterruptedException {
    for (long l = Timeout.standardTimeoutMillis(); l >= 0; l -= 250) {
      if (blacklist.getModificationCount() == expected) {
        break;
      }
      Thread.sleep(250);
    }
  }

  protected static void testRuleUpdates(final ManageableFunctionBlacklist update, final FunctionBlacklist receive) throws InterruptedException {
    assertTrue(update.getRules().isEmpty());
    assertTrue(receive.getRules().isEmpty());
    final FunctionBlacklistRule rule1 = new FunctionBlacklistRule(ComputationTargetSpecification.of(UniqueId.of("Test", "1")));
    final FunctionBlacklistRule rule2 = new FunctionBlacklistRule(ComputationTargetSpecification.of(UniqueId.of("Test", "2")));
    final FunctionBlacklistRule rule3 = new FunctionBlacklistRule(ComputationTargetSpecification.of(UniqueId.of("Test", "3")));
    final FunctionBlacklistRule rule1b = new FunctionBlacklistRule(ComputationTargetSpecification.of(UniqueId.of("Test", "1b")));
    final FunctionBlacklistRule rule2b = new FunctionBlacklistRule(ComputationTargetSpecification.of(UniqueId.of("Test", "2b")));
    final FunctionBlacklistRule rule3b = new FunctionBlacklistRule(ComputationTargetSpecification.of(UniqueId.of("Test", "3b")));
    update.addBlacklistRule(rule1);
    waitForModificationCount(receive, 1);
    assertEquals(receive.getRules().size(), 1);
    assertTrue(receive.getRules().contains(rule1));
    update.addBlacklistRule(rule1b);
    waitForModificationCount(receive, 2);
    assertEquals(receive.getRules().size(), 2);
    assertTrue(receive.getRules().contains(rule1b));
    update.addBlacklistRules(Arrays.asList(rule2, rule3));
    waitForModificationCount(receive, 3);
    assertEquals(receive.getRules().size(), 4);
    assertTrue(receive.getRules().contains(rule2));
    assertTrue(receive.getRules().contains(rule3));
    update.addBlacklistRules(Arrays.asList(rule2b, rule3b));
    waitForModificationCount(receive, 4);
    assertEquals(receive.getRules().size(), 6);
    assertTrue(receive.getRules().contains(rule2b));
    assertTrue(receive.getRules().contains(rule3b));
    update.removeBlacklistRules(Arrays.asList(rule1, rule3));
    waitForModificationCount(receive, 5);
    assertEquals(receive.getRules().size(), 4);
    assertFalse(receive.getRules().contains(rule1));
    assertFalse(receive.getRules().contains(rule3));
    update.removeBlacklistRule(rule2);
    waitForModificationCount(receive, 6);
    assertEquals(receive.getRules().size(), 3);
    assertFalse(receive.getRules().contains(rule2));
  }

  public void testReceiveUpdates() throws InterruptedException {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    final JmsConnector jmsConnector = ActiveMQTestUtils.createTestJmsConnector("RemoteFunctionBlacklistProviderTest.testReceiveUpdates");
    try {
      final InMemoryFunctionBlacklistProvider underlying = new InMemoryFunctionBlacklistProvider(executor);
      final DataFunctionBlacklistProviderResource server = new DataFunctionBlacklistProviderResource(underlying, OpenGammaFudgeContext.getInstance(), jmsConnector);
      final FunctionBlacklistProvider client = createClient(executor, jmsConnector, server);
      final FunctionBlacklist clientBlacklist = client.getBlacklist("Test");
      final ManageableFunctionBlacklist serverBlacklist = underlying.getBlacklist("Test");
      testRuleUpdates(serverBlacklist, clientBlacklist);
    } finally {
      executor.shutdown();
      jmsConnector.close();
    }
  }

}
