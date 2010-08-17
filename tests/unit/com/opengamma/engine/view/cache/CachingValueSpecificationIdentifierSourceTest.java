/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class CachingValueSpecificationIdentifierSourceTest {
  
  @Test
  public void simpleOperation() {
    final AtomicBoolean shouldFail = new AtomicBoolean(false);
    
    IdentifierMap underlying = new IdentifierMap() {
      @Override
      public long getIdentifier(ValueSpecification spec) {
        if (shouldFail.get()) {
          Assert.fail("Should not have called underlying.");
        }
        return 99L;
      }
    };
    
    CachingIdentifierMap cachingSource = new CachingIdentifierMap(underlying);
    
    ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("scheme", "fibble"))));
    assertEquals(99L, cachingSource.getIdentifier(valueSpec));
    
    shouldFail.set(true);
    assertEquals(99L, cachingSource.getIdentifier(valueSpec));
    assertEquals(99L, cachingSource.getIdentifier(valueSpec));
    assertEquals(99L, cachingSource.getIdentifier(valueSpec));
    assertEquals(99L, cachingSource.getIdentifier(valueSpec));
    assertEquals(99L, cachingSource.getIdentifier(valueSpec));
  }

}
