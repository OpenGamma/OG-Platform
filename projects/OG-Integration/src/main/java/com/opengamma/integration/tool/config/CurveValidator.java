/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.SpreadCurveDefinition;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurvePointsInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CalendarSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Class to validate third generation curve configurations
 */
public class CurveValidator {
  private static final Logger s_logger = LoggerFactory.getLogger(CurveValidator.class);
  private final ConfigMaster _configMaster;
  private final ConfigSource _configSource;
  private final ConventionSource _conventionSource;
  private List<ValidationNode> _curveConstructionConfigNodes = new ArrayList<>();
  private List<ValidationNode> _exposureConfigNodes = new ArrayList<>();
  private final RegionSource _regionSource;
  private final LocalDate _curveDate;
  private final SecuritySource _securitySource;
  private final HolidayMaster _holidayMaster;

  public CurveValidator(final ConfigMaster configMaster, final ConfigSource configSource, final ConventionSource conventionSource, final RegionSource regionSource, final SecuritySource secSource, final HolidayMaster holidayMaster) {
    _configMaster = configMaster;
    _configSource = configSource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
    _securitySource = secSource;
    _curveDate = LocalDate.now();
    _holidayMaster = holidayMaster;
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

  private void validateIborCurveTypeConfiguration(final String name, final IborCurveTypeConfiguration curveTypeConfiguration, final ValidationNode curveTypeConfigNode) {
    if (!conventionExists(curveTypeConfiguration.getConvention())) {
      final ValidationNode validationNode = new ValidationNode();
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
    if (conventionExists(curveTypeConfiguration.getConvention())) {
      final ManageableConvention convention = getConvention(curveTypeConfiguration.getConvention());
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
    if (curveDefinition.getNodes().size() == 0) {
      curveDefinitionNode.getErrors().add("Curve definition " + curveDefinition.getName() + " has zero nodes");
      curveDefinitionNode.setError(true);
    }
    for (final CurveNode node : curveDefinition.getNodes()) {
      //      ValidationNode curveNodeValidationNode = new ValidationNode();
      //      curveNodeValidationNode.setName(node.getResolvedMaturity().toFormattedString());
      //      curveNodeValidationNode.setType(node.getClass());
      validateCurveNode(node, curveDefinitionNode);
      //      curveNodeValidationNode.getSubNodes().add(curveDefinitionNode);
      //      curveDefinitionNode.getSubNodes().add(curveNodeValidationNode);
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

  private boolean holidayExists(final ExternalId customHolidayId) {
    final HolidaySearchRequest searchReq = new HolidaySearchRequest(HolidayType.CUSTOM, Collections.singletonList(customHolidayId));
    final HolidaySearchResult search = _holidayMaster.search(searchReq);
    return search.getSingleHoliday() != null;
  }

  // CSOFF
  private void validateCurveNode(final CurveNode curveNode, final ValidationNode validationNode) {
    final CurveNodeIdMapper curveNodeIdMapper = getCurveNodeIdMapper(curveNode.getCurveNodeIdMapperName());
    if (curveNodeIdMapper == null) {
      createInvalidCurveNodeValidationNode(curveNode.getResolvedMaturity(), curveNode.getClass(), validationNode, "CurveNodeIdMapper " + curveNode.getCurveNodeIdMapperName() + " is missing");
    } else {
      curveNode.accept(new CurveNodeVisitor<Void>() {
        @Override
        public Void visitBillNode(final BillNode node) {
          ExternalId billNodeId;
          try {
            billNodeId = curveNodeIdMapper.getBondNodeId(_curveDate, node.getMaturityTenor());
          } catch (final OpenGammaRuntimeException ogre) {
            billNodeId = null;
          }
          ValidationNode billNodeValidationNode;
          if (billNodeId != null) {
            try {
              final Security bond = _securitySource.getSingle(billNodeId.toBundle());
              if (bond == null) {
                billNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BillNode.class, validationNode, "Bill " + billNodeId + " not found in security master");
              } else {
                billNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BillNode.class, validationNode, null);
              }
            } catch (final IllegalArgumentException iae) {
              billNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BillNode.class, validationNode,
                  "Bill " + billNodeId + " error thrown by security master when resolving, probably invalid ID format");
            }
          } else {
            billNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BillNode.class, validationNode, "Entry missing for this tenor in CurveNodeIdMapper");
          }
          return null;
        }

        @Override
        public Void visitBondNode(final BondNode node) {
          ExternalId bondNodeId;
          try {
            bondNodeId = curveNodeIdMapper.getBondNodeId(_curveDate, node.getMaturityTenor());
          } catch (final OpenGammaRuntimeException ogre) {
            bondNodeId = null;
          }
          ValidationNode bondNodeValidationNode;
          if (bondNodeId != null) {
            try {
              final Security bond = _securitySource.getSingle(bondNodeId.toBundle());
              if (bond == null) {
                bondNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BondNode.class, validationNode, "Bond " + bondNodeId + " not found in security master");
              } else {
                bondNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BondNode.class, validationNode, null);
              }
            } catch (final IllegalArgumentException iae) {
              bondNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BondNode.class, validationNode,
                  "Bond " + bondNodeId + " error thrown by security master when resolving, probably invalid ID format");
            }
          } else {
            bondNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BondNode.class, validationNode, "Entry missing for this tenor in CurveNodeIdMapper");
          }
          return null;
        }

        @Override
        public Void visitCalendarSwapNode(final CalendarSwapNode node) {
          ExternalId calendarNodeId;
          try {
            calendarNodeId = curveNodeIdMapper.getCalendarSwapNodeId(_curveDate, node.getStartTenor(), node.getCalendarDateStartNumber(), node.getCalendarDateEndNumber());
          } catch (final OpenGammaRuntimeException ogre) {
            calendarNodeId = null;
          }
          ValidationNode calendarSwapValidationNode;
          if (calendarNodeId == null) {
            if (!holidayExists(node.getCalendarId())) {
              calendarSwapValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), CalendarSwapNode.class, validationNode,
                  "Customer holiday calendar " + node.getCalendarId() + " (and not generated by Id mapper) does not exist");
            } else {
              calendarSwapValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), CalendarSwapNode.class, validationNode, null);
            }
          } else {
            if (!holidayExists(calendarNodeId)) {
              calendarSwapValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), CalendarSwapNode.class, validationNode,
                  "Customer holiday calendar " + calendarNodeId + " (generated by Id mapper) does not exist");
            } else {
              calendarSwapValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), CalendarSwapNode.class, validationNode, null);
            }
          }
          final ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getSwapConvention().getValue());
          if (conventionExists(node.getSwapConvention())) {
            final ManageableConvention convention = getConvention(node.getSwapConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find swap convention using ID " + node.getSwapConvention());
            validationNode.setError(true);
          }
          calendarSwapValidationNode.getSubNodes().add(validationNode);
          return null;
        }

        @Override
        public Void visitCashNode(final CashNode node) {
          ExternalId cashNodeId;
          try {
            cashNodeId = curveNodeIdMapper.getCashNodeId(_curveDate, node.getMaturityTenor());
          } catch (final OpenGammaRuntimeException ogre) {
            cashNodeId = null;
          }
          ValidationNode cashNodeValidationNode;
          if (cashNodeId == null) {
            cashNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), CashNode.class, validationNode, "No curve node id mapper entry for " + node.getResolvedMaturity());
          } else {
            cashNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), CashNode.class, validationNode, null);
          }
          final ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getConvention().getValue());
          if (conventionExists(node.getConvention())) {
            final ManageableConvention convention = getConvention(node.getConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find convention using ID " + node.getConvention());
            validationNode.setError(true);
          }
          cashNodeValidationNode.getSubNodes().add(validationNode);
          return null;
        }

        @Override
        public Void visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node) {
          ExternalId continuouslyCompoundedRateNodeId;
          try {
            continuouslyCompoundedRateNodeId = curveNodeIdMapper.getContinuouslyCompoundedRateNodeId(_curveDate, node.getTenor());
          } catch (final OpenGammaRuntimeException ogre) {
            continuouslyCompoundedRateNodeId = null;
          }
          if (continuouslyCompoundedRateNodeId == null) {
            // the node get's attached to parent inside this call.
            createInvalidCurveNodeValidationNode(node.getResolvedMaturity(), CashNode.class, validationNode, "No curve node id mapper entry for " + node.getResolvedMaturity());
          } else {
            // the node get's attached to parent inside this call.
            createInvalidCurveNodeValidationNode(node.getResolvedMaturity(), CashNode.class, validationNode, null);
          }
          return null;
        }

        @Override
        public Void visitCreditSpreadNode(final CreditSpreadNode node) {
          ExternalId creditSpreadNodeId;
          try {
            creditSpreadNodeId = curveNodeIdMapper.getCreditSpreadNodeId(_curveDate, node.getTenor());
          } catch (final OpenGammaRuntimeException ogre) {
            creditSpreadNodeId = null;
          }
          if (creditSpreadNodeId == null) {
            // the node get's attached to parent inside this call.
            createInvalidCurveNodeValidationNode(node.getResolvedMaturity(), CreditSpreadNode.class, validationNode, "No curve node id mapper entry for " + node.getResolvedMaturity());
          } else {
            // the node get's attached to parent inside this call.
            createInvalidCurveNodeValidationNode(node.getResolvedMaturity(), CreditSpreadNode.class, validationNode, null);
          }
          return null;
        }

        @Override
        public Void visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode node) {
          ExternalId nodeId;
          try {
            nodeId = curveNodeIdMapper.getDeliverableSwapFutureNodeId(_curveDate, node.getStartTenor(), node.getFutureTenor(), node.getFutureNumber());
          } catch (final OpenGammaRuntimeException ogre) {
            nodeId = null;
          }
          ValidationNode dsValidationNode;
          if (nodeId == null) {
            dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), DeliverableSwapFutureNode.class, validationNode, "No curve node id mapper entry for " + node.getStartTenor());
          } else {
            dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), DeliverableSwapFutureNode.class, validationNode, null);
          }
          ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getFutureConvention().getValue());
          if (conventionExists(node.getFutureConvention())) {
            final ManageableConvention convention = getConvention(node.getFutureConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find future convention using ID " + node.getFutureConvention());
            validationNode.setError(true);
          }
          dsValidationNode.getSubNodes().add(validationNode);
          validationNode = new ValidationNode();
          validationNode.setName(node.getSwapConvention().getValue());
          if (conventionExists(node.getSwapConvention())) {
            final ManageableConvention convention = getConvention(node.getSwapConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find swap convention using ID " + node.getSwapConvention());
            validationNode.setError(true);
          }
          dsValidationNode.getSubNodes().add(validationNode);
          return null;
        }

        @Override
        public Void visitDiscountFactorNode(final DiscountFactorNode node) {
          ExternalId nodeId;
          try {
            nodeId = curveNodeIdMapper.getDiscountFactorNodeId(_curveDate, node.getTenor());
          } catch (final OpenGammaRuntimeException ogre) {
            nodeId = null;
          }
          if (nodeId == null) {
            // the node get's attached to parent inside this call.
            createInvalidCurveNodeValidationNode(node.getTenor(), DiscountFactorNode.class, validationNode, "No curve node id mapper entry for " + node.getTenor());
          } else {
            // the node get's attached to parent inside this call.
            createInvalidCurveNodeValidationNode(node.getTenor(), DiscountFactorNode.class, validationNode, null);
          }
          return null;
        }

        @Override
        public Void visitFRANode(final FRANode node) {
          ExternalId nodeId;
          try {
            nodeId = curveNodeIdMapper.getFRANodeId(_curveDate, node.getFixingEnd());
          } catch (final OpenGammaRuntimeException ogre) {
            nodeId = null;
          }
          ValidationNode dsValidationNode;
          if (nodeId == null) {
            dsValidationNode = createInvalidCurveNodeValidationNode(node.getFixingEnd(), FRANode.class, validationNode, "No curve node id mapper entry for " + node.getFixingEnd());
          } else {
            dsValidationNode = createInvalidCurveNodeValidationNode(node.getFixingEnd(), FRANode.class, validationNode, null);
          }
          final ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getConvention().getValue());
          if (conventionExists(node.getConvention())) {
            final ManageableConvention convention = getConvention(node.getConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find convention using ID " + node.getConvention());
            validationNode.setError(true);
          }
          dsValidationNode.getSubNodes().add(validationNode);
          return null;
        }

        @Override
        public Void visitFXForwardNode(final FXForwardNode node) {
          final Map<Tenor, CurveInstrumentProvider> fxForwardNodeIds = curveNodeIdMapper.getFXForwardNodeIds();
          final CurveInstrumentProvider curveInstrumentProvider = fxForwardNodeIds.get(node.getMaturityTenor());
          ExternalId nodeId;
          if (curveInstrumentProvider instanceof StaticCurvePointsInstrumentProvider) {
            nodeId = curveInstrumentProvider.getInstrument(_curveDate, node.getMaturityTenor());
          } else {
            nodeId = curveNodeIdMapper.getFXForwardNodeId(_curveDate, node.getMaturityTenor());
          }
          ValidationNode fxValidationNode;
          if (nodeId == null) {
            fxValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), FXForwardNode.class, validationNode, "No curve node id mapper entry for " + node.getMaturityTenor());
          } else {
            fxValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), FXForwardNode.class, validationNode, null);
          }
          final ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getFxForwardConvention().getValue());
          if (conventionExists(node.getFxForwardConvention())) {
            final ManageableConvention convention = getConvention(node.getFxForwardConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find convention using ID " + node.getFxForwardConvention());
            validationNode.setError(true);
          }
          fxValidationNode.getSubNodes().add(validationNode);
          return null;
        }

        @Override
        public Void visitRollDateFRANode(final RollDateFRANode node) {
          ExternalId nodeId;
          try {
            nodeId = curveNodeIdMapper.getIMMFRANodeId(_curveDate, node.getStartTenor(), node.getRollDateStartNumber(), node.getRollDateEndNumber());
          } catch (final OpenGammaRuntimeException ogre) {
            nodeId = null;
          }
          ValidationNode dsValidationNode;
          if (nodeId == null) {
            dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RollDateFRANode.class, validationNode, "No curve node id mapper entry for " + node.getStartTenor());
          } else {
            dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RollDateFRANode.class, validationNode, null);
          }
          final ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getRollDateFRAConvention().getValue());
          if (conventionExists(node.getRollDateFRAConvention())) {
            final ManageableConvention convention = getConvention(node.getRollDateFRAConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find convention using ID " + node.getRollDateFRAConvention());
            validationNode.setError(true);
          }
          dsValidationNode.getSubNodes().add(validationNode);
          return null;
        }

        @Override
        public Void visitRollDateSwapNode(final RollDateSwapNode node) {
          ExternalId nodeId;
          try {
            nodeId = curveNodeIdMapper.getIMMSwapNodeId(_curveDate, node.getStartTenor(), node.getRollDateStartNumber(), node.getRollDateEndNumber());
          } catch (final OpenGammaRuntimeException ogre) {
            nodeId = null;
          }
          ValidationNode dsValidationNode;
          if (nodeId == null) {
            dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RollDateSwapNode.class, validationNode, "No curve node id mapper entry for " + node.getStartTenor());
          } else {
            dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RollDateSwapNode.class, validationNode, null);
          }
          final ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getRollDateSwapConvention().getValue());
          if (conventionExists(node.getRollDateSwapConvention())) {
            final ManageableConvention convention = getConvention(node.getRollDateSwapConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find convention using ID " + node.getRollDateSwapConvention());
            validationNode.setError(true);
          }
          dsValidationNode.getSubNodes().add(validationNode);
          return null;
        }

        @Override
        public Void visitRateFutureNode(final RateFutureNode node) {
          ExternalId nodeId;
          try {
            nodeId = curveNodeIdMapper.getRateFutureNodeId(_curveDate, node.getStartTenor(), node.getFutureTenor(), node.getFutureNumber());
          } catch (final OpenGammaRuntimeException ogre) {
            nodeId = null;
          }
          ValidationNode dsValidationNode;
          if (nodeId == null) {
            dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RateFutureNode.class, validationNode, "No curve node id mapper entry for " + node.getStartTenor());
          } else {
            dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RateFutureNode.class, validationNode, null);
          }
          final ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getFutureConvention().getValue());
          if (conventionExists(node.getFutureConvention())) {
            final ManageableConvention convention = getConvention(node.getFutureConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find convention using ID " + node.getFutureConvention());
            validationNode.setError(true);
          }
          dsValidationNode.getSubNodes().add(validationNode);
          return null;
        }

        @Override
        public Void visitSwapNode(final SwapNode node) {
          ExternalId nodeId;
          try {
            nodeId = curveNodeIdMapper.getSwapNodeId(_curveDate, node.getMaturityTenor());
          } catch (final OpenGammaRuntimeException ogre) {
            nodeId = null;
          }
          ValidationNode sValidationNode;
          if (nodeId == null) {
            sValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), SwapNode.class, validationNode, "No curve node id mapper entry for " + node.getMaturityTenor());
          } else {
            sValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), SwapNode.class, validationNode, null);
          }
          ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getPayLegConvention().getValue());
          if (conventionExists(node.getPayLegConvention())) {
            final ManageableConvention convention = getConvention(node.getPayLegConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find pay leg convention using ID " + node.getPayLegConvention());
            validationNode.setError(true);
          }
          sValidationNode.getSubNodes().add(validationNode);
          validationNode = new ValidationNode();
          validationNode.setName(node.getReceiveLegConvention().getValue());
          if (conventionExists(node.getReceiveLegConvention())) {
            final ManageableConvention convention = getConvention(node.getReceiveLegConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find receive leg convention using ID " + node.getReceiveLegConvention());
            validationNode.setError(true);
          }
          sValidationNode.getSubNodes().add(validationNode);
          return null;
        }

        @Override
        public Void visitThreeLegBasisSwapNode(final ThreeLegBasisSwapNode node) {
          ExternalId nodeId;
          try {
            nodeId = curveNodeIdMapper.getThreeLegBasisSwapNodeId(_curveDate, node.getMaturityTenor());
          } catch (final OpenGammaRuntimeException ogre) {
            nodeId = null;
          }
          ValidationNode sValidationNode;
          if (nodeId == null) {
            sValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), ThreeLegBasisSwapNode.class, validationNode, "No curve node id mapper entry for " + node.getMaturityTenor());
          } else {
            sValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), ThreeLegBasisSwapNode.class, validationNode, null);
          }
          ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getPayLegConvention().getValue());
          if (conventionExists(node.getPayLegConvention())) {
            final ManageableConvention convention = getConvention(node.getPayLegConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find pay leg convention using ID " + node.getPayLegConvention());
            validationNode.setError(true);
          }
          sValidationNode.getSubNodes().add(validationNode);
          validationNode = new ValidationNode();
          validationNode.setName(node.getReceiveLegConvention().getValue());
          if (conventionExists(node.getReceiveLegConvention())) {
            final ManageableConvention convention = getConvention(node.getReceiveLegConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find receive leg convention using ID " + node.getReceiveLegConvention());
            validationNode.setError(true);
          }
          sValidationNode.getSubNodes().add(validationNode);
          validationNode = new ValidationNode();
          validationNode.setName(node.getSpreadLegConvention().getValue());
          if (conventionExists(node.getSpreadLegConvention())) {
            final ManageableConvention convention = getConvention(node.getSpreadLegConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find spread leg convention using ID " + node.getReceiveLegConvention());
            validationNode.setError(true);
          }
          sValidationNode.getSubNodes().add(validationNode);
          return null;
        }

        @Override
        public Void visitZeroCouponInflationNode(final ZeroCouponInflationNode node) {
          ExternalId nodeId;
          try {
            nodeId = curveNodeIdMapper.getSwapNodeId(_curveDate, node.getTenor());
          } catch (final OpenGammaRuntimeException ogre) {
            nodeId = null;
          }
          ValidationNode sValidationNode;
          if (nodeId == null) {
            sValidationNode = createInvalidCurveNodeValidationNode(node.getTenor(), ZeroCouponInflationNode.class, validationNode, "No curve node id mapper entry for " + node.getTenor());
          } else {
            sValidationNode = createInvalidCurveNodeValidationNode(node.getTenor(), ZeroCouponInflationNode.class, validationNode, null);
          }
          ValidationNode validationNode = new ValidationNode();
          validationNode.setName(node.getFixedLegConvention().getValue());
          if (conventionExists(node.getFixedLegConvention())) {
            final ManageableConvention convention = getConvention(node.getFixedLegConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find fixed leg convention using ID " + node.getFixedLegConvention());
            validationNode.setError(true);
          }
          sValidationNode.getSubNodes().add(validationNode);
          validationNode = new ValidationNode();
          validationNode.setName(node.getInflationLegConvention().getValue());
          if (conventionExists(node.getInflationLegConvention())) {
            final ManageableConvention convention = getConvention(node.getInflationLegConvention());
            validationNode.setType(convention.getClass());
          } else {
            validationNode.setType(Convention.class);
            validationNode.getErrors().add("Can't find inflation leg convention using ID " + node.getInflationLegConvention());
            validationNode.setError(true);
          }
          sValidationNode.getSubNodes().add(validationNode);
          return null;
        }
      });
    }
  }
  // CSON
  private boolean conventionExists(final ExternalId externalId) {
    return getConvention(externalId) != null;
  }

  private ManageableConvention getConvention(final ExternalId externalId) {
    try {
      return (ManageableConvention) _conventionSource.getSingle(externalId);
    } catch (final DataNotFoundException dnfe) {
      return null;
    } catch (final Exception e) {
      return null;
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
