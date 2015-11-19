OpenGamma Platform
------------------
This repository contains the source code of the OpenGamma Platform.

This is OpenGamma's server-oriented solution for financial analytics,
developed since 2009, and used in production by institutions all over
the world to satisfy a wide range of front-office and risk requirements.

Visit the developers website at http://developers.opengamma.com for more
information.

[![OpenGamma](http://developers.opengamma.com/res/display/default/chrome/masthead_logo.png "OpenGamma")](http://developers.opengamma.com)


Platform News
-------------

OpenGamma is developing our next-generation, open source toolkit for
market risk - called Strata.

The code is available on GitHub at https://github.com/OpenGamma/Strata. For more information, see the [Strata Documentation](http://opengamma.github.io/StrataDocs).


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

For examples to introduce the system, see the [sesame/sesame-examples folder](tree/master/sesame/sesame-examples).


___

Eclipse
-------
Importing the projects into Eclipse requires following a very specific set of
instructions to work properly.
Full details are in the README of the eclipse subdirectory.


More information
----------------
For more information go to http://developers.opengamma.com
