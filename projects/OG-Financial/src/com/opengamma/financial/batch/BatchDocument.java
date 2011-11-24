/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import javax.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A document used to pass into and out of the batch master.
 * <p>
 * This class is mutable and not thread-safe.
 */
@BeanDefinition
public class BatchDocument extends DirectBean {

  /**
   * The batch unique identifier.
   * This field is managed by the master but must be set for updates.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;
  /**
   * The batch valuation time, not null.
   */
  @PropertyDefinition
  private Instant _valuationTime;
  /**
   * The status of the batch, determining if it is running, not null.
   */
  @PropertyDefinition
  private BatchStatus _status;
  /**
   * The instant that the batch run was first created, not null.
   */
  @PropertyDefinition
  private Instant _creationInstant;
  /**
   * The instant that the batch run started, not null.
   */
  @PropertyDefinition
  private Instant _startInstant;
  /**
   * The instant that the batch run ended, not null.
   */
  @PropertyDefinition
  private Instant _endInstant;
  /**
   * The number of restarts.
   */
  @PropertyDefinition
  private int _numRestarts;
  /**
   * The paging information for the main batch data, not null if correctly created.
   */
  @PropertyDefinition
  private Paging _dataPaging;
  /**
   * The paged list of main batch data, may be empty, not null.
   */
  @PropertyDefinition
  private final List<ViewResultEntry> _data = new ArrayList<ViewResultEntry>();
  /**
   * The paging information for the errors, not null if correctly created.
   */
  @PropertyDefinition
  private Paging _errorsPaging;
  /**
   * The paged list of errors, may be empty, not null.
   */
  @PropertyDefinition
  private final List<BatchError> _errors = new ArrayList<BatchError>();
  /**
   * The unique id of market data snapshot, not null.
   */
  @PropertyDefinition
  private UniqueId _marketDataSnapshotUid;
    /**
   * The unique id of view definition, not null.
   */
  @PropertyDefinition
  private UniqueId _viewDefinitionUid;
  /**
   * The unique id of view definition, not null.
   */
  @PropertyDefinition
  private VersionCorrection _versionCorrection;
  
  
  public final String BATCH_DOCUMENT_UNIQUE_ID_DELIMITER = "-";

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbBat";

  /**
   * Creates an instance.
   */
  public BatchDocument() {
  }

  /**
   * Creates an instance specifying a unique identifier.
   *
   * @param uniqueId  the batch unique identifier, not null
   */
  public BatchDocument(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(uniqueId.getValue(), BATCH_DOCUMENT_UNIQUE_ID_DELIMITER);
    if (split.length == 4 && uniqueId.getScheme().equals(IDENTIFIER_SCHEME_DEFAULT)) {
      setUniqueId(uniqueId);
      setViewDefinitionUid(UniqueId.parse(split[0]));
      setMarketDataSnapshotUid(UniqueId.parse(split[1]));
      setValuationTime(Instant.parse(split[2]));
      setVersionCorrection(VersionCorrection.parse(split[3]));
    } else {
      throw new IllegalArgumentException("Invalid identifier format: " + uniqueId.getValue());
    }
  }

  /**
   * Creates an instance specifying a unique identifier.
   * 
   * @param uniqueId  the batch unique identifier, not null
   */
  public BatchDocument(UniqueId viewDefinitionUid, UniqueId marketDataSnapshotUid, Instant valuationTime, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(viewDefinitionUid, "viewDefinitionUid");
    ArgumentChecker.notNull(marketDataSnapshotUid, "marketDataSnapshotUid");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    setViewDefinitionUid(viewDefinitionUid);
    setMarketDataSnapshotUid(marketDataSnapshotUid);
    setValuationTime(valuationTime);
    setVersionCorrection(versionCorrection);
    setUniqueId(createUniqueId(viewDefinitionUid, marketDataSnapshotUid, valuationTime, versionCorrection));
  }
  
  private UniqueId createUniqueId(UniqueId viewDefinitionUid, UniqueId marketDataSnapshotUid, Instant valuationTime, VersionCorrection versionCorrection) {
    return UniqueId.of(IDENTIFIER_SCHEME_DEFAULT, viewDefinitionUid + "-" + marketDataSnapshotUid + "-" + valuationTime + "-" + versionCorrection);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the unique entry by {@code ComputationTargetSpecification}.
   * Mainly useful in tests because in general uniqueness cannot be assumed.
   * 
   * @param spec  the computation target, not null
   * @return only result in batch master for this computation target, null if not found
   * @throws IllegalArgumentException if there is more than 1 entry in batch master for the
   *  given computation target
   */
  public ViewResultEntry getData(ComputationTargetSpecification spec) {
    ViewResultEntry result = null;
    for (ViewResultEntry item : _data) {
      ComputationTargetSpecification match = item.getComputedValue().getSpecification().getTargetSpecification();
      if (spec.equals(match)) {
        if (result != null) {
          throw new IllegalArgumentException("More than 1 entry in batch DB for " + spec);
        }
        result = item;
      }
    }
    return result;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BatchDocument}.
   * @return the meta-bean, not null
   */
  public static BatchDocument.Meta meta() {
    return BatchDocument.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(BatchDocument.Meta.INSTANCE);
  }

  @Override
  public BatchDocument.Meta metaBean() {
    return BatchDocument.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case 113591406:  // valuationTime
        return getValuationTime();
      case -892481550:  // status
        return getStatus();
      case -961305086:  // creationInstant
        return getCreationInstant();
      case 1823123231:  // startInstant
        return getStartInstant();
      case -2109892474:  // endInstant
        return getEndInstant();
      case -1329836566:  // numRestarts
        return getNumRestarts();
      case 1173228502:  // dataPaging
        return getDataPaging();
      case 3076010:  // data
        return getData();
      case -49547561:  // errorsPaging
        return getErrorsPaging();
      case -1294635157:  // errors
        return getErrors();
      case -1278853626:  // marketDataSnapshotUid
        return getMarketDataSnapshotUid();
      case 276749144:  // viewDefinitionUid
        return getViewDefinitionUid();
      case -2031293866:  // versionCorrection
        return getVersionCorrection();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueId) newValue);
        return;
      case 113591406:  // valuationTime
        setValuationTime((Instant) newValue);
        return;
      case -892481550:  // status
        setStatus((BatchStatus) newValue);
        return;
      case -961305086:  // creationInstant
        setCreationInstant((Instant) newValue);
        return;
      case 1823123231:  // startInstant
        setStartInstant((Instant) newValue);
        return;
      case -2109892474:  // endInstant
        setEndInstant((Instant) newValue);
        return;
      case -1329836566:  // numRestarts
        setNumRestarts((Integer) newValue);
        return;
      case 1173228502:  // dataPaging
        setDataPaging((Paging) newValue);
        return;
      case 3076010:  // data
        setData((List<ViewResultEntry>) newValue);
        return;
      case -49547561:  // errorsPaging
        setErrorsPaging((Paging) newValue);
        return;
      case -1294635157:  // errors
        setErrors((List<BatchError>) newValue);
        return;
      case -1278853626:  // marketDataSnapshotUid
        setMarketDataSnapshotUid((UniqueId) newValue);
        return;
      case 276749144:  // viewDefinitionUid
        setViewDefinitionUid((UniqueId) newValue);
        return;
      case -2031293866:  // versionCorrection
        setVersionCorrection((VersionCorrection) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BatchDocument other = (BatchDocument) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getValuationTime(), other.getValuationTime()) &&
          JodaBeanUtils.equal(getStatus(), other.getStatus()) &&
          JodaBeanUtils.equal(getCreationInstant(), other.getCreationInstant()) &&
          JodaBeanUtils.equal(getStartInstant(), other.getStartInstant()) &&
          JodaBeanUtils.equal(getEndInstant(), other.getEndInstant()) &&
          JodaBeanUtils.equal(getNumRestarts(), other.getNumRestarts()) &&
          JodaBeanUtils.equal(getDataPaging(), other.getDataPaging()) &&
          JodaBeanUtils.equal(getData(), other.getData()) &&
          JodaBeanUtils.equal(getErrorsPaging(), other.getErrorsPaging()) &&
          JodaBeanUtils.equal(getErrors(), other.getErrors()) &&
          JodaBeanUtils.equal(getMarketDataSnapshotUid(), other.getMarketDataSnapshotUid()) &&
          JodaBeanUtils.equal(getViewDefinitionUid(), other.getViewDefinitionUid()) &&
          JodaBeanUtils.equal(getVersionCorrection(), other.getVersionCorrection());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValuationTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStatus());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCreationInstant());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStartInstant());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEndInstant());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNumRestarts());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataPaging());
    hash += hash * 31 + JodaBeanUtils.hashCode(getData());
    hash += hash * 31 + JodaBeanUtils.hashCode(getErrorsPaging());
    hash += hash * 31 + JodaBeanUtils.hashCode(getErrors());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotUid());
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewDefinitionUid());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVersionCorrection());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the batch unique identifier.
   * This field is managed by the master but must be set for updates.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the batch unique identifier.
   * This field is managed by the master but must be set for updates.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This field is managed by the master but must be set for updates.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the batch valuation time, not null.
   * @return the value of the property
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * Sets the batch valuation time, not null.
   * @param valuationTime  the new value of the property
   */
  public void setValuationTime(Instant valuationTime) {
    this._valuationTime = valuationTime;
  }

  /**
   * Gets the the {@code valuationTime} property.
   * @return the property, not null
   */
  public final Property<Instant> valuationTime() {
    return metaBean().valuationTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the status of the batch, determining if it is running, not null.
   * @return the value of the property
   */
  public BatchStatus getStatus() {
    return _status;
  }

  /**
   * Sets the status of the batch, determining if it is running, not null.
   * @param status  the new value of the property
   */
  public void setStatus(BatchStatus status) {
    this._status = status;
  }

  /**
   * Gets the the {@code status} property.
   * @return the property, not null
   */
  public final Property<BatchStatus> status() {
    return metaBean().status().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant that the batch run was first created, not null.
   * @return the value of the property
   */
  public Instant getCreationInstant() {
    return _creationInstant;
  }

  /**
   * Sets the instant that the batch run was first created, not null.
   * @param creationInstant  the new value of the property
   */
  public void setCreationInstant(Instant creationInstant) {
    this._creationInstant = creationInstant;
  }

  /**
   * Gets the the {@code creationInstant} property.
   * @return the property, not null
   */
  public final Property<Instant> creationInstant() {
    return metaBean().creationInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant that the batch run started, not null.
   * @return the value of the property
   */
  public Instant getStartInstant() {
    return _startInstant;
  }

  /**
   * Sets the instant that the batch run started, not null.
   * @param startInstant  the new value of the property
   */
  public void setStartInstant(Instant startInstant) {
    this._startInstant = startInstant;
  }

  /**
   * Gets the the {@code startInstant} property.
   * @return the property, not null
   */
  public final Property<Instant> startInstant() {
    return metaBean().startInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant that the batch run ended, not null.
   * @return the value of the property
   */
  public Instant getEndInstant() {
    return _endInstant;
  }

  /**
   * Sets the instant that the batch run ended, not null.
   * @param endInstant  the new value of the property
   */
  public void setEndInstant(Instant endInstant) {
    this._endInstant = endInstant;
  }

  /**
   * Gets the the {@code endInstant} property.
   * @return the property, not null
   */
  public final Property<Instant> endInstant() {
    return metaBean().endInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of restarts.
   * @return the value of the property
   */
  public int getNumRestarts() {
    return _numRestarts;
  }

  /**
   * Sets the number of restarts.
   * @param numRestarts  the new value of the property
   */
  public void setNumRestarts(int numRestarts) {
    this._numRestarts = numRestarts;
  }

  /**
   * Gets the the {@code numRestarts} property.
   * @return the property, not null
   */
  public final Property<Integer> numRestarts() {
    return metaBean().numRestarts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the paging information for the main batch data, not null if correctly created.
   * @return the value of the property
   */
  public Paging getDataPaging() {
    return _dataPaging;
  }

  /**
   * Sets the paging information for the main batch data, not null if correctly created.
   * @param dataPaging  the new value of the property
   */
  public void setDataPaging(Paging dataPaging) {
    this._dataPaging = dataPaging;
  }

  /**
   * Gets the the {@code dataPaging} property.
   * @return the property, not null
   */
  public final Property<Paging> dataPaging() {
    return metaBean().dataPaging().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the paged list of main batch data, may be empty, not null.
   * @return the value of the property
   */
  public List<ViewResultEntry> getData() {
    return _data;
  }

  /**
   * Sets the paged list of main batch data, may be empty, not null.
   * @param data  the new value of the property
   */
  public void setData(List<ViewResultEntry> data) {
    this._data.clear();
    this._data.addAll(data);
  }

  /**
   * Gets the the {@code data} property.
   * @return the property, not null
   */
  public final Property<List<ViewResultEntry>> data() {
    return metaBean().data().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the paging information for the errors, not null if correctly created.
   * @return the value of the property
   */
  public Paging getErrorsPaging() {
    return _errorsPaging;
  }

  /**
   * Sets the paging information for the errors, not null if correctly created.
   * @param errorsPaging  the new value of the property
   */
  public void setErrorsPaging(Paging errorsPaging) {
    this._errorsPaging = errorsPaging;
  }

  /**
   * Gets the the {@code errorsPaging} property.
   * @return the property, not null
   */
  public final Property<Paging> errorsPaging() {
    return metaBean().errorsPaging().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the paged list of errors, may be empty, not null.
   * @return the value of the property
   */
  public List<BatchError> getErrors() {
    return _errors;
  }

  /**
   * Sets the paged list of errors, may be empty, not null.
   * @param errors  the new value of the property
   */
  public void setErrors(List<BatchError> errors) {
    this._errors.clear();
    this._errors.addAll(errors);
  }

  /**
   * Gets the the {@code errors} property.
   * @return the property, not null
   */
  public final Property<List<BatchError>> errors() {
    return metaBean().errors().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique id of market data snapshot, not null.
   * @return the value of the property
   */
  public UniqueId getMarketDataSnapshotUid() {
    return _marketDataSnapshotUid;
  }

  /**
   * Sets the unique id of market data snapshot, not null.
   * @param marketDataSnapshotUid  the new value of the property
   */
  public void setMarketDataSnapshotUid(UniqueId marketDataSnapshotUid) {
    this._marketDataSnapshotUid = marketDataSnapshotUid;
  }

  /**
   * Gets the the {@code marketDataSnapshotUid} property.
   * @return the property, not null
   */
  public final Property<UniqueId> marketDataSnapshotUid() {
    return metaBean().marketDataSnapshotUid().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique id of view definition, not null.
   * @return the value of the property
   */
  public UniqueId getViewDefinitionUid() {
    return _viewDefinitionUid;
  }

  /**
   * Sets the unique id of view definition, not null.
   * @param viewDefinitionUid  the new value of the property
   */
  public void setViewDefinitionUid(UniqueId viewDefinitionUid) {
    this._viewDefinitionUid = viewDefinitionUid;
  }

  /**
   * Gets the the {@code viewDefinitionUid} property.
   * @return the property, not null
   */
  public final Property<UniqueId> viewDefinitionUid() {
    return metaBean().viewDefinitionUid().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique id of view definition, not null.
   * @return the value of the property
   */
  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  /**
   * Sets the unique id of view definition, not null.
   * @param versionCorrection  the new value of the property
   */
  public void setVersionCorrection(VersionCorrection versionCorrection) {
    this._versionCorrection = versionCorrection;
  }

  /**
   * Gets the the {@code versionCorrection} property.
   * @return the property, not null
   */
  public final Property<VersionCorrection> versionCorrection() {
    return metaBean().versionCorrection().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BatchDocument}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", BatchDocument.class, UniqueId.class);
    /**
     * The meta-property for the {@code valuationTime} property.
     */
    private final MetaProperty<Instant> _valuationTime = DirectMetaProperty.ofReadWrite(
        this, "valuationTime", BatchDocument.class, Instant.class);
    /**
     * The meta-property for the {@code status} property.
     */
    private final MetaProperty<BatchStatus> _status = DirectMetaProperty.ofReadWrite(
        this, "status", BatchDocument.class, BatchStatus.class);
    /**
     * The meta-property for the {@code creationInstant} property.
     */
    private final MetaProperty<Instant> _creationInstant = DirectMetaProperty.ofReadWrite(
        this, "creationInstant", BatchDocument.class, Instant.class);
    /**
     * The meta-property for the {@code startInstant} property.
     */
    private final MetaProperty<Instant> _startInstant = DirectMetaProperty.ofReadWrite(
        this, "startInstant", BatchDocument.class, Instant.class);
    /**
     * The meta-property for the {@code endInstant} property.
     */
    private final MetaProperty<Instant> _endInstant = DirectMetaProperty.ofReadWrite(
        this, "endInstant", BatchDocument.class, Instant.class);
    /**
     * The meta-property for the {@code numRestarts} property.
     */
    private final MetaProperty<Integer> _numRestarts = DirectMetaProperty.ofReadWrite(
        this, "numRestarts", BatchDocument.class, Integer.TYPE);
    /**
     * The meta-property for the {@code dataPaging} property.
     */
    private final MetaProperty<Paging> _dataPaging = DirectMetaProperty.ofReadWrite(
        this, "dataPaging", BatchDocument.class, Paging.class);
    /**
     * The meta-property for the {@code data} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ViewResultEntry>> _data = DirectMetaProperty.ofReadWrite(
        this, "data", BatchDocument.class, (Class) List.class);
    /**
     * The meta-property for the {@code errorsPaging} property.
     */
    private final MetaProperty<Paging> _errorsPaging = DirectMetaProperty.ofReadWrite(
        this, "errorsPaging", BatchDocument.class, Paging.class);
    /**
     * The meta-property for the {@code errors} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<BatchError>> _errors = DirectMetaProperty.ofReadWrite(
        this, "errors", BatchDocument.class, (Class) List.class);
    /**
     * The meta-property for the {@code marketDataSnapshotUid} property.
     */
    private final MetaProperty<UniqueId> _marketDataSnapshotUid = DirectMetaProperty.ofReadWrite(
        this, "marketDataSnapshotUid", BatchDocument.class, UniqueId.class);
    /**
     * The meta-property for the {@code viewDefinitionUid} property.
     */
    private final MetaProperty<UniqueId> _viewDefinitionUid = DirectMetaProperty.ofReadWrite(
        this, "viewDefinitionUid", BatchDocument.class, UniqueId.class);
    /**
     * The meta-property for the {@code versionCorrection} property.
     */
    private final MetaProperty<VersionCorrection> _versionCorrection = DirectMetaProperty.ofReadWrite(
        this, "versionCorrection", BatchDocument.class, VersionCorrection.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "valuationTime",
        "status",
        "creationInstant",
        "startInstant",
        "endInstant",
        "numRestarts",
        "dataPaging",
        "data",
        "errorsPaging",
        "errors",
        "marketDataSnapshotUid",
        "viewDefinitionUid",
        "versionCorrection");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case 113591406:  // valuationTime
          return _valuationTime;
        case -892481550:  // status
          return _status;
        case -961305086:  // creationInstant
          return _creationInstant;
        case 1823123231:  // startInstant
          return _startInstant;
        case -2109892474:  // endInstant
          return _endInstant;
        case -1329836566:  // numRestarts
          return _numRestarts;
        case 1173228502:  // dataPaging
          return _dataPaging;
        case 3076010:  // data
          return _data;
        case -49547561:  // errorsPaging
          return _errorsPaging;
        case -1294635157:  // errors
          return _errors;
        case -1278853626:  // marketDataSnapshotUid
          return _marketDataSnapshotUid;
        case 276749144:  // viewDefinitionUid
          return _viewDefinitionUid;
        case -2031293866:  // versionCorrection
          return _versionCorrection;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BatchDocument> builder() {
      return new DirectBeanBuilder<BatchDocument>(new BatchDocument());
    }

    @Override
    public Class<? extends BatchDocument> beanType() {
      return BatchDocument.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code valuationTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> valuationTime() {
      return _valuationTime;
    }

    /**
     * The meta-property for the {@code status} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BatchStatus> status() {
      return _status;
    }

    /**
     * The meta-property for the {@code creationInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> creationInstant() {
      return _creationInstant;
    }

    /**
     * The meta-property for the {@code startInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> startInstant() {
      return _startInstant;
    }

    /**
     * The meta-property for the {@code endInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> endInstant() {
      return _endInstant;
    }

    /**
     * The meta-property for the {@code numRestarts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> numRestarts() {
      return _numRestarts;
    }

    /**
     * The meta-property for the {@code dataPaging} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Paging> dataPaging() {
      return _dataPaging;
    }

    /**
     * The meta-property for the {@code data} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ViewResultEntry>> data() {
      return _data;
    }

    /**
     * The meta-property for the {@code errorsPaging} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Paging> errorsPaging() {
      return _errorsPaging;
    }

    /**
     * The meta-property for the {@code errors} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<BatchError>> errors() {
      return _errors;
    }

    /**
     * The meta-property for the {@code marketDataSnapshotUid} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> marketDataSnapshotUid() {
      return _marketDataSnapshotUid;
    }

    /**
     * The meta-property for the {@code viewDefinitionUid} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> viewDefinitionUid() {
      return _viewDefinitionUid;
    }

    /**
     * The meta-property for the {@code versionCorrection} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<VersionCorrection> versionCorrection() {
      return _versionCorrection;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
