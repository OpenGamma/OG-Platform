/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.sesame.marketdata.CompositeMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataFactory;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.TimeSeriesRequirement;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.sesame.marketdata.scenarios.SinglePerturbationMapping;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateObjectTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateObjectTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

/**
 * Sources and builds market data required for performing calculations in the calculation engine.
 * <p>
 * This class is used by the engine if it needs to perform some calculations and doesn't have all the required
 * market data. It sits between the low level market data provider and the {@link MarketDataBuilder} instances
 * that do the actual work of building the market data.
 */
public class MarketDataEnvironmentFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataEnvironmentFactory.class);

  // might need to make this a multimap and offer a requirement to each applicable builder in turn
  private final Map<Class<? extends MarketDataId>, MarketDataBuilder> _builders;
  private final MarketDataFactory _marketDataFactory;

  /**
   * @param marketDataFactory provides sources of low-level market data
   * @param builders build high level market data from lower level data
   */
  public MarketDataEnvironmentFactory(MarketDataFactory marketDataFactory, MarketDataBuilder... builders) {
    this(marketDataFactory, Arrays.asList(builders));
  }

  /**
   * @param marketDataFactory provides sources of low-level market data
   * @param builders build high level market data from lower level data
   */
  public MarketDataEnvironmentFactory(MarketDataFactory marketDataFactory, List<MarketDataBuilder> builders) {
    ArgumentChecker.notNull(builders, "builders");
    _marketDataFactory = ArgumentChecker.notNull(marketDataFactory, "marketDataFactory");
    ImmutableMap.Builder<Class<? extends MarketDataId>, MarketDataBuilder> mapBuilder =
        ImmutableMap.builder();

    for (MarketDataBuilder builder : builders) {
      mapBuilder.put(builder.getKeyType(), builder);
    }
    _builders = mapBuilder.build();
  }

  /**
   * Builds a bundle of market data to satisfy a set of requirements.
   * <p>
   * The return value includes the supplied data as well as the data built by the factory.
   * <p>
   * If {@code requirements} contains a requirement that can't be satisfied by {@code suppliedData} then this
   * method will attempt to build it by delegating to the {@link MarketDataBuilder} instances.
   *
   * @param suppliedData existing market data
   * @param requirements requirements for the market data that the factory should build
   * @param marketDataSpec specifies which low-level market data providers should be used to source the raw
   *   data for building the market data
   * @param valuationTime the valuation time used when building market data
   * @return a bundle of market data including the supplied data and the data built by the factory
   */
  public MarketDataEnvironment build(MarketDataEnvironment suppliedData,
                                     Set<MarketDataRequirement> requirements,
                                     List<SinglePerturbationMapping> perturbations,
                                     MarketDataSpecification marketDataSpec,
                                     ZonedDateTime valuationTime) {

    // create a data source to provide low-level data from market data providers
    @SuppressWarnings("unchecked")
    MarketDataSource marketDataSource = _marketDataFactory.create(marketDataSpec);

    // figure out which perturbations should be applied to the market data for this calculation cycle
    CyclePerturbations cyclePerturbations = new CyclePerturbations(requirements, perturbations);

    // build the data
    MarketDataEnvironment builtData =
        buildEnvironment(suppliedData, requirements, cyclePerturbations, valuationTime, marketDataSource);

    // filter the single values to only include the ones in the requirements, not the intermediate values that
    // were only used to build other values
    Map<SingleValueRequirement, Object> singleValues = requestedSingleValues(requirements, builtData);

    // filter the time series to only include the ones in the requirements, not the intermediate values that
    // were only used to build other values
    Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> timeSeries = requestedTimeSeries(requirements, builtData);

    MarketDataEnvironment filteredData = new MarketDataEnvironmentBuilder()
        .addSingleValues(singleValues)
        .addTimeSeries(timeSeries)
        .valuationTime(valuationTime)
        .build();
    MarketDataEnvironment mergedData = MarketDataEnvironmentBuilder.merge(suppliedData, filteredData);
    return cyclePerturbations.apply(mergedData);
  }

  /**
   * Filters the time series in the built data to only include the time series in the original requirements.
   * <p>
   * The built data also includes any time series that was used to build other values but wasn't explicitly requested.
   *
   * @param requirements requirements for the market data needed to perform the calculations
   * @param builtData all the market data that was built, including the values needed for the calculations and
   *   also the intermediate values that were used to build other market data
   * @return the time series that were requested in the original requirements
   */
  private Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> requestedTimeSeries(Set<MarketDataRequirement> requirements,
                                                                                 MarketDataEnvironment builtData) {
    // the time series that have been built
    Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> timeSeries = new HashMap<>(builtData.getTimeSeries());
    // time series IDs in the original requirements
    Set<MarketDataId<?>> requiredTimeSeriesIds = timeSeriesMarketDataIds(requirements);
    // only want to return the data in the original requirements, not the intermediate data that was only used
    // for building the requirements and their dependencies
    timeSeries.keySet().retainAll(requiredTimeSeriesIds);
    // the difference between the original time series requirements and the ones that have been built
    Set<MarketDataId<?>> missingTimeSeries = Sets.difference(requiredTimeSeriesIds, timeSeries.keySet());

    if (!missingTimeSeries.isEmpty()) {
      s_logger.warn("Unable to satisfy requirements for time series {}", missingTimeSeries);
    }
    return timeSeries;
  }

  /**
   * Filters the single values in the built data to only include the data in the original requirements.
   * <p>
   * The built data also includes any value that was used to build other values but wasn't explicitly requested.
   *
   * @param requirements requirements for the market data needed to perform the calculations
   * @param builtData all the market data that was built, including the values needed for the calculations and
   *   also the intermediate values that were used to build other market data
   * @return the single values that were requested in the original requirements
   */
  private Map<SingleValueRequirement, Object> requestedSingleValues(Set<MarketDataRequirement> requirements,
                                                                    MarketDataEnvironment builtData) {
    // the single market data values that were built
    Map<SingleValueRequirement, Object> singleValues = new HashMap<>(builtData.getData());
    // single value requirements in the original requirements
    Set<SingleValueRequirement> singleValueRequirements = singleValueRequirements(requirements);
    // only want to return the data in the original requirements, not the intermediate data that was only used
    // for building the requirements and their dependencies
    singleValues.keySet().retainAll(singleValueRequirements);
    // the difference between the original requirements and the ones that have been built
    Set<SingleValueRequirement> missingRequirements = Sets.difference(singleValueRequirements, singleValues.keySet());

    if (!missingRequirements.isEmpty()) {
      s_logger.warn("Unable to satisfy requirements for market data {}", missingRequirements);
    }
    return singleValues;
  }

  /**
   * Builds a market data environment to satisfy the requirements.
   *
   * @param suppliedData the market data that was supplied by the user. The engine won't attempt to build market
   *   data if it was supplied
   * @param requirements requirements for the market data that the factory should build
   * @param valuationTime the valuation time used for building the market data
   * @param marketDataSource source of raw market data
   * @return a market data environment containing the data specified by the requirements
   */
  private MarketDataEnvironment buildEnvironment(MarketDataEnvironment suppliedData,
                                                 Set<MarketDataRequirement> requirements,
                                                 CyclePerturbations cyclePerturbations,
                                                 ZonedDateTime valuationTime,
                                                 MarketDataSource marketDataSource) {
    // the data built so far, including data that isn't in the requirements but is needed to build the required data
    MarketDataEnvironment builtData = new MarketDataEnvironmentBuilder().valuationTime(valuationTime).build();

    // build a tree representing the market data and the data required to build it
    MarketDataNode root = buildDependencyRoot(requirements, valuationTime, suppliedData);
    // this is where the scenario framework needs to decide which perturbations to apply
    // it has access to the whole tree of market data so it can tell when data has already been perturbed
    // does the decision have to be made here? or can it be deferred for shocks that are applied after the data
    // has been built?
    // interesting question - if input shocks are considered separately from output shocks, they've implicitly got
    // a higher priority. is that behaviour we want? if not, we have to decide up from which output shocks to apply
    // without having access to the output data. is that a problem?
    // for this to be a problem we would need an output shock predicated on the data that is higher priority than
    // an input shock that matched the same piece of data. there's no way to know whether the output shock will
    // apply until the data is built, and then it's too late to apply the input shock. unless we go back and
    // build the data again with the input shock applied. that's nasty

    // market data is built in multiple passes over the dependency tree
    // in each iteration the leaves are removed from the tree and market data is built to satisfy the leaf dependencies
    // the built market data is accumulated and passed to the builders each iteration
    // the process ends when only the root node remains
    while (!root.isLeaf()) {
      Set<MarketDataRequirement> leafRequirements =  removeLeaves(root);
      List<PartitionedRequirements> partitionedRequirements = PartitionedRequirements.partition(leafRequirements);
      builtData = buildMarketData(suppliedData,
                                  builtData,
                                  partitionedRequirements,
                                  marketDataSource,
                                  valuationTime,
                                  cyclePerturbations);
    }
    return builtData;
  }

  /**
   * Recursively removes the leaf nodes from a node and all nodes below it in the tree. This method mutates the node
   * and its children.
   *
   * @param node a node
   * @return the leaf requirements removed from the requirements tree
   */
  static Set<MarketDataRequirement> removeLeaves(MarketDataNode node) {
    List<MarketDataNode> children = node.getChildren();
    ImmutableSet.Builder<MarketDataRequirement> builder = ImmutableSet.builder();

    for (Iterator<MarketDataNode> it = children.iterator(); it.hasNext(); ) {
      MarketDataNode childNode = it.next();

      if (childNode.isLeaf()) {
        it.remove();
        builder.add(childNode.getRequirement());
      } else {
        builder.addAll(removeLeaves(childNode));
      }
    }
    return builder.build();
  }

  /**
   * Builds the market data in the {@code partitionedRequirements}
   *
   * @param suppliedData existing market data
   * @param builtData the data built so far. Contains all available data required to satisfy the requirements
   * @param partitionedRequirements market data requirements, partitioned by the type of their {@link MarketDataId}
   * @param marketDataSource provider of low-level market data used for building the market data
   * @param valuationTime the valuation time used when building market data
   * @param cyclePerturbations the perturbations that should be applied to the market data for this calculation cycle
   * @return the market data for the requirements, including all the data in {@code builtData}
   */
  private MarketDataEnvironment buildMarketData(MarketDataEnvironment suppliedData,
                                                MarketDataEnvironment builtData,
                                                List<PartitionedRequirements> partitionedRequirements,
                                                MarketDataSource marketDataSource,
                                                ZonedDateTime valuationTime,
                                                CyclePerturbations cyclePerturbations) {

    MarketDataEnvironmentBuilder environmentBuilder = builtData.toBuilder();

    // submit the requirements to the appropriate builder in bulk
    for (PartitionedRequirements partitionedRequirement : partitionedRequirements) {
      Class<? extends MarketDataId> idType = partitionedRequirement._idType;
      Set<SingleValueRequirement> singleValueReqsForKey = partitionedRequirement._singleValueRequirements;
      Set<TimeSeriesRequirement> timeSeriesReqsForKey = partitionedRequirement._timeSeriesRequirements;
      // this will never be null, requirements are only in the tree if they have a builder
      MarketDataBuilder dataBuilder = _builders.get(idType);
      CompositeMarketDataBundle marketDataBundle =
          new CompositeMarketDataBundle(suppliedData.toBundle(), builtData.toBundle());

      // build the data
      Map<SingleValueRequirement, Result<?>> singleValueData =
          dataBuilder.buildSingleValues(marketDataBundle,
                                        valuationTime,
                                        singleValueReqsForKey,
                                        marketDataSource,
                                        cyclePerturbations);

      // it would be an optimisation to pass in existing time series in case there are requirements for
      // overlapping series for the same ID. as things stand the overlapping values would be built twice.
      // but if the builder can see the existing series it can avoid rebuilding duplicate data.
      // the merging logic below will ensure the correct data is created anyway, at the cost of some efficiency
      Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> timeSeriesData =
          dataBuilder.buildTimeSeries(marketDataBundle, timeSeriesReqsForKey, marketDataSource, cyclePerturbations);

      // the single values that were successfully built
      Map<SingleValueRequirement, Object> singleValues = successfulSingleValues(singleValueData);
      // the time series that were successfully built
      // TODO this merges time series built at the same time but not in different passes
      //   need to include time series from the the built data too
      Map<MarketDataId<?>, ? extends DateTimeSeries<LocalDate, ?>> timeSeries = successfulTimeSeries(timeSeriesData);

      // environment contains the passed in data, plus all the data built so far
      environmentBuilder.addSingleValues(singleValues).addTimeSeries(timeSeries).valuationTime(valuationTime).build();
    }
    return environmentBuilder.build();
  }

  /**
   * Collects and returns the time series that were successfully built and logs warnings for any failures.
   *
   * @param timeSeriesData the results of building the time series
   * @return the time series that were successfully built
   */
  private static Map<MarketDataId<?>, ? extends DateTimeSeries<LocalDate, ?>> successfulTimeSeries(
      Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> timeSeriesData) {

    Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> timeSeries = new HashMap<>();

    for (Map.Entry<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> entry :
        timeSeriesData.entrySet()) {

      Result<? extends DateTimeSeries<LocalDate, ?>> result = entry.getValue();

      if (result.isSuccess()) {
        MarketDataId id = entry.getKey().getMarketDataId();
        timeSeries.put(id, mergeTimeSeries(id, timeSeries, result.getValue()));
      } else {
        s_logger.warn("Failed to build time series {}, {}", entry.getKey(), result);
      }
    }
    return timeSeries;
  }

  /**
   * Collects and returns the market data values that were successfully built and logs warnings for any failures.
   *
   * @param singleValueData the results of building the single market data values
   * @return the market data values that were successfully built
   */
  private static Map<SingleValueRequirement, Object> successfulSingleValues(
      Map<SingleValueRequirement, Result<?>> singleValueData) {

    Map<SingleValueRequirement, Object> singleValues = new HashMap<>();

    for (Map.Entry<SingleValueRequirement, Result<?>> entry : singleValueData.entrySet()) {
      Result<?> result = entry.getValue();

      if (result.isSuccess()) {
        singleValues.put(entry.getKey(), result.getValue());
      } else {
        s_logger.warn("Failed to build market data {}, {}", entry.getKey(), result);
      }
    }
    return singleValues;
  }

  /**
   * Checks if a time series already exists for an ID and if so merges two time series of data for that ID.
   *
   * @param marketDataId ID of the data in the time series
   * @param builtData map of time series that have already been built
   * @param timeSeries newly built time series
   * @return a time series containing all the data for the ID
   * @throws IllegalArgumentException if the time series have different types. This should never happen unless
   *   there's a bug in the {@code MarketDataBuilder} that created them
   */
  @SuppressWarnings("unchecked") // LocalDateObjectTimeSeriesBuilder.putAll has a baffling signature that needs this
  private static DateTimeSeries<LocalDate, ?> mergeTimeSeries(
      MarketDataId<?> marketDataId,
      Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> builtData,
      DateTimeSeries<LocalDate, ?> timeSeries) {

    DateTimeSeries<LocalDate, ?> existingTimeSeries = builtData.get(marketDataId);

    if (existingTimeSeries == null) {
      return timeSeries;
    }
    // this should never happen unless the builder has returned two different types of time series for the same
    // ID which would definitely be a bug
    if (existingTimeSeries.getClass() != timeSeries.getClass()) {
      throw new IllegalArgumentException("Time series must be of the same type. ID: " + marketDataId +
          ", type1: " + existingTimeSeries.getClass().getName() +
          ", type2: " + timeSeries.getClass().getName());
    }
    // the time series each have data not present in the other. Merge into a single time series
    // only LocalDate time series are supported but there isn't a common supertype for the LocalDate time series impls
    if (existingTimeSeries instanceof LocalDateDoubleTimeSeries) {
      return ImmutableLocalDateDoubleTimeSeries.builder()
          .putAll((LocalDateDoubleTimeSeries) existingTimeSeries)
          .putAll((LocalDateDoubleTimeSeries) timeSeries)
          .build();
    } else {
      return ImmutableLocalDateObjectTimeSeries.builder()
          .putAll((LocalDateObjectTimeSeries) existingTimeSeries)
          .putAll((LocalDateObjectTimeSeries) timeSeries)
          .build();
    }
  }
  /**
   * Extracts the time series market data IDs from a set of requirements
   *
   * @param requirements the requirements
   * @return the market data IDs of any {@link TimeSeriesRequirement} instances in the requirements
   */
  private Set<MarketDataId<?>> timeSeriesMarketDataIds(Set<MarketDataRequirement> requirements) {
    Set<MarketDataId<?>> ids = new HashSet<>();

    for (MarketDataRequirement requirement : requirements) {
      if (requirement instanceof TimeSeriesRequirement) {
        ids.add(requirement.getMarketDataId());
      }
    }
    return ids;
  }

  /**
   * Extracts the single value requirements from a set of requirements.
   *
   * @param requirements some market data requirements
   * @return the {@code SingleValueRequirement} instances from the input set
   */
  private static Set<SingleValueRequirement> singleValueRequirements(Set<MarketDataRequirement> requirements) {
    Set<SingleValueRequirement> singleValueRequirements = new HashSet<>();

    for (MarketDataRequirement requirement : requirements) {
      if (requirement instanceof SingleValueRequirement) {
        singleValueRequirements.add((SingleValueRequirement) requirement);
      }
    }
    return singleValueRequirements;
  }

  /**
   * Builds a tree representing the dependencies between items of market data.
   * <p>
   * The immediate children of the root node are the market data values required by the calculations. Their child
   * nodes are the market data they depend on, and so on.
   * <p>
   * For example, if a function requests a curve, there will be a node below the root representing that curve.
   * The curve's node will have child nodes representing the market data values at each of the curve points. It
   * might also have a child node representing another curve, or possibly an FX rate, and the curve and FX rate nodes
   * would themselves depend on market data values.
   * <p>
   * The leaf nodes in the tree represent market data that can be sourced from a market data provider or
   * is provided by the user when running the view.
   * <p>
   * The building process starts the the leaf nodes. They are removed from the tree, and the requirements are
   * passed to the appropriate builders to build the market data values. After removing the leaf nodes, the tree
   * contains a new set of leaf nodes. The process is repeated, and the leaf requirements are passed to the
   * builders, along with their dependent market data values built in the previous step. This process continues
   * until there are no nodes remaining below the root.
   *
   * @param requirements requirements representing the market data that must be provided
   * @param valuationTime valuation time used when building market data
   * @param suppliedData data supplied by the user
   * @return the root node of the market data dependency tree
   */
  MarketDataNode buildDependencyRoot(Set<MarketDataRequirement> requirements,
                                     ZonedDateTime valuationTime,
                                     MarketDataEnvironment suppliedData) {
    MarketDataNode root = new MarketDataNode();

    for (MarketDataRequirement requirement : requirements) {
      // these are always needed, if they were supplied they shouldn't even be in the set of requirements
      MarketDataNode childNode = buildDependencyNode(requirement, valuationTime, suppliedData);
      root.addChild(childNode);
    }
    return root;
  }

  private MarketDataNode buildDependencyNode(MarketDataRequirement requirement,
                                             ZonedDateTime valuationTime,
                                             MarketDataEnvironment suppliedData) {
    Class<? extends MarketDataId> idType = requirement.getMarketDataId().getClass();
    MarketDataBuilder marketDataBuilder = _builders.get(idType);

    if (marketDataBuilder != null) {
      MarketDataNode node = new MarketDataNode(requirement);
      Set<MarketDataRequirement> childReqs = requirement.getRequirements(marketDataBuilder, valuationTime, suppliedData);

      for (MarketDataRequirement childReq : childReqs) {
        if (!containsData(suppliedData, childReq)) {
          MarketDataNode childNode = buildDependencyNode(childReq, valuationTime, suppliedData);
          node.addChild(childNode);
        }
      }
      return node;
    } else {
      throw new IllegalArgumentException("No MarketDataBuilder registered to handle " +
                                             "MarketDataIds of type " + idType.getName());
    }
  }

  /**
   * Returns true if the environment contains data to satisfy the requirement.
   */
  private static boolean containsData(MarketDataEnvironment marketData, MarketDataRequirement req) {
    if (req instanceof SingleValueRequirement) {
      return marketData.getData().containsKey(req);
    }
    MarketDataId id = req.getMarketDataId();
    DateTimeSeries<LocalDate, ?> timeSeries = marketData.getTimeSeries().get(id);

    if (timeSeries == null) {
      return false;
    }
    LocalDateRange requiredDateRange = req.getMarketDataTime().getDateRange();
    // if the time series date range completely contains the requirement range then the environment contains
    // all the required data
    LocalDate reqStart = requiredDateRange.getStartDateInclusive();
    LocalDate reqEnd = requiredDateRange.getEndDateInclusive();
    LocalDate timeSeriesStart = timeSeries.getEarliestTime();
    LocalDate timeSeriesEnd = timeSeries.getLatestTime();

    return timeSeriesStart.compareTo(reqStart) <= 0 && timeSeriesEnd.compareTo(reqEnd) >= 0;
  }

  /**
   * Sets of requirements for a single type of {@link MarketDataId}.
   */
  private static class PartitionedRequirements {

    private final Class<? extends MarketDataId> _idType;
    private final Set<SingleValueRequirement> _singleValueRequirements;
    private final Set<TimeSeriesRequirement> _timeSeriesRequirements;

    private PartitionedRequirements(Class<? extends MarketDataId> idType,
                                    Set<SingleValueRequirement> singleValueRequirements,
                                    Set<TimeSeriesRequirement> timeSeriesRequirements) {
      _idType = idType;
      _singleValueRequirements = singleValueRequirements;
      _timeSeriesRequirements = timeSeriesRequirements;
    }

    /**
     * Partitions a set of requirements by the type of their {@link MarketDataId}.
     * <p>
     * The type of the market data ID is used to find the correct {@link MarketDataBuilder}. Sorting the requirements
     * by the type of their ID allows them to be submitted to the builders in bulk.
     *
     * @param requirements the requirements
     * @return a multimap of requirements, keyed by the class of their market data ID
     */
    private static List<PartitionedRequirements> partition(Set<MarketDataRequirement> requirements) {
      // the key type is used to find the correct market data builder which can satisfy the requirements
      SetMultimap<Class<? extends MarketDataId>, SingleValueRequirement> singleValueRequirements = HashMultimap.create();
      SetMultimap<Class<? extends MarketDataId>, TimeSeriesRequirement> timeSeriesRequirements = HashMultimap.create();

      for (MarketDataRequirement requirement : requirements) {
        MarketDataId marketDataId = requirement.getMarketDataId();
        Class<? extends MarketDataId> keyClass = marketDataId.getClass();

        if (requirement instanceof SingleValueRequirement) {
          singleValueRequirements.put(keyClass, (SingleValueRequirement) requirement);
        } else {
          timeSeriesRequirements.put(keyClass, (TimeSeriesRequirement) requirement);
        }
      }
      List<PartitionedRequirements> partitionedRequirements = new ArrayList<>();
      Set<Class<? extends MarketDataId>> idTypes = Sets.union(singleValueRequirements.keySet(),
                                                              timeSeriesRequirements.keySet());

      for (Class<? extends MarketDataId> idType : idTypes) {
        Set<SingleValueRequirement> singleValueReqsForType = singleValueRequirements.get(idType);
        Set<TimeSeriesRequirement> timeSeriesReqsForType = timeSeriesRequirements.get(idType);
        partitionedRequirements.add(new PartitionedRequirements(idType, singleValueReqsForType, timeSeriesReqsForType));
      }
      return partitionedRequirements;
    }
  }

  /**
   * Mutable node in a tree of dependencies between items of market data.
   */
  static class MarketDataNode {

    private final MarketDataRequirement _requirement;
    private final List<MarketDataNode> _children = new ArrayList<>();

    // for building the root node - maybe create factory methods and hide the constructors
    MarketDataNode() {
      _requirement = null;
    }

    MarketDataNode(MarketDataRequirement requirement) {
      _requirement = ArgumentChecker.notNull(requirement, "requirement");
    }

    List<MarketDataNode> getChildren() {
      return _children;
    }

    MarketDataRequirement getRequirement() {
      return _requirement;
    }

    boolean isLeaf() {
      return _children.isEmpty();
    }

    void addChild(MarketDataNode childNode) {
      _children.add(childNode);
    }

    @Override
    public int hashCode() {
      // deriving the hash code from the fields is dangerous because they're mutable.
      // strictly speaking this is correct, if a bit unusual
      return 42;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final MarketDataNode other = (MarketDataNode) obj;
      return Objects.equals(this._requirement, other._requirement) && Objects.equals(this._children, other._children);
    }

    @Override
    public String toString() {
      return "MarketDataNode [_requirement=" + _requirement + ", _children=" + _children + "]";
    }
  }
}
