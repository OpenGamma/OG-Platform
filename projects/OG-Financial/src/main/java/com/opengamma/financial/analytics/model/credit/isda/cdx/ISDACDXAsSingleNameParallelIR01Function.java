/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdx;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaParallelIR01CDSFunction;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class ISDACDXAsSingleNameParallelIR01Function extends ISDACDXAsSingleNameIR01Function {

  public ISDACDXAsSingleNameParallelIR01Function() {
    super(ValueRequirementNames.IR01);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapDefinition definition,
                                                final ISDACompliantYieldCurve yieldCurve,
                                                final ZonedDateTime[] times,
                                                final double[] marketSpreads,
                                                final ZonedDateTime valuationDate,
                                                final ComputationTarget target,
                                                final ValueProperties properties,
                                                final FunctionInputs inputs,
                                                ISDACompliantCreditCurve hazardCurve,
                                                CDSAnalytic analytic, Tenor[] tenors) {
    final double ir01 = StandardVanillaParallelIR01CDSFunction.getParallelIR01(definition, yieldCurve, properties, hazardCurve, analytic);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.IR01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, ir01));
  }

}
