sudo apt-get update
sudo apt-get -y --force-yes upgrade

sudo apt-get install -y --force-yes python-dev
sudo apt-get install -y --force-yes default-jdk
sudo apt-get install -y --force-yes ant
sudo apt-get install -y --force-yes git
sudo apt-get install -y --force-yes r-base-core
sudo apt-get install -y --force-yes python-pip

git clone https://github.com/ananelson/dexy
cd dexy
sudo pip install -e .
cd ..

git clone https://github.com/OpenGamma/OG-Platform.git
cd OG-Platform

export OG_USER=opengamma-public
export OG_PASSWORD=opengamma

ant init
ant clone-or-pull
ant build

