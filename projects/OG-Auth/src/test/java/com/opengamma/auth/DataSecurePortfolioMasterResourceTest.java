/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.auth;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
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
import com.opengamma.auth.master.portfolio.rest.DataSecurePortfolioNodeResource;
import com.opengamma.auth.master.portfolio.rest.DataSecurePortfolioResource;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.util.rest.RestUtils;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataSecurePortfolioMasterResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private PortfolioMaster _underlying;
  private SecurePortfolioMaster _secure;
  private UriInfo _uriInfo;
  private DataSecurePortfolioMasterResource _resource;
  private PortfolioCapability _capability;


  @BeforeMethod
  public void setUp() {
    PortfolioEntitlement entitlement = PortfolioEntitlement.singlePortfolioEntitlement(UID.getObjectId(),
                                                                                       Instant.now().plusSeconds(1000),
                                                                                       ResourceAccess.READ);
    _capability = Utils.toCapability(entitlement);

    _underlying = mock(PortfolioMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));

    _secure = new SecurePortfolioMasterWrapper(_underlying);
    _resource = new DataSecurePortfolioMasterResource(_secure);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = AuthorisationException.class)
  public void testAddPortfolioWithNoRequiredEntitlements() {
    final ManageablePortfolio target = new ManageablePortfolio("Portfolio A");
    target.getRootNode().setName("RootNode");
    target.getRootNode().addChildNode(new ManageablePortfolioNode("Child"));
    final PortfolioDocument request = new PortfolioDocument(target);

    final PortfolioDocument result = new PortfolioDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);

    Response test = _resource.add(RestUtils.encodeBase64(_capability), _uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }


  @Test
  public void testAddPortfolio() {
    PortfolioCapability extendedCapability = Utils.toCapability(PortfolioEntitlement.globalPortfolioEntitlement(Instant.now().plusSeconds(
        1000), ResourceAccess.WRITE));
    final ManageablePortfolio target = new ManageablePortfolio("Portfolio A");
    target.getRootNode().setName("RootNode");
    target.getRootNode().addChildNode(new ManageablePortfolioNode("Child"));
    final PortfolioDocument request = new PortfolioDocument(target);

    final PortfolioDocument result = new PortfolioDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);

    Response test = _resource.add(RestUtils.encodeBase64(extendedCapability), _uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindPortfolio() {
    DataSecurePortfolioResource test = _resource.findPortfolio("Test~PortA");
    assertSame(_resource, test.getPortfoliosResource());
    assertEquals(ObjectId.of("Test", "PortA"), test.getUrlId());
  }

  @Test
  public void testFindPortfolioNode() {
    DataSecurePortfolioNodeResource test = _resource.findPortfolioNode("Test~NodeA");
    assertSame(_resource, test.getPortfoliosResource());
    assertEquals(UniqueId.of("Test", "NodeA"), test.getUrlNodeId());
  }

}
