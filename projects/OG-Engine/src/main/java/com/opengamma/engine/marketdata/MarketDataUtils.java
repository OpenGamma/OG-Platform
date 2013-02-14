/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.functional.Function1;

/**
 * Helper methods for working with the market data interfaces.
 */
@PublicAPI
public class MarketDataUtils {

  // [PLAT-3044] Most of the methods here should not be necessary. Arbitrary conversion from external id bundles to unique identifiers is unlikely to be a good idea. Review and deprecate/delete.

  private static ComputationTargetReferenceVisitor<ComputationTargetSpecification> s_getSpecification = new ComputationTargetReferenceVisitor<ComputationTargetSpecification>() {

    @Override
    public ComputationTargetSpecification visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
      return null;
    }

    @Override
    public ComputationTargetSpecification visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
      return specification;
    }

  };

  public static ValueSpecification createMarketDataValue(final ValueRequirement requirement, final Function1<ValueRequirement, UniqueId> uidLookup) {
    ComputationTargetSpecification targetSpec = requirement.getTargetReference().accept(s_getSpecification);
    if (targetSpec == null) {
      targetSpec = new ComputationTargetSpecification(requirement.getTargetReference().getType(), uidLookup.execute(requirement));
    }
    return createMarketDataValue(requirement.getValueName(), targetSpec, requirement.getConstraints());
  }

  /**
   * Creates a specification that can be returned as a result by a data provider that satisfies the given requirement.
   * 
   * @param requirement the original requirement to be satisfied, not null
   * @param target the satisfying target identifier - to be used if the requirement did not include a unique identifier, not null
   * @return a satisfying value specification
   */
  public static ValueSpecification createMarketDataValue(final ValueRequirement requirement, final ExternalId target) {
    return createMarketDataValue(requirement, UniqueId.of(target.getScheme().getName(), target.getValue()));
  }

  /**
   * Creates a specification that can be returned as a result by a data provider that satisfied the given requirement.
   * 
   * @param requirement the original requirement to be satisfied, not null
   * @param target the satisfying target identifier - to be used if the requirement did not include a unique identifier, not null
   * @return a satisfying value specification
   */
  public static ValueSpecification createMarketDataValue(final ValueRequirement requirement, final UniqueId target) {
    ComputationTargetSpecification targetSpec = requirement.getTargetReference().accept(s_getSpecification);
    if (targetSpec == null) {
      targetSpec = new ComputationTargetSpecification(requirement.getTargetReference().getType(), target);
    }
    return createMarketDataValue(requirement.getValueName(), targetSpec, requirement.getConstraints());
  }

  /**
   * Creates a specification that can be returned as a result by a data provider that satisfies the given requirement.
   * 
   * @param valueName the value name that is satisfied, not null
   * @param target the computation target, not null
   * @return a satisfying value specification
   */
  public static ValueSpecification createMarketDataValue(final String valueName, final ComputationTargetSpecification target) {
    return createMarketDataValue(valueName, target, ValueProperties.none());
  }

  /**
   * Creates a specification that can be returned as a result by a data provider that satisfies the given requirement.
   * 
   * @param valueName the value name that is satisfied, not null
   * @param target the computation target, not null
   * @param properties the properties of the satisfying result, not null
   * @return a satisfying value specification
   */
  public static ValueSpecification createMarketDataValue(final String valueName, final ComputationTargetSpecification target, final ValueProperties properties) {
    return new ValueSpecification(valueName, target, properties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, MarketDataSourcingFunction.UNIQUE_ID).get());
  }

}
