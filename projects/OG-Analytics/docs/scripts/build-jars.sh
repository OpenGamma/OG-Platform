dexy reset
cd ../../..

### @export "ant"
ant clean
ant clone-or-pull
ant publish-all-local
