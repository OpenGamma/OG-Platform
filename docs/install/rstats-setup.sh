cd /mnt/work/OG-Platform

### @export "configure-rstats"
mkdir -p ~/etc/OpenGammaLtd

echo "jvmLibrary=/usr/lib/jvm/java-6-openjdk/jre/lib/amd64/server/libjvm.so
jvmProperty.opengamma.configuration.url=http://localhost:8080/jax/configuration/0/
" > ~/etc/OpenGammaLtd/LanguageIntegration

echo "serviceExecutable=/mnt/work/OG-Platform/projects/OG-RStats/build/Release/ServiceRunner
connectorLogConfiguration=/mnt/work/OG-Platform/projects/OG-RStats/config/log4cxx.properties
"> ~/etc/OpenGammaLtd/OpenGammaR

sudo mkdir -p /var/log/OG-RStats/
sudo chown -R ubuntu /var/log/OG-RStats/

sudo mkdir -p /var/run/OG-Language/
sudo chown -R ubuntu /var/run/OG-Language/

### @export "r-packages"
CRAN_MIRROR=http://cran.case.edu/
R -e "install.packages(\"rjson\", repos=\"$CRAN_MIRROR\")"
R -e "install.packages(\"xts\", repos=\"$CRAN_MIRROR\")"

### @export "install-rstats"
cd projects/OG-RStats
ant install

### @export "leave OG-Platform"
cd ../..
cd ..

### @export "python"
sudo apt-get install -y --force-yes python-dev
sudo apt-get install -y --force-yes python-pip

### @export "phantom"
wget http://phantomjs.googlecode.com/files/phantomjs-1.5.0-linux-x86_64-dynamic.tar.gz
tar -xf phantomjs-1.5.0-linux-x86_64-dynamic.tar.gz 
sudo ln -s phantomjs/bin/phantomjs /usr/local/bin/phantomjs

### @export "casper"
git clone git://github.com/n1k0/casperjs.git
cd casperjs
git checkout tags/0.6.5
sudo ln -sf `pwd`/bin/casperjs /usr/local/bin/casperjs
cd ..

### @export "dexy"
git clone https://github.com/ananelson/dexy
cd dexy
sudo pip install -e .
cd ..

### @export "run-dexy"
cd OG-Platform
ant dexy
