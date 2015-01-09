/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Contains the perturbations that should be applied to market data during a single calculation cycle in a scenario.
 * <p>
 * A scenario definition defines a set of perturbations and filters that are used to run multiple calculation cycles.
 * This class represents the subset of those perturbations that should be applied in a particular cycle.
 * <p>
 * Perturbations are referred to as "input" and "output" perturbations. Input perturbations apply to the data used
 * when building market data, e.g. the quotes for the nodal points when building a curve. Output perturbations apply
 * to the market data in the {@link MarketDataEnvironment}, e.g. a shift applied to a calibrated curve.
 */
public class CyclePerturbations {

  private final ImmutableMultimap<MarketDataRequirement, FilteredPerturbation> _inputPerturbations;
  private final List<SinglePerturbationMapping> _outputMappings;

  /**
   * Creates a set of perturbations that apply to the market data specified in a set of requirements.
   *
   * @param requirements requirements for a set of market data
   * @param mappings perturbations to apply to market data as part of a single calculation cycle in a scenario
   */
  public CyclePerturbations(Set<MarketDataRequirement> requirements, List<SinglePerturbationMapping> mappings) {
    ArgumentChecker.notNull(requirements, "requirements");
    ArgumentChecker.notNull(mappings, "mappings");

    Pair<List<SinglePerturbationMapping>, List<SinglePerturbationMapping>> pair = partitionMappings(mappings);
    List<SinglePerturbationMapping> inputMappings = pair.getFirst();
    _outputMappings = pair.getSecond();
    _inputPerturbations = buildInputPerturbationMap(requirements, inputMappings);
  }

  // TODO Java 8 - replace with a stream and groupingBy or partitioningBy
  /**
   * Partitions a list of mappings into a list that applies to input data used to build market data and
   * a set that applies to the built market data.
   *
   * @param mappings perturbation mappings that might apply to input or output market data
   * @return a pair of lists. The first list is the perturbations that apply to input data used to build market
   *   data. The second list applies to the built market data
   */
  private static Pair<List<SinglePerturbationMapping>, List<SinglePerturbationMapping>> partitionMappings(
      List<SinglePerturbationMapping> mappings) {

    List<SinglePerturbationMapping> inputs = new ArrayList<>();
    List<SinglePerturbationMapping> outputs = new ArrayList<>();

    for (SinglePerturbationMapping mapping : mappings) {
      switch (mapping.getTargetType()) {
        case INPUT:
          inputs.add(mapping);
          break;
        case OUTPUT:
          outputs.add(mapping);
          break;
      }
    }
    return Pairs.of(inputs, outputs);
  }

  /**
   * Builds a map of market data requirements to the input perturbations that apply to the market data.
   *
   * @param requirements market data requirements
   * @param mappings market data perturbations and a filter that decides what data they apply to
   * @return a map of market data requirements to the perturbations that apply to the market data
   */
  private ImmutableMultimap<MarketDataRequirement, FilteredPerturbation> buildInputPerturbationMap(
      Set<MarketDataRequirement> requirements,
      List<SinglePerturbationMapping> mappings) {

    ImmutableListMultimap.Builder<MarketDataRequirement, FilteredPerturbation> builder =
        ImmutableListMultimap.builder();

    for (MarketDataRequirement requirement : requirements) {
      Collection<FilteredPerturbation> matches = perturbationsForInputs(requirement, mappings);
      builder.putAll(requirement, matches);
    }
    return builder.build();
  }

  /**
   * Returns all perturbations that should be applied to a piece of market data.
   * <p>
   * Although each piece of data should only be perturbed once, some market data objects are actually composite
   * objects containing multiple pieces of independent data that can be independently perturbed. For example
   * multicurve bundles are represented by a single requirement but contain multiple curves that can be
   * perturbed.
   *
   * @param requirement the requirement for a piece of market data
   * @param mappings the perturbation mappings for the current scenario
   * @return the perturbations that should be applied to the market data and the details of the filter matches
   */
  private Collection<FilteredPerturbation> perturbationsForInputs(
      MarketDataRequirement requirement,
      List<SinglePerturbationMapping> mappings) {

    // it is possible for multiple perturbations to apply to values where different parts of the value can
    // be perturbed independently. e.g. the individual curves inside a curve bundle.
    // the keys in this map are the match details returned by the filter. the details identify the part of
    // the value that was matched
    Map<MatchDetails, FilteredPerturbation> matches = new HashMap<>();

    for (SinglePerturbationMapping mapping : mappings) {
      // TODO this would be more efficient if the mappings were supplied in a multimap keyed by market data type
      // not worth the complication yet
      MarketDataFilter filter = mapping.getFilter();
      // TODO is the data type really the most important thing here? e.g. can be double for security and raw IDs
      // TODO double-check this logic
      Class<?> filterDataType = filter.getMarketDataType();
      Class<?> requirementDataType = requirement.getMarketDataId().getMarketDataType();
      Class<?> marketDataIdType = filter.getMarketDataIdType();
      MarketDataId marketDataId = requirement.getMarketDataId();

      if (filterDataType == requirementDataType && marketDataIdType == marketDataId.getClass()) {
        // Assumes input metadata can always be derived from the market data ID (i.e. from config).
        // But isn't that a given anyway because they're inputs so the data is getting built from config?
        // Would need to duplicate the logic that creates the metadata in every filter acting on the same market
        // data type. Lots of helper methods or abstract supertypes?
        Set<? extends MatchDetails> matchDetails = filter.apply(marketDataId);

        for (MatchDetails match : matchDetails) {
          if (!matches.containsKey(match)) {
            matches.put(match, new FilteredPerturbation(mapping.getPerturbation(), match));
          }
        }
      }
    }
    return matches.values();
  }

  /**
   * Returns a collection of output perturbations that apply to a piece of market data.
   *
   * @param requirement the requirement for the market data
   * @param marketDataValue the market data value
   * @param mappings a list of mappings to search for perturbations that apply to the data
   * @return a collection of output perturbations that apply to a piece of market data
   */
  private Collection<FilteredPerturbation> perturbationsForMarketData(MarketDataRequirement requirement,
                                                                      Object marketDataValue,
                                                                      List<SinglePerturbationMapping> mappings) {
    Map<Object, FilteredPerturbation> matches = new HashMap<>();

    for (SinglePerturbationMapping mapping : mappings) {
      MarketDataFilter filter = mapping.getFilter();
      // TODO is the data type really the most important thing here? e.g. can be double for security and raw IDs
      Class<?> filterDataType = filter.getMarketDataType();
      Class<?> requirementDataType = requirement.getMarketDataId().getMarketDataType();
      Class<?> marketDataIdType = filter.getMarketDataIdType();
      MarketDataId marketDataId = requirement.getMarketDataId();

      if (filterDataType == requirementDataType && marketDataIdType == marketDataId.getClass()) {
        Set<? extends MatchDetails> matchDetails = filter.apply(marketDataId, marketDataValue);

        for (MatchDetails match : matchDetails) {
          if (!matches.containsKey(match)) {
            matches.put(match, new FilteredPerturbation(mapping.getPerturbation(), match));
          }
        }
      }
    }
    return matches.values();
  }

  /**
   * @return perturbations that apply to input data used to build market data, keyed by the requirements for
   *   the market data
   */
  public Multimap<MarketDataRequirement, FilteredPerturbation> getInputPerturbations() {
    return _inputPerturbations;
  }

  /**
   * Applies the output perturbations to the market data in the market data environment.
   * <p>
   * A new market data environment is created containing new market data values with the perturbations applied.
   * The input environment is unchanged.
   *
   * @param marketData a set of market data
   * @return a new set of market data, derived from the input data with perturbations applied
   */
  @SuppressWarnings("unchecked")
  public MarketDataEnvironment apply(MarketDataEnvironment marketData) {
    MarketDataEnvironmentBuilder builder = marketData.toBuilder();

    for (Map.Entry<SingleValueRequirement, Object> entry : marketData.getData().entrySet()) {
      SingleValueRequirement requirement = entry.getKey();
      Object data = entry.getValue();
      // TODO check the logic here guarantees this is safe
      Collection<FilteredPerturbation> filteredPerturbations = perturbationsForMarketData(requirement, data, _outputMappings);
      Object perturbedData = data;

      for (FilteredPerturbation filteredPerturbation : filteredPerturbations) {
        perturbedData = filteredPerturbation.apply(perturbedData);
      }
      builder.add(requirement, perturbedData);
    }
    // TODO apply perturbations to time series values
    return builder.build();
  }
}
