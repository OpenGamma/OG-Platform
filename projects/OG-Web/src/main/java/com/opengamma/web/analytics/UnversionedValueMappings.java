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
      Boolean isVersioned = ((ComputationTargetSpecification) ref).getUniqueId().isVersioned();
      Boolean isParentVersionedAndNotNull = (ref.getParent() == null) ? false : ((ComputationTargetSpecification) ref.getParent()).getUniqueId().isVersioned();


      if (isVersioned || isParentVersionedAndNotNull) {
        /* If the parent is versioned and exists create an unversioned copy and create the valueRequirement
         * else if the parent is not versioned or does not exist and the passed in valueRequirement is versioned then
         * create an unversioned copy
         * otherwise return the original
         */
        if (isParentVersionedAndNotNull) {
          ComputationTargetSpecification parent = ref.getParent().getSpecification();
          ComputationTargetSpecification newParentTargetSpec = new ComputationTargetSpecification(parent.getType(), parent.getUniqueId().toLatest());
          ComputationTargetSpecification newTargetSpec = new ComputationTargetSpecification(newParentTargetSpec, ref.getType(), ((ComputationTargetSpecification) ref).getUniqueId().toLatest());
          ValueRequirement undersionedvalueRequirement = new ValueRequirement(valueRequirement.getValueName(), newTargetSpec, valueRequirement.getConstraints());
          return undersionedvalueRequirement;
        } else if (isVersioned) {
          ComputationTargetSpecification newTargetSpec = new ComputationTargetSpecification(ref.getType(), ((ComputationTargetSpecification) ref).getUniqueId().toLatest());
          ValueRequirement undersionedvalueRequirement = new ValueRequirement(valueRequirement.getValueName(), newTargetSpec, valueRequirement.getConstraints());
          return undersionedvalueRequirement;
        } else {
          return valueRequirement;
        }

      }
      return valueRequirement;
    } else {
      return valueRequirement;
    }
  }
}
