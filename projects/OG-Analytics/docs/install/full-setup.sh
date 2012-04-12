sudo apt-get update
sudo apt-get -y --force-yes upgrade

sudo apt-get install -y --force-yes python-dev
sudo apt-get install -y --force-yes default-jdk
sudo apt-get install -y --force-yes git
sudo apt-get install -y --force-yes r-base-core
sudo apt-get install -y --force-yes python-pip
sudo apt-get install -y --force-yes liblog4cxx10-dev
sudo apt-get install -y --force-yes libgcj12-dev 

# install ant
sudo apt-get install -y --force-yes ant
# TODO try apt-get install ant-contrib  and see if that works better
# ant-contrib for cpptasks
wget http://downloads.sourceforge.net/project/ant-contrib/ant-contrib/1.0b3/ant-contrib-1.0b3-bin.zip
unzip ant-contrib-1.0b3-bin.zip 
sudo cp ant-contrib/ant-contrib-1.0b3.jar /usr/share/ant/lib/

# texlive
wget http://mirror.ctan.org/systems/texlive/tlnet/install-tl-unx.tar.gz
tar -xzf install-tl-unx.tar.gz
cd install-tl-*
touch texlive.profile # blank texlive.profile forces non-interactive mode
sudo ./install-tl -profile texlive.profile
cd ..

# phantom.js
wget http://phantomjs.googlecode.com/files/phantomjs-1.5.0-linux-x86_64-dynamic.tar.gz
tar -xf phantomjs-1.5.0-linux-x86_64-dynamic.tar.gz 
sudo ln -s phantomjs/bin/phantomjs /usr/local/bin/phantomjs

# dexy
git clone https://github.com/ananelson/dexy
cd dexy
sudo pip install -e .
cd ..

cd /mnt # More room here.
mkdir work
cd work

sudo cp -r /usr/include/apr-1.0 /usr/include/apr-1

# Install Fudge Messaging 
sudo apt-get install -y --force-yes libtool
git clone https://github.com/vrai/Fudge-C.git
cd Fudge-C
./reconf
./configure 
make && sudo make install
cd ..

# OpenGamma Platform (finally)
git clone https://github.com/OpenGamma/OG-Platform.git
cd OG-Platform

export OG_USER=opengamma-public
export OG_PASSWORD=opengamma

ant init
ant clone-or-pull
ant build

# Start OG Server
cd projects/OG-Examples
ant new-hsqldb
ant init-database
ant jetty
cd ../..

# Configure OG-RStats
mkdir -p ~/etc/OpenGammaLtd

echo "jvmLibrary=/usr/lib/jvm/java-6-openjdk/jre/lib/amd64/server/libjvm.so
jvmProperty.opengamma.configuration.url=http://localhost:8080/jax/configuration/0/
" > ~/etc/OpenGammaLtd/LanguageIntegration

echo "serviceExecutable=/mnt/work/OG-Platform/projects/OG-RStats/build/Release/ServiceRunner
connectorLogConfiguration=/mnt/work/OG-Platform/projects/OG-RStats/config/log4cxx.properties
"> ~/etc/OpenGammaLtd/OpenGammaR

# Install OG-RStats
cd projects
git clone https://github.com/OpenGamma/OG-RStats.git
cd OG-RStats
ant install
cd ../..
