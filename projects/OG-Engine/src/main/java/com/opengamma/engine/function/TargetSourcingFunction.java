/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;

/**
 * Special case of function implementation that is never executed by the graph executor but is used to source computation targets. It will not be considered directly during graph construction; the
 * singleton instance is associated with DependencyNode objects to act as a marker on the node.
 * <p>
 * This should never be present in a function repository as it should never be selected for execution.
 */
public final class TargetSourcingFunction extends IntrinsicFunction {

  /**
   * Property on the value specification indicating that the resolution is suitable for relative referencing (for example by object id or one of the associated external identifiers).
   */
  public static final String REFERENCE_MODE_PROPERTY = "Ref";

  /**
   * Value of the {@link #REFERENCE_MODE_PROPERTY} property when the resolution was based on a relative reference.
   */
  public static final String REFERENCE_MODE_RELATIVE = "Rel";

  /**
   * Singleton instance.
   */
  public static final TargetSourcingFunction INSTANCE = new TargetSourcingFunction();

  /**
   * Function unique ID.
   */
  public static final String UNIQUE_ID = "TargetSourcingFunction";

  private static final ValueProperties RELATIVE_REFERENCED = ValueProperties.with(ValuePropertyNames.FUNCTION, UNIQUE_ID).with(REFERENCE_MODE_PROPERTY, REFERENCE_MODE_RELATIVE).get();

  private static final ValueProperties ABSOLUTE_REFERENCED = ValueProperties.with(ValuePropertyNames.FUNCTION, UNIQUE_ID).get();

  private TargetSourcingFunction() {
    super(UNIQUE_ID);
  }

  public static ValueRequirement createRequirement(final ComputationTargetType type, final UniqueId uid) {
    return createRequirement(new ComputationTargetSpecification(type, uid), ABSOLUTE_REFERENCED);
  }

  public static ValueRequirement createRequirement(final ComputationTargetType type, final ObjectId oid) {
    return createRequirement(new ComputationTargetSpecification(type, oid.atLatestVersion()), RELATIVE_REFERENCED);
  }

  public static ValueRequirement createRequirement(final ComputationTargetType type, final ExternalId eid) {
    return createRequirement(new ComputationTargetRequirement(type, eid), RELATIVE_REFERENCED);
  }

  public static ValueRequirement createRequirement(final ComputationTargetType type, final ExternalIdBundle eids) {
    return createRequirement(new ComputationTargetRequirement(type, eids), RELATIVE_REFERENCED);
  }

  private static ValueRequirement createRequirement(final ComputationTargetReference target, final ValueProperties constraints) {
    return new ValueRequirement(ValueRequirementNames.TARGET, target, constraints);
  }

  public static ValueSpecification createSpecification(final ValueRequirement requirement, final ComputationTargetSpecification target) {
    assert ValueRequirementNames.TARGET.equals(requirement.getValueName());
    return createSpecification(target, requirement.getConstraints());
  }

  public static ValueSpecification createSpecification(final ComputationTargetSpecification target) {
    return createSpecification(target, ABSOLUTE_REFERENCED);
  }

  private static ValueSpecification createSpecification(final ComputationTargetSpecification target, final ValueProperties properties) {
    return new ValueSpecification(ValueRequirementNames.TARGET, target, properties);
  }

}
