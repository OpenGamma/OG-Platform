/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = {TestGroup.UNIT, "ehcache"})
public class EHCachingDistributionSpecificationResolverTest {

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
  public void testCaching() {
    ExternalId id = ExternalId.of("foo", "bar");
    
    LiveDataSpecification request = new LiveDataSpecification(
        "TestNormalization",
        ExternalId.of("foo", "bar"));
    
    DistributionSpecification distributionSpec = new DistributionSpecification(id, StandardRules.getNoNormalization(), "testtopic");
    Map<LiveDataSpecification, DistributionSpecification> returnValue = new HashMap<LiveDataSpecification, DistributionSpecification>();
    returnValue.put(request, distributionSpec);        
    
    DistributionSpecificationResolver underlying = mock(DistributionSpecificationResolver.class);
    when(underlying.resolve(Collections.singletonList(request))).thenReturn(returnValue);
    
    EHCachingDistributionSpecificationResolver resolver =
        new EHCachingDistributionSpecificationResolver(underlying, _cacheManager);
    assertEquals(distributionSpec, resolver.resolve(request));
    assertEquals(distributionSpec, resolver.resolve(request));
    
    verify(underlying, times(1)).resolve(Collections.singletonList(request));
  }

}
