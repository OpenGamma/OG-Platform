### @export "workspace"
cd /mnt
sudo mkdir work
sudo chown -R ubuntu work
cd work

### @export "update-system"
sudo apt-get update
sudo apt-get -y --force-yes upgrade

### @export "install-jdk"
sudo apt-get install -y --force-yes default-jdk

### @export "install-ant"
sudo apt-get install -y --force-yes ant

### @export "install-cpp-utils"
sudo apt-get install -y --force-yes libgcj12-dev # GNU compiler for Java
sudo apt-get install -y --force-yes liblog4cxx10-dev

### @export "fix-apr"
sudo cp -r /usr/include/apr-1.0 /usr/include/apr-1

### @export "install-ant-cpptasks"
wget http://hivelocity.dl.sourceforge.net/project/ant-contrib/ant-contrib/cpptasks-1.0-beta5/cpptasks-1.0b5.tar.gz
tar -xf cpptasks-1.0b5.tar.gz
cd cpptasks-1.0b5/
ant jars
sudo cp target/lib/cpptasks.jar /usr/share/ant/lib/
cd ..

### @export "install-tex"
#sudo apt-get install -y --force-yes texlive-full

### @export "texlive-path"
export PATH=$PATH:/usr/local/texlive/2011/bin/x86_64-linux/

### @export "install-r"
sudo apt-get install -y --force-yes r-base
sudo apt-get install -y --force-yes texinfo

### @export "install-git"
sudo apt-get install -y --force-yes git

### @export "install-fudge"
sudo apt-get install -y --force-yes libtool

git clone https://github.com/vrai/Fudge-C.git
cd Fudge-C
./reconf
./configure 
make && sudo make install
cd ..

sudo ldconfig

### @export "opengamma-platform"
export OG_USER=opengamma-public
export OG_PASSWORD=opengamma

git clone https://github.com/OpenGamma/OG-Platform.git
cd OG-Platform

ant init
ant clone-or-pull
ant build

### @export "start-server"
cd examples/examples-simulated
mvn opengamma:server-init -Dconfig=fullstack
mvn opengamma:server-start -Dconfig=fullstack
cd ../..
