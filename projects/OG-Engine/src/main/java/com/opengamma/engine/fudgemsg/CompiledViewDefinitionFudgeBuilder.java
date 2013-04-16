/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.threeten.bp.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;
import com.opengamma.id.VersionCorrection;

/**
 * Fudge message builder for {@link CompiledViewDefinition}
 */
@GenericFudgeBuilderFor(CompiledViewDefinition.class)
public class CompiledViewDefinitionFudgeBuilder implements FudgeBuilder<CompiledViewDefinition> {

  private static final String VERSION_CORRECTION_FIELD = "versionCorrection";
  private static final String COMPILATION_IDENTIFIER_FIELD = "compilationId";
  private static final String VIEW_DEFINITION_FIELD = "viewDefinition";
  private static final String PORTFOLIO_FIELD = "portfolio";
  private static final String COMPILED_CALCULATION_CONFIGURATIONS_FIELD = "compiledCalculationConfigurations";
  private static final String EARLIEST_VALIDITY_FIELD = "earliestValidity";
  private static final String LATEST_VALIDITY_FIELD = "latestValidity";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CompiledViewDefinition object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, VERSION_CORRECTION_FIELD, null, object.getResolverVersionCorrection());
    serializer.addToMessage(msg, COMPILATION_IDENTIFIER_FIELD, null, object.getCompilationIdentifier());
    serializer.addToMessage(msg, VIEW_DEFINITION_FIELD, null, object.getViewDefinition());
    serializer.addToMessage(msg, PORTFOLIO_FIELD, null, object.getPortfolio());

    // Serialise manually for more control on deserialisation 
    for (CompiledViewCalculationConfiguration compiledCalculationConfiguration : object.getCompiledCalculationConfigurations()) {
      serializer.addToMessage(msg, COMPILED_CALCULATION_CONFIGURATIONS_FIELD, null, compiledCalculationConfiguration);
    }

    serializer.addToMessage(msg, EARLIEST_VALIDITY_FIELD, null, object.getValidFrom());
    serializer.addToMessage(msg, LATEST_VALIDITY_FIELD, null, object.getValidTo());
    return msg;
  }

  @Override
  public CompiledViewDefinition buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    VersionCorrection versionCorrection = deserializer.fieldValueToObject(VersionCorrection.class, message.getByName(VERSION_CORRECTION_FIELD));
    String compilationId = message.getString(COMPILATION_IDENTIFIER_FIELD);
    ViewDefinition viewDefinition = deserializer.fieldValueToObject(ViewDefinition.class, message.getByName(VIEW_DEFINITION_FIELD));
    FudgeField portfolioField = message.getByName(PORTFOLIO_FIELD);
    Portfolio portfolio = portfolioField != null ? deserializer.fieldValueToObject(Portfolio.class, portfolioField) : null;

    // Deserialise each instance specifically into the required type
    Collection<CompiledViewCalculationConfiguration> compiledCalculationConfigurations = new ArrayList<CompiledViewCalculationConfiguration>();
    List<FudgeField> calcConfigFields = message.getAllByName(COMPILED_CALCULATION_CONFIGURATIONS_FIELD);
    for (FudgeField field : calcConfigFields) {
      compiledCalculationConfigurations.add(deserializer.fieldValueToObject(CompiledViewCalculationConfiguration.class, field));
    }

    FudgeField earliestValidityField = message.getByName(EARLIEST_VALIDITY_FIELD);
    Instant earliestValidity = earliestValidityField != null ? deserializer.fieldValueToObject(Instant.class, earliestValidityField) : null;
    FudgeField latestValidityField = message.getByName(LATEST_VALIDITY_FIELD);
    Instant latestValidity = latestValidityField != null ? deserializer.fieldValueToObject(Instant.class, latestValidityField) : null;
    return new CompiledViewDefinitionImpl(versionCorrection, compilationId, viewDefinition, portfolio, compiledCalculationConfigurations, earliestValidity, latestValidity);
  }

}
