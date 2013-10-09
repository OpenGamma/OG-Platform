/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRate.Type;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleImpl;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.time.Tenor;

/**
 * Fudge builder for a {@link ConventionBundle}.
 */
@GenericFudgeBuilderFor(ConventionBundle.class)
public class ConventionBundleFudgeBuilder implements FudgeBuilder<ConventionBundle> {

  // This is here so that changes to the interface will break this builder.

  private static final class SimpleConventionBundle implements ConventionBundle {

    private UniqueId _uniqueId;
    private ExternalIdBundle _identifiers;
    private String _name;
    private DayCount _dayCount;
    private BusinessDayConvention _businessDayConvention;
    private ExternalId _region;
    private Frequency _frequency;
    private int _settlementDays;
    private Tenor _cutoffTenor;
    private int _shortSettlementDays;
    private int _longSettlementDays;
    private Double _futureYearFraction;
    private DayCount _swapFixedLegDayCount;
    private BusinessDayConvention _swapFixedLegBusinessDayConvention;
    private Frequency _swapFixedLegPaymentFrequency;
    private Frequency _swapFixedLegCompoundingFrequency;
    private InterestRate.Type _swapFixedLegCompoundingType;
    private Integer _swapFixedLegSettlementDays;
    private ExternalId _swapFixedLegRegion;
    private DayCount _swapFloatingLegDayCount;
    private BusinessDayConvention _swapFloatingLegBusinessDayConvention;
    private Frequency _swapFloatingLegPaymentFrequency;
    private Frequency _swapFloatingLegCompoundingFrequency;
    private InterestRate.Type _swapFloatingLegCompoundingType;
    private Integer _swapFloatingLegSettlementDays;
    private ExternalId _swapFloatingLegInitialRate;
    private ExternalId _swapFloatingLegRegion;
    private ExternalIdBundle _capmRiskFreeRate;
    private ExternalIdBundle _capmMarket;
    private DayCount _basisSwapPayFloatingLegDayCount;
    private BusinessDayConvention _basisSwapPayFloatingLegBusinessDayConvention;
    private Frequency _basisSwapPayFloatingLegFrequency;
    private Integer _basisSwapPayFloatingLegSettlementDays;
    private ExternalId _basisSwapPayFloatingLegInitialRate;
    private ExternalId _basisSwapPayFloatingLegRegion;
    private DayCount _basisSwapReceiveFloatingLegDayCount;
    private BusinessDayConvention _basisSwapReceiveFloatingLegBusinessDayConvention;
    private Frequency _basisSwapReceiveFloatingLegFrequency;
    private Integer _basisSwapReceiveFloatingLegSettlementDays;
    private ExternalId _basisSwapReceiveFloatingLegInitialRate;
    private ExternalId _basisSwapReceiveFloatingLegRegion;
    private Integer _overnightIndexSwapPublicationLag;
    private Boolean _eomConvention;
    private Boolean _calculateScheduleFromMaturity;
    private int _exDividendDays;
    private YieldConvention _yieldConvention;
    private boolean _rollToSettlement;
    private Period _period;
    private boolean _cashSettled;
    private String _optionExpiryCalculator;

    @Override
    public UniqueId getUniqueId() {
      return _uniqueId;
    }

    @Override
    public ExternalIdBundle getIdentifiers() {
      return _identifiers;
    }

    @Override
    public String getName() {
      return _name;
    }

    @Override
    public DayCount getDayCount() {
      return _dayCount;
    }

    @Override
    public BusinessDayConvention getBusinessDayConvention() {
      return _businessDayConvention;
    }

    @Override
    public ExternalId getRegion() {
      return _region;
    }

    @Override
    public Frequency getFrequency() {
      return _frequency;
    }

    @Override
    public Integer getSettlementDays() {
      return _settlementDays;
    }

    public Tenor getCutoffTenor() {
      return _cutoffTenor;
    }

    public int getShortSettlementDays() {
      return _shortSettlementDays;
    }

    public int getLongSettlementDays() {
      return _longSettlementDays;
    }

    @Override
    public Integer getBondSettlementDays(final ZonedDateTime bondSettlementDate, final ZonedDateTime bondMaturityDate) {
      if (_cutoffTenor != null) {
        if (bondSettlementDate.plus(_cutoffTenor.getPeriod()).isBefore(bondMaturityDate)) {
          return _shortSettlementDays;
        }
        return _longSettlementDays;
      }
      return _settlementDays;
    }

    @Override
    public Double getFutureYearFraction() {
      return _futureYearFraction;
    }

    @Override
    public DayCount getSwapFixedLegDayCount() {
      return _swapFixedLegDayCount;
    }

    @Override
    public BusinessDayConvention getSwapFixedLegBusinessDayConvention() {
      return _swapFixedLegBusinessDayConvention;
    }

    @Override
    public Frequency getSwapFixedLegFrequency() {
      return _swapFixedLegPaymentFrequency;
    }

    @Override
    public Integer getSwapFixedLegSettlementDays() {
      return _swapFixedLegSettlementDays;
    }

    @Override
    public ExternalId getSwapFixedLegRegion() {
      return _swapFixedLegRegion;
    }

    @Override
    public DayCount getSwapFloatingLegDayCount() {
      return _swapFloatingLegDayCount;
    }

    @Override
    public BusinessDayConvention getSwapFloatingLegBusinessDayConvention() {
      return _swapFloatingLegBusinessDayConvention;
    }

    @Override
    public Frequency getSwapFloatingLegFrequency() {
      return _swapFloatingLegPaymentFrequency;
    }

    @Override
    public Integer getSwapFloatingLegSettlementDays() {
      return _swapFloatingLegSettlementDays;
    }

    @Override
    public ExternalId getSwapFloatingLegInitialRate() {
      return _swapFloatingLegInitialRate;
    }

    @Override
    public ExternalId getSwapFloatingLegRegion() {
      return _swapFloatingLegRegion;
    }

    @Override
    public ExternalIdBundle getCAPMRiskFreeRate() {
      return _capmRiskFreeRate;
    }

    @Override
    public ExternalIdBundle getCAPMMarket() {
      return _capmMarket;
    }

    @Override
    public DayCount getBasisSwapPayFloatingLegDayCount() {
      return _basisSwapPayFloatingLegDayCount;
    }

    @Override
    public BusinessDayConvention getBasisSwapPayFloatingLegBusinessDayConvention() {
      return _basisSwapPayFloatingLegBusinessDayConvention;
    }

    @Override
    public Frequency getBasisSwapPayFloatingLegFrequency() {
      return _basisSwapPayFloatingLegFrequency;
    }

    @Override
    public Integer getBasisSwapPayFloatingLegSettlementDays() {
      return _basisSwapPayFloatingLegSettlementDays;
    }

    @Override
    public ExternalId getBasisSwapPayFloatingLegInitialRate() {
      return _basisSwapPayFloatingLegInitialRate;
    }

    @Override
    public ExternalId getBasisSwapPayFloatingLegRegion() {
      return _basisSwapPayFloatingLegRegion;
    }

    @Override
    public DayCount getBasisSwapReceiveFloatingLegDayCount() {
      return _basisSwapReceiveFloatingLegDayCount;
    }

    @Override
    public BusinessDayConvention getBasisSwapReceiveFloatingLegBusinessDayConvention() {
      return _basisSwapReceiveFloatingLegBusinessDayConvention;
    }

    @Override
    public Frequency getBasisSwapReceiveFloatingLegFrequency() {
      return _basisSwapReceiveFloatingLegFrequency;
    }

    @Override
    public Integer getBasisSwapReceiveFloatingLegSettlementDays() {
      return _basisSwapReceiveFloatingLegSettlementDays;
    }

    @Override
    public ExternalId getBasisSwapReceiveFloatingLegInitialRate() {
      return _basisSwapReceiveFloatingLegInitialRate;
    }

    @Override
    public ExternalId getBasisSwapReceiveFloatingLegRegion() {
      return _basisSwapReceiveFloatingLegRegion;
    }

    @Override
    public Integer getOvernightIndexSwapPublicationLag() {
      return _overnightIndexSwapPublicationLag;
    }

    @Override
    public Boolean isEOMConvention() {
      return _eomConvention;
    }

    @Override
    public Boolean calculateScheduleFromMaturity() {
      return _calculateScheduleFromMaturity;
    }

    @Override
    public int getExDividendDays() {
      return _exDividendDays;
    }

    @Override
    public YieldConvention getYieldConvention() {
      return _yieldConvention;
    }

    @Override
    public boolean rollToSettlement() {
      return _rollToSettlement;
    }

    @Override
    public Period getPeriod() {
      return _period;
    }

    @Override
    public boolean isCashSettled() {
      return _cashSettled;
    }

    @Override
    public String getOptionExpiryCalculator() {
      return _optionExpiryCalculator;
    }

    @Override
    public Frequency getSwapFixedLegCompoundingFrequency() {
      return _swapFixedLegCompoundingFrequency;
    }

    @Override
    public Frequency getSwapFloatingLegCompoundingFrequency() {
      return _swapFloatingLegCompoundingFrequency;
    }

    @Override
    public Type getSwapFixedLegCompoundingType() {
      return _swapFixedLegCompoundingType;
    }

    @Override
    public Type getSwapFloatingLegCompoundingType() {
      return _swapFloatingLegCompoundingType;
    }

  }

  private void addToMessage(final FudgeSerializer serializer, final MutableFudgeMsg msg, final ConventionBundleImpl obj) {
    serializer.addToMessage(msg, "cutoffTenor", null, obj.getCutoffTenor());
    serializer.addToMessage(msg, "shortSettlementDays", null, obj.getShortSettlementDays());
    serializer.addToMessage(msg, "longSettlementDays", null, obj.getLongSettlementDays());
  }

  private void addToMessage(final FudgeSerializer serializer, final MutableFudgeMsg msg, final SimpleConventionBundle obj) {
    serializer.addToMessage(msg, "cutoffTenor", null, obj.getCutoffTenor());
    serializer.addToMessage(msg, "shortSettlementDays", null, obj.getShortSettlementDays());
    serializer.addToMessage(msg, "longSettlementDays", null, obj.getLongSettlementDays());
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ConventionBundle obj) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, "uniqueId", null, obj.getUniqueId());
    serializer.addToMessage(msg, "identifiers", null, obj.getIdentifiers());
    serializer.addToMessage(msg, "name", null, obj.getName());
    serializer.addToMessageWithClassHeaders(msg, "dayCount", null, obj.getDayCount(), DayCount.class);
    serializer.addToMessageWithClassHeaders(msg, "businessDayConvention", null, obj.getBusinessDayConvention(), BusinessDayConvention.class);
    serializer.addToMessage(msg, "region", null, obj.getRegion());
    serializer.addToMessageWithClassHeaders(msg, "frequency", null, obj.getFrequency(), Frequency.class);
    serializer.addToMessage(msg, "settlementDays", null, obj.getSettlementDays());
    if (obj instanceof ConventionBundleImpl) {
      addToMessage(serializer, msg, (ConventionBundleImpl) obj);
    } else if (obj instanceof SimpleConventionBundle) {
      addToMessage(serializer, msg, (SimpleConventionBundle) obj);
    } else {
      // Can't handle "getBondSettlementDays" from an arbitrary object
      throw new IllegalArgumentException("Convention bundle of type " + obj.getClass() + " can't be Fudge encoded");
    }
    serializer.addToMessage(msg, "futureYearFraction", null, obj.getFutureYearFraction());
    serializer.addToMessageWithClassHeaders(msg, "swapFixedLegDayCount", null, obj.getSwapFixedLegDayCount(), DayCount.class);
    serializer.addToMessageWithClassHeaders(msg, "swapFixedLegBusinessDayConvention", null, obj.getSwapFixedLegBusinessDayConvention(), BusinessDayConvention.class);
    serializer.addToMessageWithClassHeaders(msg, "swapFixedLegFrequency", null, obj.getSwapFixedLegFrequency(), Frequency.class);
    serializer.addToMessageWithClassHeaders(msg, "swapFixedLegCompoundingFrequency", null, obj.getSwapFixedLegCompoundingFrequency(), Frequency.class);
    if (obj.getSwapFixedLegCompoundingType() != null) {
      msg.add("swapFixedLegCompoundingType", obj.getSwapFixedLegCompoundingType().name());
    }
    serializer.addToMessage(msg, "swapFixedLegSettlementDays", null, obj.getSwapFixedLegSettlementDays());
    serializer.addToMessage(msg, "swapFixedLegRegion", null, obj.getSwapFixedLegRegion());
    serializer.addToMessageWithClassHeaders(msg, "swapFloatingLegDayCount", null, obj.getSwapFloatingLegDayCount(), DayCount.class);
    serializer.addToMessageWithClassHeaders(msg, "swapFloatingLegBusinessDayConvention", null, obj.getSwapFloatingLegBusinessDayConvention(), BusinessDayConvention.class);
    serializer.addToMessageWithClassHeaders(msg, "swapFloatingLegFrequency", null, obj.getSwapFloatingLegFrequency(), Frequency.class);
    serializer.addToMessageWithClassHeaders(msg, "swapFloatingLegCompoundingFrequency", null, obj.getSwapFloatingLegCompoundingFrequency(), Frequency.class);
    if (obj.getSwapFloatingLegCompoundingType() != null) {
      msg.add("swapFloatingLegCompoundingType", obj.getSwapFloatingLegCompoundingType().name());
    }
    serializer.addToMessage(msg, "swapFloatingLegSettlementDays", null, obj.getSwapFloatingLegSettlementDays());
    serializer.addToMessage(msg, "swapFloatingLegInitialRate", null, obj.getSwapFloatingLegInitialRate());
    serializer.addToMessage(msg, "swapFloatingLegRegion", null, obj.getSwapFloatingLegRegion());
    serializer.addToMessage(msg, "capmRiskFreeRate", null, obj.getCAPMRiskFreeRate());
    serializer.addToMessage(msg, "capmMarket", null, obj.getCAPMMarket());
    serializer.addToMessageWithClassHeaders(msg, "basisSwapPayFloatingLegDayCount", null, obj.getBasisSwapPayFloatingLegDayCount(), DayCount.class);
    serializer.addToMessageWithClassHeaders(msg, "basisSwapPayFloatingLegBusinessDayConvention", null, obj.getBasisSwapPayFloatingLegBusinessDayConvention(), BusinessDayConvention.class);
    serializer.addToMessageWithClassHeaders(msg, "basisSwapPayFloatingLegFrequency", null, obj.getBasisSwapPayFloatingLegFrequency(), Frequency.class);
    serializer.addToMessage(msg, "basisSwapPayFloatingLegSettlementDays", null, obj.getBasisSwapPayFloatingLegSettlementDays());
    serializer.addToMessage(msg, "basisSwapPayFloatingLegInitialRate", null, obj.getBasisSwapPayFloatingLegInitialRate());
    serializer.addToMessage(msg, "basisSwapPayFloatingLegRegion", null, obj.getBasisSwapPayFloatingLegRegion());
    serializer.addToMessageWithClassHeaders(msg, "basisSwapReceiveFloatingLegDayCount", null, obj.getBasisSwapReceiveFloatingLegDayCount(), DayCount.class);
    serializer.addToMessageWithClassHeaders(msg, "basisSwapReceiveFloatingLegBusinessDayConvention", null, obj.getBasisSwapReceiveFloatingLegBusinessDayConvention(), BusinessDayConvention.class);
    serializer.addToMessageWithClassHeaders(msg, "basisSwapReceiveFloatingLegFrequency", null, obj.getBasisSwapReceiveFloatingLegFrequency(), Frequency.class);
    serializer.addToMessage(msg, "basisSwapReceiveFloatingLegSettlementDays", null, obj.getBasisSwapReceiveFloatingLegSettlementDays());
    serializer.addToMessage(msg, "basisSwapReceiveFloatingLegInitialRate", null, obj.getBasisSwapReceiveFloatingLegInitialRate());
    serializer.addToMessage(msg, "basisSwapReceiveFloatingLegRegion", null, obj.getBasisSwapReceiveFloatingLegRegion());
    serializer.addToMessage(msg, "overnightIndexSwapPublicationLag", null, obj.getOvernightIndexSwapPublicationLag());
    serializer.addToMessage(msg, "eomConvention", null, obj.isEOMConvention());
    serializer.addToMessage(msg, "calculateScheduleFromMaturity", null, obj.calculateScheduleFromMaturity());
    serializer.addToMessage(msg, "exDividendDays", null, obj.getExDividendDays());
    serializer.addToMessageWithClassHeaders(msg, "yieldConvention", null, obj.getYieldConvention(), YieldConvention.class);
    serializer.addToMessage(msg, "rollToSettlement", null, obj.rollToSettlement());
    serializer.addToMessage(msg, "period", null, obj.getPeriod());
    serializer.addToMessage(msg, "cashSettled", null, obj.isCashSettled());
    serializer.addToMessage(msg, "optionExpiryCalculator", null, obj.getOptionExpiryCalculator());
    return msg;
  }

  @Override
  public ConventionBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final SimpleConventionBundle obj = new SimpleConventionBundle();
    FudgeField field;
    //CSOFF
    if ((field = msg.getByName("uniqueId")) != null) {
      obj._uniqueId = deserializer.fieldValueToObject(UniqueId.class, field);
    }
    if ((field = msg.getByName("identifiers")) != null) {
      obj._identifiers = deserializer.fieldValueToObject(ExternalIdBundle.class, field);
    }
    if ((field = msg.getByName("name")) != null) {
      obj._name = deserializer.fieldValueToObject(String.class, field);
    }
    if ((field = msg.getByName("dayCount")) != null) {
      obj._dayCount = deserializer.fieldValueToObject(DayCount.class, field);
    }
    if ((field = msg.getByName("businessDayConvention")) != null) {
      obj._businessDayConvention = deserializer.fieldValueToObject(BusinessDayConvention.class, field);
    }
    if ((field = msg.getByName("region")) != null) {
      obj._region = deserializer.fieldValueToObject(ExternalId.class, field);
    }
    if ((field = msg.getByName("frequency")) != null) {
      obj._frequency = deserializer.fieldValueToObject(Frequency.class, field);
    }
    if ((field = msg.getByName("settlementDays")) != null) {
      obj._settlementDays = deserializer.fieldValueToObject(Integer.class, field);
    }
    if ((field = msg.getByName("cutoffTenor")) != null) {
      obj._cutoffTenor = deserializer.fieldValueToObject(Tenor.class, field);
    }
    if ((field = msg.getByName("shortSettlementDays")) != null) {
      obj._shortSettlementDays = deserializer.fieldValueToObject(Integer.class, field);
    }
    if ((field = msg.getByName("longSettlementDays")) != null) {
      obj._longSettlementDays = deserializer.fieldValueToObject(Integer.class, field);
    }
    if ((field = msg.getByName("futureYearFraction")) != null) {
      obj._futureYearFraction = deserializer.fieldValueToObject(Double.class, field);
    }
    if ((field = msg.getByName("swapFixedLegDayCount")) != null) {
      obj._swapFixedLegDayCount = deserializer.fieldValueToObject(DayCount.class, field);
    }
    if ((field = msg.getByName("swapFixedLegBusinessDayConvention")) != null) {
      obj._swapFixedLegBusinessDayConvention = deserializer.fieldValueToObject(BusinessDayConvention.class, field);
    }
    if ((field = msg.getByName("swapFixedLegFrequency")) != null) {
      obj._swapFixedLegPaymentFrequency = deserializer.fieldValueToObject(Frequency.class, field);
    }
    if ((field = msg.getByName("swapFixedLegCompoundingFrequency")) != null) {
      obj._swapFixedLegCompoundingFrequency = deserializer.fieldValueToObject(Frequency.class, field);
    }
    if ((field = msg.getByName("swapFixedLegCompoundingType")) != null) {
      obj._swapFixedLegCompoundingType = InterestRate.Type.valueOf((String) field.getValue());
    }
    if ((field = msg.getByName("swapFixedLegSettlementDays")) != null) {
      obj._swapFixedLegSettlementDays = deserializer.fieldValueToObject(Integer.class, field);
    }
    if ((field = msg.getByName("swapFixedLegRegion")) != null) {
      obj._swapFixedLegRegion = deserializer.fieldValueToObject(ExternalId.class, field);
    }
    if ((field = msg.getByName("swapFloatingLegDayCount")) != null) {
      obj._swapFloatingLegDayCount = deserializer.fieldValueToObject(DayCount.class, field);
    }
    if ((field = msg.getByName("swapFloatingLegBusinessDayConvention")) != null) {
      obj._swapFloatingLegBusinessDayConvention = deserializer.fieldValueToObject(BusinessDayConvention.class, field);
    }
    if ((field = msg.getByName("swapFloatingLegFrequency")) != null) {
      obj._swapFloatingLegPaymentFrequency = deserializer.fieldValueToObject(Frequency.class, field);
    }
    if ((field = msg.getByName("swapFloatingLegCompoundingFrequency")) != null) {
      obj._swapFloatingLegCompoundingFrequency = deserializer.fieldValueToObject(Frequency.class, field);
    }
    if ((field = msg.getByName("swapFloatingLegCompoundingType")) != null) {
      obj._swapFloatingLegCompoundingType = InterestRate.Type.valueOf((String) field.getValue());
    }
    if ((field = msg.getByName("swapFloatingLegSettlementDays")) != null) {
      obj._swapFloatingLegSettlementDays = deserializer.fieldValueToObject(Integer.class, field);
    }
    if ((field = msg.getByName("swapFloatingLegInitialRate")) != null) {
      obj._swapFloatingLegInitialRate = deserializer.fieldValueToObject(ExternalId.class, field);
    }
    if ((field = msg.getByName("swapFloatingLegRegion")) != null) {
      obj._swapFloatingLegRegion = deserializer.fieldValueToObject(ExternalId.class, field);
    }
    if ((field = msg.getByName("capmRiskFreeRate")) != null) {
      obj._capmRiskFreeRate = deserializer.fieldValueToObject(ExternalIdBundle.class, field);
    }
    if ((field = msg.getByName("capmMarket")) != null) {
      obj._capmMarket = deserializer.fieldValueToObject(ExternalIdBundle.class, field);
    }
    if ((field = msg.getByName("basisSwapPayFloatingLegDayCount")) != null) {
      obj._basisSwapPayFloatingLegDayCount = deserializer.fieldValueToObject(DayCount.class, field);
    }
    if ((field = msg.getByName("basisSwapPayFloatingLegBusinessDayConvention")) != null) {
      obj._basisSwapPayFloatingLegBusinessDayConvention = deserializer.fieldValueToObject(BusinessDayConvention.class, field);
    }
    if ((field = msg.getByName("basisSwapPayFloatingLegFrequency")) != null) {
      obj._basisSwapPayFloatingLegFrequency = deserializer.fieldValueToObject(Frequency.class, field);
    }
    if ((field = msg.getByName("basisSwapPayFloatingLegSettlementDays")) != null) {
      obj._basisSwapPayFloatingLegSettlementDays = deserializer.fieldValueToObject(Integer.class, field);
    }
    if ((field = msg.getByName("basisSwapPayFloatingLegInitialRate")) != null) {
      obj._basisSwapPayFloatingLegInitialRate = deserializer.fieldValueToObject(ExternalId.class, field);
    }
    if ((field = msg.getByName("basisSwapPayFloatingLegRegion")) != null) {
      obj._basisSwapPayFloatingLegRegion = deserializer.fieldValueToObject(ExternalId.class, field);
    }
    if ((field = msg.getByName("basisSwapReceiveFloatingLegDayCount")) != null) {
      obj._basisSwapReceiveFloatingLegDayCount = deserializer.fieldValueToObject(DayCount.class, field);
    }
    if ((field = msg.getByName("basisSwapReceiveFloatingLegBusinessDayConvention")) != null) {
      obj._basisSwapReceiveFloatingLegBusinessDayConvention = deserializer.fieldValueToObject(BusinessDayConvention.class, field);
    }
    if ((field = msg.getByName("basisSwapReceiveFloatingLegFrequency")) != null) {
      obj._basisSwapReceiveFloatingLegFrequency = deserializer.fieldValueToObject(Frequency.class, field);
    }
    if ((field = msg.getByName("basisSwapReceiveFloatingLegSettlementDays")) != null) {
      obj._basisSwapReceiveFloatingLegSettlementDays = deserializer.fieldValueToObject(Integer.class, field);
    }
    if ((field = msg.getByName("basisSwapReceiveFloatingLegInitialRate")) != null) {
      obj._basisSwapReceiveFloatingLegInitialRate = deserializer.fieldValueToObject(ExternalId.class, field);
    }
    if ((field = msg.getByName("basisSwapReceiveFloatingLegRegion")) != null) {
      obj._basisSwapReceiveFloatingLegRegion = deserializer.fieldValueToObject(ExternalId.class, field);
    }
    if ((field = msg.getByName("overnightIndexSwapPublicationLag")) != null) {
      obj._overnightIndexSwapPublicationLag = deserializer.fieldValueToObject(Integer.class, field);
    }
    if ((field = msg.getByName("eomConvention")) != null) {
      obj._eomConvention = deserializer.fieldValueToObject(Boolean.class, field);
    }
    if ((field = msg.getByName("calculateScheduleFromMaturity")) != null) {
      obj._calculateScheduleFromMaturity = deserializer.fieldValueToObject(Boolean.class, field);
    }
    if ((field = msg.getByName("exDividendDays")) != null) {
      obj._exDividendDays = deserializer.fieldValueToObject(Integer.class, field);
    }
    if ((field = msg.getByName("yieldConvention")) != null) {
      obj._yieldConvention = deserializer.fieldValueToObject(YieldConvention.class, field);
    }
    if ((field = msg.getByName("rollToSettlement")) != null) {
      obj._rollToSettlement = deserializer.fieldValueToObject(Boolean.class, field);
    }
    if ((field = msg.getByName("period")) != null) {
      obj._period = deserializer.fieldValueToObject(Period.class, field);
    }
    if ((field = msg.getByName("cashSettled")) != null) {
      obj._cashSettled = deserializer.fieldValueToObject(Boolean.class, field);
    }
    if ((field = msg.getByName("optionExpiryCalculator")) != null) {
      obj._optionExpiryCalculator = deserializer.fieldValueToObject(String.class, field);
    }
    return obj;
  }
}
