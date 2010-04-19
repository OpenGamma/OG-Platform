/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.resolver.CachingDistributionSpecificationResolver;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.DistributionSpecification;

/**
 * 
 *
 * @author pietari
 */
public class CachingDistributionSpecificationResolverTest {
  
  @Test
  public void testCaching() {
    
    Identifier id = new Identifier("foo", "bar");
    
    LiveDataSpecification request = new LiveDataSpecification(
        "TestNormalization",
        new Identifier("foo", "bar"));
    
    DistributionSpecification returnValue = new DistributionSpecification(
        new IdentifierBundle(id),
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
