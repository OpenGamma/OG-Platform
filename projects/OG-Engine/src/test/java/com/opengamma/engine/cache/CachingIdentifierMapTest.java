/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import static org.testng.Assert.assertEquals;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link CachingIdentifierMap} class.
 */
@Test(groups = TestGroup.UNIT)
public class CachingIdentifierMapTest {

  private ValueSpecification createValueSpec(final int id) {
    return new ValueSpecification(Integer.toString(id), ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").get());
  }

  public void testGetIdentifier() {
    final ValueSpecification spec1 = createValueSpec(1);
    final IdentifierMap underlying = Mockito.mock(IdentifierMap.class);
    final CachingIdentifierMap cache = new CachingIdentifierMap(underlying);
    Mockito.when(underlying.getIdentifier(spec1)).thenReturn(1L);
    assertEquals(cache.getIdentifier(spec1), 1L);
    assertEquals(cache.getIdentifier(spec1), 1L);
    Mockito.verify(underlying, Mockito.times(1)).getIdentifier(spec1);
  }

  public void testGetValueSpecification() {
    final ValueSpecification spec1 = createValueSpec(1);
    final IdentifierMap underlying = Mockito.mock(IdentifierMap.class);
    final CachingIdentifierMap cache = new CachingIdentifierMap(underlying);
    Mockito.when(underlying.getValueSpecification(1L)).thenReturn(spec1);
    assertEquals(cache.getValueSpecification(1L), spec1);
    assertEquals(cache.getValueSpecification(1L), spec1);
    Mockito.verify(underlying, Mockito.times(1)).getValueSpecification(1L);
  }

  public void testGetIdentifiers() {
    final ValueSpecification spec1 = createValueSpec(1);
    final ValueSpecification spec2 = createValueSpec(2);
    final ValueSpecification spec3 = createValueSpec(3);
    final Collection<ValueSpecification> spec1And2 = new LinkedList<ValueSpecification>();
    spec1And2.add(spec1);
    spec1And2.add(spec2);
    final Object2LongMap<ValueSpecification> identifier1And2 = new Object2LongOpenHashMap<ValueSpecification>();
    identifier1And2.put(spec1, 1L);
    identifier1And2.put(spec2, 2L);
    final IdentifierMap underlying = Mockito.mock(IdentifierMap.class);
    final CachingIdentifierMap cache = new CachingIdentifierMap(underlying);
    Mockito.when(underlying.getIdentifiers(spec1And2)).thenReturn(identifier1And2);
    Mockito.when(underlying.getIdentifier(spec3)).thenReturn(3L);
    Object2LongMap<ValueSpecification> result = cache.getIdentifiers(Arrays.asList(spec1, spec2));
    assertEquals(result.size(), 2);
    assertEquals((long) result.get(spec1), 1L);
    assertEquals((long) result.get(spec2), 2L);
    result = cache.getIdentifiers(Arrays.asList(spec1, spec2));
    assertEquals(result.size(), 2);
    assertEquals((long) result.get(spec1), 1L);
    assertEquals((long) result.get(spec2), 2L);
    Mockito.verify(underlying, Mockito.times(1)).getIdentifiers(spec1And2);
    result = cache.getIdentifiers(Arrays.asList(spec2, spec3));
    assertEquals(result.size(), 2);
    assertEquals((long) result.get(spec2), 2L);
    assertEquals((long) result.get(spec3), 3L);
    result = cache.getIdentifiers(Arrays.asList(spec2, spec3));
    assertEquals(result.size(), 2);
    assertEquals((long) result.get(spec2), 2L);
    assertEquals((long) result.get(spec3), 3L);
    Mockito.verify(underlying, Mockito.times(1)).getIdentifier(spec3);
    result = cache.getIdentifiers(Arrays.asList(spec1, spec2, spec3));
    assertEquals(result.size(), 3);
    assertEquals((long) result.get(spec1), 1L);
    assertEquals((long) result.get(spec2), 2L);
    assertEquals((long) result.get(spec3), 3L);
    Mockito.verifyNoMoreInteractions(underlying);
  }

  public void testGetValueSpecifications() {
    final ValueSpecification spec1 = createValueSpec(1);
    final ValueSpecification spec2 = createValueSpec(2);
    final ValueSpecification spec3 = createValueSpec(3);
    final Long2ObjectMap<ValueSpecification> spec1And2 = new Long2ObjectOpenHashMap<ValueSpecification>(new long[] {1L, 2L }, new ValueSpecification[] {spec1, spec2 });
    final IdentifierMap underlying = Mockito.mock(IdentifierMap.class);
    final CachingIdentifierMap cache = new CachingIdentifierMap(underlying);
    Mockito.when(underlying.getValueSpecifications(new LongArrayList(new long[] {1L, 2L }))).thenReturn(spec1And2);
    Mockito.when(underlying.getValueSpecification(3L)).thenReturn(spec3);
    Long2ObjectMap<ValueSpecification> result = cache.getValueSpecifications(new LongArrayList(new long[] {1L, 2L }));
    assertEquals(result.size(), 2);
    assertEquals(result.get(1L), spec1);
    assertEquals(result.get(2L), spec2);
    result = cache.getValueSpecifications(new LongArrayList(new long[] {1L, 2L }));
    assertEquals(result.size(), 2);
    assertEquals(result.get(1L), spec1);
    assertEquals(result.get(2L), spec2);
    Mockito.verify(underlying, Mockito.times(1)).getValueSpecifications(new LongArrayList(new long[] {1L, 2L }));
    result = cache.getValueSpecifications(new LongArrayList(new long[] {2L, 3L }));
    assertEquals(result.size(), 2);
    assertEquals(result.get(2L), spec2);
    assertEquals(result.get(3L), spec3);
    result = cache.getValueSpecifications(new LongArrayList(new long[] {2L, 3L }));
    assertEquals(result.size(), 2);
    assertEquals(result.get(2L), spec2);
    assertEquals(result.get(3L), spec3);
    Mockito.verify(underlying, Mockito.times(1)).getValueSpecification(3L);
    result = cache.getValueSpecifications(new LongArrayList(new long[] {1L, 2L, 3L }));
    assertEquals(result.size(), 3);
    assertEquals(result.get(1L), spec1);
    assertEquals(result.get(2L), spec2);
    assertEquals(result.get(3L), spec3);
    result = cache.getValueSpecifications(new LongArrayList(new long[] {1L, 2L, 3L }));
    assertEquals(result.size(), 3);
    assertEquals(result.get(1L), spec1);
    assertEquals(result.get(2L), spec2);
    assertEquals(result.get(3L), spec3);
    Mockito.verifyNoMoreInteractions(underlying);
  }

}
