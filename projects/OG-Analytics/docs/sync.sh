rsync -v -r --exclude=MathJax --exclude sync.sh output/* docs-static.hq.opengamma.com:/srv/web/sites/docs-static.hq.opengamma.com/html/{{ OG_VERSION }}/analytics
