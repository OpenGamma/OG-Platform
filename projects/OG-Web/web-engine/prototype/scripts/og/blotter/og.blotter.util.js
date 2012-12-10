/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.util',
    dependencies: [],
    obj: function () {
        return {
            update_block : function (section, extras){
                section.block.html(function (html) {
                    $(section.selector).html(html);
                }, extras);
            }
        };
    }
});