/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * The encapsulated logic that controls how precisely a view is to be constructed
 * and computed.
 */
@PublicAPI
@Config(description = "View definition", group = ConfigGroups.VIEWS)
public class ViewDefinition implements Serializable, UniqueIdentifiable, MutableUniqueIdentifiable {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewDefinition.class);

  private static final long serialVersionUID = 1L;

  private UniqueId _uniqueIdentifier;
  private final String _name;
  private final UniqueId _portfolioId;
  private final UserPrincipal _marketDataUser;

  private final ResultModelDefinition _resultModelDefinition;

  private Long _minDeltaCalculationPeriod;
  private Long _maxDeltaCalculationPeriod;
  private Long _minFullCalculationPeriod;
  private Long _maxFullCalculationPeriod;
  private boolean _persistent;
  
  private Currency _defaultCurrency;

  private final Map<String, ViewCalculationConfiguration> _calculationConfigurationsByName = new TreeMap<String, ViewCalculationConfiguration>();

  /**
   * If true, when a single computation cycle completes, the outputs are written
   * to a temporary file on the disk. This is not useful in a real production
   * deployment, but can be useful in tests.
   */
  private boolean _dumpComputationCacheToDisk;

  /**
   * Constructs an instance, including a reference portfolio.
   * 
   * @param name  the name of the view definition, not null
   * @param portfolioId the unique identifier of the portfolio referenced by this view definition, null if
   *                    no portfolio reference is required
   * @param marketDataUser  the name of the user who owns the view definition, not null
   */
  public ViewDefinition(final String name, final UniqueId portfolioId, final String marketDataUser) {
    this(null, name, portfolioId, marketDataUser);
  }

  /**
   * Constructs an instance, without a reference portfolio.
   * 
   * @param name  the name of the view definition, not null
   * @param marketDataUser  the name of the user who owns the view definition, not null
   */
  public ViewDefinition(final String name, final String marketDataUser) {
    this(null, name, marketDataUser);
  }

  /**
   * Constructs an instance, without a reference portfolio.
   * 
   * @param name  the name of the view definition, not null
   * @param marketDataUser  the user who owns the view definition, not null
   */
  public ViewDefinition(final String name, final UserPrincipal marketDataUser) {
    this(null, name, marketDataUser);
  }

  /**
   * Constructs an instance, without a reference portfolio.
   * 
   * @param name  the name of the view definition, not null
   * @param marketDataUser  the user who owns the view definition, not null
   * @param resultModelDefinition  configuration of the results from the view
   */
  public ViewDefinition(final String name, final UserPrincipal marketDataUser, final ResultModelDefinition resultModelDefinition) {
    this(null, name, marketDataUser, resultModelDefinition);
  }

  /**
   * Constructs an instance
   * 
   * @param name  the name of the view definition, not null
   * @param portfolioId the unique identifier of the portfolio referenced by this view definition, null if
   *                    no portfolio reference is required
   * @param marketDataUser  the user who owns the view definition, not null
   */

  public ViewDefinition(final String name, final UniqueId portfolioId, final UserPrincipal marketDataUser) {
    this(null, name, portfolioId, marketDataUser);
  }

  /**
   * Constructs an instance
   *
   * @param name  the name of the view definition, not null
   * @param portfolioId the unique identifier of the portfolio referenced by this view definition, null if
   *                    no portfolio reference is required
   * @param marketDataUser  the user who owns the view definition, not null
   * @param resultModelDefinition  configuration of the results from the view, not null
   */
  public ViewDefinition(final String name, final UniqueId portfolioId, final UserPrincipal marketDataUser, final ResultModelDefinition resultModelDefinition) {
    this(null, name, portfolioId, marketDataUser, resultModelDefinition);
  }

  //------------------------------------------------------------------------
  /**
   * Constructs an instance, including a reference portfolio.
   *
   * @param uniqueId  the unique id of the view definition
   * @param name  the name of the view definition, not null
   * @param portfolioId the unique identifier of the portfolio referenced by this view definition, null if
   *                    no portfolio reference is required
   * @param marketDataUser  the name of the user who owns the view definition, not null
   */
  public ViewDefinition(final UniqueId uniqueId, final String name, final UniqueId portfolioId, final String marketDataUser) {
    this(uniqueId, name, portfolioId, UserPrincipal.getLocalUser(marketDataUser), new ResultModelDefinition());
  }

  /**
   * Constructs an instance, without a reference portfolio.
   *
   * @param uniqueId  the unique id of the view definition
   * @param name  the name of the view definition, not null
   * @param marketDataUser  the name of the user who owns the view definition, not null
   */
  public ViewDefinition(final UniqueId uniqueId, final String name, final String marketDataUser) {
    this(uniqueId, name, UserPrincipal.getLocalUser(marketDataUser));
  }

  /**
   * Constructs an instance, without a reference portfolio.
   *
   * @param uniqueId  the unique id of the view definition
   * @param name  the name of the view definition, not null
   * @param marketDataUser  the user who owns the view definition, not null
   */
  public ViewDefinition(final UniqueId uniqueId, final String name, final UserPrincipal marketDataUser) {
    this(uniqueId, name, null, marketDataUser);
  }

  /**
   * Constructs an instance, without a reference portfolio.
   *
   * @param uniqueId  the unique id of the view definition
   * @param name  the name of the view definition, not null
   * @param marketDataUser  the user who owns the view definition, not null
   * @param resultModelDefinition  configuration of the results from the view
   */
  public ViewDefinition(final UniqueId uniqueId, final String name, final UserPrincipal marketDataUser, final ResultModelDefinition resultModelDefinition) {
    this(uniqueId, name, null, marketDataUser, resultModelDefinition);
  }

  /**
   * Constructs an instance
   *
   * @param uniqueId  the unique id of the view definition
   * @param name  the name of the view definition, not null
   * @param portfolioId the unique identifier of the portfolio referenced by this view definition, null if
   *                    no portfolio reference is required
   * @param marketDataUser  the user who owns the view definition, not null
   */
  public ViewDefinition(final UniqueId uniqueId, final String name, final UniqueId portfolioId, final UserPrincipal marketDataUser) {
    this(uniqueId, name, portfolioId, marketDataUser, new ResultModelDefinition());
  }

  /**
   * Constructs an instance
   *
   * @param uniqueId  the unique id of the view definition
   * @param name  the name of the view definition, not null
   * @param portfolioId the unique identifier of the portfolio referenced by this view definition, null if
   *                    no portfolio reference is required
   * @param marketDataUser  the user who owns the view definition, not null
   * @param resultModelDefinition  configuration of the results from the view, not null
   */
  public ViewDefinition(final UniqueId uniqueId, final String name, final UniqueId portfolioId, final UserPrincipal marketDataUser, final ResultModelDefinition resultModelDefinition) {
    ArgumentChecker.notNull(name, "View name");
    ArgumentChecker.notNull(marketDataUser, "User name");
    ArgumentChecker.notNull(resultModelDefinition, "Result model definition");

    _name = name;
    _portfolioId = portfolioId;
    _marketDataUser = marketDataUser;
    _resultModelDefinition = resultModelDefinition;

    _uniqueIdentifier = uniqueId;
  }

  //-------------------------------------------------------------------------
  /**
   * Performs a deep copy of the given view definition, with the opportunity to change its immutable fields.
   *
   * @param name  the name of the new view definition, not null
   * @param portfolioId  the unique identifier of the portfolio referenced by the new view definition, null if no
   *                     portfolio reference is required
   * @param marketDataUser  the user who owns the new view definition, not null
   * @return a copy of the base view definition with its immutable fields set to the new values, not null
   */
  public ViewDefinition copyWith(final String name, final UniqueId portfolioId, final UserPrincipal marketDataUser) {
    final ViewDefinition result = new ViewDefinition(name, portfolioId, marketDataUser, getResultModelDefinition());
    result.setDefaultCurrency(getDefaultCurrency());
    result.setDumpComputationCacheToDisk(isDumpComputationCacheToDisk());
    result.setMinDeltaCalculationPeriod(getMinDeltaCalculationPeriod());
    result.setMaxDeltaCalculationPeriod(getMaxDeltaCalculationPeriod());
    result.setMinFullCalculationPeriod(getMinFullCalculationPeriod());
    result.setMaxFullCalculationPeriod(getMaxFullCalculationPeriod());
    result.setPersistent(isPersistent());
    for (final ViewCalculationConfiguration baseCalcConfig : getAllCalculationConfigurations()) {
      baseCalcConfig.copyTo(result);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a set containing every portfolio output that is required, across all calculation configurations, regardless
   * of the security type(s) on which the output is required. These are outputs produced at the position and aggregate
   * position level, with respect to the reference portfolio.
   *
   * @return  a set of every required portfolio output across all calculation configurations, not null
   */
  public Set<Pair<String, ValueProperties>> getAllPortfolioRequirementNames() {
    final Set<Pair<String, ValueProperties>> requirements = new TreeSet<Pair<String, ValueProperties>>();
    for (final ViewCalculationConfiguration calcConfig : _calculationConfigurationsByName.values()) {
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
   * Gets the unique identifier of the portfolio referenced by this view definition. This is the portfolio on which
   * position-level calculations should be performed.
   *
   * @return  the unique identifier of the portfolio referenced by this view definition, null if no portfolio is
   *          referenced
   */
  public UniqueId getPortfolioId() {
    return _portfolioId;
  }

  /**
   * Gets the user to be associated with any market data subscriptions made for the view.
   *
   * @return the user to be associated with market data subscriptions
   */
  public UserPrincipal getMarketDataUser() {
    return _marketDataUser;
  }

  /**
   * Gets the default currency defined for this view
   *
   * @return the currency
   */
  public Currency getDefaultCurrency() {
    return _defaultCurrency;
  }

  /**
   * Sets the default currency to use
   *
   * @param currency The default currency
   */
  public void setDefaultCurrency(final Currency currency) {
    _defaultCurrency = currency;
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
   * @param configurationName  the name of the calculation configuration, not null
   * @return the calculation configuration, or null if no calculation configuration exists with that name.
   */
  public ViewCalculationConfiguration getCalculationConfiguration(final String configurationName) {
    ArgumentChecker.notNull(configurationName, "configurationName");
    return _calculationConfigurationsByName.get(configurationName);
  }

  /**
   * Adds a new calculation configuration to the view definition. If there is already a configuration with that name it will
   * be replaced.
   *
   * @param calcConfig the new configuration, not null
   */
  public void addViewCalculationConfiguration(final ViewCalculationConfiguration calcConfig) {
    ArgumentChecker.notNull(calcConfig, "calculation configuration");
    ArgumentChecker.notNull(calcConfig.getName(), "Configuration name");
    _calculationConfigurationsByName.put(calcConfig.getName(), calcConfig);
  }

  /**
   * Add an output requirement to the view definition. This will become a terminal output when constructing dependency graphs for the view.
   *
   * @param calculationConfigurationName the configuration to add this as a requirement to, not null
   * @param securityType the type of security for which an output should be produced, not null
   * @param requirementName the value name to be produced, not null
   * @param constraints additional constraints on the value produced, not null. For example this could be used to specify a currency
   * rather than use the view or portfolio default.
   */
  public void addPortfolioRequirement(final String calculationConfigurationName, final String securityType, final String requirementName, final ValueProperties constraints) {
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
   * @param calculationConfigurationName the configuration to add this as a requirement to, not null
   * @param securityType the type of security for which an output should be produced, not null
   * @param requirementName the value name to be produced, not null
   */
  public void addPortfolioRequirementName(final String calculationConfigurationName, final String securityType, final String requirementName) {
    addPortfolioRequirement(calculationConfigurationName, securityType, requirementName, ValueProperties.none());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the minimum period, in milliseconds, which must have elapsed since the start of the last delta calculation
   * before another cycle may be triggered. Delta calculations involve only those nodes in the dependency graph whose
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
   * before another cycle may be triggered. Delta calculations involve only those nodes in the dependency graph whose
   * inputs have changed since the previous calculation.
   *
   * @param minDeltaCalculationPeriod  the minimum period between the start of two delta calculations, in milliseconds,
   *                                   or <code>null</code> to indicate that no minimum period is required to elapse.
   */
  public void setMinDeltaCalculationPeriod(final Long minDeltaCalculationPeriod) {
    _minDeltaCalculationPeriod = minDeltaCalculationPeriod;
  }

  /**
   * Gets the maximum period, in milliseconds, which can elapse since the start of the last full or delta calculation
   * before a delta recalculation may be forced. In between the minimum and maximum period, any relevant market data
   * changes may immediately trigger a recalculation. The maximum calculation period is therefore a fall-back which can
   * be used to ensure that the view has always been calculated recently, even when no market data changes have
   * occurred.
   *
   * @return the maximum period allowed since the start of the last full or delta calculation, in milliseconds, or
   *         <code>null</code> if no maximum period is required.
   */
  public Long getMaxDeltaCalculationPeriod() {
    return _maxDeltaCalculationPeriod;
  }

  /**
   * Sets the maximum period, in milliseconds, which can elapse since the start of the last full or delta calculation
   * before a delta recalculation may be  forced. In between the minimum and maximum period, any relevant market data
   * changes may immediately trigger a recalculation. The maximum calculation period is therefore a fall-back which can
   * be used to ensure that the view has always been calculated recently, even when no market data changes have
   * occurred.
   *
   * @param maxDeltaCalculationPeriod  the maximum period allowed since the start of the last full or delta
   *                                   calculation, in milliseconds, or <code>null</code> if no maximum period is
   *                                   required.
   */
  public void setMaxDeltaCalculationPeriod(final Long maxDeltaCalculationPeriod) {
    _maxDeltaCalculationPeriod = maxDeltaCalculationPeriod;
  }

  /**
   * Gets the minimum period, in milliseconds, which must have elapsed since the start of the last full calculation
   * before another cycle may be triggered. Full calculations involve recalculating every node in the dependency graph,
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
   * before another cycle may be triggered. Full calculations involve recalculating every node in the dependency graph,
   * regardless of whether their inputs have changed.
   *
   * @param minFullCalculationPeriod  the minimum period between the start of two full calculations, in milliseconds,
   *                                  or <code>null</code> to indicate that no minimum period is required to elapse.
   */
  public void setMinFullCalculationPeriod(final Long minFullCalculationPeriod) {
    _minFullCalculationPeriod = minFullCalculationPeriod;
  }

  /**
   * Gets the maximum period, in milliseconds, which can elapse since the start of the last full calculation before a
   * full recalculation is forced. In between the minimum and maximum period, any relevant market data changes may
   * immediately trigger a recalculation. The maximum calculation period is therefore a fall-back which can be used to
   * ensure that the view has always been calculated recently, even when no market data changes have occurred.
   *
   * @return the maximum period allowed since the start of the last full calculation, in milliseconds, or
   *         <code>null</code> if no maximum period is required.
   */
  public Long getMaxFullCalculationPeriod() {
    return _maxFullCalculationPeriod;
  }

  /**
   * Sets the maximum period, in milliseconds, which can elapse since the start of the last full calculation before a
   * full recalculation is forced. In between the minimum and maximum period, any relevant market data changes may
   * immediately trigger a recalculation. The maximum calculation period is therefore a fall-back which can be used to
   * ensure that the view has always been calculated recently, even when no market data changes have occurred.
   *
   * @param maxFullCalculationPeriod  the maximum period allowed since the start of the last full calculation, in
   *                                  milliseconds, or <code>null</code> if no maximum period is required.
   */
  public void setMaxFullCalculationPeriod(final Long maxFullCalculationPeriod) {
    _maxFullCalculationPeriod = maxFullCalculationPeriod;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the result model definition, describing how the results should be constructed and returned after execution
   * of the view.
   *
   * @return the {@link ResultModelDefinition} instance.
   */
  public ResultModelDefinition getResultModelDefinition() {
    return _resultModelDefinition;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets whether this is a persistent view definition.
   * <p>
   * A shared view process for a persistent view definition remains alive with its dependency graphs compiled even when
   * no clients are connected. This can be useful if compilation is slow.
   * 
   * @return true if this is a persistent view definition, false otherwise
   */
  public boolean isPersistent() {
    return _persistent;
  }

  /**
   * Sets whether this is a persistent view definition.
   * <p>
   * A shared view process for a persistent view definition remains alive with its dependency graphs compiled even when
   * no clients are connected. This can be useful if compilation is slow.
   *  
   * @param persistent  true to make this a persistent view definition, false otherwise
   */
  public void setPersistent(boolean persistent) {
    _persistent = persistent;
  }  
  
  //-------------------------------------------------------------------------
  /**
   * Tests whether to dump the computation cache to disk after execution of the view. This is intended for debugging and
   * testing only. There are more efficient ways to interact with the computation cache to obtain terminal and intermediate
   * values following view execution.
   *
   * @return true if the cache should be written to disk after view execution
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
   * @param dumpComputationCacheToDisk true to write the contents of the cache to disk after view execution
   */
  public void setDumpComputationCacheToDisk(final boolean dumpComputationCacheToDisk) {
    _dumpComputationCacheToDisk = dumpComputationCacheToDisk;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ObjectUtils.hashCode(getName());
    result = prime * result + ObjectUtils.hashCode(getPortfolioId());
    result = prime * result + ObjectUtils.hashCode(getMarketDataUser());
    result = prime * result + _calculationConfigurationsByName.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof ViewDefinition)) {
      return false;
    }

    final ViewDefinition other = (ViewDefinition) obj;
    final boolean basicPropertiesEqual = ObjectUtils.equals(getName(), other.getName()) && ObjectUtils.equals(getPortfolioId(), other.getPortfolioId())
        && ObjectUtils.equals(getResultModelDefinition(), other.getResultModelDefinition()) && ObjectUtils.equals(getMarketDataUser(), other.getMarketDataUser())
        && ObjectUtils.equals(_minDeltaCalculationPeriod, other._minDeltaCalculationPeriod) && ObjectUtils.equals(_maxDeltaCalculationPeriod, other._maxDeltaCalculationPeriod)
        && ObjectUtils.equals(_minFullCalculationPeriod, other._minFullCalculationPeriod) && ObjectUtils.equals(_maxFullCalculationPeriod, other._maxFullCalculationPeriod)
        && ObjectUtils.equals(_dumpComputationCacheToDisk, other._dumpComputationCacheToDisk) && ObjectUtils.equals(getAllCalculationConfigurationNames(), other.getAllCalculationConfigurationNames())
        && ObjectUtils.equals(_defaultCurrency, other._defaultCurrency);
    if (!basicPropertiesEqual) {
      return false;
    }
    final Set<ViewCalculationConfiguration> localConfigs = new HashSet<ViewCalculationConfiguration>(_calculationConfigurationsByName.values());
    final Set<ViewCalculationConfiguration> otherConfigs = new HashSet<ViewCalculationConfiguration>(other.getAllCalculationConfigurations());
    return localConfigs.equals(otherConfigs);
  }

  @Override
  public void setUniqueId(final UniqueId uniqueIdentifier) {
    _uniqueIdentifier = uniqueIdentifier;
  }

  @Override
  public UniqueId getUniqueId() {
    return _uniqueIdentifier;
  }

  @Override
  public String toString() {
    if (s_logger.isDebugEnabled()) {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, false);
    } else {
      return "ViewDefinition[" + getName() + "]";
    }
  }

}
