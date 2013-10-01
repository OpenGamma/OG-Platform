/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.provider;

import static java.lang.String.format;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTrace;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilderProperties;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceProviderResource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to a {@link DependencyGraphTraceProvider}.
 */
public class RemoteDependencyGraphTraceProvider extends AbstractRemoteClient implements DependencyGraphTraceProvider {

  /**
   * Creates an instance.
   * 
   * @param baseUri the target URI for all RESTful web services, not null
   */
  public RemoteDependencyGraphTraceProvider(URI baseUri) {
    super(baseUri);
  }

  @Override
  public DependencyGraphBuildTrace getTrace(DependencyGraphTraceBuilderProperties properties) {
    URI uri = buildUri(properties);

    return accessRemote(uri).get(DependencyGraphBuildTrace.class);
  }

  /**
   * Builds the url to use for the remote access.
   * @param properties the properties to use
   * @return a full URI
   */
  @VisibleForTesting
  URI buildUri(DependencyGraphTraceBuilderProperties properties) {
    URI uri = getBaseUri();

    //process single value properties:
    String calcConfigName = properties.getCalculationConfigurationName();
    uri = DependencyGraphTraceProviderResource.uriCalculationConfigurationName(uri, calcConfigName);

    ValueProperties defaultProperties = properties.getDefaultProperties();
    uri = DependencyGraphTraceProviderResource.uriDefaultProperties(uri, defaultProperties);

    List<MarketDataSpecification> marketData = properties.getMarketData();
    if (marketData != null) {
      uri = DependencyGraphTraceProviderResource.uriMarketData(uri, marketData);
    }

    VersionCorrection resolutionTime = properties.getResolutionTime();
    uri = DependencyGraphTraceProviderResource.uriResolutionTime(uri, resolutionTime);

    Instant valuationTime = properties.getValuationTime();
    if (valuationTime != null) {
      uri = DependencyGraphTraceProviderResource.uriValuationTime(uri, valuationTime);
    }

    //process requirements:
    uri = processRequirements(uri, properties.getRequirements());

    return DependencyGraphTraceProviderResource.uriBuild(uri);
  }

  /**
   * Unpacks the requirements into URI form.
   * @param uri the uri to append to
   * @param requirements the requirements to append
   */
  private URI processRequirements(URI uri, Collection<ValueRequirement> requirements) {
    for (ValueRequirement valueRequirement : requirements) {

      String valueName = valueRequirement.getValueName();

      ValueProperties constraints = valueRequirement.getConstraints();

      String contraintStr = constraints.isEmpty() ? "" : constraints.toString();

      String constrainedValueName = valueName + contraintStr;

      ComputationTargetReference targetReference = valueRequirement.getTargetReference();
      String targetType = targetReference.getType().toString();

      if (targetReference instanceof ComputationTargetRequirement) {
        ComputationTargetRequirement requirement = (ComputationTargetRequirement) targetReference;
        Set<ExternalId> externalIds = requirement.getIdentifiers().getExternalIds();
        ArgumentChecker.isTrue(externalIds.size() == 1, "One (and only one) external id must be specified currently.");
        ExternalId externalId = Iterables.get(externalIds, 0);
        uri = DependencyGraphTraceProviderResource.uriValueRequirementByExternalId(uri, constrainedValueName, targetType, externalId);
      } else if (targetReference instanceof ComputationTargetSpecification) {
        UniqueId uniqueId = ((ComputationTargetSpecification) targetReference).getUniqueId();
        uri = DependencyGraphTraceProviderResource.uriValueRequirementByUniqueId(uri, constrainedValueName, targetType, uniqueId);
      } else {
        throw new IllegalArgumentException(format("Unrecognised ValueRequirement class: %s", ValueRequirement.class.getName()));
      }
    }
    return uri;
  }
}
