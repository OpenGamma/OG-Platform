/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fudgemsg;

import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.timeseries.config.TimeSeriesMetaDataConfiguration;

/**
 * Builder for converting TimeSeriesMataDataConfiguration instances to/from Fudge messages.
 */
@FudgeBuilderFor(TimeSeriesMetaDataConfiguration.class)
public class TimeSeriesMetaDataConfigurationBuilder implements FudgeBuilder<TimeSeriesMetaDataConfiguration> {

  @Override
  public MutableFudgeFieldContainer buildMessage(final FudgeSerializationContext context, final TimeSeriesMetaDataConfiguration object) {
    MutableFudgeFieldContainer message = context.newMessage();
    String securityType = object.getSecurityType();
    if (securityType != null) {
      message.add("securityType", securityType);
    }
    String defaultDataSource = object.getDefaultDataSource();
    if (defaultDataSource != null) {
      message.add("defaultDataSource", defaultDataSource);
    }
    Set<String> dataSources = object.getDataSources();
    if (dataSources != null) {
      for (String dataSource : dataSources) {
        if (dataSource != null) {
          message.add("dataSources", dataSource);
        }
      }
    }
    String defaultDataField = object.getDefaultDataField();
    if (defaultDataField != null) {
      message.add("defaultDataField", defaultDataField);
    }
    Set<String> dataFields = object.getDataFields();
    if (dataFields != null) {
      for (String dataField : dataFields) {
        if (dataField != null) {
          message.add("dataFields", dataField);
        }
      }
    }
    String defaultDataProvider = object.getDefaultDataProvider();
    if (defaultDataProvider != null) {
      message.add("defaultDataProvider", defaultDataProvider);
    }
    Set<String> dataProviders = object.getDataProviders();
    if (dataProviders != null) {
      for (String dataProvider : dataProviders) {
        if (dataProvider != null) {
          message.add("dataProviders", dataProvider);
        }
      }
    }
    return message;
  }

  @Override
  public TimeSeriesMetaDataConfiguration buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {

    String securityType = message.getString("securityType");
    if (securityType == null) {
      throw new IllegalArgumentException("Fudge message is not a TimeSeriesMetaDataConfiguration - field 'securityType' is not present");
    }

    String defaultDataSource = message.getString("defaultDataSource");
    if (defaultDataSource == null) {
      throw new IllegalArgumentException("Fudge message is not a TimeSeriesMetaDataConfiguration - field 'defaultDataSource' is not present");
    }

    String defaultDataField = message.getString("defaultDataField");
    if (defaultDataField == null) {
      throw new IllegalArgumentException("Fudge message is not a TimeSeriesMetaDataConfiguration - field 'defaultDataField' is not present");
    }

    String defaultDataProvider = message.getString("defaultDataProvider");
    if (defaultDataProvider == null) {
      throw new IllegalArgumentException("Fudge message is not a TimeSeriesMetaDataConfiguration - field 'defaultDataProvider' is not present");
    }

    TimeSeriesMetaDataConfiguration configuration = new TimeSeriesMetaDataConfiguration(securityType, defaultDataSource, defaultDataField, defaultDataProvider);

    List<FudgeField> allByName = message.getAllByName("dataSources");
    for (FudgeField fudgeField : allByName) {
      Object value = fudgeField.getValue();
      if (value instanceof String) {
        configuration.addDataSource((String) value);
      } else {
        throw new IllegalArgumentException("Fudge message is not a TimeSeriesMetaDataConfiguration - field 'dataSources' is not string");
      }
    }

    allByName = message.getAllByName("dataFields");
    for (FudgeField fudgeField : allByName) {
      Object value = fudgeField.getValue();
      if (value instanceof String) {
        configuration.addDataField((String) value);
      } else {
        throw new IllegalArgumentException("Fudge message is not a TimeSeriesMetaDataConfiguration - field 'dataFields' is not string");
      }
    }

    allByName = message.getAllByName("dataProviders");
    for (FudgeField fudgeField : allByName) {
      Object value = fudgeField.getValue();
      if (value instanceof String) {
        configuration.addDataProvider((String) value);
      } else {
        throw new IllegalArgumentException("Fudge message is not a TimeSeriesMetaDataConfiguration - field 'dataProviders' is not string");
      }
    }

    return configuration;
  }

}
