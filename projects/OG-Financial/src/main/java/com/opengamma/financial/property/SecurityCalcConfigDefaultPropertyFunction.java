/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Dummy function to inject default properties from the calculation configuration into the dependency graph for security targets.
 */
public abstract class SecurityCalcConfigDefaultPropertyFunction extends CalcConfigDefaultPropertyFunction {

  protected SecurityCalcConfigDefaultPropertyFunction(final boolean identifier) {
    super(ComputationTargetType.SECURITY, identifier);
  }

  @Override
  protected List<String> getIdentifiers(final ComputationTarget target) {
    final ExternalIdBundle externalIds = target.getSecurity().getExternalIdBundle();
    final List<String> identifiers = new ArrayList<String>(externalIds.size() + 1);
    identifiers.add(getUniqueId(target));
    for (ExternalId externalId : externalIds) {
      identifiers.add(externalId.toString());
    }
    return identifiers;
  }

  /**
   * Applies to any matching targets.
   */
  public static class Generic extends SecurityCalcConfigDefaultPropertyFunction {

    public Generic() {
      super(false);
    }

  }

  /**
   * Applies to specifically identified targets only.
   */
  public static class Specific extends SecurityCalcConfigDefaultPropertyFunction {

    public Specific() {
      super(true);
    }

  }

}
