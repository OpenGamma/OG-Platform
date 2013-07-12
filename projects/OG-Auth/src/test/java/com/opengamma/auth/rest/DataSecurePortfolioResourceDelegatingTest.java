package com.opengamma.auth.rest;

import java.net.URI;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Provides;
import com.opengamma.auth.SecurePortfolioMasterTest;
import com.opengamma.auth.master.portfolio.SecurePortfolioMaster;
import com.opengamma.auth.master.portfolio.rest.DataSecurePortfolioMasterResource;
import com.opengamma.auth.master.portfolio.rest.RemoteSecurePortfolioMaster;
import com.opengamma.util.rest.FudgeRestClient;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.lightweight.OpenGammaRestResourceTest;
import com.opengamma.util.test.lightweight.ProviderModule;

@Test(groups = TestGroup.UNIT)
public class DataSecurePortfolioResourceDelegatingTest extends OpenGammaRestResourceTest {

  private SecurePortfolioMasterTest delegate;
  private SecurePortfolioMaster originalDelegateMaster;

  @Override
  protected ProviderModule defineProviders() {
    return new ProviderModule() {
      @Provides
      DataSecurePortfolioMasterResource provideDataSecurePortfolioMasterResource() {
        // create restful resource using SecurePortfolioMaster defined in another class
        return new DataSecurePortfolioMasterResource(originalDelegateMaster);
      }
    };
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    delegate = new SecurePortfolioMasterTest();
    delegate.setUp();
    originalDelegateMaster = delegate._securePortfolioMaster;

    FudgeRestClient fudgeRestClient = new FudgeRestClient(client());
    URI baseUri = resource().getURI();
    // swap the SecurePortfolioMaster in another class with the remote one
    delegate._securePortfolioMaster = new RemoteSecurePortfolioMaster(baseUri.resolve("securePortfolioMaster"), fudgeRestClient);
  }

  public void testRob() {
    delegate.testRob();
  }
}
