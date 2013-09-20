/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.management.ValueMappings;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 * Extends {@link ValueMappings} filtering out the {@link ValueRequirement} version for both creation and lookup.
 */
public class UnversionedValueMappings extends ValueMappings {

  public UnversionedValueMappings(CompiledViewDefinition compiledViewDef) {
    super(compiledViewDef);
  }

  public UnversionedValueMappings() {
    super();
  }

  /**
   * As a subclasses of ValueMappings if the ComputationTargetReference is an instance of ComputationTargetSpecification
   * and the unique id is versioned then a new ValueRequirement is created with a unversioned
   * ComputationTargetSpecification
   * @param valueRequirement to check for versioning
   * @return either 'unversioned' or returned unaltered
   */
  @Override
  protected ValueRequirement createRequirement(ValueRequirement valueRequirement) {
    ComputationTargetReference ref = valueRequirement.getTargetReference();

    if (ref instanceof ComputationTargetSpecification) {
      if (((ComputationTargetSpecification) ref).getUniqueId() == null) {
        return valueRequirement;
      }
      if (((ComputationTargetSpecification) ref).getUniqueId().isVersioned()) {
        ComputationTargetSpecification newTargetSpec = new ComputationTargetSpecification(ref.getType(), ((ComputationTargetSpecification) ref).getUniqueId().toLatest());
        ValueRequirement valueReq = new ValueRequirement(valueRequirement.getValueName(), newTargetSpec, valueRequirement.getConstraints());
        return valueReq;
      }
      return valueRequirement;
    } else {
      return valueRequirement;
    }
  }
}
