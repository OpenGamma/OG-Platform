export VERSION=dev
rm -r $VERSION/analytics
dexy -p
cp -r output $VERSION/analytics
sudo python -m SimpleHTTPServer 80
