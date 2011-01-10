/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.DistributionSpecification;

/**
 * 
 *
 */
public class CachingDistributionSpecificationResolverTest {
  
  @Test
  public void testCaching() {
    
    Identifier id = Identifier.of("foo", "bar");
    
    LiveDataSpecification request = new LiveDataSpecification(
        "TestNormalization",
        Identifier.of("foo", "bar"));
    
    DistributionSpecification returnValue = new DistributionSpecification(
        id,
        StandardRules.getNoNormalization(),
        "testtopic");
    
    DistributionSpecificationResolver underlying = mock(DistributionSpecificationResolver.class);
    when(underlying.getDistributionSpecification(request)).thenReturn(returnValue);
    
    CachingDistributionSpecificationResolver resolver = new CachingDistributionSpecificationResolver(underlying);
    assertEquals(returnValue, resolver.getDistributionSpecification(request));
    assertEquals(returnValue, resolver.getDistributionSpecification(request));
    
    verify(underlying, times(1)).getDistributionSpecification(request);
  }

}
