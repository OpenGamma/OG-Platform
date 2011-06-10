This folder contains jython examples that encourage the user to poke about with OpenGamma projects.
The focus is on OG-Analytics, which is why, I suppose, the folder exists therein.

To get access to the platform, the following must be in 'the path'.
 
$OG-Platform/projects/OG-Util/lib/classpath.jar
$OG-Platform/projects/OG-Analytics/lib/classpath.jar
$OG-Platform/projects/OG-Analytics/build/classes
$OG-Platform/projects/OG-Util/build/classes
$OG-Platform/projects/OG-Analytics/lib/jar/it.unimi.dsi/fastutil/fastutil-5.1.5.jar

Now, depending on the version of jython that you are running, this path is either JYTHONPATH or CLASSPATH.
I tend to set both, and use '. setpath.sh' to do so.

