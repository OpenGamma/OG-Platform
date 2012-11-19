/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.container',
    dependencies: [''],
    obj: function () {
        var module = this;
        alert('hi');
        var constructor  = function () {
            var container = this, css_attr = {
                position: 'absolute', width: '500px', height: '800px', 
                zIndex: '50', marginLeft: 'auto', marginRight: 'auto'};
            container.alive = function () {
                //am I alive
            };
            container.load = function () {
                og.api.text({module: 'og.views.blotter.container'}).pipe(function (template) {
                    container.selector = $(template);
                    container.selector.appendTo($('body')).css(css_attr);
                });
               
            };
            container.load();
            container.resize = function () {
                //maybe not needed
            };
        };
        return constructor;
    }
});