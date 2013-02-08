/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Map;

import com.opengamma.core.id.ExternalIdOrderConfig;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataNotSatisfiableException;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.util.PublicAPI;

/**
 * Helper methods for working with the market data interfaces.
 */
@PublicAPI
public class MarketDataUtils {

  private static final ComputationTargetTypeMap<MarketDataValueType> s_getMarketDataValueType = getMarketDataValueType();

  private static ComputationTargetTypeMap<MarketDataValueType> getMarketDataValueType() {
    final ComputationTargetTypeMap<MarketDataValueType> map = new ComputationTargetTypeMap<MarketDataValueType>();
    map.put(ComputationTargetType.PORTFOLIO_NODE, null);
    map.put(ComputationTargetType.POSITION, null);
    map.put(ComputationTargetType.TRADE, null);
    map.put(ComputationTargetType.SECURITY, MarketDataValueType.SECURITY);
    map.put(ComputationTargetType.ANYTHING, MarketDataValueType.PRIMITIVE);
    // NULL is not a market data type, and doesn't need an explicit entry to mask ANYTHING
    return map;
  }

  /**
   * Returns the closest matching {@link MarketDataValueType} for a {@link ComputationTargetType}.
   * 
   * @param type the type to look up, not null
   * @return the market data value type
   */
  public static MarketDataValueType getMarketDataValueType(final ComputationTargetType type) {
    return s_getMarketDataValueType.get(type);
  }

  public static ExternalId getPreferredIdentifier(final ExternalIdOrderConfig ordering, final ExternalIdBundle bundle) {
    if (bundle.size() == 1) {
      return bundle.iterator().next();
    } else {
      final Map<ExternalScheme, Integer> rates = ordering.getRateMap();
      ExternalId preferred = null;
      int preferredScore = Integer.MIN_VALUE;
      for (ExternalId id : bundle) {
        final int score = rates.get(id.getScheme());
        if (preferred == null) {
          preferred = id;
          preferredScore = score;
        } else {
          if (score > preferredScore) {
            preferred = id;
            preferredScore = score;
          } else if (score == preferredScore) {
            // same score, so use natural ordering of the schemes
            if (id.getScheme().compareTo(preferred.getScheme()) < 0) {
              preferred = id;
            }
          }
        }
      }
      return preferred;
    }
  }

  /**
   * Tests whether the requirement can be satisfied by the availability provider.
   * 
   * @param provider the provider to test, not null
   * @param requirement the requirement to test, not null
   * @return true if the requirement can be satisfied by the provider, false otherwise
   */
  public static boolean isAvailable(final MarketDataAvailabilityProvider provider, final ValueRequirement requirement) {
    try {
      return provider.getAvailability(requirement) != null;
    } catch (MarketDataNotSatisfiableException e) {
      return false;
    }
  }

  /**
   * Tests whether the requirement can be satisfied by the availability provider.
   * 
   * @param provider the provider to test, not null
   * @param requirement the requirement to test, not null
   * @return one of the three availability states - see {@link MarketDataAvailability} for more details, not null
   */
  public static MarketDataAvailability getAvailability(final MarketDataAvailabilityProvider provider, final ValueRequirement requirement) {
    try {
      return (provider.getAvailability(requirement) != null) ? MarketDataAvailability.AVAILABLE : MarketDataAvailability.NOT_AVAILABLE;
    } catch (MarketDataNotSatisfiableException e) {
      return MarketDataAvailability.MISSING;
    }
  }

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

  /**
   * Support function for extracting one of the external identifiers (based on the default order config) as the best available as a unique identifier for the target.
   */
  public static final Function1<ValueRequirement, UniqueId> DEFAULT_EXTERNAL_ID = new Function1<ValueRequirement, UniqueId>() {
    @Override
    public UniqueId execute(final ValueRequirement requirement) {
      final ExternalId eid = getPreferredIdentifier(ExternalIdOrderConfig.DEFAULT_CONFIG, requirement.getTargetReference().getRequirement().getIdentifiers());
      return UniqueId.of(eid.getScheme().getName(), eid.getValue());
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
