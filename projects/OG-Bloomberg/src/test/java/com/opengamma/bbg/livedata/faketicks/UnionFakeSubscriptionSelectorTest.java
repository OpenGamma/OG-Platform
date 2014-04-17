package com.opengamma.bbg.livedata.faketicks;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.client.JmsLiveDataClient;
import com.opengamma.livedata.test.LiveDataClientTestUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class UnionFakeSubscriptionSelectorTest {

  private BloombergReferenceDataProvider _underlying;
  private FakeSubscriptionBloombergLiveDataServer _server;
  private JmsLiveDataClient _liveDataClient;

  @BeforeClass
  public void setUpClass() {
    _underlying = BloombergLiveDataServerUtils.getUnderlyingProvider();
    
    _server = BloombergLiveDataServerUtils.startTestServer(UnionFakeSubscriptionSelectorTest.class, new UnionFakeSubscriptionSelector(),
        _underlying).getFakeServer();
    _liveDataClient = LiveDataClientTestUtils.getJmsClient(_server);
  }

  @AfterClass
  public void tearDownClass() {
    BloombergLiveDataServerUtils.stopTestServer(_server);
    _liveDataClient.stop();
    _underlying.stop();
  }

  //-------------------------------------------------------------------------
  @Test
  public void splitCorrectly() throws Exception {
    final ArrayList<String> queries = Lists.newArrayList("BPSW13 Curncy", //SWAP 
        "USPL30RK Curncy", //SWAPTION VOLATILITY 
        "AAPL US 01/21/12 C145 Equity" //EQUITY OPTION
        );
    
    ByTypeFakeSubscriptionSelector swapVol = new ByTypeFakeSubscriptionSelector("SWAPTION VOLATILITY");
    ByTypeFakeSubscriptionSelector fxOptionVol = new ByTypeFakeSubscriptionSelector("SWAP");
    UnionFakeSubscriptionSelector union = new UnionFakeSubscriptionSelector(swapVol, fxOptionVol);
    
    ByTypeFakeSubscriptionSelector alternateUnion = new ByTypeFakeSubscriptionSelector("SWAPTION VOLATILITY", "SWAP");
    
    ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFake = union.splitShouldFake(_server, getSpecs(queries));
    assertEquals(queries.size(), splitShouldFake.first.size() + splitShouldFake.second.size());
    
    ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFakeAlt = alternateUnion.splitShouldFake(_server, getSpecs(queries));
    assertEquals(splitShouldFake.first, splitShouldFakeAlt.first);
    assertEquals(splitShouldFake.second, splitShouldFakeAlt.second);
  }

  @Test
  public void splitEconomicallyInOrder() throws Exception {
    final ByTypeFakeSubscriptionSelector swap = new ByTypeFakeSubscriptionSelector("SWAP");
    FakeSubscriptionSelector noSwaps = new FakeSubscriptionSelector() {
      @Override
      public ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFake(
          FakeSubscriptionBloombergLiveDataServer server, Collection<LiveDataSpecification> uniqueIds) {
        assertTrue(swap.splitShouldFake(server, uniqueIds).second.isEmpty());
        return ObjectsPair.of((Collection<LiveDataSpecification>) new HashSet<LiveDataSpecification>(), uniqueIds);
      }
    };
    ByTypeFakeSubscriptionSelector swaptionVol = new ByTypeFakeSubscriptionSelector("SWAPTION VOLATILITY ");
    FakeSubscriptionSelector throwing = new FakeSubscriptionSelector() {
      @Override
      public ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFake(
          FakeSubscriptionBloombergLiveDataServer server, Collection<LiveDataSpecification> uniqueIds) {
        throw new IllegalArgumentException();
      }
    };
    
    UnionFakeSubscriptionSelector union = new UnionFakeSubscriptionSelector(swap, noSwaps, swaptionVol, throwing);
    

    Collection<String> queries = Sets.newHashSet("BPSW13 Curncy", 
        "USPL30RK Curncy");
    ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFake = union.splitShouldFake(_server, getSpecs(queries));
    assertEquals(Sets.newHashSet(), splitShouldFake.first);
    assertEquals(queries.size(), splitShouldFake.second.size());
  }

  @Test
  public void splitBrokenWorks() {
    final ArrayList<String> queries = Lists.newArrayList("USSV15F Curncy", //Broken
        "USPL30RK Curncy" //SWAPTION VOLATILITY 
        );
    
    ByTypeFakeSubscriptionSelector swapVol = new ByTypeFakeSubscriptionSelector("SWAPTION VOLATILITY");
    ByTypeFakeSubscriptionSelector fxOptionVol = new ByTypeFakeSubscriptionSelector("SWAP");
    UnionFakeSubscriptionSelector union = new UnionFakeSubscriptionSelector(swapVol, fxOptionVol);
    
    ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFake = union.splitShouldFake(_server, getSpecs(queries));
    assertEquals(queries.size(), splitShouldFake.first.size() + splitShouldFake.second.size());
    assertEquals(1, splitShouldFake.first.size());
    assertEquals(1, splitShouldFake.second.size());
  }
  private Collection<LiveDataSpecification> getSpecs(Collection<String> queries) {
    Set<LiveDataSpecification> specs = new HashSet<LiveDataSpecification>();
    for (String query : queries) {
      specs.add(new LiveDataSpecification("No Normalization", ExternalIdBundle.of(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, query))));
    }
    return specs;
  }

}
