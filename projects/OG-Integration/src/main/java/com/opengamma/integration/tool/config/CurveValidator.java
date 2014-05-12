/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.ConstantCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.FixedDateInterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InflationCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InflationIssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.SpreadCurveDefinition;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Class to validate third generation curve configurations
 */
public class CurveValidator {
  private static final Logger s_logger = LoggerFactory.getLogger(CurveValidator.class);
  private final ConfigMaster _configMaster;
  private final ConfigSource _configSource;
  private ConfigValidationUtils _configValidationUtils;
  private List<ValidationNode> _curveConstructionConfigNodes = new ArrayList<>();
  private List<ValidationNode> _exposureConfigNodes = new ArrayList<>();
  private final RegionSource _regionSource;
  private final LocalDate _curveDate;
  private final SecuritySource _securitySource;

  /**
   * Constructor
   * @param configMaster the config master
   * @param configSource the config source
   * @param conventionSource the convention source
   * @param regionSource the region source
   * @param secSource the security source
   * @param holidayMaster the holiday master
   */
  public CurveValidator(final ConfigMaster configMaster, final ConfigSource configSource, final ConventionSource conventionSource, 
                        final RegionSource regionSource, final SecuritySource secSource, final HolidayMaster holidayMaster) {
    _configMaster = configMaster;
    _configSource = configSource;
    _configValidationUtils = new ConfigValidationUtils(conventionSource, holidayMaster);
    _regionSource = regionSource;
    _securitySource = secSource;
    _curveDate = LocalDate.now();
  }
  /**
   * Not re-entrant.
   * @return
   */
  public void validateNewCurveSetup() {
    _curveConstructionConfigNodes = new ArrayList<>();
    _exposureConfigNodes = new ArrayList<>();
    final ConfigSearchRequest<CurveConstructionConfiguration> searchReq = new ConfigSearchRequest<>(CurveConstructionConfiguration.class);
    ConfigSearchResult<CurveConstructionConfiguration> searchResult;
    try {
      searchResult = _configMaster.search(searchReq);
      final Set<String> curveConstructionConfigNames = new HashSet<>();
      for (final ConfigItem<CurveConstructionConfiguration> curveConstructionConfigItem : searchResult.getValues()) {
        final CurveConstructionConfiguration curveConstructionConfig = curveConstructionConfigItem.getValue();
        curveConstructionConfigNames.add(curveConstructionConfig.getName());
        final ValidationNode validationNode = new ValidationNode();
        validationNode.setName(curveConstructionConfig.getName());
        validationNode.setType(CurveConstructionConfiguration.class);
        checkCurveConstructionConfiguration(curveConstructionConfig, validationNode);
        _curveConstructionConfigNodes.add(validationNode);
      }
      try {
        validateExposureFunctionsConfigurations(curveConstructionConfigNames, _exposureConfigNodes);
      } catch (final Exception e) {
        s_logger.error("Error while searching config master", e);
        final ValidationNode validationNode = new ValidationNode();
        validationNode.setType(ExposureFunctions.class);
        validationNode.setName("Config query error");
        validationNode.setError(true);
        validationNode.getErrors().add(e.getMessage());
      }
    } catch (final Exception e) {
      s_logger.error("Error while searching config master", e);
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setType(CurveConstructionConfiguration.class);
      validationNode.setName("Config query error");
      validationNode.setError(true);
      validationNode.getErrors().add(e.getMessage());
      _curveConstructionConfigNodes.add(validationNode);
    }

  }

  public List<ValidationNode> getCurveConstructionConfigResults() {
    return _curveConstructionConfigNodes;
  }

  public List<ValidationNode> getExposureFunctionsConfigResults() {
    return _exposureConfigNodes;
  }

  private void validateExposureFunctionsConfigurations(final Set<String> curveConstructionConfigurationNames, final List<ValidationNode> rootNodes) {
    final ConfigSearchRequest<ExposureFunctions> searchReq = new ConfigSearchRequest<>(ExposureFunctions.class);
    final ConfigSearchResult<ExposureFunctions> searchResult = _configMaster.search(searchReq);
    if (searchResult.getValues().size() == 0) {
      final ValidationNode node = new ValidationNode();
      node.setName("Empty Configuration");
      node.setType(ExposureFunctions.class);
      node.getErrors().add("No ExposuresFunctions could be found");
      node.setError(true);
      rootNodes.add(node);
    }
    for (final ConfigItem<ExposureFunctions> configItem : searchResult.getValues()) {
      final ValidationNode node = checkExposureFunctionsConfiguration(configItem.getValue(), curveConstructionConfigurationNames);
      rootNodes.add(node);
    }
  }

  private ValidationNode checkExposureFunctionsConfiguration(final ExposureFunctions configuration, final Set<String> curveConstructionConfigurationNames) {
    final ValidationNode node = new ValidationNode();
    node.setName(configuration.getName());
    node.setType(ExposureFunctions.class);
    if (configuration.getIdsToNames() != null) {
      final Map<ExternalId, String> idsToNames = configuration.getIdsToNames();
      for (final Map.Entry<ExternalId, String> entry : idsToNames.entrySet()) {
        final ValidationNode mapEntry = new ValidationNode();
        mapEntry.setName(entry.getKey().toString());
        mapEntry.setType(Map.Entry.class);
        final String name = entry.getValue();
        if (!curveConstructionConfigurationNames.contains(name)) {
          mapEntry.getErrors().add("Entry in idsToNames map " + entry.getKey() + " => " + name + " points to non-existent CurveConstructionConfiguration");
          mapEntry.setError(true);
        }
        node.getSubNodes().add(mapEntry);
      }
    }
    return node;
  }

  private void checkCurveConstructionConfiguration(final CurveConstructionConfiguration curveConstructionConfig, final ValidationNode validationNode) {
    for (final CurveGroupConfiguration curveGroupConfig : curveConstructionConfig.getCurveGroups()) {
      for (final Entry<String, List<? extends CurveTypeConfiguration>> curveForTypeEntry : curveGroupConfig.getTypesForCurves().entrySet()) {
        if (!curveDefinitionOrSubclassExists(curveForTypeEntry.getKey())) {
          validationNode.getErrors().add("Could not find CurveDefinition or subclass called " + curveForTypeEntry.getKey() + " in type for curves entry");
          validationNode.setError(true);
        }
        validateCurveTypeConfigrations(curveForTypeEntry.getKey(), curveForTypeEntry.getValue(), validationNode);
      }
    }
  }

  private void validateCurveTypeConfigrations(final String name, final List<? extends CurveTypeConfiguration> curveTypeConfigurations, final ValidationNode parentNode) {
    for (final CurveTypeConfiguration curveTypeConfig : curveTypeConfigurations) {
      final ValidationNode curveTypeConfigNode = new ValidationNode();
      curveTypeConfigNode.setName(name);
      curveTypeConfigNode.setType(CurveTypeConfiguration.class);
      validateCurveTypeConfiguration(name, curveTypeConfig, curveTypeConfigNode);
      parentNode.getSubNodes().add(curveTypeConfigNode);
    }
  }

  private void validateCurveTypeConfiguration(final String name, final CurveTypeConfiguration curveTypeConfig, final ValidationNode curveTypeConfigNode) {
    if (curveTypeConfig instanceof DiscountingCurveTypeConfiguration) {
      final DiscountingCurveTypeConfiguration discountingCurveTypeConfig = (DiscountingCurveTypeConfiguration) curveTypeConfig;
      validateDiscountingCurveTypeConfiguration(name, discountingCurveTypeConfig, curveTypeConfigNode);
    } else if (curveTypeConfig instanceof IborCurveTypeConfiguration) {
      final IborCurveTypeConfiguration iborCurveTypeConfiguration = (IborCurveTypeConfiguration) curveTypeConfig;
      validateIborCurveTypeConfiguration(name, iborCurveTypeConfiguration, curveTypeConfigNode);
    } else if (curveTypeConfig instanceof InflationCurveTypeConfiguration) {
      final InflationCurveTypeConfiguration inflationCurveTypeConfiguration = (InflationCurveTypeConfiguration) curveTypeConfig;
      validateInflationCurveTypeConfiguration(name, inflationCurveTypeConfiguration, curveTypeConfigNode);
    } else if (curveTypeConfig instanceof InflationIssuerCurveTypeConfiguration) {
      final InflationIssuerCurveTypeConfiguration inflationIssuerCurveTypeConfiguration = (InflationIssuerCurveTypeConfiguration) curveTypeConfig;
      validateInflationIssuerCurveTypeConfiguration(name, inflationIssuerCurveTypeConfiguration, curveTypeConfigNode);
    } else if (curveTypeConfig instanceof IssuerCurveTypeConfiguration) {
      final IssuerCurveTypeConfiguration issuerCurveTypeConfiguration = (IssuerCurveTypeConfiguration) curveTypeConfig;
      validateIssuerCurveTypeConfiguration(name, issuerCurveTypeConfiguration, curveTypeConfigNode);
    } else if (curveTypeConfig instanceof OvernightCurveTypeConfiguration) {
      final OvernightCurveTypeConfiguration overnightCurveTypeConfiguration = (OvernightCurveTypeConfiguration) curveTypeConfig;
      validateOvernightCurveTypeConfiguration(name, overnightCurveTypeConfiguration, curveTypeConfigNode);
    } else {
      curveTypeConfigNode.getErrors().add("Unknown type of CurveTypeConfiguration: " + curveTypeConfig.getClass().getSimpleName());
      curveTypeConfigNode.setError(true);
    }
  }

  private void validateDiscountingCurveTypeConfiguration(final String name, final DiscountingCurveTypeConfiguration curveTypeConfiguration, final ValidationNode curveTypeConfigNode) {
    final boolean regionDryRun = checkRegion(curveTypeConfiguration.getReference(), curveTypeConfigNode, true);
    final boolean currencyDryRun = checkCurrency(curveTypeConfiguration.getReference(), curveTypeConfigNode, true);
    if (regionDryRun || currencyDryRun) {
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(curveTypeConfiguration.getReference());
      if (regionDryRun) {
        validationNode.setType(Region.class);
      } else if (currencyDryRun) {
        validationNode.setType(Currency.class);
      }
      // config is good.
    } else {
      // record both errors
      checkRegion(curveTypeConfiguration.getReference(), curveTypeConfigNode, false);
      checkCurrency(curveTypeConfiguration.getReference(), curveTypeConfigNode, false);
    }
    final AbstractCurveDefinition abstractCurveDefinition = getCurveDefinitionOrSubclass(name);
    if (abstractCurveDefinition instanceof CurveDefinition) {
      final CurveDefinition curveDefinition = (CurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateCurveDefinition(curveDefinition, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof SpreadCurveDefinition) {
      final SpreadCurveDefinition curveDefinition = (SpreadCurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateDiscountingCurveTypeConfiguration(curveDefinition.getFirstCurve(), curveTypeConfiguration, validationNode);
      validateDiscountingCurveTypeConfiguration(curveDefinition.getSecondCurve(), curveTypeConfiguration, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof ConstantCurveDefinition) {
      final ConstantCurveDefinition curveDefinition = (ConstantCurveDefinition) abstractCurveDefinition;
      // assume this is fine?
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validationNode.getErrors().add("Using IborCurveTypeConfiguration with constant curve definition: check this is okay.");
      validationNode.setError(true);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    }
  }
  
  private void validateIborCurveTypeConfiguration(String name, IborCurveTypeConfiguration curveTypeConfiguration, ValidationNode curveTypeConfigNode) {
    if (!_configValidationUtils.conventionExists(curveTypeConfiguration.getConvention())) {
      ValidationNode validationNode = new ValidationNode();
      validationNode.setName(curveTypeConfiguration.getConvention().getValue());
      validationNode.setType(Convention.class);
      validationNode.getErrors().add("Could not find convention " + curveTypeConfiguration.getConvention());
      validationNode.setError(true);
    } else {
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(curveTypeConfiguration.getConvention().getValue());
      validationNode.setType(Convention.class);
      validationNode.getErrors().add("Could not find convention " + curveTypeConfiguration.getConvention());
      validationNode.setError(true);
    }
    final AbstractCurveDefinition abstractCurveDefinition = getCurveDefinitionOrSubclass(name);
    if (abstractCurveDefinition instanceof CurveDefinition) {
      final CurveDefinition curveDefinition = (CurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateCurveDefinition(curveDefinition, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof SpreadCurveDefinition) {
      final SpreadCurveDefinition curveDefinition = (SpreadCurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateIborCurveTypeConfiguration(curveDefinition.getFirstCurve(), curveTypeConfiguration, validationNode);
      validateIborCurveTypeConfiguration(curveDefinition.getSecondCurve(), curveTypeConfiguration, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof ConstantCurveDefinition) {
      final ConstantCurveDefinition curveDefinition = (ConstantCurveDefinition) abstractCurveDefinition;
      // assume this is fine?
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validationNode.getErrors().add("Using IborCurveTypeConfiguration with constant curve definition: check this is okay.");
      validationNode.setError(true);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    }
  }

  private void validateInflationCurveTypeConfiguration(final String name, final InflationCurveTypeConfiguration curveTypeConfiguration, final ValidationNode curveTypeConfigNode) {
    final boolean regionDryRun = checkRegion(curveTypeConfiguration.getReference(), curveTypeConfigNode, true);
    final boolean currencyDryRun = checkCurrency(curveTypeConfiguration.getReference(), curveTypeConfigNode, true);
    if (regionDryRun || currencyDryRun) {
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(curveTypeConfiguration.getReference());
      if (regionDryRun) {
        validationNode.setType(Region.class);
      } else if (currencyDryRun) {
        validationNode.setType(Currency.class);
      }
      // config is good.
    } else {
      // record both errors
      checkRegion(curveTypeConfiguration.getReference(), curveTypeConfigNode, false);
      checkCurrency(curveTypeConfiguration.getReference(), curveTypeConfigNode, false);
    }
    final AbstractCurveDefinition abstractCurveDefinition = getCurveDefinitionOrSubclass(name);
    if (abstractCurveDefinition instanceof CurveDefinition) {
      final CurveDefinition curveDefinition = (CurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateCurveDefinition(curveDefinition, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof SpreadCurveDefinition) {
      final SpreadCurveDefinition curveDefinition = (SpreadCurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateInflationCurveTypeConfiguration(curveDefinition.getFirstCurve(), curveTypeConfiguration, validationNode);
      validateInflationCurveTypeConfiguration(curveDefinition.getSecondCurve(), curveTypeConfiguration, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof ConstantCurveDefinition) {
      final ConstantCurveDefinition curveDefinition = (ConstantCurveDefinition) abstractCurveDefinition;
      // assume this is fine?
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validationNode.getErrors().add("Using IborCurveTypeConfiguration with constant curve definition: check this is okay.");
      validationNode.setError(true);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    }
  }

  private void validateInflationIssuerCurveTypeConfiguration(final String name, final InflationIssuerCurveTypeConfiguration curveTypeConfiguration, final ValidationNode curveTypeConfigNode) {
    final boolean regionDryRun = checkRegion(curveTypeConfiguration.getReference(), curveTypeConfigNode, true);
    final boolean currencyDryRun = checkCurrency(curveTypeConfiguration.getReference(), curveTypeConfigNode, true);
    if (regionDryRun || currencyDryRun) {
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(curveTypeConfiguration.getReference());
      if (regionDryRun) {
        validationNode.setType(Region.class);
      } else if (currencyDryRun) {
        validationNode.setType(Currency.class);
      }
      // config is good.
    } else {
      // record both errors
      checkRegion(curveTypeConfiguration.getReference(), curveTypeConfigNode, false);
      checkCurrency(curveTypeConfiguration.getReference(), curveTypeConfigNode, false);
    }
    final AbstractCurveDefinition abstractCurveDefinition = getCurveDefinitionOrSubclass(name);
    if (abstractCurveDefinition instanceof CurveDefinition) {
      final CurveDefinition curveDefinition = (CurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateCurveDefinition(curveDefinition, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof SpreadCurveDefinition) {
      final SpreadCurveDefinition curveDefinition = (SpreadCurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateInflationIssuerCurveTypeConfiguration(curveDefinition.getFirstCurve(), curveTypeConfiguration, validationNode);
      validateInflationIssuerCurveTypeConfiguration(curveDefinition.getSecondCurve(), curveTypeConfiguration, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof ConstantCurveDefinition) {
      final ConstantCurveDefinition curveDefinition = (ConstantCurveDefinition) abstractCurveDefinition;
      // assume this is fine?
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validationNode.getErrors().add("Using IborCurveTypeConfiguration with constant curve definition: check this is okay.");
      validationNode.setError(true);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    }
  }
  
  private void validateIssuerCurveTypeConfiguration(final String name, final IssuerCurveTypeConfiguration curveTypeConfiguration, final ValidationNode curveTypeConfigNode) {
    // currency a no-op
    final AbstractCurveDefinition abstractCurveDefinition = getCurveDefinitionOrSubclass(name);
    if (abstractCurveDefinition instanceof CurveDefinition) {
      final CurveDefinition curveDefinition = (CurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateCurveDefinition(curveDefinition, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof SpreadCurveDefinition) {
      final SpreadCurveDefinition curveDefinition = (SpreadCurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateIssuerCurveTypeConfiguration(curveDefinition.getFirstCurve(), curveTypeConfiguration, validationNode);
      validateIssuerCurveTypeConfiguration(curveDefinition.getSecondCurve(), curveTypeConfiguration, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof ConstantCurveDefinition) {
      final ConstantCurveDefinition curveDefinition = (ConstantCurveDefinition) abstractCurveDefinition;
      // assume this is fine?
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validationNode.getErrors().add("Using IborCurveTypeConfiguration with constant curve definition: check this is okay.");
      validationNode.setError(true);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    }
  }

  private void validateOvernightCurveTypeConfiguration(final String name, final OvernightCurveTypeConfiguration curveTypeConfiguration, final ValidationNode curveTypeConfigNode) {
    final ValidationNode onValidationNode = new ValidationNode();
    onValidationNode.setName(curveTypeConfiguration.getConvention().getValue());
    if (_configValidationUtils.conventionExists(curveTypeConfiguration.getConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(curveTypeConfiguration.getConvention());
      onValidationNode.setType(convention.getClass());
    } else {
      onValidationNode.setType(Convention.class);
      onValidationNode.getErrors().add("Can't find overnight convention using ID " + curveTypeConfiguration.getConvention());
      onValidationNode.setError(true);
    }
    curveTypeConfigNode.getSubNodes().add(onValidationNode);
    final AbstractCurveDefinition abstractCurveDefinition = getCurveDefinitionOrSubclass(name);
    if (abstractCurveDefinition instanceof CurveDefinition) {
      final CurveDefinition curveDefinition = (CurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateCurveDefinition(curveDefinition, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof SpreadCurveDefinition) {
      final SpreadCurveDefinition curveDefinition = (SpreadCurveDefinition) abstractCurveDefinition;
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validateOvernightCurveTypeConfiguration(curveDefinition.getFirstCurve(), curveTypeConfiguration, validationNode);
      validateOvernightCurveTypeConfiguration(curveDefinition.getSecondCurve(), curveTypeConfiguration, validationNode);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    } else if (abstractCurveDefinition instanceof ConstantCurveDefinition) {
      final ConstantCurveDefinition curveDefinition = (ConstantCurveDefinition) abstractCurveDefinition;
      // assume this is fine?
      final ValidationNode validationNode = new ValidationNode();
      validationNode.setName(name);
      validationNode.setType(curveDefinition.getClass());
      validationNode.getErrors().add("Using IborCurveTypeConfiguration with constant curve definition: check this is okay.");
      validationNode.setError(true);
      curveTypeConfigNode.getSubNodes().add(validationNode);
    }
  }

  private void validateCurveDefinition(final CurveDefinition curveDefinition, final ValidationNode curveDefinitionNode) {
    // check for an empty curve def

    if (curveDefinition.getNodes().size() == 0) {
      curveDefinitionNode.getErrors().add("Curve definition " + curveDefinition.getName() + " has zero nodes");
      curveDefinitionNode.setError(true);
    }
    // otherwise process the nodes
    for (CurveNode node : curveDefinition.getNodes()) {
      validateCurveNode(node, curveDefinitionNode);
    }
  }

  private ValidationNode createInvalidCurveNodeValidationNode(final Tenor tenor, final Class<? extends CurveNode> curveNodeType, final ValidationNode parentNode, final String message) {
    final ValidationNode validationNode = new ValidationNode();
    validationNode.setName(tenor.toFormattedString());
    validationNode.setType(curveNodeType);
    if (message != null) {
      validationNode.getErrors().add(message);
      validationNode.setError(true);
    }
    parentNode.getSubNodes().add(validationNode);
    return validationNode;
  }

  private void validateCurveNode(CurveNode curveNode, final ValidationNode validationNode) {
    final CurveNodeIdMapper curveNodeIdMapper = getCurveNodeIdMapper(curveNode.getCurveNodeIdMapperName());
    if (curveNodeIdMapper == null) {
      createInvalidCurveNodeValidationNode(curveNode.getResolvedMaturity(), curveNode.getClass(), validationNode, "CurveNodeIdMapper " + curveNode.getCurveNodeIdMapperName() + " is missing");
    } else {
      curveNode.accept(new CurveNodeValidator(_curveDate, _configValidationUtils, _securitySource, validationNode, curveNodeIdMapper, _configSource));
    }
  }

  private CurveNodeIdMapper getCurveNodeIdMapper(final String curveNodeIdMapperName) {
    return _configSource.getLatestByName(CurveNodeIdMapper.class, curveNodeIdMapperName);
  }

  /**
   * Check the string region ISO code (2-character) for validity against the RegionSource.  Records error in node object if dryRun is not set.
   * @param regionStr the potential region code.  Will add a warning but return true if region is not a country (e.g. EU).
   * @param node a node to record errors in
   * @param dryRun whether to omit writing an error
   * @return true, if this is a valid region code
   */
  private boolean checkRegion(final String regionStr, final ValidationNode node, final boolean dryRun) {
    final Region highestLevelRegion = _regionSource.getHighestLevelRegion(ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, regionStr));
    if (highestLevelRegion != null) {
      if (highestLevelRegion.getCountry() == null && !dryRun) {
        node.getWarnings().add("Warning: Region string " + regionStr + " was region but not country");
      }
      return true;
    } else {
      if (!dryRun) {
        node.getWarnings().add("Region string " + regionStr + " was not found as a region or country");
      }
      return false;
    }
  }

  /**
   * Check the string currency valid and record error if not dryRun.  Also adds warning if valid lexigraphically but not contained
   * in Currency class as a constant.
   * @param currencyStr the currency in string format (3-character ISO), not null
   * @param node the error recording node, not null
   * @param dryRun doesn't record an error if true
   * @return true if valid currency
   */
  private boolean checkCurrency(final String currencyStr, final ValidationNode node, final boolean dryRun) {
    try {
      final Currency currency = Currency.of(currencyStr);
      if (!checkCurrencyClass(currency)) {
        if (dryRun) {
          return false;
        } else {
          node.getWarnings().add("Warning: String expected to be currency parses, but is not in Currency class as constant");
          return true;
        }
      }
    } catch (final IllegalArgumentException iae) {
      if (dryRun) {
        return false;
      } else {
        node.getWarnings().add("String expected to be currency " + currencyStr + " will not parse");
      }
    }
    return true;
  }

  /**
   * Check the Currency class for a constant matching this currency instance
   * @param currency a currency instance, not null
   * @return true if there is a constant matching the supplied currency
   */
  private boolean checkCurrencyClass(final Currency currency) {
    final Field[] fields = Currency.class.getFields();
    for (final Field field : fields) {
      if (field.getName().equals(currency.getCode())) {
        return true;
      }
    }
    return false;
  }

  private boolean curveDefinitionOrSubclassExists(final String nameOfCurveDefinition) {
    return getCurveDefinitionOrSubclass(nameOfCurveDefinition) != null;
  }

  private AbstractCurveDefinition getCurveDefinitionOrSubclass(final String nameOfCurveDefinition) {
    final CurveDefinition curveDefinition = _configSource.getLatestByName(CurveDefinition.class, nameOfCurveDefinition);
    if (curveDefinition != null) {
      return curveDefinition;
    }
    final InterpolatedCurveDefinition interpolatedCurveDefinition = _configSource.getLatestByName(InterpolatedCurveDefinition.class, nameOfCurveDefinition);
    if (interpolatedCurveDefinition != null) {
      return interpolatedCurveDefinition;
    }
    final FixedDateInterpolatedCurveDefinition fixedDateInterpolatedCurveDefinition = _configSource.getLatestByName(FixedDateInterpolatedCurveDefinition.class, nameOfCurveDefinition);
    if (fixedDateInterpolatedCurveDefinition != null) {
      return fixedDateInterpolatedCurveDefinition;
    }
    final ConstantCurveDefinition constantCurveDefinition = _configSource.getLatestByName(ConstantCurveDefinition.class, nameOfCurveDefinition);
    if (constantCurveDefinition != null) {
      return constantCurveDefinition;
    }
    final SpreadCurveDefinition spreadCurveDefinition = _configSource.getLatestByName(SpreadCurveDefinition.class, nameOfCurveDefinition);
    if (spreadCurveDefinition != null) {
      return spreadCurveDefinition;
    }
    return null;
  }
}
