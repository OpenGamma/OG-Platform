[![OpenGamma](http://developers.opengamma.com/res/display/default/chrome/masthead_logo.png "OpenGamma")](http://www.opengamma.com)

**Please note that this project has been discontinued and is no longer under active development.**


# Strata

Our development has transitioned to Strata - OpenGamma's next generation, open source toolkit for market risk.

Strata is available on GitHub at [https://github.com/OpenGamma/Strata](https://github.com/OpenGamma/Strata). Pease see the [Strata Documentation](http://opengamma.github.io/StrataDocs) for full details. 

If you have any questions, please get in touch using the [OpenGamma Forums](http://forums.opengamma.com).


# OG-Platform

This repository contains the source code of the legacy OpenGamma Platform.

This is OpenGamma's server-oriented solution for financial analytics,
developed from 2009, and has been used in production to satisfy a wide range of
front-office and risk requirements.


## Installing and building OG-Platform

Firstly you need to make sure Apache Maven and Git are installed and working.
Version 3.0.4 or later of Maven is required.

### Obtaining the source code

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


### Eclipse

Importing the projects into Eclipse requires following a very specific set of
instructions to work properly.
Full details are in the README of the eclipse subdirectory.

