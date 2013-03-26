/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;

import javax.ws.rs.WebApplicationException;

import org.fudgemsg.FudgeMsg;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import com.opengamma.financial.temptarget.InMemoryTempTargetRepository;
import com.opengamma.financial.temptarget.MockTempTarget;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterface;

/**
 * Tests the {@link RemoteTempTargetRepository} and {@link DataTempTargetRepositoryResource} classes.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteTempTargetRepositoryTest {

  public void testGet() {
    final InMemoryTempTargetRepository underlying = new InMemoryTempTargetRepository();
    final UniqueId uid = underlying.locateOrStore(new MockTempTarget("Test"));
    assertEquals(uid, UniqueId.of("TmpMem", "1"));
    final DataTempTargetRepositoryResource server = new DataTempTargetRepositoryResource(underlying);
    final TempTargetRepository client = new RemoteTempTargetRepository(URI.create("http://localhost/")) {
      @Override
      protected UniformInterface accessRemote(final URI uri) {
        assertTrue(uri.getPath().startsWith("/target/"));
        final UniformInterface builder = Mockito.mock(UniformInterface.class);
        Mockito.when(builder.get(FudgeMsg.class)).thenAnswer(new Answer<FudgeMsg>() {
          @Override
          public FudgeMsg answer(final InvocationOnMock invocation) throws Throwable {
            try {
              return (FudgeMsg) server.get(uri.getPath().substring(8)).getEntity();
            } catch (final WebApplicationException e) {
              assertEquals(e.getResponse().getStatus(), 404);
              throw new UniformInterfaceException404NotFound(new ClientResponse(404, null, null, null), false);
            }
          }
        });
        return builder;
      }
    };
    assertNull(client.get(UniqueId.of("Invalid", "Foo")));
    assertEquals(client.get(uid), new MockTempTarget("Test"));
  }

  public void testLocateOrStore() {
    final InMemoryTempTargetRepository underlying = new InMemoryTempTargetRepository();
    final DataTempTargetRepositoryResource server = new DataTempTargetRepositoryResource(underlying);
    final TempTargetRepository client = new RemoteTempTargetRepository(URI.create("http://localhost/")) {
      @SuppressWarnings("unchecked")
      @Override
      protected UniformInterface accessRemote(final URI uri) {
        assertTrue(uri.getPath().equals("/target"));
        final UniformInterface builder = Mockito.mock(UniformInterface.class);
        Mockito.doAnswer(new Answer<FudgeMsg>() {
          @Override
          public FudgeMsg answer(final InvocationOnMock invocation) throws Throwable {
            return (FudgeMsg) server.locateOrStore((FudgeMsg) invocation.getArguments()[1]).getEntity();
          }
        }).when(builder).post(Mockito.any(Class.class), Mockito.anyObject());
        return builder;
      }
    };
    assertEquals(client.locateOrStore(new MockTempTarget("Test")), UniqueId.of("TmpMem", "1"));
    assertEquals(client.locateOrStore(new MockTempTarget("Test")), UniqueId.of("TmpMem", "1"));
    assertEquals(client.locateOrStore(new MockTempTarget("Foo")), UniqueId.of("TmpMem", "2"));
  }

}
