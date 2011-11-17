set -e
export VERSION=dev
rm -rf $VERSION
dexy --output --globals OG_VERSION=$VERSION
mkdir -p $VERSION/java
cp -r ../../../build/docs/javadoc-all/ $VERSION/java/javadocs
cp -r output $VERSION/analytics
