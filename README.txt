Installing and building OG-Platform
-----------------------------------
Firstly you need to make sure Apache Maven and Git are installed and working.
Version 3.0.4 or later of Maven is required.


Obtaining the source code
-------------------------
The source code can be cloned using git from GitHub:

  git clone https://github.com/OpenGamma/OG-Platform.git

A source tarball can also be downloaded from http://developers.opengamma.com.


Building
--------
The source code must be compiled before use. This will build multiple jar
files and install them into your local Maven repository.
Simply run this command from the root directory of the source code:

  mvn install

The command above will run unit tests.
These can be skipped to save time if desired:

  mvn install -DskipTests


Examples
--------
For examples to introduce the system, see the sesame/sesame-examples folder.


Eclipse
-------
Importing the projects into Eclipse requires following a very specific set of
instructions to work properly.
Full details are in the README of the eclipse subdirectory.
