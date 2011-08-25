export VERSION=0.9.1
rm -r $VERSION
dexy
mkdir -p $VERSION/java
cp -r ../../../build/docs/javadoc-all/ $VERSION/java/javadocs
cp -r output $VERSION/analytics
sudo python -m SimpleHTTPServer 80
