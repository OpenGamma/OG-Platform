/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.scrollbar_size',
    dependencies: [],
    obj: function () {
        var html = '<div style="width: 100px; height: 100px; position: absolute; \
            visibility: hidden; overflow: auto; left: -10000px; z-index: -10000; bottom: 100px" />';
        return 100 - $(html).appendTo('body').append('<div />').find('div').css('height', '200px').width();
    }
});