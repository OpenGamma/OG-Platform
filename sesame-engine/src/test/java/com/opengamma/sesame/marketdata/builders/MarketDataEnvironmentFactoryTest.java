/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.builders;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.marketdata.CompositeMarketDataFactory;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.TimeSeriesRequirement;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory.MarketDataNode;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.LocalDateRange;

@Test(groups = TestGroup.UNIT)
public class MarketDataEnvironmentFactoryTest {

  @Test
  public void removeLeaves() {
    MarketDataNode root =
        rootNode(
            valueNode(new RawId("1")),
            valueNode(new CurveId("curve1"),
                      valueNode(new RawId("2")),
                      valueNode(new RawId("3")),
                      valueNode(new CurveId("curve2"),
                                seriesNode(new RawId("4"), dateRange(2011, 3, 8, 2012, 3, 7)))),
            valueNode(new SecurityId("sec", "1")));

    Set<MarketDataRequirement> reqs1 = new HashSet<>();
    MarketDataEnvironmentFactory.removeLeaves(root, reqs1);

    Set<MarketDataRequirement> expectedReqs1 =
        ImmutableSet.<MarketDataRequirement>of(
            SingleValueRequirement.of(new RawId("1")),
            SingleValueRequirement.of(new RawId("2")),
            SingleValueRequirement.of(new RawId("3")),
            TimeSeriesRequirement.of(new RawId("4"), dateRange(2011, 3, 8, 2012, 3, 7)),
            SingleValueRequirement.of(new SecurityId("sec", "1")));

    MarketDataNode expectedDependencies1 =
        rootNode(
            valueNode(new CurveId("curve1"),
                      valueNode(new CurveId("curve2"))));

    assertEquals(expectedReqs1, reqs1);
    assertEquals(expectedDependencies1, root);

    Set<MarketDataRequirement> reqs2 = new HashSet<>();
    MarketDataEnvironmentFactory.removeLeaves(root, reqs2);
    Set<MarketDataRequirement> expectedReqs2 =
        ImmutableSet.<MarketDataRequirement>of(SingleValueRequirement.of(new CurveId("curve2")));
    MarketDataNode expectedDependencies2 = rootNode(valueNode(new CurveId("curve1")));

    assertEquals(expectedReqs2, reqs2);
    assertEquals(expectedDependencies2, root);

    Set<MarketDataRequirement> reqs3 = new HashSet<>();
    MarketDataEnvironmentFactory.removeLeaves(root, reqs3);
    Set<MarketDataRequirement> expectedReqs3 =
        ImmutableSet.<MarketDataRequirement>of(SingleValueRequirement.of(new CurveId("curve1")));
    MarketDataNode expectedDependencies3 = rootNode();

    assertEquals(expectedReqs3, reqs3);
    assertEquals(expectedDependencies3, root);
  }

  private LocalDateRange dateRange(int year1, int month1, int date1, int year2, int month2, int date2) {
    return LocalDateRange.of(LocalDate.of(year1, month1, date1), LocalDate.of(year2, month2, date2), true);
  }

  @Test
  public void buildDependencyRoot() {
    Set<MarketDataRequirement> reqs =
        ImmutableSet.<MarketDataRequirement>of(SingleValueRequirement.of(new RawId("1")),
                                               SingleValueRequirement.of(new CurveId("curve1")),
                                               SingleValueRequirement.of(new SecurityId("sec", "1")));
    MarketDataNode expectedRoot =
        rootNode(
            valueNode(new RawId("1")),
            valueNode(new CurveId("curve1"),
                      valueNode(new RawId("2")),
                      valueNode(new RawId("3")),
                      valueNode(new CurveId("curve2"),
                                valueNode(new RawId("4")))),
            valueNode(new SecurityId("sec", "1")));

    MarketDataEnvironmentFactory builder = new MarketDataEnvironmentFactory(new CompositeMarketDataFactory(),
                                                                            new CurveBuilder(),
                                                                            new RawBuilder(),
                                                                            new SecurityBuilder());
    MarketDataNode root = builder.buildDependencyRoot(reqs, ZonedDateTime.now(), MarketDataEnvironmentBuilder.empty());
    assertEquals(expectedRoot, root);
  }

  @Test
  public void buildSingleValues() {
    Set<MarketDataRequirement> reqs =
        ImmutableSet.<MarketDataRequirement>of(SingleValueRequirement.of(new RawId("1")),
                                               SingleValueRequirement.of(new CurveId("curve1")),
                                               SingleValueRequirement.of(new SecurityId("sec", "1")));
    MarketDataEnvironmentFactory builder = new MarketDataEnvironmentFactory(new CompositeMarketDataFactory(),
                                                                            new CurveBuilder(),
                                                                            new RawBuilder(),
                                                                            new SecurityBuilder());
    MarketDataEnvironment env = new MarketDataEnvironmentBuilder().valuationTime(ZonedDateTime.now()).build();
    MarketDataBundle builtBundle = builder.build(env, reqs, EmptyMarketDataSpec.INSTANCE, ZonedDateTime.now()).toBundle();

    assertEquals(1d, builtBundle.get(new RawId("1"), Double.class).getValue());
    assertEquals(1d, builtBundle.get(new SecurityId("sec", "1"), Double.class).getValue());
    YieldAndDiscountCurve curve1 = builtBundle.get(new CurveId("curve1"), YieldAndDiscountCurve.class).getValue();
    assertEquals(1d, curve1.getInterestRate(2d));
    assertEquals(1d, curve1.getInterestRate(4d));

    // these are transitive requirements, they shouldn't be in the final environment
    Result<?> raw2Result = builtBundle.get(new RawId("2"), Double.class);
    Result<?> raw3Result = builtBundle.get(new RawId("3"), Double.class);
    Result<?> raw4Result = builtBundle.get(new RawId("4"), Double.class);
    assertFalse(raw2Result.isSuccess());
    assertFalse(raw3Result.isSuccess());
    assertFalse(raw4Result.isSuccess());
  }

  @Test
  public void buildSingleTimeSeries() {
    MarketDataEnvironmentFactory builder = new MarketDataEnvironmentFactory(new CompositeMarketDataFactory(),
                                                                            new CurveBuilder(),
                                                                            new RawBuilder(),
                                                                            new SecurityBuilder());
    LocalDate start = LocalDate.of(2011, 3, 8);
    LocalDate end = LocalDate.of(2012, 3, 8);
    TimeSeriesRequirement req = TimeSeriesRequirement.of(new RawId("1"), LocalDateRange.of(start, end, true));
    MarketDataEnvironment empty = MarketDataEnvironmentBuilder.empty();
    MarketDataEnvironment env = builder.build(empty,
                                              ImmutableSet.<MarketDataRequirement>of(req),
                                              EmptyMarketDataSpec.INSTANCE,
                                              ZonedDateTime.now());
    DateTimeSeries<LocalDate, ?> timeSeries = env.getTimeSeries().get(req.getMarketDataId());
    assertNotNull(timeSeries);
    assertEquals(start, timeSeries.getEarliestTime());
    assertEquals(end, timeSeries.getLatestTime());

    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
      Object value = timeSeries.getValue(date);
      assertEquals((double) date.toEpochDay(), value);
    }
  }

  /**
   * tests that two time series for the same ID are correctly merged when they overlap
   * TODO need to test this works when they're built in different phases (which hasn't been implemented yet)
   */
  @Test
  public void buildOverlappingTimeSeries() {
    LocalDate start1 = LocalDate.of(2010, 6, 17);
    LocalDate end1 = LocalDate.of(2012, 4, 18);
    RawId id = new RawId("1");
    TimeSeriesRequirement req1 = TimeSeriesRequirement.of(id, LocalDateRange.of(start1, end1, true));

    LocalDate start2 = LocalDate.of(2011, 3, 8);
    LocalDate end2 = LocalDate.of(2013, 3, 8);
    TimeSeriesRequirement req2 = TimeSeriesRequirement.of(id, LocalDateRange.of(start2, end2, true));

    MarketDataEnvironmentFactory builder = new MarketDataEnvironmentFactory(new CompositeMarketDataFactory(),
                                                                            new CurveBuilder(),
                                                                            new RawBuilder(),
                                                                            new SecurityBuilder());
    Set<MarketDataRequirement> reqs = ImmutableSet.<MarketDataRequirement>of(req1, req2);
    MarketDataEnvironment empty = MarketDataEnvironmentBuilder.empty();
    MarketDataEnvironment env = builder.build(empty, reqs, EmptyMarketDataSpec.INSTANCE, empty.getValuationTime());
    DateTimeSeries<LocalDate, ?> timeSeries = env.getTimeSeries().get(id);
    assertNotNull(timeSeries);
    assertEquals(start1, timeSeries.getEarliestTime());
    assertEquals(end2, timeSeries.getLatestTime());

    for (LocalDate date = start1; !date.isAfter(end2); date = date.plusDays(1)) {
      Object value = timeSeries.getValue(date);
      assertEquals((double) date.toEpochDay(), value);
    }
  }

  /**
   * tests that two time series for the same ID are correctly merged when they don't overlap
   */
  @Test
  public void buildDisjointTimeSeries() {
    LocalDate start1 = LocalDate.of(2010, 6, 17);
    LocalDate end1 = LocalDate.of(2011, 4, 18);
    RawId id = new RawId("1");
    TimeSeriesRequirement req1 = TimeSeriesRequirement.of(id, LocalDateRange.of(start1, end1, true));

    LocalDate start2 = LocalDate.of(2012, 3, 8);
    LocalDate end2 = LocalDate.of(2013, 3, 8);
    TimeSeriesRequirement req2 = TimeSeriesRequirement.of(id, LocalDateRange.of(start2, end2, true));

    MarketDataEnvironmentFactory builder = new MarketDataEnvironmentFactory(new CompositeMarketDataFactory(),
                                                                            new CurveBuilder(),
                                                                            new RawBuilder(),
                                                                            new SecurityBuilder());
    Set<MarketDataRequirement> reqs = ImmutableSet.<MarketDataRequirement>of(req1, req2);
    MarketDataEnvironment empty = MarketDataEnvironmentBuilder.empty();
    MarketDataEnvironment env = builder.build(empty, reqs, EmptyMarketDataSpec.INSTANCE, ZonedDateTime.now());
    DateTimeSeries<LocalDate, ?> timeSeries = env.getTimeSeries().get(id);
    assertNotNull(timeSeries);
    assertEquals(start1, timeSeries.getEarliestTime());
    assertEquals(end2, timeSeries.getLatestTime());

    for (LocalDate date = start1; !date.isAfter(end1); date = date.plusDays(1)) {
      Object value = timeSeries.getValue(date);
      assertEquals((double) date.toEpochDay(), value);
    }
    for (LocalDate date = end1.plusDays(1); !date.isAfter(start2.minusDays(1)); date = date.plusDays(1)) {
      assertNull("value not expected for " + date, timeSeries.getValue(date));
    }
    for (LocalDate date = start2; !date.isAfter(end2); date = date.plusDays(1)) {
      Object value = timeSeries.getValue(date);
      assertEquals((double) date.toEpochDay(), value);
    }
  }

  /**
   * tests that the tree of requirements doesn't include requirements for data supplied by the user.
   * this specifically tests requirements that aren't direct children of the root. when the requirements
   * are recorded, they are ignored if the data is in the supplied data. therefore there should never be
   * a requirement for supplied data as the direct child of the root node in the tree.
   */
  @Test
  public void suppliedDataNotInRequirementTree() {
    // date range is greater than the required data, shouldn't be a requirement in the tree
    // required range: 2010-6-3 to 2011-6-3
    LocalDate suppliedStart1 = LocalDate.of(2009, 6, 3);
    LocalDate suppliedEnd1 = LocalDate.of(2014, 6, 3);
    LocalDateRange suppliedRange1 = LocalDateRange.of(suppliedStart1, suppliedEnd1, true);

    // date range doesn't completely cover the required data, requirement will be in the tree
    // required range: 2012-6-3 to 2013-6-3
    LocalDate suppliedStart2 = LocalDate.of(2012, 6, 2);
    LocalDate suppliedEnd2 = LocalDate.of(2013, 6, 2);
    LocalDateRange suppliedRange2 = LocalDateRange.of(suppliedStart2, suppliedEnd2, true);

    // the expected dates in the requirements
    LocalDate expectedStart = LocalDate.of(2012, 6, 3);
    LocalDate expectedEnd = LocalDate.of(2013, 6, 3);
    LocalDateRange expectedDateRange = LocalDateRange.of(expectedStart, expectedEnd, true);

    // curve3 asks for single values for raw IDs 3 & 4 and time series for 1 & 2
    // supplied data contains raw ID 3, a time series for 1 that's greater than the one in the requirements
    // and a time series for 2 that doesn't cover the whole of the requirements
    MarketDataNode expectedRoot =
        rootNode(
            valueNode(new CurveId("curve3"),
                      seriesNode(new RawId("2"), expectedDateRange),
                      valueNode(new RawId("4"))));

    MarketDataEnvironment suppliedData = new MarketDataEnvironmentBuilder()
        .add(new RawId("3"), 1d)
        .add(new RawId("1"), buildTestTimeSeries(suppliedRange1))
        .add(new RawId("2"), buildTestTimeSeries(suppliedRange2))
        .valuationTime(ZonedDateTime.now())
        .build();

    MarketDataEnvironmentFactory builder = new MarketDataEnvironmentFactory(new CompositeMarketDataFactory(),
                                                                            new CurveBuilder(),
                                                                            new RawBuilder(),
                                                                            new SecurityBuilder());

    SingleValueRequirement req = SingleValueRequirement.of(new CurveId("curve3"));
    ImmutableSet<MarketDataRequirement> reqs = ImmutableSet.<MarketDataRequirement>of(req);
    MarketDataNode root = builder.buildDependencyRoot(reqs, ZonedDateTime.now(), suppliedData);

    assertEquals(expectedRoot, root);
  }

  private LocalDateDoubleTimeSeries buildTestTimeSeries(LocalDateRange dateRange) {
    LocalDate start = dateRange.getStartDateInclusive();
    LocalDate end = dateRange.getEndDateInclusive();
    LocalDateDoubleTimeSeriesBuilder builder = ImmutableLocalDateDoubleTimeSeries.builder();

    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
      builder.put(date, date.toEpochDay());
    }
    return builder.build();
  }


  private static MarketDataNode rootNode(MarketDataNode... children) {
    MarketDataNode node = new MarketDataNode();
    Collections.addAll(node.getChildren(), children);
    return node;
  }

  private static MarketDataNode valueNode(MarketDataId key, MarketDataNode... children) {
    MarketDataNode node = new MarketDataNode(SingleValueRequirement.of(key));
    Collections.addAll(node.getChildren(), children);
    return node;
  }

  private static MarketDataNode seriesNode(MarketDataId key, LocalDateRange dateRange, MarketDataNode... children) {
    MarketDataNode node = new MarketDataNode(TimeSeriesRequirement.of(key, dateRange));
    Collections.addAll(node.getChildren(), children);
    return node;
  }
}

class RawId implements MarketDataId<Double> {

  private final String _id;

  RawId(String id) {
    _id = id;
  }

  public Object getMetadata() {
    return _id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    RawId other = (RawId) obj;
    return Objects.equals(this._id, other._id);
  }

  @Override
  public String toString() {
    return "RawKey [_id='" + _id + "']";
  }

  @Override
  public Class<Double> getMarketDataType() {
    return Double.class;
  }
}

class CurveId implements MarketDataId<YieldAndDiscountCurve> {

  private final String _curveName;

  CurveId(String curveName) {
    _curveName = curveName;
  }

  public Object getMetadata() {
    return _curveName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_curveName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    CurveId other = (CurveId) obj;
    return Objects.equals(this._curveName, other._curveName);
  }

  @Override
  public String toString() {
    return "CurveKey [_curveName='" + _curveName + "']";
  }

  @Override
  public Class<YieldAndDiscountCurve> getMarketDataType() {
    return YieldAndDiscountCurve.class;
  }
}

class SecurityId implements MarketDataId<Double> {

  private final ExternalId _id;

  SecurityId(String scheme, String value) {
    _id = ExternalId.of(scheme, value);
  }

  public Object getMetadata() {
    return _id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SecurityId other = (SecurityId) obj;
    return Objects.equals(this._id, other._id);
  }

  @Override
  public Class<Double> getMarketDataType() {
    return Double.class;
  }
}

class RawBuilder implements MarketDataBuilder {

  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    return Collections.emptySet();
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(TimeSeriesRequirement requirement,
                                                              Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {
    return Collections.emptySet();
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource) {
    Map<SingleValueRequirement, Result<?>> results = new HashMap<>();

    for (SingleValueRequirement requirement : requirements) {
      RawId key = (RawId) requirement.getMarketDataId();
      String id = (String) key.getMetadata();

      switch (id) {
        case "1":
          results.put(requirement, Result.success(1d));
          break;
        case "2":
          results.put(requirement, Result.success(2d));
          break;
        case "3":
          results.put(requirement, Result.success(3d));
          break;
        case "4":
          results.put(requirement, Result.success(4d));
          break;
      }
    }
    return results;
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource) {

    Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> results = new HashMap<>();

    for (TimeSeriesRequirement requirement : requirements) {
      LocalDateDoubleTimeSeriesBuilder builder = ImmutableLocalDateDoubleTimeSeries.builder();
      LocalDateRange dateRange = requirement.getMarketDataTime().getDateRange();
      LocalDate start = dateRange.getStartDateInclusive();
      LocalDate end = dateRange.getEndDateInclusive();

      for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
        builder.put(date, date.toEpochDay());
      }
      Result<DateTimeSeries<LocalDate, ?>> success = Result.<DateTimeSeries<LocalDate, ?>>success(builder.build());
      results.put(requirement, success);
    }
    return results;
  }

  @Override
  public Class<? extends MarketDataId> getKeyType() {
    return RawId.class;
  }
}

class CurveBuilder implements MarketDataBuilder {

  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    CurveId key = (CurveId) requirement.getMarketDataId();
    String curveName = ((String) key.getMetadata());

    switch (curveName) {
      case "curve1":
        return ImmutableSet.<MarketDataRequirement>of(
            SingleValueRequirement.of(new RawId("2")),
            SingleValueRequirement.of(new RawId("3")),
            SingleValueRequirement.of(new CurveId("curve2")));
      case "curve2":
        return ImmutableSet.<MarketDataRequirement>of(SingleValueRequirement.of(new RawId("4")));
      case "curve3":
        LocalDate start1 = LocalDate.of(2010, 6, 3);
        LocalDate end1 = LocalDate.of(2011, 6, 3);
        LocalDate start2 = LocalDate.of(2012, 6, 3);
        LocalDate end2 = LocalDate.of(2013, 6, 3);
        TimeSeriesRequirement req1 = TimeSeriesRequirement.of(new RawId("1"), LocalDateRange.of(start1, end1, true));
        TimeSeriesRequirement req2 = TimeSeriesRequirement.of(new RawId("2"), LocalDateRange.of(start2, end2, true));
        SingleValueRequirement req3 = SingleValueRequirement.of(new RawId("3"));
        SingleValueRequirement req4 = SingleValueRequirement.of(new RawId("4"));
        return ImmutableSet.<MarketDataRequirement>of(req1, req2, req3, req4);
      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(
      TimeSeriesRequirement requirement,
      Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {

    // TODO implement getTimeSeriesRequirements()
    throw new UnsupportedOperationException("getTimeSeriesRequirements not implemented");
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource) {
    Map<SingleValueRequirement, Result<?>> results = new HashMap<>();

    for (SingleValueRequirement requirement : requirements) {
      CurveId curveId = (CurveId) requirement.getMarketDataId();

      switch (((String) curveId.getMetadata())) {
        case "curve1":
          Result<?> raw2 = marketDataBundle.get(new RawId("2"), Double.class);
          Result<?> raw3 = marketDataBundle.get(new RawId("3"), Double.class);
          Result<?> curve2 = marketDataBundle.get(new CurveId("curve2"), YieldAndDiscountCurve.class);
          assertTrue(raw2.isSuccess());
          assertTrue(raw3.isSuccess());
          assertTrue(curve2.isSuccess());
          assertEquals(raw2.getValue(), 2d);
          assertEquals(raw3.getValue(), 3d);
          YieldAndDiscountCurve curve2Value = (YieldAndDiscountCurve) curve2.getValue();
          assertEquals(2d, curve2Value.getInterestRate(1d));
          results.put(requirement, Result.success(new YieldCurve("curve1", ConstantDoublesCurve.from(1d))));
          break;
        case "curve2":
          Result<?> raw4 = marketDataBundle.get(new RawId("4"), Double.class);
          assertTrue(raw4.isSuccess());
          results.put(requirement, Result.success(new YieldCurve("curve2", ConstantDoublesCurve.from(2d))));
          break;
      }
    }
    return results;
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource) {

    return Collections.emptyMap();
  }

  @Override
  public Class<? extends MarketDataId> getKeyType() {
    return CurveId.class;
  }
}

class SecurityBuilder implements MarketDataBuilder {

  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    return Collections.emptySet();
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(TimeSeriesRequirement requirement,
                                                              Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {
    // TODO implement getTimeSeriesRequirements()
    throw new UnsupportedOperationException("getTimeSeriesRequirements not implemented");
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource) {
    Map<SingleValueRequirement, Result<?>> results = new HashMap<>();

    for (SingleValueRequirement requirement : requirements) {
      SecurityId key = (SecurityId) requirement.getMarketDataId();
      ExternalId id = (ExternalId) key.getMetadata();

      if (id.equals(ExternalId.of("sec", "1"))) {
        results.put(requirement, Result.success(1d));
      }
    }
    return results;
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource) {

    return Collections.emptyMap();
  }

  @Override
  public Class<? extends MarketDataId> getKeyType() {
    return SecurityId.class;
  }
}
