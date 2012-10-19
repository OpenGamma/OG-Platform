package com.opengamma.master.exchange.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
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
      public Exchange getSingle(ExternalIdBundle identifierBundle) {
        return null;
      }
      
      @Override
      public Exchange getSingle(ExternalId identifier) {
        getCount.incrementAndGet();
        SimpleExchange simpleExchange = new SimpleExchange();
        simpleExchange.setExternalIdBundle(ExternalIdBundle.of(identifier));
        return simpleExchange;
      }
      
      @Override
      public Collection<? extends Exchange> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
        return null;
      }
      
      @Override
      public Exchange get(ObjectId objectId, VersionCorrection versionCorrection) {
        return null;
      }
      
      @Override
      public Exchange get(UniqueId uniqueId) {
        return null;
      }

      @Override
      public Map<UniqueId, Exchange> get(Collection<UniqueId> uniqueIds) {
        Map<UniqueId, Exchange> result = Maps.newHashMap();
        for (UniqueId uniqueId : uniqueIds) {
          try {
            Exchange exchange = get(uniqueId);
            result.put(uniqueId, exchange);
          } catch (DataNotFoundException ex) {
            // do nothing
          }
        }
        return result;
      }
    };
    long ttl = Timeout.standardTimeoutSeconds();
    EHCachingExchangeSource source = new EHCachingExchangeSource(underlying, EHCacheUtils.createCacheManager());
    source.setTTL((int) ttl);
    assertEquals(0, getCount.get());
    ExternalScheme scheme = ExternalScheme.of("Scheme");
    ExternalId id = ExternalId.of(scheme, "Value");
    Exchange get1 = source.getSingle(id);
    assertEquals(1, getCount.get());
    assertEquals(1, get1.getExternalIdBundle().size());
    assertEquals(id, get1.getExternalIdBundle().getExternalId(scheme));

    Exchange get2 = source.getSingle(id);
    assertEquals(1, getCount.get());
    assertTrue(get1 == get2);
    
    Thread.sleep(ttl * 2000);

    Exchange get3 = source.getSingle(id);
    assertEquals(2, getCount.get());
    assertTrue(get3 != get2);
  }
}
