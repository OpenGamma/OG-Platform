===============
Currency Matrix
===============

The currency matrix is core to how FX rates are interpreted and used in OpenGamma. It configures:

* what market data id to use as the source for a currency pair. (e.g. ``GBP Curncy`` for ``GBP|USD``).
* how to interpret the market data quote (e.g. whether the quote is ``x`` or ``1/x`` for a quoted pair)
* how to triangulate cross rates

Specifying market data sources
==============================

Market data sources for FX quotes are configured on a per-currency-pair basis. These are elements in the
``valueReq`` section of the XML:

.. code:: xml
  
  <valueReq type="message">
    <!-- start currency pair GBP|USD -->
    <USD type="message">
      <GBP type="message">
        <valueName type="string">Market_Value</valueName>
        <computationTargetType type="string">PRIMITIVE</computationTargetType>
        <computationTargetIdentifier type="message">
           <ID type="message">
              <Scheme type="string">BLOOMBERG_TICKER</Scheme>
              <Value type="string">GBP Curncy</Value>
           </ID>
        </computationTargetIdentifier>
        <reciprocal type="boolean">false</reciprocal>
      </GBP>
    </USD>
    <!-- end currency pair GBP|USD -->
    
    <!-- more pairs -->
    
  </valueReq>
  
This XML snippet expresses that Bloomberg key ``GBP Curncy`` should be used as the source for quote ``GBP|USD``.
It is crucial that the base and counter currencies are specified at the correct level - counter on the 
outside and base on the inside. So for the ``GBP|USD`` example above, ``USD`` (counter) is on the outside 
and ``GBP`` on the inside.

Should the user prefer to specify this relationship the other way around, this can be done using the 
``reciprocal`` flag. For example, this snippet is semantically equivalent to the one above:

.. code:: xml
  
  <valueReq type="message">
    <!-- start currency pair GBP|USD -->
    <GBP type="message">
      <USD type="message">
        <valueName type="string">Market_Value</valueName>
        <computationTargetType type="string">PRIMITIVE</computationTargetType>
        <computationTargetIdentifier type="message">
           <ID type="message">
              <Scheme type="string">BLOOMBERG_TICKER</Scheme>
              <Value type="string">GBP Curncy</Value>
           </ID>
        </computationTargetIdentifier>
        <reciprocal type="boolean">true</reciprocal>
      </USD>
    </GBP>
    <!-- end currency pair GBP|USD -->
    
    <!-- more pairs -->
    
  </valueReq>

Specifying cross rates
======================

The triangulation of cross rates should also be configured in the currency matrix. These are included in
the ``crossConvert`` section as follows:

.. code:: xml
  
    <crossConvert type="message">
      <USD type="message">
        <CHF type="string">JPY</CHF>
        <CHF type="string">NZD</CHF>
         
        <!-- other USD-based cross rates -->
      </USD>
      
      <!-- other ccy-based cross rates -->
    </crossConvert>
    
This states that for ``CHF|JPY``, use ``USD|CHF`` and ``USD|JPY`` to infer the rate.