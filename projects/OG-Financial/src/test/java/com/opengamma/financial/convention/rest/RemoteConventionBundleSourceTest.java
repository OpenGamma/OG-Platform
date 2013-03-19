/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.WebApplicationException;

import org.fudgemsg.FudgeMsg;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleImpl;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterface;

/**
 * Tests the {@link RemoteConventionBundleSource} and {@link DataConventionBundleSourceResource} classes
 */
@Test(groups = TestGroup.UNIT)
public class RemoteConventionBundleSourceTest {

  private ConventionBundle createBundle() {
    final ConventionBundleImpl bundle = new ConventionBundleImpl("Test", true);
    bundle.setUniqueId(UniqueId.of("Mock", "0"));
    return bundle;
  }

  public void testGetByIdentifier() {
    ConventionBundle bundle = createBundle();
    final ConventionBundleSource underlying = Mockito.mock(ConventionBundleSource.class);
    Mockito.when(underlying.getConventionBundle(ExternalId.of("Test", "Foo"))).thenReturn(bundle);
    Mockito.when(underlying.getConventionBundle(ExternalId.of("Test", "Bar"))).thenReturn(null);
    final DataConventionBundleSourceResource server = new DataConventionBundleSourceResource(underlying);
    final ConventionBundleSource client = new RemoteConventionBundleSource(URI.create("http://localhost/")) {
      @Override
      protected UniformInterface accessRemote(final URI uri) {
        assertTrue(uri.getPath().startsWith("/identifier/"));
        final UniformInterface builder = Mockito.mock(UniformInterface.class);
        Mockito.when(builder.get(ConventionBundle.class)).thenAnswer(new Answer<ConventionBundle>() {
          @Override
          public ConventionBundle answer(final InvocationOnMock invocation) throws Throwable {
            try {
              return OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConventionBundle.class, (FudgeMsg) server.getByIdentifier(uri.getPath().substring(12)).getEntity());
            } catch (final WebApplicationException e) {
              assertEquals(e.getResponse().getStatus(), 404);
              throw new UniformInterfaceException404NotFound(new ClientResponse(404, null, null, null), false);
            }
          }
        });
        return builder;
      }
    };
    bundle = client.getConventionBundle(ExternalId.of("Test", "Foo"));
    assertNotNull(bundle);
    assertEquals(bundle.getUniqueId(), UniqueId.of("Mock", "0"));
    bundle = client.getConventionBundle(ExternalId.of("Test", "Bar"));
    assertNull(bundle);
  }

  public void testGetByBundle() {
    ConventionBundle bundle = createBundle();
    final ConventionBundleSource underlying = Mockito.mock(ConventionBundleSource.class);
    Mockito.when(underlying.getConventionBundle(ExternalId.of("Test", "Foo").toBundle())).thenReturn(bundle);
    Mockito.when(underlying.getConventionBundle(ExternalId.of("Test", "Bar").toBundle())).thenReturn(null);
    final DataConventionBundleSourceResource server = new DataConventionBundleSourceResource(underlying);
    final ConventionBundleSource client = new RemoteConventionBundleSource(URI.create("http://localhost/")) {
      @Override
      protected UniformInterface accessRemote(final URI uri) {
        assertTrue(uri.getPath().startsWith("/bundle"));
        assertTrue(uri.getQuery().startsWith("id="));
        final UniformInterface builder = Mockito.mock(UniformInterface.class);
        Mockito.when(builder.get(ConventionBundle.class)).thenAnswer(new Answer<ConventionBundle>() {
          @Override
          public ConventionBundle answer(final InvocationOnMock invocation) throws Throwable {
            try {
              return OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConventionBundle.class, (FudgeMsg) server.getByBundle(Arrays.asList(uri.getQuery().substring(3))).getEntity());
            } catch (final WebApplicationException e) {
              assertEquals(e.getResponse().getStatus(), 404);
              throw new UniformInterfaceException404NotFound(new ClientResponse(404, null, null, null), false);
            }
          }
        });
        return builder;
      }
    };
    bundle = client.getConventionBundle(ExternalId.of("Test", "Foo").toBundle());
    assertNotNull(bundle);
    assertEquals(bundle.getUniqueId(), UniqueId.of("Mock", "0"));
    bundle = client.getConventionBundle(ExternalId.of("Test", "Bar").toBundle());
    assertNull(bundle);
  }

  public void testGetByUniqueId() {
    ConventionBundle bundle = createBundle();
    final ConventionBundleSource underlying = Mockito.mock(ConventionBundleSource.class);
    Mockito.when(underlying.getConventionBundle(UniqueId.of("Mock", "0"))).thenReturn(bundle);
    Mockito.when(underlying.getConventionBundle(UniqueId.of("Mock", "1"))).thenReturn(null);
    final DataConventionBundleSourceResource server = new DataConventionBundleSourceResource(underlying);
    final ConventionBundleSource client = new RemoteConventionBundleSource(URI.create("http://localhost/")) {
      @Override
      protected UniformInterface accessRemote(final URI uri) {
        assertTrue(uri.getPath().startsWith("/unique/"));
        final UniformInterface builder = Mockito.mock(UniformInterface.class);
        Mockito.when(builder.get(ConventionBundle.class)).thenAnswer(new Answer<ConventionBundle>() {
          @Override
          public ConventionBundle answer(final InvocationOnMock invocation) throws Throwable {
            try {
              return OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConventionBundle.class, (FudgeMsg) server.getByUniqueId(uri.getPath().substring(8)).getEntity());
            } catch (final WebApplicationException e) {
              assertEquals(e.getResponse().getStatus(), 404);
              throw new UniformInterfaceException404NotFound(new ClientResponse(404, null, null, null), false);
            }
          }
        });
        return builder;
      }
    };
    bundle = client.getConventionBundle(UniqueId.of("Mock", "0"));
    assertNotNull(bundle);
    assertEquals(bundle.getUniqueId(), UniqueId.of("Mock", "0"));
    bundle = client.getConventionBundle(UniqueId.of("Mock", "1"));
    assertNull(bundle);
  }

}
