package com.opengamma.master.user.impl.rest;

import java.net.URI;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Provides;
import com.opengamma.DataNotFoundException;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.impl.DataUserMasterResource;
import com.opengamma.master.user.impl.InMemoryUserMasterTest;
import com.opengamma.master.user.impl.RemoteUserMaster;
import com.opengamma.util.rest.FudgeRestClient;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.lightweight.OpenGammaRestResourceTest;
import com.opengamma.util.test.lightweight.ProviderModule;

@Test(groups = TestGroup.UNIT)
public class RemoteUserMasterTest extends OpenGammaRestResourceTest {

  private InMemoryUserMasterTest delegate;
  private UserMaster originalDelegateMaster;

  @Override
  protected ProviderModule defineProviders() {
    return new ProviderModule() {
      @Provides
      DataUserMasterResource provideDataSecurePortfolioMasterResource() {
        // create restful resource using UserMaster defined in another class
        return new DataUserMasterResource(originalDelegateMaster);
      }
    };
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    delegate = new InMemoryUserMasterTest();
    delegate.setUp();
    //
    FudgeRestClient fudgeRestClient = new FudgeRestClient(client());
    URI baseUri = resource().getURI();
    originalDelegateMaster = delegate._master;
    // swap the UserMaster in another class with the remote one
    delegate._master = new RemoteUserMaster(baseUri.resolve("userMaster"), fudgeRestClient);
    //

  }

  // ----- delegating


  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_noMatch() {
    delegate.test_get_noMatch();
  }

  public void test_get_match() {
    delegate.test_get_match();
  }
}
