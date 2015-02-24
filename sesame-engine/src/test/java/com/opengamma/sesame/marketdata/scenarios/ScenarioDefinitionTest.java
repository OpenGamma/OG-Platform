package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.sesame.marketdata.FxMatrixId;
import com.opengamma.sesame.marketdata.MarketDataId;

@Test
public class ScenarioDefinitionTest {

  private static final TestFilter FILTER_A = new TestFilter("a");
  private static final TestFilter FILTER_B = new TestFilter("b");
  private static final TestFilter FILTER_C = new TestFilter("c");
  private static final TestPerturbation PERTURBATION_A1 = new TestPerturbation(1);
  private static final TestPerturbation PERTURBATION_A2 = new TestPerturbation(2);
  private static final TestPerturbation PERTURBATION_B1 = new TestPerturbation(3);
  private static final TestPerturbation PERTURBATION_B2 = new TestPerturbation(4);
  private static final TestPerturbation PERTURBATION_C1 = new TestPerturbation(5);
  private static final TestPerturbation PERTURBATION_C2 = new TestPerturbation(6);

  private static final PerturbationMapping<TestPerturbation> MAPPING_A =
      PerturbationMapping.of(FILTER_A, PERTURBATION_A1, PERTURBATION_A2);

  private static final PerturbationMapping<TestPerturbation> MAPPING_B =
      PerturbationMapping.of(FILTER_B, PERTURBATION_B1, PERTURBATION_B2);

  private static final PerturbationMapping<TestPerturbation> MAPPING_C =
      PerturbationMapping.of(FILTER_C, PERTURBATION_C1, PERTURBATION_C2);

  public void ofMappings() {
    List<PerturbationMapping<TestPerturbation>> mappings = ImmutableList.of(MAPPING_A, MAPPING_B, MAPPING_C);
    ScenarioDefinition scenarioDefinition = ScenarioDefinition.ofMappings(mappings);
    List<SingleScenarioDefinition> scenarios =
        ImmutableList.of(
            SingleScenarioDefinition.of(
                "Scenario 1",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "Scenario 2",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)));

    assertEquals(scenarios, scenarioDefinition.getScenarios());
  }

  public void ofMappingsWithNames() {
    List<PerturbationMapping<TestPerturbation>> mappings = ImmutableList.of(MAPPING_A, MAPPING_B, MAPPING_C);
    ImmutableSet<String> scenarioNames = ImmutableSet.of("foo", "bar");
    ScenarioDefinition scenarioDefinition = ScenarioDefinition.ofMappings(scenarioNames, mappings);
    List<SingleScenarioDefinition> scenarios =
        ImmutableList.of(
            SingleScenarioDefinition.of(
                "foo",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "bar",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)));

    assertEquals(scenarios, scenarioDefinition.getScenarios());
  }

  public void allCombinationsOf() {
    List<PerturbationMapping<TestPerturbation>> mappings = ImmutableList.of(MAPPING_A, MAPPING_B, MAPPING_C);
    ScenarioDefinition scenarioDefinition = ScenarioDefinition.allCombinationsOf(mappings);
    List<SingleScenarioDefinition> scenarios =
        ImmutableList.of(
            SingleScenarioDefinition.of(
                "Scenario 1",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "Scenario 2",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)),
            SingleScenarioDefinition.of(
                "Scenario 3",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "Scenario 4",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)),
            SingleScenarioDefinition.of(
                "Scenario 5",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "Scenario 6",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)),
            SingleScenarioDefinition.of(
                "Scenario 7",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "Scenario 8",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)));

    assertEquals(scenarios, scenarioDefinition.getScenarios());
  }

  public void allCombinationsOfWithNames() {
    List<PerturbationMapping<TestPerturbation>> mappings = ImmutableList.of(MAPPING_A, MAPPING_B, MAPPING_C);
    ImmutableSet<String> scenarioNames = ImmutableSet.of("foo1", "foo2", "foo3", "foo4", "foo5", "foo6", "foo7", "foo8");
    ScenarioDefinition scenarioDefinition = ScenarioDefinition.allCombinationsOf(scenarioNames, mappings);
    List<SingleScenarioDefinition> scenarios =
        ImmutableList.of(
            SingleScenarioDefinition.of(
                "foo1",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "foo2",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)),
            SingleScenarioDefinition.of(
                "foo3",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "foo4",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)),
            SingleScenarioDefinition.of(
                "foo5",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "foo6",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)),
            SingleScenarioDefinition.of(
                "foo7",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "foo8",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)));

    assertEquals(scenarios, scenarioDefinition.getScenarios());
  }

  /**
   * Tests that a scenario definition won't be built if the scenarios names are specified and there
   * are the wrong number. The mappings all have 2 perturbations which should mean 2 scenarios, but
   * there are 3 scenario names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void ofMappingsWrongNumberOfScenarioNames() {
    List<PerturbationMapping<TestPerturbation>> mappings = ImmutableList.of(MAPPING_A, MAPPING_B, MAPPING_C);
    ImmutableSet<String> scenarioNames = ImmutableSet.of("foo", "bar", "baz");
    ScenarioDefinition.ofMappings(scenarioNames, mappings);
  }

  /**
   * Tests that a scenario definition won't be built if the mappings don't have the same number of perturbations
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void ofMappingsDifferentNumberOfPerturbations() {
    PerturbationMapping<TestPerturbation> mappingC = PerturbationMapping.of(FILTER_C, PERTURBATION_C1);
    List<PerturbationMapping<TestPerturbation>> mappings = ImmutableList.of(MAPPING_A, MAPPING_B, mappingC);
    ScenarioDefinition.ofMappings(mappings);
  }

  /**
   * Tests that a scenario definition won't be built if the mappings don't have the same number of perturbations
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void ofMappingsWithNamesDifferentNumberOfPerturbations() {
    PerturbationMapping<TestPerturbation> mappingC = PerturbationMapping.of(FILTER_C, PERTURBATION_C1);
    List<PerturbationMapping<TestPerturbation>> mappings = ImmutableList.of(MAPPING_A, MAPPING_B, mappingC);
    ImmutableSet<String> scenarioNames = ImmutableSet.of("foo", "bar");
    ScenarioDefinition.ofMappings(scenarioNames, mappings);
  }

  /**
   * Tests that a scenario definition won't be built if the scenarios names are specified and there are the wrong number
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void allCombinationsOfWrongNumberOfScenarioNames() {
    List<PerturbationMapping<TestPerturbation>> mappings = ImmutableList.of(MAPPING_A, MAPPING_B, MAPPING_C);
    ImmutableSet<String> scenarioNames = ImmutableSet.of("foo1", "foo2", "foo3", "foo4", "foo5", "foo6", "foo7");
    ScenarioDefinition.allCombinationsOf(scenarioNames, mappings);
  }

  public void ofScenarios() {
    List<SingleScenarioDefinition> scenarios =
        ImmutableList.of(
            SingleScenarioDefinition.of(
                "Scenario 1",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1)),
            SingleScenarioDefinition.of(
                "Scenario 2",
                SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
                SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
                SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2)));

    ScenarioDefinition scenarioDefinition = ScenarioDefinition.ofScenarios(scenarios);
    assertEquals(scenarios, scenarioDefinition.getScenarios());
  }

  public void ofScenariosVarargs() {
    SingleScenarioDefinition scenario1 =
        SingleScenarioDefinition.of(
            "Scenario 1",
            SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A1),
            SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B1),
            SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C1));
    SingleScenarioDefinition scenario2 =
        SingleScenarioDefinition.of(
            "Scenario 2",
            SinglePerturbationMapping.of(FILTER_A, PERTURBATION_A2),
            SinglePerturbationMapping.of(FILTER_B, PERTURBATION_B2),
            SinglePerturbationMapping.of(FILTER_C, PERTURBATION_C2));
    ScenarioDefinition scenarioDefinition = ScenarioDefinition.ofScenarios(scenario1, scenario2);
    assertEquals(ImmutableList.of(scenario1, scenario2), scenarioDefinition.getScenarios());
  }

  private static final class TestPerturbation implements Perturbation {

    private final int id;

    private TestPerturbation(int id) {
      this.id = id;
    }

    @Override
    public Object apply(Object marketData, MatchDetails matchDetails) {
      return marketData;
    }

    @Override
    public Class<?> getMarketDataType() {
      return Object.class;
    }

    @Override
    public Class<? extends MatchDetails> getMatchDetailsType() {
      return StandardMatchDetails.NoDetails.class;
    }

    @Override
    public PerturbationTarget getTargetType() {
      return PerturbationTarget.OUTPUT;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestPerturbation that = (TestPerturbation) o;
      return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }
  }

  private static final class TestFilter implements MarketDataFilter {

    private final String name;

    private TestFilter(String name) {
      this.name = name;
    }

    @Override
    public Set<? extends MatchDetails> apply(MarketDataId<?> marketDataId) {
      return ImmutableSet.of();
    }

    @Override
    public Set<? extends MatchDetails> apply(MarketDataId<?> marketDataId, Object marketData) {
      return ImmutableSet.of();
    }

    @Override
    public Class<?> getMarketDataType() {
      return Object.class;
    }

    @Override
    public Class<? extends MarketDataId<?>> getMarketDataIdType() {
      return FxMatrixId.class;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestFilter that = (TestFilter) o;
      return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }
}
