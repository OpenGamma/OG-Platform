/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.IndicatorType;

import com.opengamma.engine.depgraph.ambiguity.FullRequirementResolution;
import com.opengamma.engine.depgraph.ambiguity.RequirementResolution;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Fudge builder for {@link FullRequirementResolution}.
 * 
 * <pre>
 * message FullRequirementResolution {
 *   required ValueRequirement requirement;       // the attempted resolution
 *   repeated RequirementResolution resolution;   // a specific resolution, repeated in descending priority order
 *   repeated RequirementResolution[] resolution; // an ambiguous resolution, repeated in descending priority order
 * }
 * </pre>
 */
@FudgeBuilderFor(FullRequirementResolution.class)
public class FullRequirementResolutionFudgeBuilder implements FudgeBuilder<FullRequirementResolution> {

  // TODO: Improve efficiency - there may be a lot of duplication in the resolutions

  private static final String REQUIREMENT_FIELD_NAME = "requirement";
  private static final String RESOLUTION_FIELD_NAME = "resolution";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FullRequirementResolution object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, REQUIREMENT_FIELD_NAME, null, object.getRequirement());
    for (Collection<RequirementResolution> resolutions : object.getResolutions()) {
      if (resolutions.size() == 1) {
        serializer.addToMessage(msg, RESOLUTION_FIELD_NAME, null, resolutions.iterator().next());
      } else if (resolutions.size() > 1) {
        final MutableFudgeMsg resolutionsMsg = msg.addSubMessage(RESOLUTION_FIELD_NAME, null);
        for (RequirementResolution resolution : resolutions) {
          if (resolution != null) {
            serializer.addToMessage(resolutionsMsg, null, null, resolution);
          } else {
            resolutionsMsg.add(null, null, IndicatorType.INSTANCE);
          }
        }
      }
    }
    return msg;
  }

  @Override
  public FullRequirementResolution buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final FullRequirementResolution result = new FullRequirementResolution(deserializer.fieldValueToObject(ValueRequirement.class, msg.getByName(REQUIREMENT_FIELD_NAME)));
    final Collection<FudgeField> resolutionFields = msg.getAllByName(RESOLUTION_FIELD_NAME);
    for (FudgeField resolutionField : resolutionFields) {
      final FudgeMsg resolutionsMessage = ((FudgeMsg) resolutionField.getValue());
      if (resolutionsMessage.hasField((String) null)) {
        final Collection<RequirementResolution> resolved = new ArrayList<RequirementResolution>(resolutionsMessage.getNumFields());
        for (FudgeField resolutionField2 : resolutionsMessage) {
          if (resolutionField2.getValue() instanceof IndicatorType) {
            resolved.add(null);
          } else {
            resolved.add(deserializer.fieldValueToObject(RequirementResolution.class, resolutionField2));
          }
        }
        result.addResolutions(resolved);
      } else {
        result.addResolutions(Collections.singleton(deserializer.fieldValueToObject(RequirementResolution.class, resolutionField)));
      }
    }
    return result;
  }

}
