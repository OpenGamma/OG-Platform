export VERSION=0.9.4
rm -r $VERSION
dexy --global OG_VERSION=$VERSION
mkdir -p $VERSION/java
cp -r ../../../build/docs/javadoc-all/ $VERSION/java/javadocs
cp -r output $VERSION/analytics
R CMD BATCH graph-memory.R
sudo python -m SimpleHTTPServer 80
