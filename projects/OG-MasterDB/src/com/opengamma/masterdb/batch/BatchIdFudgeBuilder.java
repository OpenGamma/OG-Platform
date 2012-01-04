/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import javax.time.Instant;

import com.opengamma.financial.batch.BatchId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;


/**
 * Fudge message builder for {@code BatchId}.
 */
@FudgeBuilderFor(BatchId.class)
public class BatchIdFudgeBuilder implements FudgeBuilder<BatchId> {

  /** Field name. */
  public static final String SNAPSHOT_UID_FIELD_NAME = "marketDataSnapshotUid";
  /** Field name. */
  public static final String VALUATION_TIME_FIELD_NAME = "valuationTime";
  /** Field name. */
  public static final String VERSION_CORRECTION_FIELD_NAME = "versionCorrection";
  /** Field name. */
  public static final String VIEW_DEFINITION_UID_FIELD_NAME = "viewDefinitionUid";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, BatchId object) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(SNAPSHOT_UID_FIELD_NAME, null, object.getMarketDataSnapshotUid());
    msg.add(VALUATION_TIME_FIELD_NAME, null, object.getValuationTime());
    msg.add(VERSION_CORRECTION_FIELD_NAME, null, object.getVersionCorrection());
    msg.add(VIEW_DEFINITION_UID_FIELD_NAME, null, object.getViewDefinitionUid());
    return msg;
  }

  @Override
  public BatchId buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField snapshotUidField = message.getByName(SNAPSHOT_UID_FIELD_NAME);
    FudgeField valuationTimeField = message.getByName(VALUATION_TIME_FIELD_NAME);
    FudgeField versionCorrectionField = message.getByName(VERSION_CORRECTION_FIELD_NAME);
    FudgeField viewDefinitionUidField = message.getByName(VIEW_DEFINITION_UID_FIELD_NAME);

    Validate.notNull(snapshotUidField, "Fudge message is not a BatchId - field " + SNAPSHOT_UID_FIELD_NAME + " is not present");
    Validate.notNull(valuationTimeField, "Fudge message is not a BatchId - field " + VALUATION_TIME_FIELD_NAME + " is not present");
    Validate.notNull(versionCorrectionField, "Fudge message is not a BatchId - field " + VERSION_CORRECTION_FIELD_NAME + " is not present");
    Validate.notNull(viewDefinitionUidField, "Fudge message is not a BatchId - field " + VIEW_DEFINITION_UID_FIELD_NAME + " is not present");

    UniqueId snapshotUid = message.getFieldValue(UniqueId.class, snapshotUidField);
    Instant valuationTime = message.getFieldValue(Instant.class, valuationTimeField);
    VersionCorrection versionCorrection = message.getFieldValue(VersionCorrection.class, versionCorrectionField);
    UniqueId viewDefinitionUid = message.getFieldValue(UniqueId.class, viewDefinitionUidField);

    return new BatchId(snapshotUid, viewDefinitionUid, versionCorrection, valuationTime);
  }

}
