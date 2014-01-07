/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A source of market data that aggregates data from multiple underlying {@link MarketDataProvider}s.
 */
public class ViewExecutionDataProvider {

  /** The market data user */
  private final UserPrincipal _user;
  /** The underlying providers in priority order. */
  private final List<MarketDataProvider> _providers;
  /** The market data specifications in priority order. */
  private final List<MarketDataSpecification> _specs;
  private final MarketDataPermissionProvider _permissionsProvider;

  /**
   * @param user The user requesting the data, not null
   * @param specs Specifications of the underlying providers in priority order, not empty
   * @param resolver For resolving market data specifications into providers, not null
   * @throws IllegalArgumentException If any of the data providers in {@code specs} can't be resolved
   */
  public ViewExecutionDataProvider(final UserPrincipal user,
      final List<MarketDataSpecification> specs,
      final MarketDataProviderResolver resolver) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notEmpty(specs, "specs");
    ArgumentChecker.notNull(resolver, "resolver");
    _user = user;
    _specs = ImmutableList.copyOf(specs);
    if (_specs.size() == 1) {
      final MarketDataProvider provider = resolver.resolve(user, _specs.get(0));
      if (provider == null) {
        throw new IllegalArgumentException("Unable to resolve market data spec " + _specs.get(0));
      }
      _providers = Collections.singletonList(provider);
      _permissionsProvider = provider.getPermissionProvider();
    } else {
      _providers = Lists.newArrayListWithCapacity(_specs.size());
      for (final MarketDataSpecification spec : _specs) {
        final MarketDataProvider provider = resolver.resolve(user, spec);
        if (provider == null) {
          throw new IllegalArgumentException("Unable to resolve market data spec " + spec);
        }
        _providers.add(provider);
      }
      _permissionsProvider = new CompositePermissionProvider();
    }
  }

  public UserPrincipal getMarketDataUser() {
    return _user;
  }

  protected List<MarketDataProvider> getProviders() {
    return _providers;
  }

  public List<MarketDataSpecification> getSpecifications() {
    return _specs;
  }

  /**
   * @return An permissions provider backed by the permissions providers of the underlying market data providers
   */
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionsProvider;
  }

  /**
   * Divides up the specifications into a set for each underlying provider. The values from each underlying will have been tagged with a provider property which indicates the underlying they came
   * from. This is removed from the returned result so the original value specifications as used by the underlying are returned.
   * 
   * @param numProviders the number of providers in total
   * @param specifications The market data specifications
   * @return A set of specifications for each underlying provider, in the same order as the providers
   */
  protected static List<Set<ValueSpecification>> partitionSpecificationsByProvider(final int numProviders, final Set<ValueSpecification> specifications) {
    if (numProviders == 1) {
      return Collections.singletonList(specifications);
    } else {
      final List<Set<ValueSpecification>> result = Lists.newArrayListWithCapacity(numProviders);
      for (int i = 0; i < numProviders; i++) {
        result.add(Sets.<ValueSpecification>newHashSet());
      }
      for (final ValueSpecification specification : specifications) {
        String provider = specification.getProperty(ValuePropertyNames.DATA_PROVIDER);
        if (provider != null) {
          final ValueProperties.Builder underlyingProperties = specification.getProperties().copy().withoutAny(ValuePropertyNames.DATA_PROVIDER);
          final int slash = provider.indexOf('/');
          if (slash > 0) {
            underlyingProperties.with(ValuePropertyNames.DATA_PROVIDER, provider.substring(0, slash));
            provider = provider.substring(slash + 1);
          }
          try {
            result.get(Integer.parseInt(provider)).add(new ValueSpecification(specification.getValueName(), specification.getTargetSpecification(), underlyingProperties.get()));
          } catch (final NumberFormatException e) {
            // Ignore
          }
        }
      }
      return result;
    }
  }

  /**
   * Identifies the provider a given specification is used for. The values from the underlying will have been tagged with a provider property which indicates the underlying. Thsi is removed from the
   * result so the original value specification as used by the underlying is returned.
   * 
   * @param specification the specification to test
   * @return the provider index and the underlying's specification, or null if it could not be found
   */
  protected static Pair<Integer, ValueSpecification> getProviderSpecification(final ValueSpecification specification) {
    String provider = specification.getProperty(ValuePropertyNames.DATA_PROVIDER);
    if (provider != null) {
      final ValueProperties.Builder underlyingProperties = specification.getProperties().copy().withoutAny(ValuePropertyNames.DATA_PROVIDER);
      final int slash = provider.indexOf('/');
      if (slash > 0) {
        underlyingProperties.with(ValuePropertyNames.DATA_PROVIDER, provider.substring(0, slash));
        provider = provider.substring(slash + 1);
      }
      try {
        return Pairs.of(Integer.parseInt(provider), new ValueSpecification(specification.getValueName(), specification.getTargetSpecification(), underlyingProperties.get()));
      } catch (final NumberFormatException e) {
        // Ignore
      }
    }
    return null;
  }

  /**
   * Converts a value specification as used by a given underlying to one that can be used by this provider. An integer identifier for the underlying provider will be put into a property that the
   * {@link #partitionSpecificationsByProvider} helper will use to map the specification back to the originating underlying.
   * 
   * @param providerId the index of the provider in the list
   * @param underlying the value specification as used by the underlying
   * @return a value specification for external use
   */
  protected static ValueSpecification convertUnderlyingSpecification(final int providerId, final ValueSpecification underlying) {
    final ValueProperties.Builder properties = underlying.getProperties().copy();
    final String dataProvider = underlying.getProperty(ValuePropertyNames.DATA_PROVIDER);
    if (dataProvider != null) {
      properties.withoutAny(ValuePropertyNames.DATA_PROVIDER).with(ValuePropertyNames.DATA_PROVIDER, dataProvider + "/" + Integer.toString(providerId));
    } else {
      properties.with(ValuePropertyNames.DATA_PROVIDER, Integer.toString(providerId));
    }
    return new ValueSpecification(underlying.getValueName(), underlying.getTargetSpecification(), properties.get());
  }

  /**
   * {@link MarketDataPermissionProvider} that checks the permissions using the underlying {@link MarketDataProvider}s. The underlying provider will be the one that returned the original availability
   * of the data.
   */
  private class CompositePermissionProvider implements MarketDataPermissionProvider {

    /**
     * Checks permissions with the underlying providers and returns any requirements for which the user has no permissions with any provider.
     * 
     * @param user The user whose market data permissions should be checked
     * @param specifications The market data to check access to
     * @return Values for which the user has no permissions with any of the underlying providers
     */
    @Override
    public Set<ValueSpecification> checkMarketDataPermissions(final UserPrincipal user, final Set<ValueSpecification> specifications) {
      final List<Set<ValueSpecification>> specsByProvider = partitionSpecificationsByProvider(_providers.size(), specifications);
      final Set<ValueSpecification> missingSpecifications = Sets.newHashSet();
      for (int i = 0; i < _providers.size(); i++) {
        final MarketDataPermissionProvider permissionProvider = _providers.get(i).getPermissionProvider();
        final Set<ValueSpecification> specsForProvider = specsByProvider.get(i);
        final Set<ValueSpecification> missing = permissionProvider.checkMarketDataPermissions(user, specsForProvider);
        if (!missing.isEmpty()) {
          for (final ValueSpecification specification : missing) {
            missingSpecifications.add(convertUnderlyingSpecification(i, specification));
          }
        }
      }
      return missingSpecifications;
    }
  }

}
