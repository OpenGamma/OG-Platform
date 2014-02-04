package com.opengamma.auth.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.net.URI;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.inject.Provides;
import com.opengamma.auth.AuthorisationException;
import com.opengamma.auth.Utils;
import com.opengamma.auth.master.portfolio.PortfolioCapability;
import com.opengamma.auth.master.portfolio.PortfolioEntitlement;
import com.opengamma.auth.master.portfolio.SecurePortfolioMaster;
import com.opengamma.auth.master.portfolio.SecurePortfolioMasterWrapper;
import com.opengamma.auth.master.portfolio.rest.DataSecurePortfolioMasterResource;
import com.opengamma.auth.master.portfolio.rest.DataSecurePortfolioResource;
import com.opengamma.auth.master.portfolio.rest.RemoteSecurePortfolioMaster;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.util.rest.FudgeRestClient;
import com.opengamma.util.rest.RestUtils;
import com.opengamma.util.test.lightweight.OpenGammaRestResourceTest;
import com.opengamma.util.test.lightweight.ProviderModule;

public class DataSecurePortfolioResourceTest extends OpenGammaRestResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");

  private final PortfolioMaster _portfolioMaster = mock(PortfolioMaster.class);
  private SecurePortfolioMaster _securePortfolioMaster;
  private final PortfolioEntitlement entitlement = PortfolioEntitlement.singlePortfolioEntitlement(UID.getObjectId(),
                                                                                                   Instant.now().plusSeconds(
                                                                                                       10000),
                                                                                                   ResourceAccess.READ);
  private final PortfolioCapability _capability = Utils.toCapability(entitlement);


  @Override
  protected ProviderModule defineProviders() {
    return new ProviderModule() {
      @Provides
      DataSecurePortfolioMasterResource provideDataSecurePortfolioMasterResource() {
        DataSecurePortfolioMasterResource dataSecurePortfolioMasterResource = new DataSecurePortfolioMasterResource(
            _securePortfolioMaster);
        return dataSecurePortfolioMasterResource;
      }
    };
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    _securePortfolioMaster = new SecurePortfolioMasterWrapper(_portfolioMaster);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = AuthorisationException.class)
  public void testAddPortfolioWithNoRequiredEntitlements() {
    ObjectId oid = UID.getObjectId();

    final PortfolioEntitlement entitlement = PortfolioEntitlement.singlePortfolioEntitlement(ObjectId.of("test",
                                                                                                         "fake"),
                                                                                             Instant.now().plusSeconds(
                                                                                                 10000),
                                                                                             ResourceAccess.READ);
    final PortfolioCapability capability = Utils.toCapability(entitlement);

    DataSecurePortfolioResource portfolioResource = resource().path("securePortfolioMaster/portfolios/" + oid.toString())
        .header("Capability", RestUtils.encodeBase64(capability))
        .get(DataSecurePortfolioResource.class);
  }

  @Test
  public void remoteGet() {
    reset(_portfolioMaster);
    PortfolioDocument document = new PortfolioDocument();
    ManageablePortfolio portfolio = new ManageablePortfolio();
    portfolio.setUniqueId(UID);
    document.setPortfolio(portfolio);
    document.setUniqueId(UID);
    when(_portfolioMaster.get(UID.getObjectId(), VersionCorrection.LATEST)).thenReturn(document);

    //
    FudgeRestClient fudgeRestClient = new FudgeRestClient(client());
    URI baseUri = resource().getURI();

    RemoteSecurePortfolioMaster remoteSecurePortfolioMaster = new RemoteSecurePortfolioMaster(baseUri.resolve(
        "securePortfolioMaster"), fudgeRestClient);
    PortfolioEntitlement entitlement = PortfolioEntitlement.globalPortfolioEntitlement(Instant.now().plusSeconds(10000),
                                                                                       ResourceAccess.READ);
    PortfolioCapability capability = Utils.toCapability(entitlement);
    UniqueId uid = UID.toLatest();
    PortfolioDocument portfolioDocument = remoteSecurePortfolioMaster.get(capability, uid);

    assertEquals(portfolioDocument.getUniqueId(), UID);
  }

  @Test(expectedExceptions = AuthorisationException.class)
  public void remoteAddWithNoRequiredEntitlements() {
    reset(_portfolioMaster);
    PortfolioDocument document = new PortfolioDocument();
    ManageablePortfolio portfolio = new ManageablePortfolio();
    portfolio.setUniqueId(UID);
    document.setPortfolio(portfolio);
    document.setUniqueId(UID);
    when(_portfolioMaster.get(UID.getObjectId(), VersionCorrection.LATEST)).thenReturn(document);

    //
    FudgeRestClient fudgeRestClient = new FudgeRestClient(client());
    URI baseUri = resource().getURI();

    RemoteSecurePortfolioMaster remoteSecurePortfolioMaster = new RemoteSecurePortfolioMaster(baseUri.resolve(
        "securePortfolioMaster"), fudgeRestClient);
    PortfolioEntitlement entitlement = PortfolioEntitlement.globalPortfolioEntitlement(Instant.now().plusSeconds(10000),
                                                                                       ResourceAccess.READ);
    PortfolioCapability capability = Utils.toCapability(entitlement);

    remoteSecurePortfolioMaster.add(capability, document);
  }

  @Test
  public void remoteAdd() {
    reset(_portfolioMaster);
    PortfolioDocument document = new PortfolioDocument();
    ManageablePortfolio portfolio = new ManageablePortfolio();
    portfolio.setUniqueId(UID);
    document.setPortfolio(portfolio);
    document.setUniqueId(UID);

    when(_portfolioMaster.add(document)).thenReturn(document);

    //
    FudgeRestClient fudgeRestClient = new FudgeRestClient(client());
    URI baseUri = resource().getURI();

    RemoteSecurePortfolioMaster remoteSecurePortfolioMaster = new RemoteSecurePortfolioMaster(baseUri.resolve(
        "securePortfolioMaster"), fudgeRestClient);
    PortfolioEntitlement entitlement = PortfolioEntitlement.globalPortfolioEntitlement(Instant.now().plusSeconds(10000),
                                                                                       ResourceAccess.WRITE);
    PortfolioCapability capability = Utils.toCapability(entitlement);

    PortfolioDocument portfolioDocument = remoteSecurePortfolioMaster.add(capability, document);

    assertEquals(portfolioDocument.getUniqueId(), UID);
  }
}
