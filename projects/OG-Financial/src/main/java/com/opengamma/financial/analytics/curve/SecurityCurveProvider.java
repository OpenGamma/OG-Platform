/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;

/**
 * 
 */
public class SecurityCurveProvider extends CurveProvider<Object> {
  private static final CurveConfigurationForSecurityVisitor PER_SECURITY_VISITOR = new CurveConfigurationForSecurityVisitor();

  @Override
  public Set<ValueRequirement> getValueRequirements(final Object target, final InstrumentExposureConfiguration exposureConfiguration) {
    FinancialSecurity security;
    if (target instanceof FinancialSecurity) {
      security = (FinancialSecurity) target;
    } else if (target instanceof Trade) {
      security = (FinancialSecurity) ((Trade) target).getSecurity();
    } else {
      throw new OpenGammaRuntimeException("Unhandled type " + target);
    }
    final List<List<ExternalId>> ids = security.accept(PER_SECURITY_VISITOR);
    for (final List<ExternalId> idList : ids) {
      final Set<ValueRequirement> requirements = new HashSet<>();
      for (final ExternalId id : idList) {
        requirements.addAll(getValueRequirements(id, exposureConfiguration));
      }
      if (!requirements.isEmpty()) {
        return requirements;
      }
    }
    throw new OpenGammaRuntimeException("Could not get yield curve value requirements for " + target);
  }

}
