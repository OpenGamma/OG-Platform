/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;

/**
 * The encapsulated logic that controls how precisely a view is to be constructed
 * and computed.
 */
@PublicAPI
public class ViewDefinition implements Serializable {

  private final String _name;
  private final UniqueIdentifier _portfolioId;
  private final UserPrincipal _liveDataUser;

  private final ResultModelDefinition _resultModelDefinition;

  private Long _minDeltaCalculationPeriod;
  private Long _maxDeltaCalculationPeriod;

  private Long _minFullCalculationPeriod;
  private Long _maxFullCalculationPeriod;

  private final Map<String, ViewCalculationConfiguration> _calculationConfigurationsByName = new TreeMap<String, ViewCalculationConfiguration>();

  /**
   * If true, when a single computation cycle completes, the outputs are written
   * to a temporary file on the disk. This is not useful in a real production 
   * deployment, but can be useful in tests.
   */
  private boolean _dumpComputationCacheToDisk;

  // --------------------------------------------------------------------------
  /**
   * Constructs an instance, including a reference portfolio.
   * 
   * @param name  the name of the view definition
   * @param portfolioId  the unique identifier of the reference portfolio for this view definition
   * @param userName  the name of the user who owns the view definition
   */
  public ViewDefinition(String name, UniqueIdentifier portfolioId, String userName) {
    this(name, portfolioId, UserPrincipal.getLocalUser(userName), new ResultModelDefinition());
  }

  /**
   * Constructs an instance, without a reference portfolio.
   * 
   * @param name  the name of the view definition
   * @param userName  the name of the user who owns the view definition
   */
  public ViewDefinition(String name, String userName) {
    this(name, UserPrincipal.getLocalUser(userName));
  }

  /**
   * Constructs an instance, without a reference portfolio.
   * 
   * @param name  the name of the view definition
   * @param liveDataUser  the user who owns the view definition
   */
  public ViewDefinition(String name, UserPrincipal liveDataUser) {
    this(name, null, liveDataUser);
  }

  /**
   * Constructs an instance, without a reference portfolio.
   * 
   * @param name  the name of the view definition
   * @param liveDataUser  the user who owns the view definition
   * @param resultModelDefinition  configuration of the results from the view
   */
  public ViewDefinition(String name, UserPrincipal liveDataUser, ResultModelDefinition resultModelDefinition) {
    this(name, null, liveDataUser, resultModelDefinition);
  }

  /**
   * Constructs an instance
   * 
   * @param name  the name of the view definition
   * @param portfolioId  the unique identifier of the reference portfolio for this view definition, or
   *                     <code>null</code> if no reference portfolio is required
   * @param liveDataUser  the user who owns the view definition
   */
  public ViewDefinition(String name, UniqueIdentifier portfolioId, UserPrincipal liveDataUser) {
    this(name, portfolioId, liveDataUser, new ResultModelDefinition());
  }

  /**
   * Constructs an instance
   * 
   * @param name  the name of the view definition
   * @param portfolioId  the unique identifier of the reference portfolio for this view definition, or
   *                     <code>null</code> if no reference portfolio is required
   * @param liveDataUser  the user who owns the view definition
   * @param resultModelDefinition  configuration of the results from the view
   */
  public ViewDefinition(String name, UniqueIdentifier portfolioId, UserPrincipal liveDataUser, ResultModelDefinition resultModelDefinition) {
    ArgumentChecker.notNull(name, "View name");
    ArgumentChecker.notNull(liveDataUser, "User name");
    ArgumentChecker.notNull(resultModelDefinition, "Result model definition");

    _name = name;
    _portfolioId = portfolioId;
    _liveDataUser = liveDataUser;
    _resultModelDefinition = resultModelDefinition;
  }

  // --------------------------------------------------------------------------
  /**
   * Gets a set containing every portfolio output that is required, across all calculation configurations, regardless
   * of the security type(s) on which the output is required. These are outputs produced at the position and aggregate
   * position level, with respect to the reference portfolio. 
   * 
   * @return  a set of every required portfolio output across all calculation configurations, not null
   */
  public Set<Pair<String, ValueProperties>> getAllPortfolioRequirementNames() {
    Set<Pair<String, ValueProperties>> requirements = new TreeSet<Pair<String, ValueProperties>>();
    for (ViewCalculationConfiguration calcConfig : _calculationConfigurationsByName.values()) {
      requirements.addAll(calcConfig.getAllPortfolioRequirements());
    }
    return requirements;
  }

  /**
   * Returns the name of the view.
   * 
   * @return the view name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the unique identifier of the reference portfolio for this view. This is the portfolio on which position-level
   * calculations will be performed.
   * 
   * @return  the unique identifier of the reference portfolio, possibly null.
   */
  public UniqueIdentifier getPortfolioId() {
    return _portfolioId;
  }

  /**
   * Returns the user who 'owns' the view. The LiveData user should be used to create subscriptions. It ensures that the
   * view can be created and initialized without any end users trying to use it. Authorizing end users to interact with
   * the view is a separate matter and independent of this user. 
   * 
   * @return The LiveData user to create LiveData subscriptions for the view  
   */
  public UserPrincipal getLiveDataUser() {
    return _liveDataUser;
  }

  /**
   * Returns the calculation configurations.
   * 
   * @return the configurations
   */
  public Collection<ViewCalculationConfiguration> getAllCalculationConfigurations() {
    return new ArrayList<ViewCalculationConfiguration>(_calculationConfigurationsByName.values());
  }

  /**
   * Returns the set of calculation configuration names. These names can be passed to {@link #getCalculationConfiguration (String)}
   * to retrieve the configuration information.
   * 
   * @return the configuration names
   */
  public Set<String> getAllCalculationConfigurationNames() {
    return Collections.unmodifiableSet(_calculationConfigurationsByName.keySet());
  }

  /**
   * Returns a map of calculation configuration names to configurations.
   * 
   * @return the calculation configurations
   */
  public Map<String, ViewCalculationConfiguration> getAllCalculationConfigurationsByName() {
    return Collections.unmodifiableMap(_calculationConfigurationsByName);
  }

  /**
   * Returns the named calculation configuration.
   * 
   * @param configurationName name of the configuration
   * @return the configuration
   */
  public ViewCalculationConfiguration getCalculationConfiguration(String configurationName) {
    return _calculationConfigurationsByName.get(configurationName);
  }

  /**
   * Adds a new calculation configuration to the view definition. If there is already a configuration with that name it will
   * be replaced.
   * 
   * @param calcConfig the new configuration, not {@code null}
   */
  public void addViewCalculationConfiguration(ViewCalculationConfiguration calcConfig) {
    ArgumentChecker.notNull(calcConfig, "calculation configuration");
    ArgumentChecker.notNull(calcConfig.getName(), "Configuration name");
    _calculationConfigurationsByName.put(calcConfig.getName(), calcConfig);
  }

  /**
   * Add an output requirement to the view definition. This will become a terminal output when constructing dependency graphs for the view.
   * 
   * @param calculationConfigurationName the configuration to add this as a requirement to, not {@code null}
   * @param securityType the type of security for which an output should be produced, not {@code null}
   * @param requirementName the value name to be produced, not {@code null}
   * @param constraints additional constraints on the value produced, not {@code null}. For example this could be used to specify a currency
   * rather than use the view or portfolio default. 
   */
  public void addPortfolioRequirement(String calculationConfigurationName, String securityType, String requirementName, ValueProperties constraints) {
    ViewCalculationConfiguration calcConfig = _calculationConfigurationsByName.get(calculationConfigurationName);
    if (calcConfig == null) {
      calcConfig = new ViewCalculationConfiguration(this, calculationConfigurationName);
      _calculationConfigurationsByName.put(calculationConfigurationName, calcConfig);
    }
    calcConfig.addPortfolioRequirement(securityType, requirementName, constraints);
  }

  /**
   * Add an output requirement to the view definition. This will become a terminal output when constructing dependency graphs for the view.
   * The value is added without any constraints.
   * 
   * @param calculationConfigurationName the configuration to add this as a requirement to, not {@code null}
   * @param securityType the type of security for which an output should be produced, not {@code null}
   * @param requirementName the value name to be produced, not {@code null}
   */
  public void addPortfolioRequirementName(final String calculationConfigurationName, final String securityType, final String requirementName) {
    addPortfolioRequirement(calculationConfigurationName, securityType, requirementName, ValueProperties.none());
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the minimum period, in milliseconds, which must have elapsed since the start of the last delta calculation
   * when live computations are running. Delta calculations involve only those nodes in the dependency graph whose
   * inputs have changed since the previous calculation.
   * 
   * @return the minimum period between the start of two delta calculations, in milliseconds, or <code>null</code> to
   *         indicate that no minimum period is required to elapse.
   */
  public Long getMinDeltaCalculationPeriod() {
    return _minDeltaCalculationPeriod;
  }

  /**
   * Sets the minimum period, in milliseconds, which must have elapsed since the start of the last delta calculation
   * when live computations are running. Delta calculations involve only those nodes in the dependency graph whose
   * inputs have changed since the previous calculation.
   * 
   * @param minDeltaCalculationPeriod  the minimum period between the start of two delta calculations, in milliseconds,
   *                                   or <code>null</code> to indicate that no minimum period is required to elapse.
   */
  public void setMinDeltaCalculationPeriod(Long minDeltaCalculationPeriod) {
    _minDeltaCalculationPeriod = minDeltaCalculationPeriod;
  }

  /**
   * Gets the maximum period, in milliseconds, which can elapse since the start of the last full or delta calculation
   * before a delta recalculation is forced when live computations are running. In between the minimum and maximum
   * period, any relevant live data changes will immediately trigger a recalculation. The maximum calculation period is
   * therefore a fall-back which can be used to ensure that the view has always been calculated recently, even when no
   * live data changes have occurred. 
   * 
   * @return the maximum period allowed since the start of the last full or delta calculation, in milliseconds, or
   *         <code>null</code> if no maximum period is required.
   */
  public Long getMaxDeltaCalculationPeriod() {
    return _maxDeltaCalculationPeriod;
  }

  /**
   * Sets the maximum period, in milliseconds, which can elapse since the start of the last full or delta calculation
   * before a delta recalculation is forced when live computations are running. In between the minimum and maximum
   * period, any relevant live data changes will immediately trigger a recalculation. The maximum calculation period is
   * therefore a fall-back which can be used to ensure that the view has always been calculated recently, even when no
   * live data changes have occurred. 
   * 
   * @param maxDeltaCalculationPeriod  the maximum period allowed since the start of the last full or delta
   *                                   calculation, in milliseconds, or <code>null</code> if no maximum period is
   *                                   required.
   */
  public void setMaxDeltaCalculationPeriod(Long maxDeltaCalculationPeriod) {
    _maxDeltaCalculationPeriod = maxDeltaCalculationPeriod;
  }

  /**
   * Gets the minimum period, in milliseconds, which must have elapsed since the start of the last full calculation
   * when live computations are running. Full calculations involve recalculating every node in the dependency graph,
   * regardless of whether their inputs have changed.
   * 
   * @return the minimum period between the start of two full calculations, in milliseconds, or <code>null</code> to
   *         indicate that no minimum period is required to elapse.
   */
  public Long getMinFullCalculationPeriod() {
    return _minFullCalculationPeriod;
  }

  /**
   * Sets the minimum period, in milliseconds, which must have elapsed since the start of the last full calculation
   * when live computations are running. Full calculations involve recalculating every node in the dependency graph,
   * regardless of whether their inputs have changed.
   * 
   * @param minFullCalculationPeriod  the minimum period between the start of two full calculations, in milliseconds,
   *                                  or <code>null</code> to indicate that no minimum period is required to elapse.
   */
  public void setMinFullCalculationPeriod(Long minFullCalculationPeriod) {
    _minFullCalculationPeriod = minFullCalculationPeriod;
  }

  /**
   * Gets the maximum period, in milliseconds, which can elapse since the start of the last full calculation before a
   * full recalculation is forced when live computations are running. In between the minimum and maximum period, any
   * relevant live data changes will immediately trigger a recalculation. The maximum calculation period is therefore a
   * fall-back which can be used to ensure that the view has always been calculated recently, even when no live data
   * changes have occurred. 
   * 
   * @return the maximum period allowed since the start of the last full calculation, in milliseconds, or
   *         <code>null</code> if no maximum period is required.
   */
  public Long getMaxFullCalculationPeriod() {
    return _maxFullCalculationPeriod;
  }

  /**
   * Sets the maximum period, in milliseconds, which can elapse since the start of the last full calculation before a
   * full recalculation is forced when live computations are running. In between the minimum and maximum period, any
   * relevant live data changes will immediately trigger a recalculation. The maximum calculation period is therefore a
   * fall-back which can be used to ensure that the view has always been calculated recently, even when no live data
   * changes have occurred. 
   * 
   * @param maxFullCalculationPeriod  the maximum period allowed since the start of the last full calculation, in
   *                                  milliseconds, or <code>null</code> if no maximum period is required.
   */
  public void setMaxFullCalculationPeriod(Long maxFullCalculationPeriod) {
    _maxFullCalculationPeriod = maxFullCalculationPeriod;
  }

  // -------------------------------------------------------------------------
  /**
   * Returns the result model definition, describing how the results should be constructed and returned after execution
   * of the view.
   * 
   * @return the {@link ResultModelDefinition} instance.
   */
  public ResultModelDefinition getResultModelDefinition() {
    return _resultModelDefinition;
  }

  /**
   * Tests whether to dump the computation cache to disk after execution of the view. This is intended for debugging and
   * testing only. There are more efficient ways to interact with the computation cache to obtain terminal and intermediate
   * values following view execution.
   * 
   * @return {@code true} if the cache should be written to disk after view execution, {@code false} otherwise.
   */
  public boolean isDumpComputationCacheToDisk() {
    return _dumpComputationCacheToDisk;
  }

  /**
   * Sets whether to dump the computation cache to disk after execution of the view. This is intended for debugging and
   * testing only. There are more efficient ways to interact with the computation cache to obtain terminal and intermediate
   * values following view execution.
   * <p>
   * A view executor should write to a file in the system temporary directory with a filename based on the executing view's name.
   * 
   * @param dumpComputationCacheToDisk {@code true} to write the contents of the cache to disk after view execution, {@code false} otherwise
   */
  public void setDumpComputationCacheToDisk(boolean dumpComputationCacheToDisk) {
    _dumpComputationCacheToDisk = dumpComputationCacheToDisk;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ObjectUtils.hashCode(getName());
    result = prime * result + ObjectUtils.hashCode(getPortfolioId());
    result = prime * result + ObjectUtils.hashCode(getLiveDataUser());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof ViewDefinition)) {
      return false;
    }

    ViewDefinition other = (ViewDefinition) obj;
    boolean basicPropertiesEqual = ObjectUtils.equals(getName(), other.getName()) && ObjectUtils.equals(getPortfolioId(), other.getPortfolioId())
        && ObjectUtils.equals(getResultModelDefinition(), other.getResultModelDefinition()) && ObjectUtils.equals(getLiveDataUser(), other.getLiveDataUser())
        && ObjectUtils.equals(_minDeltaCalculationPeriod, other._minDeltaCalculationPeriod) && ObjectUtils.equals(_maxDeltaCalculationPeriod, other._maxDeltaCalculationPeriod)
        && ObjectUtils.equals(_minFullCalculationPeriod, other._minFullCalculationPeriod) && ObjectUtils.equals(_maxFullCalculationPeriod, other._maxFullCalculationPeriod)
        && ObjectUtils.equals(_dumpComputationCacheToDisk, other._dumpComputationCacheToDisk) && ObjectUtils.equals(getAllCalculationConfigurationNames(), other.getAllCalculationConfigurationNames());
    if (!basicPropertiesEqual) {
      return false;
    }

    for (ViewCalculationConfiguration localCalcConfig : _calculationConfigurationsByName.values()) {
      ViewCalculationConfiguration otherCalcConfig = other.getCalculationConfiguration(localCalcConfig.getName());
      if (!localCalcConfig.equals(otherCalcConfig)) {
        return false;
      }
    }

    return true;
  }

}
