/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.financial.greeks.PDEGreekResultCollection;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;

/**
 * 
 */
/* package */ final class PDEResultsFudgeBuilder {

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

  @FudgeBuilderFor(PDEGreekResultCollection.class)
  public static final class PDEGreekResultCollectionFudgeBuilder extends AbstractFudgeBuilder<PDEGreekResultCollection> {
    private static final String STRIKES_FIELD = "strikesField";
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
    public PDEGreekResultCollection buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] strikes = deserializer.fieldValueToObject(double[].class, message.getByName(STRIKES_FIELD));
      final PDEGreekResultCollection result = new PDEGreekResultCollection(strikes);
      if (message.getByName(GRID_BLACK_DELTA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_DELTA_FIELD));
        result.put(PDEGreekResultCollection.GRID_BLACK_DELTA, greek);
      }
      if (message.getByName(GRID_BLACK_DUAL_DELTA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_DUAL_DELTA_FIELD));
        result.put(PDEGreekResultCollection.GRID_BLACK_DUAL_DELTA, greek);
      }
      if (message.getByName(GRID_BLACK_GAMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_GAMMA_FIELD));
        result.put(PDEGreekResultCollection.GRID_BLACK_GAMMA, greek);
      }
      if (message.getByName(GRID_BLACK_DUAL_GAMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_DUAL_GAMMA_FIELD));
        result.put(PDEGreekResultCollection.GRID_BLACK_DUAL_GAMMA, greek);
      }
      if (message.getByName(GRID_BLACK_VEGA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_VEGA_FIELD));
        result.put(PDEGreekResultCollection.GRID_BLACK_VEGA, greek);
      }
      if (message.getByName(GRID_BLACK_VANNA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_VANNA_FIELD));
        result.put(PDEGreekResultCollection.GRID_BLACK_VANNA, greek);
      }
      if (message.getByName(GRID_BLACK_VOMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_BLACK_VOMMA_FIELD));
        result.put(PDEGreekResultCollection.GRID_BLACK_VOMMA, greek);
      }
      if (message.getByName(GRID_DELTA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_DELTA_FIELD));
        result.put(PDEGreekResultCollection.GRID_DELTA, greek);
      }
      if (message.getByName(GRID_DUAL_DELTA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_DUAL_DELTA_FIELD));
        result.put(PDEGreekResultCollection.GRID_DUAL_DELTA, greek);
      }
      if (message.getByName(GRID_GAMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_GAMMA_FIELD));
        result.put(PDEGreekResultCollection.GRID_GAMMA, greek);
      }
      if (message.getByName(GRID_DUAL_GAMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_DUAL_GAMMA_FIELD));
        result.put(PDEGreekResultCollection.GRID_DUAL_GAMMA, greek);
      }
      if (message.getByName(GRID_VEGA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_VEGA_FIELD));
        result.put(PDEGreekResultCollection.GRID_VEGA, greek);
      }
      if (message.getByName(GRID_VANNA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_VANNA_FIELD));
        result.put(PDEGreekResultCollection.GRID_VANNA, greek);
      }
      if (message.getByName(GRID_VOMMA_FIELD) != null) {
        final double[] greek = deserializer.fieldValueToObject(double[].class, message.getByName(GRID_VOMMA_FIELD));
        result.put(PDEGreekResultCollection.GRID_VOMMA, greek);
      }
      return result;
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final PDEGreekResultCollection object) {
      serializer.addToMessage(message, STRIKES_FIELD, null, object.getStrikes());
      if (object.contains(PDEGreekResultCollection.GRID_BLACK_DELTA)) {
        serializer.addToMessage(message, GRID_BLACK_DELTA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_BLACK_DELTA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_BLACK_DUAL_DELTA)) {
        serializer.addToMessage(message, GRID_BLACK_DUAL_DELTA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_BLACK_DUAL_DELTA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_BLACK_GAMMA)) {
        serializer.addToMessage(message, GRID_BLACK_GAMMA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_BLACK_GAMMA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_BLACK_DUAL_GAMMA)) {
        serializer.addToMessage(message, GRID_BLACK_DUAL_GAMMA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_BLACK_DUAL_GAMMA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_BLACK_VEGA)) {
        serializer.addToMessage(message, GRID_BLACK_VEGA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_BLACK_VEGA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_BLACK_VANNA)) {
        serializer.addToMessage(message, GRID_BLACK_VANNA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_BLACK_VANNA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_BLACK_VOMMA)) {
        serializer.addToMessage(message, GRID_BLACK_VOMMA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_BLACK_VOMMA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_DELTA)) {
        serializer.addToMessage(message, GRID_DELTA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_DELTA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_DUAL_DELTA)) {
        serializer.addToMessage(message, GRID_DUAL_DELTA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_DUAL_DELTA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_GAMMA)) {
        serializer.addToMessage(message, GRID_GAMMA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_GAMMA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_DUAL_GAMMA)) {
        serializer.addToMessage(message, GRID_DUAL_GAMMA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_DUAL_GAMMA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_VEGA)) {
        serializer.addToMessage(message, GRID_VEGA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_VEGA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_VANNA)) {
        serializer.addToMessage(message, GRID_VANNA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_VANNA));
      }
      if (object.contains(PDEGreekResultCollection.GRID_VOMMA)) {
        serializer.addToMessage(message, GRID_VOMMA_FIELD, null, object.getGridGreeks(PDEGreekResultCollection.GRID_VOMMA));
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
}
