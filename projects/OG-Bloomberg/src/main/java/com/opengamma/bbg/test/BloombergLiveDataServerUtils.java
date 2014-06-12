/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.test;

import java.lang.reflect.Method;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.livedata.BloombergLiveDataServer;
import com.opengamma.bbg.livedata.faketicks.CombiningBloombergLiveDataServer;
import com.opengamma.bbg.livedata.faketicks.FakeSubscriptionBloombergLiveDataServer;
import com.opengamma.bbg.livedata.faketicks.FakeSubscriptionSelector;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.AbstractInvalidFieldCachingReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.AbstractValueCachingReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.CombiningLiveDataServer;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Test utilities for Bloomberg.
 */
public class BloombergLiveDataServerUtils {

  /**
   * Gets a reference data provider for a class, defined by a method.
   * 
   * @param testMethod  the test method, not null
   * @return the data provider, not null
   */
  public static ReferenceDataProvider getCachingReferenceDataProvider(Method testMethod) {
    return getCachingReferenceDataProvider(testMethod.getClass());
  }

  /**
   * Gets a reference data provider for a class.
   * 
   * @param testClass  the test class, not null
   * @return the data provider, not null
   */
  public static ReferenceDataProvider getCachingReferenceDataProvider(Class<?> testClass) {
    BloombergReferenceDataProvider brdp = getUnderlyingProvider();
    return getCachingReferenceDataProvider(brdp, testClass);
  }

  /**
   * Adds caching to a reference data provider.
   * 
   * @param underlying  the underlying provider, not null
   * @param testClass  the test class, not null
   * @return the data provider, not null
   */
  private static ReferenceDataProvider getCachingReferenceDataProvider(ReferenceDataProvider underlying, Class<?> testClass) {
    return MongoCachedReferenceData.makeMongoProvider(underlying, testClass);
  }

  /**
   * Creates a Bloomberg reference data provider, that has been started, for testing.
   * 
   * @return the provider, not null
   */
  public static BloombergReferenceDataProvider getUnderlyingProvider() {
    BloombergConnector connector = BloombergTestUtils.getBloombergConnector();
    BloombergReferenceDataProvider brdp = new BloombergReferenceDataProvider(connector);
    brdp.start();
    return brdp;
  }

  /**
   * Stops the specified reference data provider, as best as possible.
   * 
   * @param refDataProvider  the provider to stop, null ignored
   */
  public static void stopCachingReferenceDataProvider(ReferenceDataProvider refDataProvider) {
    if (refDataProvider != null) {
      if (refDataProvider instanceof BloombergReferenceDataProvider) {
        BloombergReferenceDataProvider bbgProvider = (BloombergReferenceDataProvider) refDataProvider;
        bbgProvider.stop();
        
      } else if (refDataProvider instanceof AbstractValueCachingReferenceDataProvider) {
        stopCachingReferenceDataProvider(((AbstractValueCachingReferenceDataProvider) refDataProvider).getUnderlying());
        
      } else if (refDataProvider instanceof AbstractInvalidFieldCachingReferenceDataProvider) {
        stopCachingReferenceDataProvider(((AbstractInvalidFieldCachingReferenceDataProvider) refDataProvider).getUnderlying());
      }
    }
  }

  //-------------------------------------------------------------------------
  public static BloombergLiveDataServer startTestServer(Method testMethod) {
    return startTestServer(testMethod.getClass());
  }

  public static BloombergLiveDataServer startTestServer(Class<?> testClass) {
    ReferenceDataProvider refDataProvider = getCachingReferenceDataProvider(testClass);
    return getTestServer(refDataProvider);
  }

  public static void stopTestServer(BloombergLiveDataServer testServer) {
    stopCachingReferenceDataProvider(testServer.getReferenceDataProvider());
    testServer.stop();
  }

  public static BloombergLiveDataServer getTestServer(ReferenceDataProvider cachingRefDataProvider) {
    FudgeMessageSender fudgeMessageSender = new FudgeMessageSender() {
      @Override
      public void send(FudgeMsg message) {
        // do nothing
      }
      @Override
      public FudgeContext getFudgeContext() {
        return OpenGammaFudgeContext.getInstance();
      }
    };
    BloombergLiveDataServer server = new BloombergLiveDataServer(BloombergTestUtils.getBloombergConnector(),
                                                                 cachingRefDataProvider,
                                                                 EHCacheUtils.createCacheManager(),
                                                                 fudgeMessageSender);
    DistributionSpecificationResolver distributionSpecificationResolver = server.getDefaultDistributionSpecificationResolver();
    server.setDistributionSpecificationResolver(distributionSpecificationResolver);
    
    server.start();
    return server;
  }

  public static CombiningBloombergLiveDataServer startTestServer(Class<?> testClass, FakeSubscriptionSelector subscriptionSelector, ReferenceDataProvider refDataProvider) {
    ReferenceDataProvider cachingRefDataProvider = getCachingReferenceDataProvider(refDataProvider, testClass);
    BloombergLiveDataServer underlying = getTestServer(cachingRefDataProvider);
    
    CacheManager cacheManager = EHCacheUtils.createCacheManager();
    FakeSubscriptionBloombergLiveDataServer fakeServer = new FakeSubscriptionBloombergLiveDataServer(underlying, ExternalSchemes.BLOOMBERG_BUID_WEAK, cacheManager);
    fakeServer.start();
    
    CombiningBloombergLiveDataServer combinedServer = new CombiningBloombergLiveDataServer(fakeServer, underlying, subscriptionSelector, cacheManager);
        
    combinedServer.start();
    return combinedServer;
  }

  public static void stopTestServer(StandardLiveDataServer server) {
    if (server instanceof BloombergLiveDataServer) {
      stopTestServer((BloombergLiveDataServer) server);
    } else if (server instanceof CombiningLiveDataServer) {
      stopTestServer(((CombiningBloombergLiveDataServer) server).getFakeServer());
    }
  }

}
