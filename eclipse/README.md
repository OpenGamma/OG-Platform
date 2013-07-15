Setting up the OpenGamma Platform for Eclipse
---------------------------------------------

This guide is designed to allow you to setup OpenGamma in Eclipse for development.
If you are installing OpenGamma for initial evaluation purposes, or you are not a developer,
then you probably want to install the binary rather than follow these instructions.


#### Overview

OpenGamma uses Maven as its primary build system.
Installation into Eclipse is therefore based on the m2e Maven-Eclipse plugin.


#### Installation steps

1. Download Eclipse
 - We recommend using the Classic download of Eclipse as it has less bloat than other builds.
 - We have tested using Eclipse Juno 4.2.2, http://www.eclipse.org/downloads/
  
2. Obtain the OpenGamma source code
 - This can be obtained by downloading the OpenGamma source release from http://developers.opengamma.com/
   or by cloning the git repository using `git clone https://github.com/OpenGamma/OG-Platform.git`

3. Install and Start Eclipse
 - Simply unzip the download into your preferred installation location
 - Start Eclipse, ensuring that you have a brand new workspace
 - Note that the OpenGamma source code must NOT be located inside your Eclipse workspace
  
4. Install the Eclipse preferences
 - Go to "File -> Import"
 - Select the "General -> Preferences" option
 - In the popup, click the "Browse..." button
 - Choose the file `OG-Platform/eclipse/install/OpenGamma-Eclipse-Preferences.epf`
 - Click "Finish"

5. Install the Eclipse plugins
 - Go to "Help -> Install new software..."
 - Click the 'Add...' button in the top right
 - Type 'OpenGamma Eclipse update site' into the Name: field
 - Type 'http://eclipse.opengamma.com/updatesite' into the Location: field
 - Click 'OK'
 - You should now see a selection of plug-ins available.  Click 'Select All'.
 - Click "Next" and accept any warnings
 - Restart Eclipse if requested to do so
  
6. Import the OpenGamma source code
 - Go to "File -> Import"
 - Select the "Maven -> Existing Maven Projects" option
 - In the popup, click the "Browse..." button
 - Choose the root directory of the OpenGamma source code - `OG-Platform`
 - Click "Finish"
 - Enjoy a coffee while everything is installed!
   Note that this will involve Maven downloading many jar file dependencies to the local repository cache.

7. Get exploring!
 - A variety of launch configurations are supplied.
   See the "Run configurations..." popup (the down arrow beside the play button)


#### Hints and Tips

- Ensure that you do not have your source code inside your Eclipse workspace.
  If you do, nothing will work correctly!

- The combination of Eclipse, Maven and m2e sometimes gets confused.
  If projects have compile errors at any point when they shouldn't, try these three steps:
 - Select the project and right-click "Refresh"
 - Select the project and right-click "Maven -> Update Maven"
 - Select the project and from the main menu choose "Project -> Clean"


#### Active development of OpenGamma

If you are actively devloping OpenGamma, then it is recommended to take some addition steps.

8. Load the code formatter
 - Go to the Eclipse Preferences.
   This is normally 'Window->Preferences...', but 'Eclipse->Preferences..' on Mac.
 - Select the "Java -> Code Style -> Formatter" page
 - Click "Import..."
 - Choose the file `OG-Platform/eclipse/install/OpenGamma-Eclipse-Formatter-Java.xml`
 - Click "OK"

9. Load the code templates
 - Go to the Eclipse Preferences
 - Select the "Java -> Code Style -> Code templates" page
 - Click "Import..."
 - Choose the file `OG-Platform/eclipse/install/OpenGamma-Eclipse-CodeTemplates-Java.xml`
 - Click "OK"


#### Questions and comments

Please contact us publicly via the [forums](http://forums.opengamma.com/) or
privately as per your support contract.

[![OpenGamma](http://developers.opengamma.com/res/display/default/chrome/masthead_logo.png "OpenGamma")](http://developers.opengamma.com)

