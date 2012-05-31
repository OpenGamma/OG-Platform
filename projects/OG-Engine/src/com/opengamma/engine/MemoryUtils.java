/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.WeakInstanceCache;

/**
 * Badly named class collating all of the hacks used to reduce the memory footprint of some algorithms.
 */
public final class MemoryUtils {

  // TODO: Why do we need value specifications everywhere; can't we just work with the longs that go into the jobs? Converting ValueSpecifications to/from the longs
  // is cheap enough - if in the cache, map operations no more costly than the approach here

  private static final WeakInstanceCache<ComputationTargetSpecification> s_computationTargetSpecification = new WeakInstanceCache<ComputationTargetSpecification>();
  private static final WeakInstanceCache<ValueProperties> s_valueProperties = new WeakInstanceCache<ValueProperties>();
  private static final WeakInstanceCache<ValueRequirement> s_valueRequirement = new WeakInstanceCache<ValueRequirement>();
  private static final WeakInstanceCache<ValueSpecification> s_valueSpecification = new WeakInstanceCache<ValueSpecification>();

  private MemoryUtils() {
  }

  public static ComputationTargetSpecification instance(final ComputationTargetSpecification computationTargetSpecification) {
    return s_computationTargetSpecification.get(computationTargetSpecification);
  }

  public static ValueProperties instance(final ValueProperties valueProperties) {
    return s_valueProperties.get(valueProperties);
  }

  public static ValueRequirement instance(final ValueRequirement valueRequirement) {
    final ComputationTargetSpecification ctspec = instance(valueRequirement.getTargetSpecification());
    final ValueProperties constraints = instance(valueRequirement.getConstraints());
    if ((ctspec == valueRequirement.getTargetSpecification()) && (constraints == valueRequirement.getConstraints())) {
      return s_valueRequirement.get(valueRequirement);
    } else {
      return s_valueRequirement.get(new ValueRequirement(valueRequirement.getValueName(), ctspec, constraints));
    }
  }

  public static ValueSpecification instance(final ValueSpecification valueSpecification) {
    final ComputationTargetSpecification ctspec = instance(valueSpecification.getTargetSpecification());
    final ValueProperties properties = instance(valueSpecification.getProperties());
    if ((ctspec == valueSpecification.getTargetSpecification()) && (properties == valueSpecification.getProperties())) {
      return s_valueSpecification.get(valueSpecification);
    } else {
      return s_valueSpecification.get(new ValueSpecification(valueSpecification.getValueName(), ctspec, properties));
    }
  }

  /**
   * Estimate the size of an object in memory. This is based on its serialized form which is crude but better than nothing.
   * 
   * @param object the object to estimate the size of
   * @return the size estimate in bytes
   */
  public static long estimateSize(final Object object) {
    if (object == null) {
      return 0;
    }
    try {
      final File temp = File.createTempFile("object", "bin");
      try {
        final ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(temp)));
        out.writeObject(object);
        out.close();
        return temp.length();
      } finally {
        temp.delete();
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("I/O error", e);
    }
  }

}
