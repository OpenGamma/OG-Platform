cd ../../../
pwd

### @export "ant"
ant docs
cp -r build/docs/javadoc-all/ projects/OG-Analytics/docs/output/javadoc
