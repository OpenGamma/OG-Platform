/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_forms.swaptionvolatilitycubespecification',
    dependencies: [
        'og.api',
        'og.common.util.ui'
    ],
    obj: function () {
        var Form = og.common.util.ui.Form, constructor,
            INDX = '<INDEX>', CQTP = 'cubeQuoteType', QTUN = 'quoteUnits', NAME = 'name',
            CIPR = 'cubeInstrumentProvider', DATA = 'dataField', PREF = 'prefix', TARG = 'target', CURR = 'currency',
            type_map = [
                ['0',                          Form.type.STR],
                [CQTP,                         Form.type.STR],
                [NAME,                         Form.type.STR],
                [QTUN,                         Form.type.STR],
                [[CIPR, '0'].join('.'),        Form.type.STR],
                [[CIPR, DATA].join('.'),       Form.type.STR],
                [[CIPR, PREF].join('.'),       Form.type.STR],
                [[TARG, CURR].join('.'),       Form.type.STR],
                [[TARG, '0', INDX].join('.'),  Form.type.STR]
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {});
        constructor = og.views.config_forms['default'].preload({type_map: type_map});
        constructor.type_map = type_map;
        return constructor;
    }
});