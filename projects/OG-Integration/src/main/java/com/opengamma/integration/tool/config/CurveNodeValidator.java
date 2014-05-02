/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.DateSet;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
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
import com.opengamma.financial.analytics.ircurve.strips.PeriodicallyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.time.Tenor;

/**
 * Visitor for validating curve nodes.
 */
public final class CurveNodeValidator implements CurveNodeVisitor<Void> {
  /**
   * 
   */
  private final ValidationNode _validationNode;
  private final CurveNodeIdMapper _curveNodeIdMapper;
  private final ConfigValidationUtils _configValidationUtils;
  private final SecuritySource _securitySource;
  private final ConfigSource _configSource;
  private final LocalDate _curveDate;
 
  /**
   * @param curveDate
   * @param configValidationUtils
   * @param securitySource
   * @param validationNode
   * @param curveNodeIdMapper
   */
  public CurveNodeValidator(LocalDate curveDate, ConfigValidationUtils configValidationUtils, SecuritySource securitySource, ValidationNode validationNode, CurveNodeIdMapper curveNodeIdMapper, ConfigSource configSource) {
    _curveDate = curveDate;
    _configValidationUtils = configValidationUtils;
    _securitySource = securitySource;
    _validationNode = validationNode;
    _curveNodeIdMapper = curveNodeIdMapper;
    _configSource = configSource;
  }
  

  ValidationNode createInvalidCurveNodeValidationNode(Tenor tenor, Class<? extends CurveNode> curveNodeType, ValidationNode parentNode, String message) {
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(tenor.toFormattedString());
    validationNode.setType(curveNodeType);
    if (message != null) {
      validationNode.getErrors().add(message);
      validationNode.setError(true);
    }
    parentNode.getSubNodes().add(validationNode);
    return validationNode;
  }

  @Override
  public Void visitBondNode(BondNode node) {
    ExternalId bondNodeId;
    try {
      bondNodeId = _curveNodeIdMapper.getBondNodeId(_curveDate, node.getMaturityTenor());
    } catch (OpenGammaRuntimeException ogre) {
      bondNodeId = null;
    }
    ValidationNode bondNodeValidationNode;
    if (bondNodeId != null) {
      try {
        Security bond = _securitySource.getSingle(bondNodeId.toBundle());  
        if (bond == null) {
          bondNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BondNode.class, _validationNode, "Bond " + bondNodeId + " not found in security master");
        } else {
          bondNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BondNode.class, _validationNode, null);
        }
      } catch (IllegalArgumentException iae) {
        bondNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BondNode.class, _validationNode, 
            "Bond " + bondNodeId + " error thrown by security master when resolving, probably invalid ID format");
      }
    } else {
      bondNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BondNode.class, _validationNode, "Entry missing for this tenor in CurveNodeIdMapper");
    }
    return null;
  }

  @Override
  public Void visitCalendarSwapNode(CalendarSwapNode node) {
    ExternalId calendarNodeId;
    try {
      calendarNodeId = _curveNodeIdMapper.getCalendarSwapNodeId(_curveDate, node.getStartTenor(), node.getStartDateNumber(), node.getEndDateNumber());
    } catch (OpenGammaRuntimeException ogre) {
      calendarNodeId = null;
    }
    ValidationNode calendarSwapValidationNode;
    if (calendarNodeId == null) {
      calendarSwapValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), CalendarSwapNode.class, _validationNode, "Entry missing for this tenor in CurveNodeIdMapper");
    } else {
      calendarSwapValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), CalendarSwapNode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getSwapConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getSwapConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getSwapConvention());
      validationNode.setType(convention.getClass());
    } else {
      validationNode.setType(Convention.class);
      validationNode.getErrors().add("Can't find swap convention using ID " + node.getSwapConvention());
      validationNode.setError(true);
    }
    ValidationNode validationNode2 = new ValidationNode();
    validationNode2.setName(node.getSwapConvention().getValue());
    if (_configSource.get(DateSet.class, node.getDateSetName(), VersionCorrection.LATEST) != null) {
      validationNode2.setType(DateSet.class);
    } else {
      validationNode2.setType(DateSet.class);
      validationNode2.getErrors().add("Can't find calendar (DateSet) named " + node.getDateSetName());
      validationNode2.setError(true);
    }
    calendarSwapValidationNode.getSubNodes().add(validationNode2);
    return null;
  }

  @Override
  public Void visitCashNode(CashNode node) {
    ExternalId cashNodeId;
    try {
      cashNodeId = _curveNodeIdMapper.getCashNodeId(_curveDate, node.getMaturityTenor());
    } catch (OpenGammaRuntimeException ogre) {
      cashNodeId = null;
    }
    ValidationNode cashNodeValidationNode;
    if (cashNodeId == null) {
      cashNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), CashNode.class, _validationNode, "No curve node id mapper entry for " + node.getResolvedMaturity());
    } else {
      cashNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), CashNode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getConvention());
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
  public Void visitContinuouslyCompoundedRateNode(ContinuouslyCompoundedRateNode node) {
    ExternalId continuouslyCompoundedRateNodeId;
    try {
      continuouslyCompoundedRateNodeId = _curveNodeIdMapper.getContinuouslyCompoundedRateNodeId(_curveDate, node.getTenor());
    } catch (OpenGammaRuntimeException ogre) {
      continuouslyCompoundedRateNodeId = null;
    }
    if (continuouslyCompoundedRateNodeId == null) {
      // the node get's attached to parent inside this call.
      createInvalidCurveNodeValidationNode(node.getResolvedMaturity(), CashNode.class, _validationNode, "No curve node id mapper entry for " + node.getResolvedMaturity());
    } else {
      // the node get's attached to parent inside this call.
      createInvalidCurveNodeValidationNode(node.getResolvedMaturity(), CashNode.class, _validationNode, null);
    }
    return null;
  }

  @Override
  public Void visitPeriodicallyCompoundedRateNode(PeriodicallyCompoundedRateNode node) {
    ExternalId id;
    try {
      id = _curveNodeIdMapper.getPeriodicallyCompoundedRateNodeId(_curveDate, node.getTenor());
    } catch (OpenGammaRuntimeException ogre) {
      id = null;
    }
    if (id == null) {
      // the node get's attached to parent inside this call.
      createInvalidCurveNodeValidationNode(node.getResolvedMaturity(), CashNode.class, _validationNode, "No curve node id mapper entry for " + node.getResolvedMaturity());
    } else {
      // the node get's attached to parent inside this call.
      createInvalidCurveNodeValidationNode(node.getResolvedMaturity(), CashNode.class, _validationNode, null);
    }
    return null;
  }

  @Override
  public Void visitCreditSpreadNode(CreditSpreadNode node) {
    ExternalId creditSpreadNodeId;
    try {
      creditSpreadNodeId = _curveNodeIdMapper.getCreditSpreadNodeId(_curveDate, node.getTenor());
    } catch (OpenGammaRuntimeException ogre) {
      creditSpreadNodeId = null;
    }
    if (creditSpreadNodeId == null) {
      // the node get's attached to parent inside this call.
      createInvalidCurveNodeValidationNode(node.getResolvedMaturity(), CreditSpreadNode.class, _validationNode, "No curve node id mapper entry for " + node.getResolvedMaturity());
    } else {
      // the node get's attached to parent inside this call.
      createInvalidCurveNodeValidationNode(node.getResolvedMaturity(), CreditSpreadNode.class, _validationNode, null);
    }
    return null;
  }

  @Override
  public Void visitDeliverableSwapFutureNode(DeliverableSwapFutureNode node) {
    ExternalId nodeId;
    try {
      nodeId = _curveNodeIdMapper.getDeliverableSwapFutureNodeId(_curveDate, node.getStartTenor(), node.getFutureTenor(), node.getFutureNumber());
    } catch (OpenGammaRuntimeException ogre) {
      nodeId = null;
    }
    ValidationNode dsValidationNode;
    if (nodeId == null) {
      dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), DeliverableSwapFutureNode.class, _validationNode, "No curve node id mapper entry for " + node.getStartTenor());
    } else {
      dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), DeliverableSwapFutureNode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getFutureConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getFutureConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getFutureConvention());
      validationNode.setType(convention.getClass());
    } else {
      validationNode.setType(Convention.class);
      validationNode.getErrors().add("Can't find future convention using ID " + node.getFutureConvention());
      validationNode.setError(true);
    }
    dsValidationNode.getSubNodes().add(validationNode);
    validationNode = new ValidationNode();
    validationNode.setName(node.getSwapConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getSwapConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getSwapConvention());
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
  public Void visitDiscountFactorNode(DiscountFactorNode node) {
    ExternalId nodeId;
    try {
      nodeId = _curveNodeIdMapper.getDiscountFactorNodeId(_curveDate, node.getTenor());
    } catch (OpenGammaRuntimeException ogre) {
      nodeId = null;
    }
    if (nodeId == null) {
      // the node get's attached to parent inside this call.
      createInvalidCurveNodeValidationNode(node.getTenor(), DiscountFactorNode.class, _validationNode, "No curve node id mapper entry for " + node.getTenor());
    } else {
      // the node get's attached to parent inside this call.
      createInvalidCurveNodeValidationNode(node.getTenor(), DiscountFactorNode.class, _validationNode, null);
    }
    return null;
  }

  @Override
  public Void visitFRANode(FRANode node) {
    ExternalId nodeId;
    try {
      nodeId = _curveNodeIdMapper.getFRANodeId(_curveDate, node.getFixingEnd());
    } catch (OpenGammaRuntimeException ogre) {
      nodeId = null;
    }
    ValidationNode dsValidationNode;
    if (nodeId == null) {
      dsValidationNode = createInvalidCurveNodeValidationNode(node.getFixingEnd(), FRANode.class, _validationNode, "No curve node id mapper entry for " + node.getFixingEnd());
    } else {
      dsValidationNode = createInvalidCurveNodeValidationNode(node.getFixingEnd(), FRANode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getConvention());
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
  public Void visitFXForwardNode(FXForwardNode node) {
    Map<Tenor, CurveInstrumentProvider> fxForwardNodeIds = _curveNodeIdMapper.getFXForwardNodeIds();
    CurveInstrumentProvider curveInstrumentProvider = fxForwardNodeIds.get(node.getMaturityTenor());
    ExternalId nodeId;
    if (curveInstrumentProvider instanceof StaticCurvePointsInstrumentProvider) {
      nodeId = curveInstrumentProvider.getInstrument(_curveDate, node.getMaturityTenor());
    } else {
      nodeId = _curveNodeIdMapper.getFXForwardNodeId(_curveDate, node.getMaturityTenor());
    }
    ValidationNode fxValidationNode;
    if (nodeId == null) {
      fxValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), FXForwardNode.class, _validationNode, "No curve node id mapper entry for " + node.getMaturityTenor());
    } else {
      fxValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), FXForwardNode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getFxForwardConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getFxForwardConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getFxForwardConvention());
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
  public Void visitRollDateFRANode(RollDateFRANode node) {
    ExternalId nodeId;
    try {
      nodeId = _curveNodeIdMapper.getIMMFRANodeId(_curveDate, node.getStartTenor(), node.getRollDateStartNumber(), node.getRollDateEndNumber());
    } catch (OpenGammaRuntimeException ogre) {
      nodeId = null;
    }
    ValidationNode dsValidationNode;
    if (nodeId == null) {
      dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RollDateFRANode.class, _validationNode, "No curve node id mapper entry for " + node.getStartTenor());
    } else {
      dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RollDateFRANode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getRollDateFRAConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getRollDateFRAConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getRollDateFRAConvention());
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
  public Void visitRollDateSwapNode(RollDateSwapNode node) {
    ExternalId nodeId;
    try {
      nodeId = _curveNodeIdMapper.getIMMSwapNodeId(_curveDate, node.getStartTenor(), node.getRollDateStartNumber(), node.getRollDateEndNumber());
    } catch (OpenGammaRuntimeException ogre) {
      nodeId = null;
    }
    ValidationNode dsValidationNode;
    if (nodeId == null) {
      dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RollDateSwapNode.class, _validationNode, "No curve node id mapper entry for " + node.getStartTenor());
    } else {
      dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RollDateSwapNode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getRollDateSwapConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getRollDateSwapConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getRollDateSwapConvention());
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
  public Void visitRateFutureNode(RateFutureNode node) {
    ExternalId nodeId;
    try {
      nodeId = _curveNodeIdMapper.getRateFutureNodeId(_curveDate, node.getStartTenor(), node.getFutureTenor(), node.getFutureNumber());
    } catch (OpenGammaRuntimeException ogre) {
      nodeId = null;
    }
    ValidationNode dsValidationNode;
    if (nodeId == null) {
      dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RateFutureNode.class, _validationNode, "No curve node id mapper entry for " + node.getStartTenor());
    } else {
      dsValidationNode = createInvalidCurveNodeValidationNode(node.getStartTenor(), RateFutureNode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getFutureConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getFutureConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getFutureConvention());
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
  public Void visitSwapNode(SwapNode node) {
    ExternalId nodeId;
    try {
      nodeId = _curveNodeIdMapper.getSwapNodeId(_curveDate, node.getMaturityTenor());
    } catch (OpenGammaRuntimeException ogre) {
      nodeId = null;
    }
    ValidationNode sValidationNode;
    if (nodeId == null) {
      sValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), SwapNode.class, _validationNode, "No curve node id mapper entry for " + node.getMaturityTenor());
    } else {
      sValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), SwapNode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getPayLegConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getPayLegConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getPayLegConvention());
      validationNode.setType(convention.getClass());
    } else {
      validationNode.setType(Convention.class);
      validationNode.getErrors().add("Can't find pay leg convention using ID " + node.getPayLegConvention());
      validationNode.setError(true);
    }
    sValidationNode.getSubNodes().add(validationNode);
    validationNode = new ValidationNode();
    validationNode.setName(node.getReceiveLegConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getReceiveLegConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getReceiveLegConvention());
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
  public Void visitThreeLegBasisSwapNode(ThreeLegBasisSwapNode node) {
    ExternalId nodeId;
    try {
      nodeId = _curveNodeIdMapper.getThreeLegBasisSwapNodeId(_curveDate, node.getMaturityTenor());
    } catch (OpenGammaRuntimeException ogre) {
      nodeId = null;
    }
    ValidationNode sValidationNode;
    if (nodeId == null) {
      sValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), ThreeLegBasisSwapNode.class, _validationNode, "No curve node id mapper entry for " + node.getMaturityTenor());
    } else {
      sValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), ThreeLegBasisSwapNode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getPayLegConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getPayLegConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getPayLegConvention());
      validationNode.setType(convention.getClass());
    } else {
      validationNode.setType(Convention.class);
      validationNode.getErrors().add("Can't find pay leg convention using ID " + node.getPayLegConvention());
      validationNode.setError(true);
    }
    sValidationNode.getSubNodes().add(validationNode);
    validationNode = new ValidationNode();
    validationNode.setName(node.getReceiveLegConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getReceiveLegConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getReceiveLegConvention());
      validationNode.setType(convention.getClass());
    } else {
      validationNode.setType(Convention.class);
      validationNode.getErrors().add("Can't find receive leg convention using ID " + node.getReceiveLegConvention());
      validationNode.setError(true);
    }
    sValidationNode.getSubNodes().add(validationNode);
    validationNode = new ValidationNode();
    validationNode.setName(node.getSpreadLegConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getSpreadLegConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getSpreadLegConvention());
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
  public Void visitZeroCouponInflationNode(ZeroCouponInflationNode node) {
    ExternalId nodeId;
    try {
      nodeId = _curveNodeIdMapper.getSwapNodeId(_curveDate, node.getTenor());
    } catch (OpenGammaRuntimeException ogre) {
      nodeId = null;
    }
    ValidationNode sValidationNode;
    if (nodeId == null) {
      sValidationNode = createInvalidCurveNodeValidationNode(node.getTenor(), ZeroCouponInflationNode.class, _validationNode, "No curve node id mapper entry for " + node.getTenor());
    } else {
      sValidationNode = createInvalidCurveNodeValidationNode(node.getTenor(), ZeroCouponInflationNode.class, _validationNode, null);
    }
    ValidationNode validationNode = new ValidationNode();
    validationNode.setName(node.getFixedLegConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getFixedLegConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getFixedLegConvention());
      validationNode.setType(convention.getClass());
    } else {
      validationNode.setType(Convention.class);
      validationNode.getErrors().add("Can't find fixed leg convention using ID " + node.getFixedLegConvention());
      validationNode.setError(true);
    }
    sValidationNode.getSubNodes().add(validationNode);
    validationNode = new ValidationNode();
    validationNode.setName(node.getInflationLegConvention().getValue());
    if (_configValidationUtils.conventionExists(node.getInflationLegConvention())) {
      ManageableConvention convention = _configValidationUtils.getConvention(node.getInflationLegConvention());
      validationNode.setType(convention.getClass());
    } else {
      validationNode.setType(Convention.class);
      validationNode.getErrors().add("Can't find inflation leg convention using ID " + node.getInflationLegConvention());
      validationNode.setError(true);
    }
    sValidationNode.getSubNodes().add(validationNode);
    return null;
  }


  @Override
  public Void visitBillNode(BillNode node) {
    ExternalId billNodeId;
    try {
      billNodeId = _curveNodeIdMapper.getBillNodeId(_curveDate, node.getMaturityTenor());
    } catch (OpenGammaRuntimeException ogre) {
      billNodeId = null;
    }
    ValidationNode billNodeValidationNode;
    if (billNodeId != null) {
      try {
        Security bill = _securitySource.getSingle(billNodeId.toBundle());  
        if (bill == null) {
          billNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BillNode.class, _validationNode, "Bill " + billNodeId + " not found in security master");
        } else {
          billNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BillNode.class, _validationNode, null);
        }
      } catch (IllegalArgumentException iae) {
        billNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BillNode.class, _validationNode, 
            "Bond " + billNodeId + " error thrown by security master when resolving, probably invalid ID format");
      }
    } else {
      billNodeValidationNode = createInvalidCurveNodeValidationNode(node.getMaturityTenor(), BillNode.class, _validationNode, "Entry missing for this tenor in CurveNodeIdMapper");
    }
    return null;
  }


}