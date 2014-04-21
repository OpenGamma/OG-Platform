package com.opengamma.master.exchange.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.atomic.AtomicLong;

import net.sf.ehcache.CacheManager;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.exchange.impl.SimpleExchange;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = {TestGroup.UNIT, "ehcache" })
public class EHCachingExchangeSourceTest {

  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCachingExchangeSourceTest.class);
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  //-------------------------------------------------------------------------
  @Test
  public void getById() throws InterruptedException {
    final AtomicLong getCount = new AtomicLong(0);
    final ExchangeSource underlying = Mockito.mock(ExchangeSource.class);
    Mockito.when(underlying.getSingle(Mockito.<ExternalId>anyObject())).thenAnswer(new Answer<Exchange>() {
      @Override
      public Exchange answer(InvocationOnMock invocation) throws Throwable {
        getCount.incrementAndGet();
        SimpleExchange simpleExchange = new SimpleExchange();
        simpleExchange.setUniqueId(UniqueId.of("Test", "Foo", "0"));
        simpleExchange.setExternalIdBundle(ExternalIdBundle.of((ExternalId) invocation.getArguments()[0]));
        return simpleExchange;
      }
    });
    Mockito.when(underlying.changeManager()).thenReturn(Mockito.mock(ChangeManager.class));
    EHCachingExchangeSource source = new EHCachingExchangeSource(underlying, _cacheManager);
    assertEquals(0, getCount.get());
    ExternalScheme scheme = ExternalScheme.of("Scheme");
    ExternalId id = ExternalId.of(scheme, "Value");
    Exchange get1 = source.getSingle(id);
    assertEquals(1, getCount.get());
    assertEquals(1, get1.getExternalIdBundle().size());
    assertEquals(id, get1.getExternalIdBundle().getExternalId(scheme));

    Exchange get2 = source.get(UniqueId.of("Test", "Foo", "0"));
    assertEquals(1, getCount.get());
    assertTrue(get1 == get2);

  }

}
