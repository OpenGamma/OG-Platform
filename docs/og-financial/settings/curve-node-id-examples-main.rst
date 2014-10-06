Curve Node ID: Examples - Main
=====================

For single currency settings, the names in the examples are **XXX-INS-ZZZ**. The **XXX** is the currency. The **INS** is a description of the instruments referenced. The last **ZZZ** is the data source.

For multi-currency settings, the names in the examples are **XXX-YYY-INS-ZZZ**. The **XXX** and **YYY** are the currencies. The **INS** is a description of the instruments referenced. The last **ZZZ** is the data source.

Common instruments are:

* Depo: Deposits
* Depo-T+1: Deposits starting at T+1; to be able to distinguish between ON and TN.
* XIBORyM: Instruments (Fixing, FRA, STIR Futures, and IRS) linked to a unique index with tenor **yM**.
* XIBOR-BS-yM-zM: Basis swaps between the index with tenor **yM** and tenor **zM**.
* FFF: Federal Funds futures
* FFS: Federal Funds swaps (Arithmetic Average of Overnight v LIBOR).
* FXSwap: Forex swap.
* XIBORxM-YIBORyM: Cross-currency instruments linked to to two indexes with tenors **xM** and **yM**. The market standard for most currency pairs is 3M for both legs.

USD
-----

* USD-Depo-BBG
* USD-Depo-T+1-BBG
* USD-FFF-FFS-BBG
* USD-LIBOR3M-BBG
* USD-LIBOR-BS-1M-3M-BBG
* USD-LIBOR-BS-3M-6M-BBG
* USD-LIBOR1M-BBG
* USD-LIBOR3M-BBG
* USD-LIBOR6M-BBG

GBP
-----

* GBP-LIBOR3M-BBG

GBP/USD
------

* GBP-USD-FXSwap-BBG
* GBP-USD-LIBOR3M-LIBOR3M-BBG

