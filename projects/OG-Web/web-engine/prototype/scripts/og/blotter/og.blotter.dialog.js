/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.Dialog',
    dependencies: [],
    obj: function () {   
        return function () {
            var dialog = this, close = '.OG-blotter-form-close'; 
            dialog.form_block = '.OG-blotter-form-block';

            dialog.alive = function () {
                //am I alive?
            },
            dialog.load = function () {
                og.api.text({module: 'og.blotter.dialog_tash'}).pipe(function (tmpl) {
                    template = Handlebars.compile(tmpl);
                    dialog.selector = $(template())
                    .appendTo($('body'))
                    .on('click', close, function () {
                       dialog.kill(); 
                    });
                });
                  
            },
            dialog.load();
            dialog.resize = function () {
                //maybe not needed
            },
            dialog.kill = function () {
                dialog.selector.remove();
            };
        };
    }
});
