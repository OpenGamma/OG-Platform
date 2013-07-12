/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.auth;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.auth.master.portfolio.PortfolioCapability;
import com.opengamma.auth.master.portfolio.PortfolioEntitlement;
import com.opengamma.auth.master.portfolio.SecurePortfolioMaster;
import com.opengamma.auth.master.portfolio.SecurePortfolioMasterWrapper;
import com.opengamma.auth.master.portfolio.rest.DataSecurePortfolioMasterResource;
import com.opengamma.auth.master.portfolio.rest.DataSecurePortfolioResource;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.util.rest.RestUtils;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPortfolioResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataSecurePortfolioResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PortA");
  private PortfolioMaster _underlying;
  private SecurePortfolioMaster _secure;
  private DataSecurePortfolioResource _resource;
  private UriInfo _uriInfo;
  private PortfolioCapability _capability;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PortfolioMaster.class);

    PortfolioEntitlement entitlement = PortfolioEntitlement.singlePortfolioEntitlement(OID,
                                                                                       Instant.now().plusSeconds(1000),
                                                                                       ResourceAccess.READ);
    _capability = Utils.toCapability(entitlement);

    _secure = new SecurePortfolioMasterWrapper(_underlying);
    _resource = new DataSecurePortfolioResource(new DataSecurePortfolioMasterResource(_secure), OID);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPortfolio() {
    final ManageablePortfolio target = new ManageablePortfolio("Portfolio");
    final PortfolioDocument result = new PortfolioDocument(target);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);

    Response test = _resource.get(RestUtils.encodeBase64(_capability), null, null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test(expectedExceptions = AuthorisationException.class)
  public void testUpdatePortfolioWithNoRequiredEntitlements() {
    final ManageablePortfolio target = new ManageablePortfolio("Portfolio");
    final PortfolioDocument request = new PortfolioDocument(target);
    request.setUniqueId(OID.atVersion("1"));

    final PortfolioDocument result = new PortfolioDocument(target);
    result.setUniqueId(OID.atVersion("1"));
    when(_underlying.update(same(request))).thenReturn(result);

    Response test = _resource.update(RestUtils.encodeBase64(_capability), _uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdatePortfolio() {
    PortfolioCapability extendedCapability = Utils.toCapability(PortfolioEntitlement.singlePortfolioEntitlement(OID,
                                                                                                                Instant.now().plusSeconds(
                                                                                                                    1000),
                                                                                                                ResourceAccess.WRITE));

    final ManageablePortfolio target = new ManageablePortfolio("Portfolio");
    final PortfolioDocument request = new PortfolioDocument(target);
    request.setUniqueId(OID.atVersion("1"));

    final PortfolioDocument result = new PortfolioDocument(target);
    result.setUniqueId(OID.atVersion("1"));
    when(_underlying.update(same(request))).thenReturn(result);

    Response test = _resource.update(RestUtils.encodeBase64(extendedCapability), _uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test(expectedExceptions = AuthorisationException.class)
  public void testDeletePortfolioWithNoRequiredEntitlements() {
    _resource.remove(RestUtils.encodeBase64(_capability));
    verify(_underlying).remove(OID.atLatestVersion());
  }

  @Test
  public void testDeletePortfolio() {
    PortfolioCapability extendedCapability = Utils.toCapability(PortfolioEntitlement.singlePortfolioEntitlement(OID,
                                                                                                                Instant.now().plusSeconds(
                                                                                                                    1000),
                                                                                                                ResourceAccess.DELETE));
    _resource.remove(RestUtils.encodeBase64(extendedCapability));
    verify(_underlying).remove(OID.atLatestVersion());
  }

  @Test(expectedExceptions = AuthorisationException.class)
  public void testDeletePortfolioWithExpiredEntitlement() {
    PortfolioCapability newCapability = Utils.toCapability(PortfolioEntitlement.singlePortfolioEntitlement(OID,
                                                                                                           Instant.now().minusSeconds(
                                                                                                               1000),
                                                                                                           ResourceAccess.DELETE));
    _resource.remove(RestUtils.encodeBase64(newCapability));
    verify(_underlying).remove(OID.atLatestVersion());
  }
}
