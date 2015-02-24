/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.sesame.marketdata.MarketDataUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * A scenario definition defines how to create multiple sets of market data for running calculations over
 * a set of scenarios. The scenario data is created by applying perturbations to a set of base market data.
 * A different set of perturbations is used for each scenario.
 * <p>
 * A definition contains multiple {@link SingleScenarioDefinition} instances, each corresponding to one
 * scenario. Each single scenario definition contains market data filters and perturbations. Filters
 * are used to choose items of market data that are shocked in the scenario, and the perturbations
 * define those shocks.
 * <p>
 * Perturbations are applied in the order they are defined in scenario. An item of market data
 * can only be perturbed once, so if multiple mappings apply to it, only the first will be used.
 */
@BeanDefinition
public final class ScenarioDefinition implements ImmutableBean {

  /** The market data filters and perturbations that define the scenarios. */
  @PropertyDefinition(validate = "notEmpty")
  private final ImmutableList<SingleScenarioDefinition> _scenarios;

  /**
   * Creates a scenario definition from a list of individual scenarios.
   *
   * @param scenarios  the scenarios that make up the scenario definition
   * @return a scenario definition containing the scenarios
   */
  public static ScenarioDefinition ofScenarios(List<SingleScenarioDefinition> scenarios) {
    return new ScenarioDefinition(scenarios);
  }

  /**
   * Creates a scenario definition from individual scenarios.
   *
   * @param scenarios  the scenarios that make up the scenario definition
   * @return a scenario definition containing the scenarios
   */
  public static ScenarioDefinition ofScenarios(SingleScenarioDefinition... scenarios) {
    return new ScenarioDefinition(ImmutableList.copyOf(scenarios));
  }

  /**
   * Returns a scenario definition containing the perturbations in {@code mappings}.
   * <p>
   * Each mapping must contain the same number of perturbations. The definition will contain the
   * same number of scenarios as the number of perturbations in each mapping.
   * <p>
   * The first scenario contains the first perturbation from each mapping, the second scenario contains
   * the second perturbation from each mapping, and so on.
   * <p>
   * Given three mappings, A, B and C, each containing two perturbations, 1 and 2, there will be two
   * scenarios generated:
   * <pre>
   * |            |  A   |  B   |  C   |
   * |------------|------|------|------|
   * | Scenario 1 | A[1] | B[1] | C[1] |
   * | Scenario 2 | A[2] | B[2] | C[2] |
   * </pre>
   * For example, consider the following perturbation mappings:
   * <ul>
   *   <li>Filter: USD Curves, Shocks: [-10bp, 0, +10bp]</li>
   *   <li>Filter: EUR/USD Rate, Shocks: [+5%, 0, -5%]</li>
   * </ul>
   * The scenario definition would contain the following three scenarios:
   * <pre>
   * |            | USD Curves | EUR/USD Rate |
   * |------------|------------|--------------|
   * | Scenario 1 |     -10bp  |     +5%      |
   * | Scenario 2 |       0    |      0       |
   * | Scenario 3 |     +10bp  |     -5%      |
   * </pre>
   *
   * @param mappings  the filters and perturbations that define the scenario. Each mapping must contain the same
   *   number of perturbations
   * @return a scenario definition containing the perturbations in the mappings
   */
  public static ScenarioDefinition ofMappings(List<? extends PerturbationMapping<?>> mappings) {
    ArgumentChecker.notEmpty(mappings, "mappings");
    int numScenarios = countScenarios(mappings, false);

    for (int i = 1; i < mappings.size(); i++) {
      if (mappings.get(i).getPerturbations().size() != numScenarios) {
        throw new IllegalArgumentException(
            "All mappings must have the same number of perturbations. First mapping" +
                " has " + numScenarios + " perturbations, mapping " + i + " has " +
                mappings.get(i).getPerturbations().size());
      }
    }
    ImmutableSet<String> scenarioNames = generateNames(numScenarios);
    return new ScenarioDefinition(createScenarios(scenarioNames, mappings, false));
  }

  /**
   * Returns a scenario definition containing the perturbations in {@code mappings}.
   * <p>
   * Each mapping must contain the same number of perturbations. The definition will contain the
   * same number of scenarios as the number of perturbations in each mapping.
   * <p>
   * The first scenario contains the first perturbation from each mapping, the second scenario contains
   * the second perturbation from each mapping, and so on.
   * <p>
   * The set of scenario names must contain the same number of elements as the mappings.
   * <p>
   * Given three mappings, A, B and C, each containing two perturbations, 1 and 2, there will be two
   * scenarios generated:
   * <pre>
   * |            |  A   |  B   |  C   |
   * |------------|------|------|------|
   * | Scenario 1 | A[1] | B[1] | C[1] |
   * | Scenario 2 | A[2] | B[2] | C[2] |
   * </pre>
   * For example, consider the following perturbation mappings:
   * <ul>
   *   <li>Filter: USD Curves, Shocks: [-10bp, 0, +10bp]</li>
   *   <li>Filter: EUR/USD Rate, Shocks: [+5%, 0, -5%]</li>
   * </ul>
   * The scenario definition would contain the following three scenarios:
   * <pre>
   * |            | USD Curves | EUR/USD Rate |
   * |------------|------------|--------------|
   * | Scenario 1 |     -10bp  |     +5%      |
   * | Scenario 2 |       0    |      0       |
   * | Scenario 3 |     +10bp  |     -5%      |
   *
   * @param mappings  the filters and perturbations that define the scenario. Each mapping must contain the same
   *   number of perturbations
   * @param scenarioNames  the names of the scenarios. This must be the same size as the list of perturbations
   *   in each mapping
   * @return a scenario definition containing the perturbations in the mappings
   */
  public static ScenarioDefinition ofMappings(
      ImmutableSet<String> scenarioNames,
      List<? extends PerturbationMapping<?>> mappings) {

    ArgumentChecker.notNull(scenarioNames, "scenarioNames");
    int numScenarios = scenarioNames.size();

    for (int i = 0; i < mappings.size(); i++) {
      if (mappings.get(i).getPerturbations().size() != numScenarios) {
        throw new IllegalArgumentException(
            "Each mapping must contain the same number of perturbations as there are scenarios. There are " +
                numScenarios + " scenarios, mapping " + i + " has " + mappings.get(i).getPerturbations().size() +
                " perturbations.");
      }
    }
    return new ScenarioDefinition(createScenarios(scenarioNames, mappings, false));
  }

  /**
   * Returns a scenario definition created from all possible combinations of the mappings.
   * <p>
   * The mappings can have any number of perturbations, they do not need to have the same number as each other.
   * Each scenario contain one perturbation from each mapping. One scenario is created for each
   * possible combination of perturbations formed by taking one from each mapping.
   * <p>
   * The number of scenarios in the definition will be equal to the product of the number of perturbations
   * in the mappings.
   * <p>
   * Given three mappings, A, B and C, each containing two perturbations, 1 and 2, there will be eight
   * scenarios generated:
   * <pre>
   * |            |   A  |   B  |   C  |
   * |------------|------|------|------|
   * | Scenario 1 | A[1] | B[1] | C[1] |
   * | Scenario 2 | A[1] | B[1] | C[2] |
   * | Scenario 3 | A[1] | B[2] | C[1] |
   * | Scenario 4 | A[1] | B[2] | C[2] |
   * | Scenario 5 | A[2] | B[1] | C[1] |
   * | Scenario 6 | A[2] | B[1] | C[2] |
   * | Scenario 7 | A[2] | B[2] | C[1] |
   * | Scenario 8 | A[2] | B[2] | C[2] |
   * </pre>
   * For example, consider the following perturbation mappings:
   * <ul>
   *   <li>Filter: USD Curves, Shocks: [-10bp, 0, +10bp]</li>
   *   <li>Filter: EUR/USD Rate, Shocks: [+5%, 0, -5%]</li>
   * </ul>
   * The scenario definition would contain the following nine scenarios:
   * <pre>
   * |            | USD Curves | EUR/USD Rate |
   * |------------|------------|--------------|
   * | Scenario 1 |     -10bp  |     +5%      |
   * | Scenario 2 |     -10bp  |      0       |
   * | Scenario 3 |     -10bp  |     -5%      |
   * | Scenario 4 |       0    |     +5%      |
   * | Scenario 5 |       0    |      0       |
   * | Scenario 6 |       0    |     -5%      |
   * | Scenario 7 |     +10bp  |     +5%      |
   * | Scenario 8 |     +10bp  |      0       |
   * | Scenario 9 |     +10bp  |     -5%      |
   *
   * @param mappings  the filters and perturbations that define the scenarios. They can contain any number
   *   of perturbations, and they do not need to have the same number of perturbations
   * @return a scenario definition containing the perturbations in the mappings
   */
  public static ScenarioDefinition allCombinationsOf(List<? extends PerturbationMapping<?>> mappings) {
    int numScenarios = countScenarios(mappings, true);
    ImmutableSet<String> scenarioNames = generateNames(numScenarios);
    return new ScenarioDefinition(createScenarios(scenarioNames, mappings, true));
  }

  /**
   * Returns a scenario definition created from all possible combinations of the mappings.
   * <p>
   * The mappings can have any number of perturbations, they do not need to have the same number as each other.
   * Each scenario contain one perturbation from each mapping. One scenario is created for each
   * possible combination of perturbations formed by taking one from each mapping.
   * <p>
   * The number of scenarios in the definition will be equal to the product of the number of perturbations
   * in the mappings.
   * <p>
   * Given three mappings, A, B and C, each containing two perturbations, 1 and 2, there will be eight
   * scenarios generated:
   * <pre>
   * |            |   A  |   B  |   C  |
   * |------------|------|------|------|
   * | Scenario 1 | A[1] | B[1] | C[1] |
   * | Scenario 2 | A[1] | B[1] | C[2] |
   * | Scenario 3 | A[1] | B[2] | C[1] |
   * | Scenario 4 | A[1] | B[2] | C[2] |
   * | Scenario 5 | A[2] | B[1] | C[1] |
   * | Scenario 6 | A[2] | B[1] | C[2] |
   * | Scenario 7 | A[2] | B[2] | C[1] |
   * | Scenario 8 | A[2] | B[2] | C[2] |
   * </pre>
   * For example, consider the following perturbation mappings:
   * <ul>
   *   <li>Filter: USD Curves, Shocks: [-10bp, 0, +10bp]</li>
   *   <li>Filter: EUR/USD Rate, Shocks: [+5%, 0, -5%]</li>
   * </ul>
   * The scenario definition would contain the following nine scenarios:
   * <pre>
   * |            | USD Curves | EUR/USD Rate |
   * |------------|------------|--------------|
   * | Scenario 1 |     -10bp  |     +5%      |
   * | Scenario 2 |     -10bp  |      0       |
   * | Scenario 3 |     -10bp  |     -5%      |
   * | Scenario 4 |       0    |     +5%      |
   * | Scenario 5 |       0    |      0       |
   * | Scenario 6 |       0    |     -5%      |
   * | Scenario 7 |     +10bp  |     +5%      |
   * | Scenario 8 |     +10bp  |      0       |
   * | Scenario 9 |     +10bp  |     -5%      |
   *
   * @param mappings  the filters and perturbations that define the scenarios. They can contain any number
   *   of perturbations, and they do not need to have the same number of perturbations
   * @return a scenario definition containing the perturbations in the mappings
   */
  public static ScenarioDefinition allCombinationsOf(
      ImmutableSet<String> scenarioNames,
      List<? extends PerturbationMapping<?>> mappings) {

    ArgumentChecker.notEmpty(scenarioNames, "scenarioNames");
    ArgumentChecker.notEmpty(mappings, "mappings");
    int numScenarios = countScenarios(mappings, true);

    if (numScenarios != scenarioNames.size()) {
      throw new IllegalArgumentException(
          "The number of scenario names provided is " + scenarioNames.size() + " but " +
              "the number of scenarios is " + numScenarios);
    }
    return new ScenarioDefinition(createScenarios(scenarioNames, mappings, true));
  }

  /**
   * Counts the number of scenarios implied by the mappings and the {@code allCombinations} flag.
   *
   * @param mappings  the mappings that make up the scenarios
   * @param allCombinations  whether the scenarios are generated by taking all combinations of perturbations
   *   formed by taking one from each mapping
   * @return the number of scenarios
   */
  private static int countScenarios(List<? extends PerturbationMapping<?>> mappings, boolean allCombinations) {
    ArgumentChecker.notEmpty(mappings, "mappings");

    if (allCombinations) {
      // This accumulates the number of scenarios
      int numScenarios = mappings.get(0).getPerturbations().size();

      for (int i = 1; i < mappings.size(); i++) {
        numScenarios *= mappings.get(i).getPerturbations().size();
      }
      return numScenarios;
    } else {
      return mappings.get(0).getPerturbations().size();
    }
  }

  /**
   * Returns a definition for each scenario.
   *
   * @param scenarioNames  the names of the scenarios
   * @param mappings  the filters and perturbations that define the scenarios
   * @return the perturbations that should be applied in each scenario
   */
  private static List<SingleScenarioDefinition> createScenarios(
      ImmutableSet<String> scenarioNames,
      List<? extends PerturbationMapping<?>> mappings,
      boolean allCombinations) {

    ImmutableList.Builder<List<SinglePerturbationMapping>> singleMappingsBuilder = ImmutableList.builder();

    // Flatten the perturbation mappings into lists of single perturbations.
    // The mappings contain a filter and multiple perturbations used across multiple scenarios.
    // The single perturbation mappings contain a filter and a perturbation, and are used in a single scenario
    for (PerturbationMapping mapping : mappings) {
      singleMappingsBuilder.add(flattenPerturbations(mapping));
    }
    List<List<SinglePerturbationMapping>> perturbations = singleMappingsBuilder.build();
    List<String> scenarioNamesList = scenarioNames.asList();

    if (allCombinations) {
      return createScenariosForAllCombinations(perturbations, scenarioNamesList);
    } else {
      return createScenariosForSimpleCombinations(perturbations, scenarioNamesList);
    }
  }

  /**
   * Creates definitions for each individual scenario. Each scenario is created by taking one
   * perturbation for each perturbation mapping.
   * <p>
   * For example, consider a scenario definition containing a perturbation mapping for a curve with five
   * perturbations and a perturbation mapping for an FX rate with five perturbations. The result would
   * be five scenarios.
   * <p>
   * The first scenario contains the first curve shock and the first FX shock, the second scenario contains
   * the second curve shock and second FX shock, and so on.
   *
   * @param perturbations  the perturbations. The outer list contains an element for each perturbation mapping.
   *   The inner list contains an element for each perturbation in the perturbation mapping. The inners lists
   *   must have the same size
   * @param scenarioNames  the names of the scenarios
   * @return definitions for the individual scenarios
   */
  private static List<SingleScenarioDefinition> createScenariosForSimpleCombinations(
      List<List<SinglePerturbationMapping>> perturbations,
      List<String> scenarioNames) {

    ImmutableList.Builder<SingleScenarioDefinition> scenariosBuilder = ImmutableList.builder();

    for (int i = 0; i < scenarioNames.size(); i++) {
      ImmutableList.Builder<SinglePerturbationMapping> scenarioBuilder = ImmutableList.builder();

      for (List<SinglePerturbationMapping> mappingPerturbations : perturbations) {
        scenarioBuilder.add(mappingPerturbations.get(i));
      }
      scenariosBuilder.add(SingleScenarioDefinition.of(scenarioNames.get(i), scenarioBuilder.build()));
    }
    return scenariosBuilder.build();
  }

  /**
   * Creates definitions for each individual scenario. Each scenario contains one perturbation from each
   * perturbation mapping. A scenario is created for all possible combinations of perturbations.
   *
   * @param perturbations  the perturbations. The outer list contains an element for each perturbation mapping.
   *   The inner list contains an element for each perturbation in the perturbation mapping. The inners lists
   *   do not have to have the same size
   * @param scenarioNames  the names of the scenarios
   * @return definitions for the individual scenarios
   */
  private static List<SingleScenarioDefinition> createScenariosForAllCombinations(
      List<List<SinglePerturbationMapping>> perturbations,
      List<String> scenarioNames) {

    List<List<SinglePerturbationMapping>> cartesianProduct = MarketDataUtils.cartesianProduct(perturbations);
    ImmutableList.Builder<SingleScenarioDefinition> scenariosBuilder = ImmutableList.builder();

    // Each of the inner lists corresponds to a single scenario
    for (int i = 0; i < scenarioNames.size(); i++) {
      scenariosBuilder.add(SingleScenarioDefinition.of(scenarioNames.get(i), cartesianProduct.get(i)));
    }
    return scenariosBuilder.build();
  }

  /**
   * Generates simple names for the scenarios of the form 'Scenario 1' etc.
   */
  private static ImmutableSet<String> generateNames(int numScenarios) {
    ImmutableSet.Builder<String> namesBuilder = ImmutableSet.builder();

    for (int i = 1; i <= numScenarios; i++) {
      namesBuilder.add("Scenario " + Integer.toString(i));
    }
    return namesBuilder.build();
  }

  /**
   * A {@link PerturbationMapping} contains one filter and multiple perturbations. This method converts a
   * {@code PerturbationMapping} into a list of {@link SinglePerturbationMapping} which are a pair of filter
   * and perturbation.
   * <p>
   * The input data is:
   * <pre>
   *   [mapping, [perturbation1, perturbation2, perturbation3, ...]]
   * </pre>
   * and the output data is:
   * <pre>
   *   [[mapping, perturbation1], [mapping, perturbation2], [mapping, perturbation3], ...]
   * </pre>
   *
   * @param mapping a perturbation mapping
   * @return a list in which each element contains the filter from {@code mapping} and a perturbation
   */
  private static List<SinglePerturbationMapping> flattenPerturbations(PerturbationMapping<?> mapping) {
    ImmutableList.Builder<SinglePerturbationMapping> builder = ImmutableList.builder();

    for (Perturbation perturbation : mapping.getPerturbations()) {
      SinglePerturbationMapping singleMapping =
          SinglePerturbationMapping.builder()
              .filter(mapping.getFilter())
              .perturbation(perturbation)
              .build();
      builder.add(singleMapping);
    }
    return builder.build();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ScenarioDefinition}.
   * @return the meta-bean, not null
   */
  public static ScenarioDefinition.Meta meta() {
    return ScenarioDefinition.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ScenarioDefinition.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static ScenarioDefinition.Builder builder() {
    return new ScenarioDefinition.Builder();
  }

  private ScenarioDefinition(
      List<SingleScenarioDefinition> scenarios) {
    JodaBeanUtils.notEmpty(scenarios, "scenarios");
    this._scenarios = ImmutableList.copyOf(scenarios);
  }

  @Override
  public ScenarioDefinition.Meta metaBean() {
    return ScenarioDefinition.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data filters and perturbations that define the scenarios.
   * @return the value of the property, not empty
   */
  public ImmutableList<SingleScenarioDefinition> getScenarios() {
    return _scenarios;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ScenarioDefinition other = (ScenarioDefinition) obj;
      return JodaBeanUtils.equal(getScenarios(), other.getScenarios());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getScenarios());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("ScenarioDefinition{");
    buf.append("scenarios").append('=').append(JodaBeanUtils.toString(getScenarios()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ScenarioDefinition}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code scenarios} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableList<SingleScenarioDefinition>> _scenarios = DirectMetaProperty.ofImmutable(
        this, "scenarios", ScenarioDefinition.class, (Class) ImmutableList.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "scenarios");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1726545635:  // scenarios
          return _scenarios;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ScenarioDefinition.Builder builder() {
      return new ScenarioDefinition.Builder();
    }

    @Override
    public Class<? extends ScenarioDefinition> beanType() {
      return ScenarioDefinition.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code scenarios} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableList<SingleScenarioDefinition>> scenarios() {
      return _scenarios;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1726545635:  // scenarios
          return ((ScenarioDefinition) bean).getScenarios();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code ScenarioDefinition}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<ScenarioDefinition> {

    private List<SingleScenarioDefinition> _scenarios = new ArrayList<SingleScenarioDefinition>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(ScenarioDefinition beanToCopy) {
      this._scenarios = new ArrayList<SingleScenarioDefinition>(beanToCopy.getScenarios());
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1726545635:  // scenarios
          return _scenarios;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 1726545635:  // scenarios
          this._scenarios = (List<SingleScenarioDefinition>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public ScenarioDefinition build() {
      return new ScenarioDefinition(
          _scenarios);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code scenarios} property in the builder.
     * @param scenarios  the new value, not empty
     * @return this, for chaining, not null
     */
    public Builder scenarios(List<SingleScenarioDefinition> scenarios) {
      JodaBeanUtils.notEmpty(scenarios, "scenarios");
      this._scenarios = scenarios;
      return this;
    }

    /**
     * Sets the {@code scenarios} property in the builder
     * from an array of objects.
     * @param scenarios  the new value, not empty
     * @return this, for chaining, not null
     */
    public Builder scenarios(SingleScenarioDefinition... scenarios) {
      return scenarios(Arrays.asList(scenarios));
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("ScenarioDefinition.Builder{");
      buf.append("scenarios").append('=').append(JodaBeanUtils.toString(_scenarios));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
