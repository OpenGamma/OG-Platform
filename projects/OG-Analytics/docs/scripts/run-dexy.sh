set -e
pwd
export VERSION=dev
rm -rf $VERSION

### @export "run-dexy"
dexy --globals OG_VERSION=$VERSION --output
### @end

mkdir -p $VERSION/java
cp -r ../../../build/docs/javadoc-all/ $VERSION/java/javadocs
cp -r output $VERSION/analytics
