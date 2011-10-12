package com.opengamma.master.exchange.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;

import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.exchange.impl.SimpleExchange;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.Timeout;

public class EHCachingExchangeSourceTest {

  @Test
  public void getById() throws InterruptedException
  {
    final AtomicLong getCount = new AtomicLong(0);
    ExchangeSource underlying = new ExchangeSource() {
      
      @Override
      public Exchange getSingleExchange(ExternalIdBundle identifierBundle) {
        return null;
      }
      
      @Override
      public Exchange getSingleExchange(ExternalId identifier) {
        getCount.incrementAndGet();
        SimpleExchange simpleExchange = new SimpleExchange();
        simpleExchange.setExternalIdBundle(ExternalIdBundle.of(identifier));
        return simpleExchange;
      }
      
      @Override
      public Collection<? extends Exchange> getExchanges(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
        return null;
      }
      
      @Override
      public Exchange getExchange(ObjectId objectId, VersionCorrection versionCorrection) {
        return null;
      }
      
      @Override
      public Exchange getExchange(UniqueId uniqueId) {
        return null;
      }
    };
    long ttl = Timeout.standardTimeoutSeconds();
    EHCachingExchangeSource source = new EHCachingExchangeSource(underlying, EHCacheUtils.createCacheManager());
    source.setTTL((int) ttl);
    assertEquals(0, getCount.get());
    ExternalScheme scheme = ExternalScheme.of("Scheme");
    ExternalId id = ExternalId.of(scheme, "Value");
    Exchange get1 = source.getSingleExchange(id);
    assertEquals(1, getCount.get());
    assertEquals(1, get1.getExternalIdBundle().size());
    assertEquals(id, get1.getExternalIdBundle().getExternalId(scheme));
    
    Exchange get2 = source.getSingleExchange(id);
    assertEquals(1, getCount.get());
    assertTrue(get1 == get2);
    
    Thread.sleep(ttl * 2000);
    
    Exchange get3 = source.getSingleExchange(id);
    assertEquals(2, getCount.get());
    assertTrue(get3 != get2);
  }
}
