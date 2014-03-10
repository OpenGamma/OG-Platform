package com.opengamma.integration.tool.config;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.convention.impl.MockConvention;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.EquityConvention;
import com.opengamma.financial.convention.ExchangeTradedFutureAndOptionConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.FixedInterestRateSwapLegConvention;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.FloatingInterestRateSwapLegConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.ONCompoundedLegRollDateConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;
import com.opengamma.financial.security.index.Index;
import com.opengamma.id.ExternalId;
import com.opengamma.master.convention.ManageableConvention;

/**
 * Class to recursively validate conventions.
 */
public class ConventionValidator extends ConventionFollower<ValidationNode, Void> {

  private ConfigValidationUtils _utils;

  public ConventionValidator(ConfigValidationUtils utils) {
    _utils = utils;
  }
  
  @SuppressWarnings("unchecked")
  private static final Set<Class<? extends FinancialConvention>> s_legTypes = Sets.newHashSet(
      CMSLegConvention.class, 
      CompoundingIborLegConvention.class, 
      FixedInterestRateSwapLegConvention.class,
      FloatingInterestRateSwapLegConvention.class,
      InflationLegConvention.class,
      OISLegConvention.class,
      SwapFixedLegConvention.class,
      VanillaIborLegConvention.class);
  
  @SuppressWarnings("unchecked")
  private static final Set<Class<? extends FinancialConvention>> s_legRollDateConventionTypes = Sets.newHashSet(
      FixedLegRollDateConvention.class, 
      ONCompoundedLegRollDateConvention.class, 
      VanillaIborLegRollDateConvention.class);
  
  @SuppressWarnings("unchecked")
  private static final Set<Class<? extends FinancialConvention>> s_rollDateConventionTypes = Sets.newHashSet(
      RollDateFRAConvention.class, 
      RollDateSwapConvention.class);
  
  @SuppressWarnings("unchecked")
  private static final Set<Class<? extends FinancialConvention>> s_indexConventionTypes = Sets.newHashSet(
      OvernightIndexConvention.class, 
      IborIndexConvention.class,
      PriceIndexConvention.class,
      SwapIndexConvention.class);
  
  private ValidationNode checkConventionType(ExternalId conventionId, Class<? extends FinancialConvention> clazz, ValidationNode parentNode, String name) {
    ValidationNode node = new ValidationNode();
    node.setName(name); // poor proxy for name
    node.setType(clazz);
    if (_utils.conventionExists(conventionId, clazz)) {
      ManageableConvention convention = _utils.getConvention(conventionId);
      followConvention(convention, node);
    } else {
      if (_utils.conventionExists(conventionId)) {
        ManageableConvention convention = _utils.getConvention(conventionId);
        node.getErrors().add("Covention " + convention + " was expected to be of type " + clazz + " but was " + convention.getClass());
      } else {
        node.getErrors().add("Covention with id " + conventionId + " was not found");
      }
      node.setError(true);
    }
    parentNode.getSubNodes().add(node);
    return node;
  }
  
  private ValidationNode checkConventionLeg(ExternalId conventionId, ValidationNode parentNode, String name) {
    return checkConventionTypes(conventionId, s_legTypes, parentNode, name);
  }
  
  private ValidationNode checkConventionLegRollDate(ExternalId conventionId, ValidationNode parentNode, String name) {
    return checkConventionTypes(conventionId, s_legRollDateConventionTypes, parentNode, name);
  }
  
  private ValidationNode checkConventionRollDate(ExternalId conventionId, ValidationNode parentNode, String name) {
    return checkConventionTypes(conventionId, s_rollDateConventionTypes, parentNode, name);
  }
  
  private ValidationNode checkConventionIndex(ExternalId conventionId, ValidationNode parentNode, String name) {
    return checkConventionTypes(conventionId, s_rollDateConventionTypes, parentNode, name);
  }
  
  private ValidationNode checkConventionTypes(ExternalId conventionId, Set<Class<? extends FinancialConvention>> clazzes, ValidationNode parentNode, String name) {
    ValidationNode node = new ValidationNode();
    node.setName(name); // poor proxy for name
    if (_utils.conventionExists(conventionId, clazzes)) {
      ManageableConvention convention = _utils.getConvention(conventionId);
      node.setType(convention.getClass());
      followConvention(convention, node);
    } else {
      if (_utils.conventionExists(conventionId)) {
        ManageableConvention convention = _utils.getConvention(conventionId);
        node.setType(convention.getClass());
        node.getErrors().add("Covention " + convention + " was expected to be one of types " + clazzes + " but was " + convention.getClass());
      } else {
        node.setType(Convention.class);
        node.getErrors().add("Covention with id " + conventionId + " was not found");
      }
      node.setError(true);
    }
    parentNode.getSubNodes().add(node);
    return node;
  }
  
  private ValidationNode checkCalendar(ExternalId regionId, ValidationNode parentNode, String name) {
    ValidationNode node = new ValidationNode();
    node.setName(name); // poor proxy for name
    if (_utils.holidayExists(regionId)) {
      node.setType(Holiday.class);
    } else {
      node.setType(Holiday.class);
      node.getErrors().add("Calendar for  " + regionId + " did not exist");
      node.setError(true);
    }
    parentNode.getSubNodes().add(node);
    return node;
  }
  
  private ValidationNode checkIndex(ExternalId indexId, ValidationNode parentNode, String name) {
    ValidationNode node = new ValidationNode();
    node.setName(name); // poor proxy for name
    if (_utils.indexExists(indexId)) {
      Index index = _utils.getIndex(indexId);
      node.setType(index.getClass());
    } else {
      node.setType(Index.class);
      node.getErrors().add("Index " + indexId + " did not exist");
      node.setError(true);
    }
    parentNode.getSubNodes().add(node);
    return node;
  }
  
  @Override
  public Void followVanillaIborLegRollDateConvention(VanillaIborLegRollDateConvention convention, ValidationNode parent) {
    checkIndex(convention.getIborIndexConvention(), parent, "iborIndexConvention");
    //checkConventionType(convention.getIborIndexConvention(), IborIndexConvention.class, parent, "iborIndexConvention");
    return null;
  }

  @Override
  public Void followVanillaIborLegConvention(VanillaIborLegConvention convention, ValidationNode parent) {
    checkIndex(convention.getIborIndexConvention(), parent, "iborIndexConvention");
    //checkConventionType(convention.getIborIndexConvention(), IborIndexConvention.class, parent, "iborIndexConvention");
    return null;
  }

  @Override
  public Void followSwapIndexConvention(SwapIndexConvention convention, ValidationNode parent) {
    checkConventionType(convention.getSwapConvention(), SwapConvention.class, parent, "swapIndexConvention");
    return null;
  }

  @Override
  public Void followSwapFixedLegConvention(SwapFixedLegConvention convention, ValidationNode parent) {
    checkCalendar(convention.getRegionCalendar(), parent, "regionCalendar");
    return null;
  }

  @Override
  public Void followSwapConvention(SwapConvention convention, ValidationNode parent) {
    checkConventionLeg(convention.getPayLegConvention(), parent, "payLegConvention");
    checkConventionLeg(convention.getReceiveLegConvention(), parent, "receiveLegConvention");
    return null;
  }

  @Override
  public Void followRollDateSwapConvention(RollDateSwapConvention convention, ValidationNode parent) {
    checkConventionLeg(convention.getPayLegConvention(), parent, "payLegConvention");
    checkConventionLeg(convention.getReceiveLegConvention(), parent, "receiveLegConvention");
    checkConventionType(convention.getRollDateConvention(), RollDateSwapConvention.class, parent, "rollDateConvention");
    return null;
  }

  @Override
  public Void followRollDateFRAConvention(RollDateFRAConvention convention, ValidationNode parent) {
    checkConventionIndex(convention.getIndexConvention(), parent, "indexConvention");
    checkConventionRollDate(convention.getRollDateConvention(), parent, "rollDateConvention");
    return null;
  }

  @Override
  public Void followPriceIndexConvention(PriceIndexConvention convention, ValidationNode parent) {
    checkCalendar(convention.getRegion(), parent, "region");
    checkIndex(convention.getPriceIndexId(), parent, "priceIndexId");
    return null;
  }

  @Override
  public Void followOvernightIndexConvention(OvernightIndexConvention convention, ValidationNode parent) {
    checkCalendar(convention.getRegionCalendar(), parent, "regionCalendar");
    return null;
  }

  @Override
  public Void followONCompoundedLegRollRateConvention(ONCompoundedLegRollDateConvention convention, ValidationNode parent) {
    checkIndex(convention.getOvernightIndexConvention(), parent, "overnightIndexConvention");
    //checkConventionType(convention.getOvernightIndexConvention(), OvernightIndexConvention.class, parent, "overnightIndexConvention");
    return null;
  }

  @Override
  public Void followONArithmeticAverageLegConvention(ONArithmeticAverageLegConvention convention, ValidationNode parent) {
    checkIndex(convention.getOvernightIndexConvention(), parent, "overnightIndexConvention");
    //checkConventionType(convention.getOvernightIndexConvention(), OvernightIndexConvention.class, parent, "overnightIndexConvention");
    return null;
  }

  @Override
  public Void followOISLegConvention(OISLegConvention convention, ValidationNode parent) {
    checkIndex(convention.getOvernightIndexConvention(), parent, "overnightIndexConvention");
//    checkConventionType(convention.getOvernightIndexConvention(), OvernightIndexConvention.class, parent, "overnightIndexConvention");
    return null;
  }

  @Override
  public Void followMockConvention(MockConvention convention, ValidationNode parent) {
    ValidationNode node = new ValidationNode();
    node.setName("Mock");
    node.setError(true);
    node.getErrors().add("Mock should never be present");
    return null;
  }
  
  private void checkCalendars(Set<ExternalId> calendars, String name, ValidationNode parent) {
    if (calendars.size() == 0) {
      return;
    }
    ValidationNode calcCalNode = new ValidationNode();
    calcCalNode.setType(Set.class);
    calcCalNode.setName(name);
    int i = 0;
    for (ExternalId calendarId : calendars) {
      checkCalendar(calendarId, calcCalNode, name + "[" + i + "]");
      i++;
    }
    parent.getSubNodes().add(calcCalNode);
  }

  @Override
  public Void followFloatingInterestRateSwapLegConvention(FloatingInterestRateSwapLegConvention convention, ValidationNode parent) {
    checkCalendars(convention.getCalculationCalendars(), "calculationCalendars", parent);
    checkCalendars(convention.getFixingCalendars(), "fixingCalendars", parent);
    checkCalendars(convention.getMaturityCalendars(), "maturityCalendars", parent);
    checkCalendars(convention.getPaymentCalendars(), "paymentCalendars", parent);
    checkCalendars(convention.getResetCalendars(), "resetCalendars", parent);
    return null;
  }

  @Override
  public Void followFixedInterestRateSwapLegConvention(FixedInterestRateSwapLegConvention convention, ValidationNode parent) {
    checkCalendars(convention.getCalculationCalendars(), "calculationCalendars", parent);
    checkCalendars(convention.getMaturityCalendars(), "maturityCalendars", parent);
    checkCalendars(convention.getPaymentCalendars(), "paymentCalendars", parent);
    return null;
  }

  @Override
  public Void followInflationLegConvention(InflationLegConvention convention, ValidationNode parent) {
    checkIndex(convention.getPriceIndexConvention(), parent, "priceIndexConvention");
    //checkConventionType(convention.getPriceIndexConvention(), PriceIndexConvention.class, parent, "priceIndexConvention");
    return null;
  }

  @Override
  public Void followIborIndexConvention(IborIndexConvention convention, ValidationNode parent) {
    checkCalendar(convention.getFixingCalendar(), parent, "fixingCalendar");
    checkCalendar(convention.getRegionCalendar(), parent, "regionCalendar");
    return null;
  }

  @Override
  public Void followFXSpotConvention(FXSpotConvention convention, ValidationNode parent) {
    checkCalendar(convention.getSettlementRegion(), parent, "settlementRegion");
    return null;
  }

  @Override
  public Void followFXForwardAndSwapConvention(FXForwardAndSwapConvention convention, ValidationNode parent) {
    checkConventionType(convention.getSpotConvention(), FXSpotConvention.class, parent, "spotConvention");
    checkCalendar(convention.getSettlementRegion(), parent, "settlementRegion");
    return null;
  }

  @Override
  public Void followFixedLegRollRateConvention(FixedLegRollDateConvention convention, ValidationNode parent) {
    checkCalendar(convention.getRegionCalendar(), parent, "regionCalendar");
    return null;
  }

  @Override
  public Void followOtherExchangeTradedFutureAndOptionConvention(ExchangeTradedFutureAndOptionConvention convention, ValidationNode parent) {
    ValidationNode node = new ValidationNode();
    node.setName("Other ExchangeTradedFutureAndOptionConvention");
    node.setError(true);
    node.getErrors().add("Add support for convention type " + convention.getClass() + " to the tool");
    return null;
  }

  @Override
  public Void followInterestRateFutureConvention(InterestRateFutureConvention convention, ValidationNode parent) {
    checkCalendar(convention.getExchangeCalendar(), parent, "exchangeCalendar");
    // TODO: deal with 'Expiry convention' nonsense (refers to some factory rather than a data source).
    return null;
  }

  @Override
  public Void followFederalFundsFutureConvention(FederalFundsFutureConvention convention, ValidationNode parent) {
    checkIndex(convention.getIndexConvention(), parent, "indexConvention");
    //checkConventionType(convention.getIndexConvention(), OvernightIndexConvention.class, parent, "indexConvention");
    checkCalendar(convention.getExchangeCalendar(), parent, "exchangeCalendar");
    // TODO: deal with 'Expiry convention' nonsense (refers to some factory rather than a data source).
    return null;
  }

  @Override
  public Void followDeliverablePriceQuotedSwapFutureConvention(DeliverablePriceQuotedSwapFutureConvention convention, ValidationNode parent) {
    checkCalendar(convention.getExchangeCalendar(), parent, "exchangeCalendar");
    checkConventionType(convention.getSwapConvention(), SwapConvention.class, parent, "swapConvention");
    return null;
  }

  @Override
  public Void followEquityConvention(EquityConvention convention, ValidationNode parent) {
    return null;
  }

  @Override
  public Void followDepositorConvention(DepositConvention convention, ValidationNode parent) {
    checkCalendar(convention.getRegionCalendar(), parent, "regionCalendar");
    return null;
  }

  @Override
  public Void followCompoundingIborLegConvention(CompoundingIborLegConvention convention, ValidationNode parent) {
    checkIndex(convention.getIborIndexConvention(), parent, "iborIndexConvention");
    //checkConventionType(convention.getIborIndexConvention(), IborIndexConvention.class, parent, "iborIndexConvention");
    return null;
  }

  @Override
  public Void followCMSLegConvention(CMSLegConvention convention, ValidationNode parent) {
    checkIndex(convention.getSwapIndexConvention(), parent, "swapIndexConvention");
    //checkConventionType(convention.getSwapIndexConvention(), SwapIndexConvention.class, parent, "swapIndexConvention");
    return null;
  }

}
