Welcome to the OpenGamma Platform!
----------------------------------
OpenGamma's flagship technology for financial institutions, the OpenGamma
Platform, is a comprehensive solution for financial analytics capable of
satisfying the full range of front-office and risk requirements.
It supports pre-trade ad-hoc calculations, near-real-time streaming analytics,
batch/overnight risk calculations, and complex historical scenarios and
stress tests in the same system.

Built as a completely open architecture, the OpenGamma Platform is designed so
that every component can be individually used, or individually replaced, based
on customer requirements. We don't believe in forklift upgrades, and we built
the OpenGamma Platform so that they're never necessary: individual projects can
use OpenGamma components when they provide a clear advantage, and later migrate
additional portions of their infrastructure if and when time and resources
permit.

Visit the developer website at http://developers.opengamma.com for more
information, downloads, docs and more.

[![OpenGamma](http://developers.opengamma.com/res/display/default/chrome/masthead_logo.png "OpenGamma")](http://developers.opengamma.com)


Installing and building OpenGamma
---------------------------------
Firstly you need to make sure Apache Maven and Git are installed and working.
Version 3.0.4 or later of Maven is required.

### Obtaining the source code

The OpenGamma Platform is open source software using the Apache License v2.
The [company](http://www.opengamma.com/) behind OpenGamma also offers support
and some additional commercial components (The commercial components typically
have dependencies with restrictive licensing incompatible with open source.)
This README only refers to the open source components.

The source code can be cloned using git from GitHub:
```
  git clone https://github.com/OpenGamma/OG-Platform.git
```

A source tarball can also be downloaded from http://developers.opengamma.com.

### Building

The source code must be compiled before use. This will build multiple jar
files and install them into your local Maven repository.
Simply run this command from the root directory of the source code:
```
  mvn install
```
The command above will run unit tests.
These can be skipped to save time if desired:
```
  mvn install -DskipTests
```


### Examples

For examples to introduce the system, see the sesame/sesame-examples folder.


___

Eclipse
-------
Importing the projects into Eclipse requires following a very specific set of
instructions to work properly.
Full details are in the README of the eclipse subdirectory.


More information
----------------
For more information go to http://developers.opengamma.com
