/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.opengamma.master.security.ManageableSecurity;

/**
 * Source of security instances from a selection of underlying generators.
 * 
 * @param <T> common security super-type.
 */
public class CombiningSecurityGenerator<T extends ManageableSecurity> extends SecurityGenerator<T> {

  private final List<SecurityGenerator<? extends T>> _generators;

  public CombiningSecurityGenerator(final SecurityGenerator<? extends T>... generators) {
    _generators = Arrays.asList(generators);
  }

  public CombiningSecurityGenerator(final Collection<SecurityGenerator<? extends T>> generators) {
    _generators = new ArrayList<SecurityGenerator<? extends T>>(generators);
  }

  protected List<SecurityGenerator<? extends T>> getGenerators() {
    return _generators;
  }

  @Override
  public T createSecurity() {
    return getGenerators().get(getRandom(getGenerators().size())).createSecurity();
  }

}
