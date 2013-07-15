/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.analytics.financial.greeks.PDEResultCollection;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.analytics.model.volatility.local.deprecated.ForexLocalVolatilityPDEPresentValueResultCollection;

/**
 * @deprecated Builds results from deprecated functions
 */
/* package */@Deprecated
final class PDEResultsFudgeBuilder {

  private PDEResultsFudgeBuilder() {
  }

  @FudgeBuilderFor(PDEGrid1D.class)
  public static final class PDEGrid1DFudgeBuilder extends AbstractFudgeBuilder<PDEGrid1D> {
    private static final String TIME_NODES_FIELD = "timeNodesField";
    private static final String SPACE_NODES_FIELD = "spaceNodesField";

    @Override
    public PDEGrid1D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] timeNodes = deserializer.fieldValueToObject(double[].class, message.getByName(TIME_NODES_FIELD));
      final double[] spaceNodes = deserializer.fieldValueToObject(double[].class, message.getByName(SPACE_NODES_FIELD));
      return new PDEGrid1D(timeNodes, spaceNodes);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final PDEGrid1D object) {
      serializer.addToMessage(message, TIME_NODES_FIELD, null, object.getTimeNodes());
      serializer.addToMessage(message, SPACE_NODES_FIELD, null, object.getSpaceNodes());
    }

  }

  @FudgeBuilderFor(PDEFullResults1D.class)
  public static final class PDEFullResults1DFudgeBuilder extends AbstractFudgeBuilder<PDEFullResults1D> {
    private static final String SOLVER_DATA_FIELD = "solverDataField";
    private static final String GRID_FIELD = "gridField";

    @Override
    public PDEFullResults1D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[][] solverData = deserializer.fieldValueToObject(double[][].class, message.getByName(SOLVER_DATA_FIELD));
      final PDEGrid1D grid = deserializer.fieldValueToObject(PDEGrid1D.class, message.getByName(GRID_FIELD));
      return new PDEFullResults1D(grid, solverData);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final PDEFullResults1D object) {
      serializer.addToMessage(message, SOLVER_DATA_FIELD, null, object.getF());
      serializer.addToMessage(message, GRID_FIELD, null, object.getGrid());
    }
  }

  @FudgeBuilderFor(PDEResultCollection.class)
  public static final class PDEResultCollectionFudgeBuilder extends AbstractFudgeBuilder<PDEResultCollection> {
    private static final String STRIKES_FIELD = "strikesField";
    private static final String GRID_IMPLIED_VOLS_FIELD = "impliedVolatilityField";
    private static final String GRID_FOREX_PV_QUOTES_FIELD = "forexPVQuotesField"; //DEBUG trying to get a new number out
    private static final String GRID_PRICE_FIELD = "gridPriceField";
    private static final String GRID_BLACK_PRICE_FIELD = "gridBlackPriceField";
    private static final String GRID_BLACK_DELTA_FIELD = "gridBlackDeltaField";
    private static final String GRID_BLACK_DUAL_DELTA_FIELD = "gridBlackDualDeltaField";
    private static final String GRID_BLACK_GAMMA_FIELD = "gridBlackGammaField";
    private static final String GRID_BLACK_DUAL_GAMMA_FIELD = "gridBlackDualGammaField";
    private static final String GRID_BLACK_VEGA_FIELD = "gridBlackVegaField";
    private static final String GRID_BLACK_VANNA_FIELD = "gridBlackVannaField";
    private static final String GRID_BLACK_VOMMA_FIELD = "gridBlackVommaField";
    private static final String GRID_DELTA_FIELD = "gridDeltaField";
    private static final String GRID_DUAL_DELTA_FIELD = "gridDualDeltaField";
    private static final String GRID_GAMMA_FIELD = "gridGammaField";
    private static final String GRID_DUAL_GAMMA_FIELD = "gridDualGammaField";
    private static final String GRID_VEGA_FIELD = "gridVegaField";
    private static final String GRID_VANNA_FIELD = "gridVannaField";
    private static final String GRID_VOMMA_FIELD = "gridVommaField";

    @Override
    public PDEResultCollection buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] strikes = deserializer.fieldValueToObject(double[].class, message.getByName(STRIKES_FIELD));
      final PDEResultCollection result = new PDEResultCollection(strikes);
      if (message.getByName(GRID_IMPLIED_VOLS_FIELD) != null) {
        final double[] impliedVol = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_IMPLIED_VOLS_FIELD));
        result.put(PDEResultCollection.GRID_IMPLIED_VOL, impliedVol);
      }
      //DEBUG trying to get a new number out
      if (message.getByName(GRID_FOREX_PV_QUOTES_FIELD) != null) {
        final double[] domesticAbsolute = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_FOREX_PV_QUOTES_FIELD));
        result.put(PDEResultCollection.GRID_DOMESTIC_PV_QUOTE, domesticAbsolute);
      }

      if (message.getByName(GRID_PRICE_FIELD) != null) {
        final double[] price = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_PRICE_FIELD));
        result.put(PDEResultCollection.GRID_PRICE, price);
      }
      if (message.getByName(GRID_BLACK_PRICE_FIELD) != null) {
        final double[] price = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_PRICE_FIELD));
        result.put(PDEResultCollection.GRID_BLACK_PRICE, price);
      }
      if (message.getByName(GRID_BLACK_DELTA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_DELTA_FIELD));
        result.put(PDEResultCollection.GRID_BLACK_DELTA, greek);
      }
      if (message.getByName(GRID_BLACK_DUAL_DELTA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_DUAL_DELTA_FIELD));
        result.put(PDEResultCollection.GRID_BLACK_DUAL_DELTA, greek);
      }
      if (message.getByName(GRID_BLACK_GAMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_GAMMA_FIELD));
        result.put(PDEResultCollection.GRID_BLACK_GAMMA, greek);
      }
      if (message.getByName(GRID_BLACK_DUAL_GAMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_DUAL_GAMMA_FIELD));
        result.put(PDEResultCollection.GRID_BLACK_DUAL_GAMMA, greek);
      }
      if (message.getByName(GRID_BLACK_VEGA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_VEGA_FIELD));
        result.put(PDEResultCollection.GRID_BLACK_VEGA, greek);
      }
      if (message.getByName(GRID_BLACK_VANNA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_VANNA_FIELD));
        result.put(PDEResultCollection.GRID_BLACK_VANNA, greek);
      }
      if (message.getByName(GRID_BLACK_VOMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_VOMMA_FIELD));
        result.put(PDEResultCollection.GRID_BLACK_VOMMA, greek);
      }
      if (message.getByName(GRID_DELTA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_DELTA_FIELD));
        result.put(PDEResultCollection.GRID_DELTA, greek);
      }
      if (message.getByName(GRID_DUAL_DELTA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_DUAL_DELTA_FIELD));
        result.put(PDEResultCollection.GRID_DUAL_DELTA, greek);
      }
      if (message.getByName(GRID_GAMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_GAMMA_FIELD));
        result.put(PDEResultCollection.GRID_GAMMA, greek);
      }
      if (message.getByName(GRID_DUAL_GAMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_DUAL_GAMMA_FIELD));
        result.put(PDEResultCollection.GRID_DUAL_GAMMA, greek);
      }
      if (message.getByName(GRID_VEGA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_VEGA_FIELD));
        result.put(PDEResultCollection.GRID_VEGA, greek);
      }
      if (message.getByName(GRID_VANNA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_VANNA_FIELD));
        result.put(PDEResultCollection.GRID_VANNA, greek);
      }
      if (message.getByName(GRID_VOMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_VOMMA_FIELD));
        result.put(PDEResultCollection.GRID_VOMMA, greek);
      }
      return result;
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final PDEResultCollection object) {
      serializer.addToMessage(message, STRIKES_FIELD, null, object.getStrikes());
      if (object.contains(PDEResultCollection.GRID_IMPLIED_VOL)) {
        serializer.addToMessage(message, GRID_IMPLIED_VOLS_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_IMPLIED_VOL));
      }
      //DEBUG trying to get a new number out
      if (object.contains(PDEResultCollection.GRID_DOMESTIC_PV_QUOTE)) {
        serializer.addToMessage(message, GRID_FOREX_PV_QUOTES_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_DOMESTIC_PV_QUOTE));
      }
      if (object.contains(PDEResultCollection.GRID_IMPLIED_VOL)) {
        serializer.addToMessage(message, GRID_IMPLIED_VOLS_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_IMPLIED_VOL));
      }
      if (object.contains(PDEResultCollection.GRID_PRICE)) {
        serializer.addToMessage(message, GRID_PRICE_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_PRICE));
      }
      if (object.contains(PDEResultCollection.GRID_BLACK_PRICE)) {
        serializer.addToMessage(message, GRID_BLACK_PRICE_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_BLACK_PRICE));
      }
      if (object.contains(PDEResultCollection.GRID_BLACK_DELTA)) {
        serializer.addToMessage(message, GRID_BLACK_DELTA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_BLACK_DELTA));
      }
      if (object.contains(PDEResultCollection.GRID_BLACK_DUAL_DELTA)) {
        serializer.addToMessage(message, GRID_BLACK_DUAL_DELTA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_BLACK_DUAL_DELTA));
      }
      if (object.contains(PDEResultCollection.GRID_BLACK_GAMMA)) {
        serializer.addToMessage(message, GRID_BLACK_GAMMA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_BLACK_GAMMA));
      }
      if (object.contains(PDEResultCollection.GRID_BLACK_DUAL_GAMMA)) {
        serializer.addToMessage(message, GRID_BLACK_DUAL_GAMMA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_BLACK_DUAL_GAMMA));
      }
      if (object.contains(PDEResultCollection.GRID_BLACK_VEGA)) {
        serializer.addToMessage(message, GRID_BLACK_VEGA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_BLACK_VEGA));
      }
      if (object.contains(PDEResultCollection.GRID_BLACK_VANNA)) {
        serializer.addToMessage(message, GRID_BLACK_VANNA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_BLACK_VANNA));
      }
      if (object.contains(PDEResultCollection.GRID_BLACK_VOMMA)) {
        serializer.addToMessage(message, GRID_BLACK_VOMMA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_BLACK_VOMMA));
      }
      if (object.contains(PDEResultCollection.GRID_DELTA)) {
        serializer.addToMessage(message, GRID_DELTA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_DELTA));
      }
      if (object.contains(PDEResultCollection.GRID_DUAL_DELTA)) {
        serializer.addToMessage(message, GRID_DUAL_DELTA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_DUAL_DELTA));
      }
      if (object.contains(PDEResultCollection.GRID_GAMMA)) {
        serializer.addToMessage(message, GRID_GAMMA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_GAMMA));
      }
      if (object.contains(PDEResultCollection.GRID_DUAL_GAMMA)) {
        serializer.addToMessage(message, GRID_DUAL_GAMMA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_DUAL_GAMMA));
      }
      if (object.contains(PDEResultCollection.GRID_VEGA)) {
        serializer.addToMessage(message, GRID_VEGA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_VEGA));
      }
      if (object.contains(PDEResultCollection.GRID_VANNA)) {
        serializer.addToMessage(message, GRID_VANNA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_VANNA));
      }
      if (object.contains(PDEResultCollection.GRID_VOMMA)) {
        serializer.addToMessage(message, GRID_VOMMA_FIELD, null, object.getGridGreeks(PDEResultCollection.GRID_VOMMA));
      }
    }
  }

  @FudgeBuilderFor(BucketedGreekResultCollection.class)
  public static final class BucketedGreekResultCollectionFudgeBuilder extends AbstractFudgeBuilder<BucketedGreekResultCollection> {
    private static final String EXPIRIES_FIELD = "expiriesField";
    private static final String STRIKES_FIELD = "strikesField";
    private static final String BUCKETED_VEGA_FIELD = "bucketedVegaField";

    @Override
    public BucketedGreekResultCollection buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] expiries = deserializer.fieldValueToObject(double[].class, message.getByName(EXPIRIES_FIELD));
      final double[][] strikes = deserializer.fieldValueToObject(double[][].class, message.getByName(STRIKES_FIELD));
      final BucketedGreekResultCollection result = new BucketedGreekResultCollection(expiries, strikes);
      if (message.getByName(BUCKETED_VEGA_FIELD) != null) {
        final double[][] greek = deserializer.fieldValueToObject(double[][].class, message.getByName(BUCKETED_VEGA_FIELD));
        result.put(BucketedGreekResultCollection.BUCKETED_VEGA, greek);
      }
      return result;
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final BucketedGreekResultCollection object) {
      serializer.addToMessage(message, EXPIRIES_FIELD, null, object.getExpiries());
      serializer.addToMessage(message, STRIKES_FIELD, null, object.getStrikes());
      if (object.contains(BucketedGreekResultCollection.BUCKETED_VEGA)) {
        serializer.addToMessage(message, BUCKETED_VEGA_FIELD, null, object.getBucketedGreeks(BucketedGreekResultCollection.BUCKETED_VEGA));
      }
    }

  }

  @FudgeBuilderFor(ForexLocalVolatilityPDEPresentValueResultCollection.class)
  public static final class ForexLocalVolatilityPDEPresentValueResultCollectionBuilder extends AbstractFudgeBuilder<ForexLocalVolatilityPDEPresentValueResultCollection> {
    private static final String STRIKES_FIELD = "strikes";

    @Override
    public ForexLocalVolatilityPDEPresentValueResultCollection buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] strikes = deserializer.fieldValueToObject(double[].class, message.getByName(STRIKES_FIELD));
      final Map<String, double[]> pvDataMap = new HashMap<String, double[]>();
      if (message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.LV_PIPS) != null) {
        final double[] array = deserializer.fieldValueToObject(double[].class, message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.LV_PIPS));
        pvDataMap.put(ForexLocalVolatilityPDEPresentValueResultCollection.LV_PIPS, array);
      }
      if (message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.LV_PUT_PV) != null) {
        final double[] array = deserializer.fieldValueToObject(double[].class, message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.LV_PUT_PV));
        pvDataMap.put(ForexLocalVolatilityPDEPresentValueResultCollection.LV_PUT_PV, array);
      }
      if (message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.LV_CALL_PV) != null) {
        final double[] array = deserializer.fieldValueToObject(double[].class, message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.LV_CALL_PV));
        pvDataMap.put(ForexLocalVolatilityPDEPresentValueResultCollection.LV_CALL_PV, array);
      }
      if (message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_PIPS) != null) {
        final double[] array = deserializer.fieldValueToObject(double[].class, message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_PIPS));
        pvDataMap.put(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_PIPS, array);
      }
      if (message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_PUT_PV) != null) {
        final double[] array = deserializer.fieldValueToObject(double[].class, message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_PUT_PV));
        pvDataMap.put(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_PUT_PV, array);
      }
      if (message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_CALL_PV) != null) {
        final double[] array = deserializer.fieldValueToObject(double[].class, message.getByName(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_CALL_PV));
        pvDataMap.put(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_CALL_PV, array);
      }
      return new ForexLocalVolatilityPDEPresentValueResultCollection(strikes, pvDataMap);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ForexLocalVolatilityPDEPresentValueResultCollection object) {
      serializer.addToMessage(message, STRIKES_FIELD, null, object.getStrikes());
      serializer.addToMessage(message, ForexLocalVolatilityPDEPresentValueResultCollection.LV_PIPS, null, object.getPV(ForexLocalVolatilityPDEPresentValueResultCollection.LV_PIPS));
      serializer.addToMessage(message, ForexLocalVolatilityPDEPresentValueResultCollection.LV_PUT_PV, null, object.getPV(ForexLocalVolatilityPDEPresentValueResultCollection.LV_PUT_PV));
      serializer.addToMessage(message, ForexLocalVolatilityPDEPresentValueResultCollection.LV_CALL_PV, null, object.getPV(ForexLocalVolatilityPDEPresentValueResultCollection.LV_CALL_PV));
      serializer.addToMessage(message, ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_PIPS, null, object.getPV(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_PIPS));
      serializer.addToMessage(message, ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_PUT_PV, null, object.getPV(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_PUT_PV));
      serializer.addToMessage(message, ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_CALL_PV, null, object.getPV(ForexLocalVolatilityPDEPresentValueResultCollection.BLACK_CALL_PV));
    }
  }
}
