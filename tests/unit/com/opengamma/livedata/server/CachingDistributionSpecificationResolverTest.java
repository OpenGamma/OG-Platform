/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.livedata.LiveDataSpecificationImpl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * 
 *
 * @author pietari
 */
public class CachingDistributionSpecificationResolverTest {
  
  @Test
  public void testCaching() {
    
    LiveDataSpecificationImpl testSpec = new LiveDataSpecificationImpl(new DomainSpecificIdentifier("foo", "bar"));
    
    DistributionSpecificationResolver underlying = mock(DistributionSpecificationResolver.class);
    when(underlying.getDistributionSpecification(testSpec)).thenReturn("myDistSpec");
    
    CachingDistributionSpecificationResolver resolver = new CachingDistributionSpecificationResolver(underlying);
    assertEquals("myDistSpec", resolver.getDistributionSpecification(testSpec));
    assertEquals("myDistSpec", resolver.getDistributionSpecification(testSpec));
    
    verify(underlying, times(1)).getDistributionSpecification(testSpec);
  }

}
