/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter;

/**
 * An extension of {@link JdbcTemplate} to handle Oracle.
 * <p>
 * This class parses Oracle formatted strings to application format.
 * Specifically, it handle empty strings by converting them from a single whitespace.
 * <p>
 * To get the proper round trip behavior, ensure that {@link OracleNamedParameterJdbcTemplate} is used.
 */
final class OracleJdbcTemplate extends JdbcTemplate {

  /**
   * Creates an instance.
   */
  public OracleJdbcTemplate() {
    super();
  }

  /**
   * Creates an instance.
   * 
   * @param dataSource  the data source
   */
  public OracleJdbcTemplate(DataSource dataSource) {
    super(dataSource);
  }

  //-------------------------------------------------------------------------
  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    // abuse the NativeJdbcExtractor to wrap the ResultSet
    setNativeJdbcExtractor(new NativeJdbcExtractorAdapter() {
      @Override
      public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
        return new ResultSetDecorator(rs);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
    return new ArgumentPreparedStatementSetter(args) {
      @Override
      protected void doSetValue(PreparedStatement ps, int parameterPosition, Object argValue) throws SQLException {
        if (argValue instanceof SqlParameterValue) {
          SqlParameterValue paramValue = (SqlParameterValue) argValue;
          if (paramValue.getValue() instanceof String) {
            String str = Oracle11gDbDialect.INSTANCE.toDatabaseString((String) paramValue.getValue());
            paramValue = new SqlParameterValue(paramValue.getSqlType(), str);
          }
          StatementCreatorUtils.setParameterValue(ps, parameterPosition, paramValue, paramValue.getValue());
        } else {
          if (argValue instanceof String) {
            argValue = Oracle11gDbDialect.INSTANCE.toDatabaseString((String) argValue);
          }
          StatementCreatorUtils.setParameterValue(ps, parameterPosition, SqlTypeValue.TYPE_UNKNOWN, argValue);
        }
      }
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "OracleJdbcTemplate:" + super.toString();
  }

  //-------------------------------------------------------------------------
  /**
   * Decorates {@code ResultSet} for Oracle.
   */
  static class ResultSetDecorator implements ResultSet {
    // underlying result set
    private final ResultSet _underlying;

    // create using an underlying
    ResultSetDecorator(ResultSet underlying) {
      _underlying = underlying;
    }

    // decode strings (common cases, not every last one)
    //-------------------------------------------------------------------------
    public String getString(int columnIndex) throws SQLException {
      String str = _underlying.getString(columnIndex);
      return Oracle11gDbDialect.INSTANCE.fromDatabaseString(str);
    }
    public String getString(String columnLabel) throws SQLException {
      String str = _underlying.getString(columnLabel);
      return Oracle11gDbDialect.INSTANCE.fromDatabaseString(str);
    }
    public String getNString(int columnIndex) throws SQLException {
      String str = _underlying.getNString(columnIndex);
      return Oracle11gDbDialect.INSTANCE.fromDatabaseString(str);
    }
    public String getNString(String columnLabel) throws SQLException {
      String str = _underlying.getNString(columnLabel);
      return Oracle11gDbDialect.INSTANCE.fromDatabaseString(str);
    }
    public Object getObject(int columnIndex) throws SQLException {
      Object obj = _underlying.getObject(columnIndex);
      if (obj instanceof String) {
        return Oracle11gDbDialect.INSTANCE.fromDatabaseString((String) obj);
      }
      return obj;
    }
    public Object getObject(String columnLabel) throws SQLException {
      Object obj = _underlying.getObject(columnLabel);
      if (obj instanceof String) {
        return Oracle11gDbDialect.INSTANCE.fromDatabaseString((String) obj);
      }
      return obj;
    }
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
      if (type == String.class) {
        return type.cast(getString(columnIndex));
      }
      return _underlying.getObject(columnIndex, type);
    }
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
      if (type == String.class) {
        return type.cast(getString(columnLabel));
      }
      return _underlying.getObject(columnLabel, type);
    }

    // delegate to other methods
    //-------------------------------------------------------------------------
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return _underlying.unwrap(iface);
    }
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return _underlying.isWrapperFor(iface);
    }
    public boolean next() throws SQLException {
      return _underlying.next();
    }
    public void close() throws SQLException {
      _underlying.close();
    }
    public boolean wasNull() throws SQLException {
      return _underlying.wasNull();
    }
    public boolean getBoolean(int columnIndex) throws SQLException {
      return _underlying.getBoolean(columnIndex);
    }
    public byte getByte(int columnIndex) throws SQLException {
      return _underlying.getByte(columnIndex);
    }
    public short getShort(int columnIndex) throws SQLException {
      return _underlying.getShort(columnIndex);
    }
    public int getInt(int columnIndex) throws SQLException {
      return _underlying.getInt(columnIndex);
    }
    public long getLong(int columnIndex) throws SQLException {
      return _underlying.getLong(columnIndex);
    }
    public float getFloat(int columnIndex) throws SQLException {
      return _underlying.getFloat(columnIndex);
    }
    public double getDouble(int columnIndex) throws SQLException {
      return _underlying.getDouble(columnIndex);
    }
    @SuppressWarnings("deprecation")
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
      return _underlying.getBigDecimal(columnIndex, scale);
    }
    public byte[] getBytes(int columnIndex) throws SQLException {
      return _underlying.getBytes(columnIndex);
    }
    public Date getDate(int columnIndex) throws SQLException {
      return _underlying.getDate(columnIndex);
    }
    public Time getTime(int columnIndex) throws SQLException {
      return _underlying.getTime(columnIndex);
    }
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
      return _underlying.getTimestamp(columnIndex);
    }
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
      return _underlying.getAsciiStream(columnIndex);
    }
    @SuppressWarnings("deprecation")
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
      return _underlying.getUnicodeStream(columnIndex);
    }
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
      return _underlying.getBinaryStream(columnIndex);
    }
    public boolean getBoolean(String columnLabel) throws SQLException {
      return _underlying.getBoolean(columnLabel);
    }
    public byte getByte(String columnLabel) throws SQLException {
      return _underlying.getByte(columnLabel);
    }
    public short getShort(String columnLabel) throws SQLException {
      return _underlying.getShort(columnLabel);
    }
    public int getInt(String columnLabel) throws SQLException {
      return _underlying.getInt(columnLabel);
    }
    public long getLong(String columnLabel) throws SQLException {
      return _underlying.getLong(columnLabel);
    }
    public float getFloat(String columnLabel) throws SQLException {
      return _underlying.getFloat(columnLabel);
    }
    public double getDouble(String columnLabel) throws SQLException {
      return _underlying.getDouble(columnLabel);
    }
    @SuppressWarnings("deprecation")
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
      return _underlying.getBigDecimal(columnLabel, scale);
    }
    public byte[] getBytes(String columnLabel) throws SQLException {
      return _underlying.getBytes(columnLabel);
    }
    public Date getDate(String columnLabel) throws SQLException {
      return _underlying.getDate(columnLabel);
    }
    public Time getTime(String columnLabel) throws SQLException {
      return _underlying.getTime(columnLabel);
    }
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
      return _underlying.getTimestamp(columnLabel);
    }
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
      return _underlying.getAsciiStream(columnLabel);
    }
    @SuppressWarnings("deprecation")
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
      return _underlying.getUnicodeStream(columnLabel);
    }
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
      return _underlying.getBinaryStream(columnLabel);
    }
    public SQLWarning getWarnings() throws SQLException {
      return _underlying.getWarnings();
    }
    public void clearWarnings() throws SQLException {
      _underlying.clearWarnings();
    }
    public String getCursorName() throws SQLException {
      return _underlying.getCursorName();
    }
    public ResultSetMetaData getMetaData() throws SQLException {
      return _underlying.getMetaData();
    }
    public int findColumn(String columnLabel) throws SQLException {
      return _underlying.findColumn(columnLabel);
    }
    public Reader getCharacterStream(int columnIndex) throws SQLException {
      return _underlying.getCharacterStream(columnIndex);
    }
    public Reader getCharacterStream(String columnLabel) throws SQLException {
      return _underlying.getCharacterStream(columnLabel);
    }
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
      return _underlying.getBigDecimal(columnIndex);
    }
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
      return _underlying.getBigDecimal(columnLabel);
    }
    public boolean isBeforeFirst() throws SQLException {
      return _underlying.isBeforeFirst();
    }
    public boolean isAfterLast() throws SQLException {
      return _underlying.isAfterLast();
    }
    public boolean isFirst() throws SQLException {
      return _underlying.isFirst();
    }
    public boolean isLast() throws SQLException {
      return _underlying.isLast();
    }
    public void beforeFirst() throws SQLException {
      _underlying.beforeFirst();
    }
    public void afterLast() throws SQLException {
      _underlying.afterLast();
    }
    public boolean first() throws SQLException {
      return _underlying.first();
    }
    public boolean last() throws SQLException {
      return _underlying.last();
    }
    public int getRow() throws SQLException {
      return _underlying.getRow();
    }
    public boolean absolute(int row) throws SQLException {
      return _underlying.absolute(row);
    }
    public boolean relative(int rows) throws SQLException {
      return _underlying.relative(rows);
    }
    public boolean previous() throws SQLException {
      return _underlying.previous();
    }
    public void setFetchDirection(int direction) throws SQLException {
      _underlying.setFetchDirection(direction);
    }
    public int getFetchDirection() throws SQLException {
      return _underlying.getFetchDirection();
    }
    public void setFetchSize(int rows) throws SQLException {
      _underlying.setFetchSize(rows);
    }
    public int getFetchSize() throws SQLException {
      return _underlying.getFetchSize();
    }
    public int getType() throws SQLException {
      return _underlying.getType();
    }
    public int getConcurrency() throws SQLException {
      return _underlying.getConcurrency();
    }
    public boolean rowUpdated() throws SQLException {
      return _underlying.rowUpdated();
    }
    public boolean rowInserted() throws SQLException {
      return _underlying.rowInserted();
    }
    public boolean rowDeleted() throws SQLException {
      return _underlying.rowDeleted();
    }
    public void updateNull(int columnIndex) throws SQLException {
      _underlying.updateNull(columnIndex);
    }
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
      _underlying.updateBoolean(columnIndex, x);
    }
    public void updateByte(int columnIndex, byte x) throws SQLException {
      _underlying.updateByte(columnIndex, x);
    }
    public void updateShort(int columnIndex, short x) throws SQLException {
      _underlying.updateShort(columnIndex, x);
    }
    public void updateInt(int columnIndex, int x) throws SQLException {
      _underlying.updateInt(columnIndex, x);
    }
    public void updateLong(int columnIndex, long x) throws SQLException {
      _underlying.updateLong(columnIndex, x);
    }
    public void updateFloat(int columnIndex, float x) throws SQLException {
      _underlying.updateFloat(columnIndex, x);
    }
    public void updateDouble(int columnIndex, double x) throws SQLException {
      _underlying.updateDouble(columnIndex, x);
    }
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
      _underlying.updateBigDecimal(columnIndex, x);
    }
    public void updateString(int columnIndex, String x) throws SQLException {
      _underlying.updateString(columnIndex, x);
    }
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
      _underlying.updateBytes(columnIndex, x);
    }
    public void updateDate(int columnIndex, Date x) throws SQLException {
      _underlying.updateDate(columnIndex, x);
    }
    public void updateTime(int columnIndex, Time x) throws SQLException {
      _underlying.updateTime(columnIndex, x);
    }
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
      _underlying.updateTimestamp(columnIndex, x);
    }
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
      _underlying.updateAsciiStream(columnIndex, x, length);
    }
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
      _underlying.updateBinaryStream(columnIndex, x, length);
    }
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
      _underlying.updateCharacterStream(columnIndex, x, length);
    }
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
      _underlying.updateObject(columnIndex, x, scaleOrLength);
    }
    public void updateObject(int columnIndex, Object x) throws SQLException {
      _underlying.updateObject(columnIndex, x);
    }
    public void updateNull(String columnLabel) throws SQLException {
      _underlying.updateNull(columnLabel);
    }
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
      _underlying.updateBoolean(columnLabel, x);
    }
    public void updateByte(String columnLabel, byte x) throws SQLException {
      _underlying.updateByte(columnLabel, x);
    }
    public void updateShort(String columnLabel, short x) throws SQLException {
      _underlying.updateShort(columnLabel, x);
    }
    public void updateInt(String columnLabel, int x) throws SQLException {
      _underlying.updateInt(columnLabel, x);
    }
    public void updateLong(String columnLabel, long x) throws SQLException {
      _underlying.updateLong(columnLabel, x);
    }
    public void updateFloat(String columnLabel, float x) throws SQLException {
      _underlying.updateFloat(columnLabel, x);
    }
    public void updateDouble(String columnLabel, double x) throws SQLException {
      _underlying.updateDouble(columnLabel, x);
    }
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
      _underlying.updateBigDecimal(columnLabel, x);
    }
    public void updateString(String columnLabel, String x) throws SQLException {
      _underlying.updateString(columnLabel, x);
    }
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
      _underlying.updateBytes(columnLabel, x);
    }
    public void updateDate(String columnLabel, Date x) throws SQLException {
      _underlying.updateDate(columnLabel, x);
    }
    public void updateTime(String columnLabel, Time x) throws SQLException {
      _underlying.updateTime(columnLabel, x);
    }
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
      _underlying.updateTimestamp(columnLabel, x);
    }
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
      _underlying.updateAsciiStream(columnLabel, x, length);
    }
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
      _underlying.updateBinaryStream(columnLabel, x, length);
    }
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
      _underlying.updateCharacterStream(columnLabel, reader, length);
    }
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
      _underlying.updateObject(columnLabel, x, scaleOrLength);
    }
    public void updateObject(String columnLabel, Object x) throws SQLException {
      _underlying.updateObject(columnLabel, x);
    }
    public void insertRow() throws SQLException {
      _underlying.insertRow();
    }
    public void updateRow() throws SQLException {
      _underlying.updateRow();
    }
    public void deleteRow() throws SQLException {
      _underlying.deleteRow();
    }
    public void refreshRow() throws SQLException {
      _underlying.refreshRow();
    }
    public void cancelRowUpdates() throws SQLException {
      _underlying.cancelRowUpdates();
    }
    public void moveToInsertRow() throws SQLException {
      _underlying.moveToInsertRow();
    }
    public void moveToCurrentRow() throws SQLException {
      _underlying.moveToCurrentRow();
    }
    public Statement getStatement() throws SQLException {
      return _underlying.getStatement();
    }
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
      return _underlying.getObject(columnIndex, map);
    }
    public Ref getRef(int columnIndex) throws SQLException {
      return _underlying.getRef(columnIndex);
    }
    public Blob getBlob(int columnIndex) throws SQLException {
      return _underlying.getBlob(columnIndex);
    }
    public Clob getClob(int columnIndex) throws SQLException {
      return _underlying.getClob(columnIndex);
    }
    public Array getArray(int columnIndex) throws SQLException {
      return _underlying.getArray(columnIndex);
    }
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
      return _underlying.getObject(columnLabel, map);
    }
    public Ref getRef(String columnLabel) throws SQLException {
      return _underlying.getRef(columnLabel);
    }
    public Blob getBlob(String columnLabel) throws SQLException {
      return _underlying.getBlob(columnLabel);
    }
    public Clob getClob(String columnLabel) throws SQLException {
      return _underlying.getClob(columnLabel);
    }
    public Array getArray(String columnLabel) throws SQLException {
      return _underlying.getArray(columnLabel);
    }
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
      return _underlying.getDate(columnIndex, cal);
    }
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
      return _underlying.getDate(columnLabel, cal);
    }
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
      return _underlying.getTime(columnIndex, cal);
    }
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
      return _underlying.getTime(columnLabel, cal);
    }
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
      return _underlying.getTimestamp(columnIndex, cal);
    }
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
      return _underlying.getTimestamp(columnLabel, cal);
    }
    public URL getURL(int columnIndex) throws SQLException {
      return _underlying.getURL(columnIndex);
    }
    public URL getURL(String columnLabel) throws SQLException {
      return _underlying.getURL(columnLabel);
    }
    public void updateRef(int columnIndex, Ref x) throws SQLException {
      _underlying.updateRef(columnIndex, x);
    }
    public void updateRef(String columnLabel, Ref x) throws SQLException {
      _underlying.updateRef(columnLabel, x);
    }
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
      _underlying.updateBlob(columnIndex, x);
    }
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
      _underlying.updateBlob(columnLabel, x);
    }
    public void updateClob(int columnIndex, Clob x) throws SQLException {
      _underlying.updateClob(columnIndex, x);
    }
    public void updateClob(String columnLabel, Clob x) throws SQLException {
      _underlying.updateClob(columnLabel, x);
    }
    public void updateArray(int columnIndex, Array x) throws SQLException {
      _underlying.updateArray(columnIndex, x);
    }
    public void updateArray(String columnLabel, Array x) throws SQLException {
      _underlying.updateArray(columnLabel, x);
    }
    public RowId getRowId(int columnIndex) throws SQLException {
      return _underlying.getRowId(columnIndex);
    }
    public RowId getRowId(String columnLabel) throws SQLException {
      return _underlying.getRowId(columnLabel);
    }
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
      _underlying.updateRowId(columnIndex, x);
    }
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
      _underlying.updateRowId(columnLabel, x);
    }
    public int getHoldability() throws SQLException {
      return _underlying.getHoldability();
    }
    public boolean isClosed() throws SQLException {
      return _underlying.isClosed();
    }
    public void updateNString(int columnIndex, String nString) throws SQLException {
      _underlying.updateNString(columnIndex, nString);
    }
    public void updateNString(String columnLabel, String nString) throws SQLException {
      _underlying.updateNString(columnLabel, nString);
    }
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
      _underlying.updateNClob(columnIndex, nClob);
    }
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
      _underlying.updateNClob(columnLabel, nClob);
    }
    public NClob getNClob(int columnIndex) throws SQLException {
      return _underlying.getNClob(columnIndex);
    }
    public NClob getNClob(String columnLabel) throws SQLException {
      return _underlying.getNClob(columnLabel);
    }
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
      return _underlying.getSQLXML(columnIndex);
    }
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
      return _underlying.getSQLXML(columnLabel);
    }
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
      _underlying.updateSQLXML(columnIndex, xmlObject);
    }
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
      _underlying.updateSQLXML(columnLabel, xmlObject);
    }
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
      return _underlying.getNCharacterStream(columnIndex);
    }
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
      return _underlying.getNCharacterStream(columnLabel);
    }
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
      _underlying.updateNCharacterStream(columnIndex, x, length);
    }
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
      _underlying.updateNCharacterStream(columnLabel, reader, length);
    }
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
      _underlying.updateAsciiStream(columnIndex, x, length);
    }
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
      _underlying.updateBinaryStream(columnIndex, x, length);
    }
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
      _underlying.updateCharacterStream(columnIndex, x, length);
    }
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
      _underlying.updateAsciiStream(columnLabel, x, length);
    }
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
      _underlying.updateBinaryStream(columnLabel, x, length);
    }
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
      _underlying.updateCharacterStream(columnLabel, reader, length);
    }
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
      _underlying.updateBlob(columnIndex, inputStream, length);
    }
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
      _underlying.updateBlob(columnLabel, inputStream, length);
    }
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
      _underlying.updateClob(columnIndex, reader, length);
    }
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
      _underlying.updateClob(columnLabel, reader, length);
    }
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
      _underlying.updateNClob(columnIndex, reader, length);
    }
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
      _underlying.updateNClob(columnLabel, reader, length);
    }
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
      _underlying.updateNCharacterStream(columnIndex, x);
    }
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
      _underlying.updateNCharacterStream(columnLabel, reader);
    }
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
      _underlying.updateAsciiStream(columnIndex, x);
    }
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
      _underlying.updateBinaryStream(columnIndex, x);
    }
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
      _underlying.updateCharacterStream(columnIndex, x);
    }
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
      _underlying.updateAsciiStream(columnLabel, x);
    }
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
      _underlying.updateBinaryStream(columnLabel, x);
    }
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
      _underlying.updateCharacterStream(columnLabel, reader);
    }
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
      _underlying.updateBlob(columnIndex, inputStream);
    }
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
      _underlying.updateBlob(columnLabel, inputStream);
    }
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
      _underlying.updateClob(columnIndex, reader);
    }
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
      _underlying.updateClob(columnLabel, reader);
    }
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
      _underlying.updateNClob(columnIndex, reader);
    }
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
      _underlying.updateNClob(columnLabel, reader);
    }
  }

}
