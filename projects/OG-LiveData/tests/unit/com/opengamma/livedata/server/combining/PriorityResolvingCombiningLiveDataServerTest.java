package com.opengamma.livedata.server.combining;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.AbstractEntitlementChecker;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.livedata.server.MockDistributionSpecificationResolver;
import com.opengamma.livedata.server.MockLiveDataServer;

public class PriorityResolvingCombiningLiveDataServerTest {

    private static final UserPrincipal unauthorizedUser = new UserPrincipal("unauthorized", "127.0.0.1");
    private static final UserPrincipal authorizedUser = new UserPrincipal("authorized", "127.0.0.1");
  
    
    private ExternalScheme _domainB;
    private ExternalScheme _domainC;
    private ExternalScheme _domainD;
    private MockLiveDataServer _serverB;
    private MockLiveDataServer _serverC;
    private PriorityResolvingCombiningLiveDataServer _combiningServer;
    
    @BeforeMethod
    public void setUp() {
      
      _domainB = ExternalScheme.of("B");
      _serverB = new MockLiveDataServer(_domainB);
      _serverB.setDistributionSpecificationResolver(new MockDistributionSpecificationResolver(_domainB));
      setEntitlementChecker(_serverB);
      _serverB.connect();
      
      _domainC = ExternalScheme.of("C");
      _serverC = new MockLiveDataServer(_domainC);
      _serverC.setDistributionSpecificationResolver(new MockDistributionSpecificationResolver(_domainC));
      setEntitlementChecker(_serverC);
      _serverC.connect();
      
      _combiningServer = new PriorityResolvingCombiningLiveDataServer(Lists.newArrayList(_serverB, _serverC));
      _combiningServer.start();
      
      assertEquals(AbstractLiveDataServer.ConnectionStatus.CONNECTED, _combiningServer.getConnectionStatus());
      _domainD = ExternalScheme.of("D");
    }

    private void setEntitlementChecker(MockLiveDataServer server) {
      server.setEntitlementChecker(getEntitlementChecker(server.getUniqueIdDomain()));
    }

    private LiveDataEntitlementChecker getEntitlementChecker(ExternalScheme domain) {
      return new AbstractEntitlementChecker() {
        @Override
        public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
          if (user == unauthorizedUser)
          {
            return false;
          }
          else if (user == authorizedUser)
          {
            return true;
          }
          else{
            throw new OpenGammaRuntimeException("Unexpected request for user "+user);
          }
        }
      };
    }
    
    @AfterMethod
    public void teardown() {
      assertEquals(AbstractLiveDataServer.ConnectionStatus.CONNECTED, _combiningServer.getConnectionStatus());
      _combiningServer.stop();
      assertEquals(AbstractLiveDataServer.ConnectionStatus.NOT_CONNECTED, _combiningServer.getConnectionStatus());
    }
    
    @Test
    public void defaultSubscription() {
      LiveDataSpecification spec = new LiveDataSpecification("No Normalization", ExternalId.of(_domainD, "X"));
      LiveDataSubscriptionResponse subscribe = _combiningServer.subscribe(spec, false);
      assertEquals(LiveDataSubscriptionResult.NOT_PRESENT, subscribe.getSubscriptionResult());
    }
    
    @Test(expectedExceptions =  Throwable.class)
    public void failingSubscriptionsDontStopWorking() {
      LiveDataSpecification specWorking = new LiveDataSpecification("No Normalization", ExternalId.of(_domainC, "X"));
      LiveDataSpecification specFailed = new LiveDataSpecification("No Normalization", ExternalId.of(_domainD, "X"));
      LiveDataSubscriptionResponseMsg subscriptionRequestMade = _combiningServer.subscriptionRequestMade(new LiveDataSubscriptionRequest(UserPrincipal.getLocalUser(), SubscriptionType.NON_PERSISTENT,  Lists.newArrayList(specWorking, specFailed)));
      
      assertEquals(2, subscriptionRequestMade.getResponses().size());
      for (LiveDataSubscriptionResponse response : subscriptionRequestMade.getResponses()) {
        if (response.getRequestedSpecification().equals(specWorking))
        {
          assertEquals(LiveDataSubscriptionResult.SUCCESS, response.getSubscriptionResult());    
        }
        else if (response.getRequestedSpecification().equals(specFailed))
        {
          assertEquals(LiveDataSubscriptionResult.INTERNAL_ERROR, response.getSubscriptionResult());    
        }
      }
      
      assertEquals(0, _serverB.getSubscriptions().size());
      assertEquals(1, _serverC.getSubscriptions().size());
      
    }
    @Test
    public void matchingSubscription() {
      LiveDataSpecification spec = new LiveDataSpecification("No Normalization", ExternalId.of(_domainC, "X"));
      LiveDataSubscriptionResponse result = _combiningServer.subscribe(spec, false);
      assertEquals(LiveDataSubscriptionResult.SUCCESS, result.getSubscriptionResult());
      
      assertEquals(0, _serverB.getSubscriptions().size());
      assertEquals(1, _serverC.getSubscriptions().size());
      
    }
    @Test
    public void prioritySubscription() {
      LiveDataSpecification spec = new LiveDataSpecification("No Normalization", ExternalId.of(_domainB, "X"), ExternalId.of(_domainC, "X"));
      LiveDataSubscriptionResponse result = _combiningServer.subscribe(spec, false);
      assertEquals(LiveDataSubscriptionResult.SUCCESS, result.getSubscriptionResult());
      
      assertEquals(1, _serverB.getSubscriptions().size());
      assertEquals(0, _serverC.getSubscriptions().size());
      
    }
    
    @Test
    public void matchingResolution() {
      LiveDataSpecification spec = new LiveDataSpecification("No Normalization", ExternalId.of(_domainC, "X"));
      DistributionSpecification combined = _combiningServer.getDefaultDistributionSpecificationResolver().resolve(spec);
      DistributionSpecification direct = _serverC.getDistributionSpecificationResolver().resolve(spec);
      assertEquals(direct, combined);
    }
    
    @Test
    public void snapshot() {
      MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
      msg.add("FIELD", "VALUE");
      _serverC.addMarketDataMapping("X", msg);
      LiveDataSpecification spec = new LiveDataSpecification("No Normalization", ExternalId.of(_domainC, "X"));
      LiveDataSubscriptionRequest request = new LiveDataSubscriptionRequest(authorizedUser, SubscriptionType.SNAPSHOT, Collections.singleton(spec));
      LiveDataSubscriptionResponseMsg responseMsg = _combiningServer.subscriptionRequestMade(request);
      assertEquals(responseMsg.getRequestingUser(), authorizedUser);
      assertEquals(1, responseMsg.getResponses().size());
      for (LiveDataSubscriptionResponse response : responseMsg.getResponses()) {
        assertEquals(LiveDataSubscriptionResult.SUCCESS, response.getSubscriptionResult());
        LiveDataValueUpdateBean snap = response.getSnapshot();
        assertEquals("VALUE", snap.getFields().getString("FIELD"));
        assertEquals(1, snap.getFields().getNumFields());
      }
      
      assertEquals(0, _serverB.getSubscriptions().size());
      assertEquals(0, _serverC.getSubscriptions().size());
    }
    
    @Test
    public void entitled() {
      LiveDataSpecification spec = new LiveDataSpecification("No Normalization", ExternalId.of(_domainC, "X"));
      LiveDataSubscriptionRequest request = new LiveDataSubscriptionRequest(authorizedUser, SubscriptionType.NON_PERSISTENT, Collections.singleton(spec));
      LiveDataSubscriptionResponseMsg responseMsg = _combiningServer.subscriptionRequestMade(request);
      assertEquals(responseMsg.getRequestingUser(), authorizedUser);
      assertEquals(1, responseMsg.getResponses().size());
      for (LiveDataSubscriptionResponse response : responseMsg.getResponses()) {
        assertEquals(LiveDataSubscriptionResult.SUCCESS, response.getSubscriptionResult()); 
      }
      
      assertEquals(0, _serverB.getSubscriptions().size());
      assertEquals(1, _serverC.getSubscriptions().size());
    }
    
    @Test
    public void notEntitled() {
      LiveDataSpecification spec = new LiveDataSpecification("No Normalization", ExternalId.of(_domainC, "X"));
      LiveDataSubscriptionRequest request = new LiveDataSubscriptionRequest(unauthorizedUser, SubscriptionType.NON_PERSISTENT, Collections.singleton(spec));
      LiveDataSubscriptionResponseMsg responseMsg = _combiningServer.subscriptionRequestMade(request);
      assertEquals(responseMsg.getRequestingUser(), unauthorizedUser);
      assertEquals(1, responseMsg.getResponses().size());
      for (LiveDataSubscriptionResponse response : responseMsg.getResponses()) {
        assertEquals(LiveDataSubscriptionResult.NOT_AUTHORIZED, response.getSubscriptionResult()); 
      }
      
      assertEquals(0, _serverB.getSubscriptions().size());
      assertEquals(0, _serverC.getSubscriptions().size());
    }
}
