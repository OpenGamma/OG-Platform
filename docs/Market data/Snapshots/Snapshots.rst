Market data snapshots
=====================

A snapshot is a set of market data that can be used as a source of data for a view. There are a number of snapshot
types available, some of which are specialised.

Snapshots implement the ``com.opengamma.core.marketdatasnapshot.NamedSnapshot`` interface. Some implementations
of this are:

Market data quotes
------------------

``com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot`` implements a store for unstructured market data.
Generally this would be comprised of instrument tickers and market values.
The UnstructuredMarketDataSnapshot is generally contained within a StructuredMarketDataSnapshot.

Snapshot masters
----------------

To provide a snapshot into a view it must be persisted into a snapshot master.

Java code
---------

The following code sample shows an example of creating a snapshot of market data that the engine will use to generate a
curve from.

.. code-block:: java

 // create empty unstructured snapshot
 ManageableUnstructuredMarketDataSnapshot unstructured = new ManageableUnstructuredMarketDataSnapshot();
 // fill with tickers and values.
 unstructured.putValue(ExternalId.of("BLOOMBERG_TICKER", "BPDR1T Curncy", MarketDataRequirementNames.MARKET_VALUE, ValueSnapshot.of(0.001);
 unstructured.putValue(ExternalId.of("BLOOMBERG_TICKER", "BPSWSA Curncy", MarketDataRequirementNames.MARKET_VALUE, ValueSnapshot.of(0.002);
 // form a structured snapshot containing the unstructured data (name is optional and only to aid display).
 StructuredMarketDataSnapshot snapshot = new ManageableMarketDataSnapshot("my gbp curve snapshot data", unstructured, null);
 // optionally set the time that snapshot was taken
 snapshot.setValuationTime(Instant.now());

 // persist to a snapshot master
 MarketDataSnapshotMaster snapMaster = new InMemorySnapshotMaster();
 MarketDataSnapshotDocument doc = snapMaster.add(new MarketDataSnapshotDocument(snapshot);
 System.out.println("Persisted snapshot with id " + doc.getUniqueId()); 


Serialisation format
--------------------

Snapshots have a number of serialisation formats available. An excel (xls) sheet format is available but is not described here.

A csv format is available, and looks like:

::

  type,external id bundle,market value,value name,name
  name,,,,my gbp curve snapshot data
  global values,BLOOMBERG_TICKER~BPDR1T Curncy,0.001,Market_Value,
  global values,BLOOMBERG_TICKER~BPSWSA Curncy,0.002,Market_Value,



This snapshot can be read and written by the ``com.opengamma.integration.copier.snapshot.reader.SnapshotReader``,
``com.opengamma.integration.copier.snapshot.writer.SnapshotWriter`` & ``com.opengamma.integration.copier.snapshot.copier.SnapshotCopier`` interfaces.

The csv snippet above could be read or created with the following code:

.. code-block:: java

 // persist a snapshot to a csv file
 SnapshotReader snapshotReader = new MasterSnapshotReader(doc.getUniqueId(), snapMaster);
 SnapshotWriter snapshotWriter = new CsvSnapshotWriter("snapshot.csv");
 SnapshotCopier snapshotCopier = new SimpleSnapshotCopier();
 snapshotCopier.copy(snapshotReader, snapshotWriter);
 // load a csv file into a snapshot
 SnapshotReaer csvSnapshotReader = new CsvSnapshotReader("snapshot.csv");
 SnapshotWriter snapshotMasterWriter = new MasterSnapshotWriter(snapMaster);
 snapshotCopier.copy(csvSnapshotReader, snapshotMasterWriter); 


The command line tools ``market-data-snapshot-import-tool.sh`` and ``market-data-snapshot-export-tool.sh`` use similar code to provide loading and exporting functionality on the command line.

Credit curve data
-----------------

Used to hold credit instruments comprising a credit curve. For details see `ISDA Curves documentation`_.

Curve, volatility surface & cube market data
--------------------------------------------

``com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot`` implementations can hold complex data types
including curves, surfaces & cubes. These are not discussed in detail as support for them is incomplete.

.. _ISDA Curves documentation: ../Product types/credit/ISDA Curves.rst

