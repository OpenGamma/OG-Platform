// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.calcnode.msg;
public class Invocations extends com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage implements java.io.Serializable {
  public void accept (RemoteCalcNodeMessageVisitor visitor) { visitor.visitInvocationsMessage (this); }
  private static final long serialVersionUID = 61775777385l;
  public static class PerConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 55882320992411l;
    public static class PerFunction implements java.io.Serializable {
      private static final long serialVersionUID = -2696372187157615071l;
      private String _identifier;
      public static final String IDENTIFIER_KEY = "identifier";
      private int _count;
      public static final String COUNT_KEY = "count";
      private double _invocation;
      public static final String INVOCATION_KEY = "invocation";
      private double _dataInput;
      public static final String DATA_INPUT_KEY = "dataInput";
      private double _dataOutput;
      public static final String DATA_OUTPUT_KEY = "dataOutput";
      public PerFunction (String identifier, int count, double invocation, double dataInput, double dataOutput) {
        if (identifier == null) throw new NullPointerException ("identifier' cannot be null");
        _identifier = identifier;
        _count = count;
        _invocation = invocation;
        _dataInput = dataInput;
        _dataOutput = dataOutput;
      }
      protected PerFunction (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
        org.fudgemsg.FudgeField fudgeField;
        fudgeField = fudgeMsg.getByName (IDENTIFIER_KEY);
        if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PerFunction - field 'identifier' is not present");
        try {
          _identifier = fudgeField.getValue ().toString ();
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a PerFunction - field 'identifier' is not string", e);
        }
        fudgeField = fudgeMsg.getByName (COUNT_KEY);
        if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PerFunction - field 'count' is not present");
        try {
          _count = fudgeMsg.getFieldValue (Integer.class, fudgeField);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a PerFunction - field 'count' is not integer", e);
        }
        fudgeField = fudgeMsg.getByName (INVOCATION_KEY);
        if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PerFunction - field 'invocation' is not present");
        try {
          _invocation = fudgeMsg.getFieldValue (Double.class, fudgeField);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a PerFunction - field 'invocation' is not double", e);
        }
        fudgeField = fudgeMsg.getByName (DATA_INPUT_KEY);
        if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PerFunction - field 'dataInput' is not present");
        try {
          _dataInput = fudgeMsg.getFieldValue (Double.class, fudgeField);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a PerFunction - field 'dataInput' is not double", e);
        }
        fudgeField = fudgeMsg.getByName (DATA_OUTPUT_KEY);
        if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PerFunction - field 'dataOutput' is not present");
        try {
          _dataOutput = fudgeMsg.getFieldValue (Double.class, fudgeField);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a PerFunction - field 'dataOutput' is not double", e);
        }
      }
      protected PerFunction (final PerFunction source) {
        if (source == null) throw new NullPointerException ("'source' must not be null");
        _identifier = source._identifier;
        _count = source._count;
        _invocation = source._invocation;
        _dataInput = source._dataInput;
        _dataOutput = source._dataOutput;
      }
      public PerFunction clone () {
        return new PerFunction (this);
      }
      public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
        if (serializer == null) throw new NullPointerException ("serializer must not be null");
        final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
        toFudgeMsg (serializer, msg);
        return msg;
      }
      public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
        if (_identifier != null)  {
          msg.add (IDENTIFIER_KEY, null, _identifier);
        }
        msg.add (COUNT_KEY, null, _count);
        msg.add (INVOCATION_KEY, null, _invocation);
        msg.add (DATA_INPUT_KEY, null, _dataInput);
        msg.add (DATA_OUTPUT_KEY, null, _dataOutput);
      }
      public static PerFunction fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
        final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
        for (org.fudgemsg.FudgeField field : types) {
          final String className = (String)field.getValue ();
          if ("com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction".equals (className)) break;
          try {
            return (com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
          }
          catch (Throwable t) {
            // no-action
          }
        }
        return new PerFunction (deserializer, fudgeMsg);
      }
      public String getIdentifier () {
        return _identifier;
      }
      public void setIdentifier (String identifier) {
        if (identifier == null) throw new NullPointerException ("identifier' cannot be null");
        _identifier = identifier;
      }
      public int getCount () {
        return _count;
      }
      public void setCount (int count) {
        _count = count;
      }
      public double getInvocation () {
        return _invocation;
      }
      public void setInvocation (double invocation) {
        _invocation = invocation;
      }
      public double getDataInput () {
        return _dataInput;
      }
      public void setDataInput (double dataInput) {
        _dataInput = dataInput;
      }
      public double getDataOutput () {
        return _dataOutput;
      }
      public void setDataOutput (double dataOutput) {
        _dataOutput = dataOutput;
      }
      public String toString () {
        return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
      }
    }
    private String _configuration;
    public static final String CONFIGURATION_KEY = "configuration";
    private java.util.List<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> _function;
    public static final String FUNCTION_KEY = "function";
    public PerConfiguration (String configuration, java.util.Collection<? extends com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> function) {
      if (configuration == null) throw new NullPointerException ("configuration' cannot be null");
      _configuration = configuration;
      if (function == null) throw new NullPointerException ("'function' cannot be null");
      else {
        final java.util.List<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> fudge0 = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> (function);
        if (function.size () == 0) throw new IllegalArgumentException ("'function' cannot be an empty list");
        for (java.util.ListIterator<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
          com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction fudge2 = fudge1.next ();
          if (fudge2 == null) throw new NullPointerException ("List element of 'function' cannot be null");
          fudge1.set ((com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction)fudge2.clone ());
        }
        _function = fudge0;
      }
    }
    protected PerConfiguration (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
      org.fudgemsg.FudgeField fudgeField;
      java.util.List<org.fudgemsg.FudgeField> fudgeFields;
      fudgeField = fudgeMsg.getByName (CONFIGURATION_KEY);
      if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PerConfiguration - field 'configuration' is not present");
      try {
        _configuration = fudgeField.getValue ().toString ();
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a PerConfiguration - field 'configuration' is not string", e);
      }
      fudgeFields = fudgeMsg.getAllByName (FUNCTION_KEY);
      if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a PerConfiguration - field 'function' is not present");
      _function = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
        try {
          final com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction fudge2;
          fudge2 = com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge1));
          _function.add (fudge2);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a PerConfiguration - field 'function' is not PerFunction message", e);
        }
      }
    }
    protected PerConfiguration (final PerConfiguration source) {
      if (source == null) throw new NullPointerException ("'source' must not be null");
      _configuration = source._configuration;
      if (source._function == null) _function = null;
      else {
        final java.util.List<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> fudge0 = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> (source._function);
        for (java.util.ListIterator<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
          com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction fudge2 = fudge1.next ();
          fudge1.set ((com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction)fudge2.clone ());
        }
        _function = fudge0;
      }
    }
    public PerConfiguration clone () {
      return new PerConfiguration (this);
    }
    public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
      if (serializer == null) throw new NullPointerException ("serializer must not be null");
      final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
      toFudgeMsg (serializer, msg);
      return msg;
    }
    public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
      if (_configuration != null)  {
        msg.add (CONFIGURATION_KEY, null, _configuration);
      }
      if (_function != null)  {
        for (com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction fudge1 : _function) {
          final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge1.getClass (), com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction.class);
          fudge1.toFudgeMsg (serializer, fudge2);
          msg.add (FUNCTION_KEY, null, fudge2);
        }
      }
    }
    public static PerConfiguration fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
      final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
      for (org.fudgemsg.FudgeField field : types) {
        final String className = (String)field.getValue ();
        if ("com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration".equals (className)) break;
        try {
          return (com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
        }
        catch (Throwable t) {
          // no-action
        }
      }
      return new PerConfiguration (deserializer, fudgeMsg);
    }
    public String getConfiguration () {
      return _configuration;
    }
    public void setConfiguration (String configuration) {
      if (configuration == null) throw new NullPointerException ("configuration' cannot be null");
      _configuration = configuration;
    }
    public java.util.List<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> getFunction () {
      return java.util.Collections.unmodifiableList (_function);
    }
    public void setFunction (com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction function) {
      if (function == null) throw new NullPointerException ("'function' cannot be null");
      else {
        _function = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> (1);
        addFunction (function);
      }
    }
    public void setFunction (java.util.Collection<? extends com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> function) {
      if (function == null) throw new NullPointerException ("'function' cannot be null");
      else {
        final java.util.List<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> fudge0 = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> (function);
        if (function.size () == 0) throw new IllegalArgumentException ("'function' cannot be an empty list");
        for (java.util.ListIterator<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
          com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction fudge2 = fudge1.next ();
          if (fudge2 == null) throw new NullPointerException ("List element of 'function' cannot be null");
          fudge1.set ((com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction)fudge2.clone ());
        }
        _function = fudge0;
      }
    }
    public void addFunction (com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction function) {
      if (function == null) throw new NullPointerException ("'function' cannot be null");
      if (_function == null) _function = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction> ();
      _function.add ((com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction)function.clone ());
    }
    public String toString () {
      return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }
  private java.util.List<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> _configuration;
  public static final String CONFIGURATION_KEY = "configuration";
  public Invocations (java.util.Collection<? extends com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> configuration) {
    if (configuration == null) throw new NullPointerException ("'configuration' cannot be null");
    else {
      final java.util.List<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> fudge0 = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> (configuration);
      if (configuration.size () == 0) throw new IllegalArgumentException ("'configuration' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'configuration' cannot be null");
        fudge1.set ((com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration)fudge2.clone ());
      }
      _configuration = fudge0;
    }
  }
  protected Invocations (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (CONFIGURATION_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a Invocations - field 'configuration' is not present");
    _configuration = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration fudge2;
        fudge2 = com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge1));
        _configuration.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Invocations - field 'configuration' is not PerConfiguration message", e);
      }
    }
  }
  protected Invocations (final Invocations source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._configuration == null) _configuration = null;
    else {
      final java.util.List<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> fudge0 = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> (source._configuration);
      for (java.util.ListIterator<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration fudge2 = fudge1.next ();
        fudge1.set ((com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration)fudge2.clone ());
      }
      _configuration = fudge0;
    }
  }
  public Invocations clone () {
    return new Invocations (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_configuration != null)  {
      for (com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration fudge1 : _configuration) {
        final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge1.getClass (), com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.class);
        fudge1.toFudgeMsg (serializer, fudge2);
        msg.add (CONFIGURATION_KEY, null, fudge2);
      }
    }
  }
  public static Invocations fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.calcnode.msg.Invocations".equals (className)) break;
      try {
        return (com.opengamma.engine.calcnode.msg.Invocations)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Invocations (deserializer, fudgeMsg);
  }
  public java.util.List<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> getConfiguration () {
    return java.util.Collections.unmodifiableList (_configuration);
  }
  public void setConfiguration (com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration configuration) {
    if (configuration == null) throw new NullPointerException ("'configuration' cannot be null");
    else {
      _configuration = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> (1);
      addConfiguration (configuration);
    }
  }
  public void setConfiguration (java.util.Collection<? extends com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> configuration) {
    if (configuration == null) throw new NullPointerException ("'configuration' cannot be null");
    else {
      final java.util.List<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> fudge0 = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> (configuration);
      if (configuration.size () == 0) throw new IllegalArgumentException ("'configuration' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'configuration' cannot be null");
        fudge1.set ((com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration)fudge2.clone ());
      }
      _configuration = fudge0;
    }
  }
  public void addConfiguration (com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration configuration) {
    if (configuration == null) throw new NullPointerException ("'configuration' cannot be null");
    if (_configuration == null) _configuration = new java.util.ArrayList<com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration> ();
    _configuration.add ((com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration)configuration.clone ());
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
