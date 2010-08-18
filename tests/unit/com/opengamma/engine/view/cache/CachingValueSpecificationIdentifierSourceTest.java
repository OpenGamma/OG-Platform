/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.tuple.Pair;

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

      @Override
      public Map<ValueSpecification, Long> getIdentifiers(Collection<ValueSpecification> specs) {
        if (shouldFail.get()) {
          Assert.fail("Should not have called underlying.");
        }
        final Map<ValueSpecification, Long> identifiers = new HashMap<ValueSpecification, Long> ();
        for (ValueSpecification spec : specs) {
          identifiers.put (spec, 98L);
        }
        return identifiers;
      }
    };
    
    CachingIdentifierMap cachingSource = new CachingIdentifierMap(underlying);
    
    final ValueSpecification valueSpec1 = new ValueSpecification(new ValueRequirement("value1", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("scheme", "fibble"))));
    final ValueSpecification valueSpec2 = new ValueSpecification(new ValueRequirement("value2", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("scheme", "fibble"))));
    final ValueSpecification valueSpec3 = new ValueSpecification(new ValueRequirement("value3", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("scheme", "fibble"))));
    assertEquals(99L, cachingSource.getIdentifier(valueSpec1));
    final Map<ValueSpecification, Long> identifiers = new HashMap<ValueSpecification, Long> ();
    identifiers.put (valueSpec2, 98L);
    identifiers.put (valueSpec3, 98L);
    assertEquals (identifiers, cachingSource.getIdentifiers (Arrays.asList(valueSpec2, valueSpec3)));
    
    shouldFail.set(true);
    assertEquals(99L, cachingSource.getIdentifier(valueSpec1));
    assertEquals(99L, cachingSource.getIdentifier(valueSpec1));
    assertEquals(98L, cachingSource.getIdentifier(valueSpec2));
    assertEquals(98L, cachingSource.getIdentifier(valueSpec2));
    assertEquals(98L, cachingSource.getIdentifier(valueSpec3));
    assertEquals(98L, cachingSource.getIdentifier(valueSpec3));
  }

}
