package com.opengamma.livedata.server;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

@Test(groups = {TestGroup.UNIT, "ehcache"})
public class AbstractPersistentSubscriptionManagerTest {
  
  //TODO test async logic
  private final ExternalScheme _scheme = ExternalScheme.of("SomeScheme");
  private final String _normalizationRulesetId = StandardRules.getNoNormalization().getId();
  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testNormalStartup() throws InterruptedException {
    MockLiveDataServer server = new MockLiveDataServer(_scheme, _cacheManager);
    TestPersistentSubscriptionManager subManager = new TestPersistentSubscriptionManager(server);
    String ticker = "X";
    subManager.getPendingReads().add(Sets.newHashSet(getSubscription(ticker)));
    server.start();
    subManager.start();
    Thread.sleep(1000);
    assertEquals(Sets.newHashSet(ticker), server.getActiveSubscriptionIds());
    subManager.stop();
    server.stop();
  }

  @Test
  public void testLateRefresh() throws InterruptedException {
    MockLiveDataServer server = new MockLiveDataServer(_scheme, _cacheManager);
    TestPersistentSubscriptionManager subManager = new TestPersistentSubscriptionManager(server);
    String ticker = "X";
    subManager.getPendingReads().add(new HashSet<PersistentSubscription>());
    subManager.getPendingReads().add(Sets.newHashSet(getSubscription(ticker)));
    server.start();
    subManager.start();
    Thread.sleep(1000);
    assertEquals(new HashSet<String>(), server.getActiveSubscriptionIds());
    
    server.subscribe(ticker);
    
    assertEquals(Sets.newHashSet(ticker), server.getActiveSubscriptionIds());
    MarketDataDistributor marketDataDistributor = server.getMarketDataDistributor(ticker);
    assertEquals(false, marketDataDistributor.isPersistent());
    
    subManager.refresh();
    Thread.sleep(1000);
    assertEquals(true, marketDataDistributor.isPersistent());
    assertEquals(Sets.newHashSet(ticker), server.getActiveSubscriptionIds());
    subManager.stop();
    server.stop();
  }
  
  private PersistentSubscription getSubscription(String ticker) {
    return new PersistentSubscription(getSpec(ticker));
  }

  private LiveDataSpecification getSpec(String ticker) {
    return new LiveDataSpecification(_normalizationRulesetId, ExternalId.of(_scheme, ticker));
  }
  
  class TestPersistentSubscriptionManager extends AbstractPersistentSubscriptionManager  {

    public TestPersistentSubscriptionManager(StandardLiveDataServer server) {
      super(server);
    }

    private final Queue<Set<PersistentSubscription>> _pendingReads = new LinkedBlockingQueue<Set<PersistentSubscription>>();
    private final Queue<Set<PersistentSubscription>> _pendingWrites= new LinkedBlockingQueue<Set<PersistentSubscription>>();
    
    @Override
    protected void readFromStorage() {
      Set<PersistentSubscription> remove = _pendingReads.remove();
      for (PersistentSubscription persistentSubscription : remove) {
        addPersistentSubscription(persistentSubscription);
      }
    }

    @Override
    public void saveToStorage(Set<PersistentSubscription> newState) {
      _pendingWrites.add(newState);
    }

    public Queue<Set<PersistentSubscription>> getPendingReads() {
      return _pendingReads;
    }

    public Queue<Set<PersistentSubscription>> getPendingWrites() {
      return _pendingWrites;
    }
  }

}
