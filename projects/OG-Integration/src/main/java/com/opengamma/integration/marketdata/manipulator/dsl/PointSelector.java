/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.SelectorResolver;
import com.opengamma.engine.target.resolver.PrimitiveResolver;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.util.ArgumentChecker;

/**
 * Selects raw market data points that will be manipulated.
 */
public class PointSelector implements DistinctMarketDataSelector {

  /** Calc configs to which this selector will apply, null will match any config. */
  private final Set<String> _calcConfigNames;
  /** ID of the market data point to be manipulated. */
  private final Set<ExternalId> _ids;
  /** External ID scheme used when pattern matching ID value. */
  private final ExternalScheme _idMatchScheme;
  /** Regex pattern for matching ID value. */
  private final PatternWrapper _idMatchPattern;
  /** External ID scheme used when glob matching ID value. */
  private final ExternalScheme _idLikeScheme;
  /** Regex pattern for glob matching ID value. */
  private final PatternWrapper _idLikePattern;
  /** Security type names - matches if the ID identifies a security of the specified type. */
  private final Set<String> _securityTypes;

  /* package */ PointSelector(Set<String> calcConfigNames,
                              Set<ExternalId> ids,
                              ExternalScheme idMatchScheme,
                              Pattern idMatchPattern,
                              ExternalScheme idLikeScheme,
                              Pattern idLikePattern,
                              Set<String> securityTypes) {
    if (idMatchScheme == null && idMatchPattern != null || idMatchScheme != null && idMatchPattern == null) {
      throw new IllegalArgumentException("Scheme and pattern must both be specified to pattern match on ID");
    }
    if (idLikeScheme == null && idLikePattern != null || idLikeScheme != null && idLikePattern == null) {
      throw new IllegalArgumentException("Scheme and pattern must both be specified to glob match on ID");
    }
    _idLikeScheme = idLikeScheme;
    _idLikePattern = PatternWrapper.wrap(idLikePattern);
    _securityTypes = securityTypes;
    _idMatchScheme = idMatchScheme;
    _idMatchPattern = PatternWrapper.wrap(idMatchPattern);
    _calcConfigNames = calcConfigNames;
    _ids = ids;
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(ValueSpecification valueSpecification,
                                                         String calcConfigName,
                                                         final SelectorResolver resolver) {
    if (_calcConfigNames != null && !_calcConfigNames.contains(calcConfigName)) {
      return null;
    }
    if (!MarketDataRequirementNames.MARKET_VALUE.equals(valueSpecification.getValueName())) {
      return null;
    }
    ExternalIdBundle specificationIdBundle = createIds(valueSpecification);
    Set<ExternalId> specificationIds = new HashSet<>(specificationIdBundle.getExternalIds());
    if (_ids != null) {
      specificationIds.retainAll(_ids);
      if (specificationIds.isEmpty()) {
        return null;
      }
    }
    
    if (_idMatchScheme != null && _idMatchPattern != null) {
      if (functional(specificationIds).all(new Function1<ExternalId, Boolean>() {
        @Override
        public Boolean execute(ExternalId externalId) {
          return !_idMatchScheme.equals(externalId.getScheme());
        }
      })) {
        return null;
      }
      if (functional(specificationIds).all(new Function1<ExternalId, Boolean>() {
        @Override
        public Boolean execute(ExternalId externalId) {
          return !_idMatchPattern.getPattern().matcher(externalId.getValue()).matches();
        }
      })) {
        return null;
      }
    }
    if (_idLikeScheme != null && _idLikePattern != null) {
      if (functional(specificationIds).all(new Function1<ExternalId, Boolean>() {
        @Override
        public Boolean execute(ExternalId externalId) {
          return !_idLikeScheme.equals(externalId.getScheme());
        }
      })) {
        return null;
      }
      if (functional(specificationIds).all(new Function1<ExternalId, Boolean>() {
        @Override
        public Boolean execute(ExternalId externalId) {
          return !_idLikePattern.getPattern().matcher(externalId.getValue()).matches();
        }
      })) {
        return null;
      }
    }
    if (_securityTypes != null) {
      if (functional(specificationIds).all(new Function1<ExternalId, Boolean>() {
        @Override
        public Boolean execute(ExternalId externalId) {
          Security security = resolver.resolveSecurity(externalId);
          return !_securityTypes.contains(security.getSecurityType().toLowerCase());
        }
      })) {
        return null;
      }
    }
    return this;
  }

  private static ExternalIdBundle createIds(ValueSpecification valueSpecification) {
    if (valueSpecification.getProperty("Id") != null) {
      return ExternalIdBundle.of(ExternalId.parse(valueSpecification.getProperty("Id")));
    } else {
      // Id may not always be present - maybe with snapshots? (get External from UniqueId)
      UniqueId uniqueId = valueSpecification.getTargetSpecification().getUniqueId();
      String scheme = uniqueId.getScheme();
      if (scheme.startsWith("ExternalId-")) {
        return PrimitiveResolver.resolveExternalIds(uniqueId, "ExternalId-");
      } else {
        return ExternalIdBundle.of(scheme, uniqueId.getValue());
      }
    }
  }

  /* package */ Set<ExternalId> getIds() {
    return _ids;
  }

  /* package */ Set<String> getCalculationConfigurationNames() {
    return _calcConfigNames;
  }

  /* package */ ExternalScheme getIdMatchScheme() {
    return _idMatchScheme;
  }

  /* package */ Pattern getIdMatchPattern() {
    return _idMatchPattern == null ? null : _idMatchPattern.getPattern();
  }

  /* package */ Set<String> getSecurityTypes() {
    return _securityTypes;
  }

  /* package */ ExternalScheme getIdLikeScheme() {
    return _idLikeScheme;
  }

  /* package */ Pattern getIdLikePattern() {
    return _idLikePattern == null ? null : _idLikePattern.getPattern();
  }

  @Override
  public int hashCode() {
    return Objects.hash(_calcConfigNames, _ids, _idMatchScheme, _idMatchPattern, _idLikeScheme, _idLikePattern, _securityTypes);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final PointSelector other = (PointSelector) obj;
    return Objects.equals(this._calcConfigNames, other._calcConfigNames) &&
        Objects.equals(this._ids, other._ids) &&
        Objects.equals(this._idMatchScheme, other._idMatchScheme) &&
        Objects.equals(this._idMatchPattern, other._idMatchPattern) &&
        Objects.equals(this._idLikeScheme, other._idLikeScheme) &&
        Objects.equals(this._idLikePattern, other._idLikePattern) &&
        Objects.equals(this._securityTypes, other._securityTypes);
  }

  @Override
  public String toString() {
    return "PointSelector [" +
        "_calcConfigNames=" + _calcConfigNames +
        ", _ids=" + _ids +
        ", _idMatchScheme=" + _idMatchScheme +
        ", _idMatchPattern=" + _idMatchPattern +
        ", _idLikeScheme=" + _idLikeScheme +
        ", _idLikePattern=" + _idLikePattern +
        ", _securityTypes=" + _securityTypes +
        "]";
  }

  /**
   * Mutable builder to create {@link PointSelector}s.
   */
  public static class Builder {
    /** Scenario that the transformation will be added to. */
    private final Scenario _scenario;

    /** ID of the market data point to be manipulated. */
    private Set<ExternalId> _ids;
    /** External ID scheme used when pattern matching ID value. */
    private ExternalScheme _idMatchScheme;
    /** Regex pattern for matching ID value. */
    private Pattern _idMatchPattern;
    /** External ID scheme used when glob matching ID value. */
    private ExternalScheme _idLikeScheme;
    /** Regex pattern for glob matching ID value. */
    private Pattern _idLikePattern;
    /** Security type names - matches if the ID identifies a security of the specified type. */
    private Set<String> _securityTypes;

    /* package */ Builder(Scenario scenario) {
      _scenario = scenario;
    }

    /**
     * @return A selector built from this object's data.
     */
    public PointManipulatorBuilder apply() {
      return new PointManipulatorBuilder(_scenario, getSelector());
    }

    /* package */ PointSelector getSelector() {
      return new PointSelector(_scenario.getCalcConfigNames(),
                               _ids,
                               _idMatchScheme,
                               _idMatchPattern,
                               _idLikeScheme,
                               _idLikePattern,
                               _securityTypes);
    }

    /**
     * Adds a test for the market data ID value to match exactly.
     * @param scheme External ID scheme that must match the market data's ID scheme
     * @param value External ID value that must match the market data's ID value
     * @return This builder
     */
    public Builder id(String scheme, String value) {
      ArgumentChecker.notEmpty(scheme, "scheme");
      ArgumentChecker.notEmpty(value, "value");
      if (_ids != null) {
        throw new IllegalStateException("id() or ids() can only be called once");
      }
      _ids = ImmutableSet.of(ExternalId.of(scheme, value));
      return this;
    }

    /**
     * Adds a test for the market data ID value to match exactly.
     * @param ids The external IDs to match
     * @return This builder
     */
    public Builder ids(String... ids) {
      ArgumentChecker.notEmpty(ids, "ids");
      ArgumentChecker.notEmpty(ids, "ids");
      if (_ids != null) {
        throw new IllegalStateException("id() or ids() can only be called once");
      }
      Set<ExternalId> idSet = Sets.newHashSetWithExpectedSize(ids.length);
      for (String id : ids) {
        idSet.add(ExternalId.parse(id));
      }
      _ids = Collections.unmodifiableSet(idSet);
      return this;
    }

    /**
     * Adds a test for the market data ID value to match exactly.
     * @param ids The IDs to match
     * @return This builder
     */
    public Builder ids(ExternalId... ids) {
      ArgumentChecker.notEmpty(ids, "ids");
      if (_ids != null) {
        throw new IllegalStateException("id() or ids() can only be called once");
      }
      _ids = ImmutableSet.copyOf(ids);
      return this;
    }

    /**
     * Adds a test for the market data ID value to match a regular expression.
     * @param scheme External ID scheme that must match the market data's ID scheme
     * @param valueRegex Regular expression that must match the market data's ID value
     * @return This builder
     */
    public Builder idMatches(String scheme, String valueRegex) {
      ArgumentChecker.notEmpty(scheme, "scheme");
      ArgumentChecker.notEmpty(valueRegex, "valueRegex");
      if (_idMatchScheme != null) {
        throw new IllegalStateException("idMatches can only be called once");
      }
      _idMatchScheme = ExternalScheme.of(scheme);
      _idMatchPattern = Pattern.compile(valueRegex);
      return this;
    }

    /**
     * Adds a test for the market data ID value to match a regular expression.
     * @param scheme External ID scheme that must match the market data's ID scheme
     * @param valueGlob Glob for matching the ID value
     * @return This builder
     */
    public Builder idLike(String scheme, String valueGlob) {
      ArgumentChecker.notEmpty(scheme, "scheme");
      ArgumentChecker.notEmpty(valueGlob, "valueGlob");
      if (_idLikeScheme != null) {
        throw new IllegalStateException("idLike can only be called once");
      }
      _idLikeScheme = ExternalScheme.of(scheme);
      _idLikePattern = SimulationUtils.patternForGlob(valueGlob);
      return this;
    }

    /**
     * Limits the selection to the market value of IDs that identify a particular security type.
     * @param types The security types to match, case insensitive.
     * @return This builder
     */
    public Builder securityTypes(String... types) {
      ArgumentChecker.notEmpty(types, "types");
      if (_securityTypes != null) {
        throw new IllegalStateException("securityTypes can only be called once");
      }
      Set<String> securityTypes = Sets.newHashSet();
      for (String type : types) {
        if (type == null) {
          throw new IllegalArgumentException("Security type names must be non-null");
        }
        // downcase here and also when comparing so comparison isn't case sensitive
        securityTypes.add(type.toLowerCase());
      }
      _securityTypes = Collections.unmodifiableSet(securityTypes);
      return this;
    }

    /* package */ Scenario getScenario() {
      return _scenario;
    }
  }
}
