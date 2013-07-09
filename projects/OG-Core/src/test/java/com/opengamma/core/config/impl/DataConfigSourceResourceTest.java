/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.net.URI;
import java.util.Collections;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsg;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.exchange.impl.SimpleExchange;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataConfigSourceResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "A");
  private static final UniqueId UID = OID.atVersion("B");
  private static final VersionCorrection VC = VersionCorrection.LATEST.withLatestFixed(Instant.now());
  private static final String NAME = "name";
  private ConfigSource _underlying;
  private UriInfo _uriInfo;
  private DataConfigSourceResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(ConfigSource.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataConfigSourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings({"unchecked", "rawtypes" })
  @Test
  public void testGetConfigByUid() {
    final SimpleExchange target = new SimpleExchange();
    target.setName("Test");
    when(_underlying.get(eq(UID))).thenReturn((ConfigItem) ConfigItem.of(target));
    final Response test = _resource.get(UID.toString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(target, OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) test.getEntity()).getValue());
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Test
  public void testGetConfigByOid() {
    final SimpleExchange target = new SimpleExchange();
    target.setName("Test");
    when(_underlying.get(eq(OID), eq(VC))).thenReturn((ConfigItem) ConfigItem.of(target));
    final Response test = _resource.getByOidVersionCorrection(OID.toString(), VC.toString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(target, OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) test.getEntity()).getValue());
  }

  @Test
  public void testSearch() {
    final ConfigItem<SimpleExchange> target = ConfigItem.of(new SimpleExchange());
    target.setName("Test");
    when(_underlying.get(eq(SimpleExchange.class), eq(NAME), eq(VC))).thenReturn(Collections.singleton(target));
    final Response test = _resource.search(SimpleExchange.class.getName(), VC.toString(), NAME);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    final FudgeMsg msg = (FudgeMsg) test.getEntity();
    assertEquals(1, msg.getNumFields());
    assertEquals(target, OpenGammaFudgeContext.getInstance().fromFudgeMsg(ConfigItem.class, (FudgeMsg) msg.getAllFields().get(0).getValue()));
  }


}
