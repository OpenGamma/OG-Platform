package com.opengamma.livedata.server.combining;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.livedata.server.MockDistributionSpecificationResolver;
import com.opengamma.livedata.server.MockLiveDataServer;

public class PriorityResolvingCombiningLiveDataServerTest {

    
    private IdentificationScheme _domainB;
    private IdentificationScheme _domainC;
    private IdentificationScheme _domainD;
    private MockLiveDataServer _serverB;
    private MockLiveDataServer _serverC;
    private PriorityResolvingCombiningLiveDataServer _combiningServer;
    
    @BeforeMethod
    public void setUp() {
      
      _domainB = IdentificationScheme.of("B");
      _serverB = new MockLiveDataServer(_domainB);
      _serverB.setDistributionSpecificationResolver(new MockDistributionSpecificationResolver(_domainB));
      _serverB.connect();
      
      _domainC = IdentificationScheme.of("C");
      _serverC = new MockLiveDataServer(_domainC);
      _serverC.setDistributionSpecificationResolver(new MockDistributionSpecificationResolver(_domainC));
      _serverC.connect();
      
      _combiningServer = new PriorityResolvingCombiningLiveDataServer(Lists.newArrayList(_serverB, _serverC));
      _combiningServer.start();
      
      _domainD = IdentificationScheme.of("D");
    }
    
    @Test(expectedExceptions =  Throwable.class)
    public void defaultSubscription() {
      LiveDataSpecification spec = new LiveDataSpecification("No Normalization", Identifier.of(_domainD, "X"));
      _combiningServer.subscribe(spec, false);
    }
    
    @Test(expectedExceptions =  Throwable.class)
    public void failingSubscriptionsDontStopWorking() {
      LiveDataSpecification specWorking = new LiveDataSpecification("No Normalization", Identifier.of(_domainC, "X"));
      LiveDataSpecification specFailed = new LiveDataSpecification("No Normalization", Identifier.of(_domainD, "X"));
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
      LiveDataSpecification spec = new LiveDataSpecification("No Normalization", Identifier.of(_domainC, "X"));
      LiveDataSubscriptionResponse result = _combiningServer.subscribe(spec, false);
      assertEquals(LiveDataSubscriptionResult.SUCCESS, result.getSubscriptionResult());
      
      assertEquals(0, _serverB.getSubscriptions().size());
      assertEquals(1, _serverC.getSubscriptions().size());
      
    }
    @Test
    public void prioritySubscription() {
      LiveDataSpecification spec = new LiveDataSpecification("No Normalization", Identifier.of(_domainB, "X"), Identifier.of(_domainC, "X"));
      LiveDataSubscriptionResponse result = _combiningServer.subscribe(spec, false);
      assertEquals(LiveDataSubscriptionResult.SUCCESS, result.getSubscriptionResult());
      
      assertEquals(1, _serverB.getSubscriptions().size());
      assertEquals(0, _serverC.getSubscriptions().size());
      
    }
}
