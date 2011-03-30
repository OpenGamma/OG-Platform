/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.interestrate.NelsonSiegelBondCurveModel;
import com.opengamma.financial.interestrate.NelsonSiegelSvennsonBondCurveModel;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * Fudge builders for com.opengamma.financial.interestrate.* classes
 */
final class FinancialInterestRate {

  private FinancialInterestRate() {
  }

  /**
   * Fudge builder for {@code NelsonSiegelBondCurveModel.SerializedForm}
   */
  @FudgeBuilderFor(NelsonSiegelBondCurveModel.SerializedForm.class)
  public static final class NelsonSiegelBondCurveModelSerializedForm extends AbstractSerializedFormBuilder<NelsonSiegelBondCurveModel.SerializedForm> {

    @Override
    public Object buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      final NelsonSiegelBondCurveModel model = new NelsonSiegelBondCurveModel();
      return model.getParameterizedFunction();
    }

    @Override
    public void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final NelsonSiegelBondCurveModel.SerializedForm object) {
      // No state
    }
  }

  /**
   * Fudge builder for {@code NelsonSiegelSvennsonBondCurveModel}. This may be temporary based on the current structure; remove if
   * the model is rewritten to match the NelsonSiegelBondCurveModel function.
   */
  @FudgeBuilderFor(NelsonSiegelSvennsonBondCurveModel.class)
  public static final class NelsonSiegelSvennsonBondCurveModelBuilder extends AbstractFudgeBuilder<NelsonSiegelSvennsonBondCurveModel> {

    @Override
    protected void buildMessage(FudgeSerializationContext context, MutableFudgeFieldContainer message, NelsonSiegelSvennsonBondCurveModel object) {
      context.objectToFudgeMsg(message, "parameters", null, object.getParameters().getData());
    }

    @Override
    public NelsonSiegelSvennsonBondCurveModel buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
      final double[] parameters = context.fieldValueToObject(double[].class, message.getByName("parameters"));
      return new NelsonSiegelSvennsonBondCurveModel(new DoubleMatrix1D(parameters));
    }

  }

}
