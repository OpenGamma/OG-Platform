/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_forms.forwardswapcurvespecification',
    dependencies: [
        'og.api',
        'og.common.util.ui'
    ],
    obj: function () {
        var Form = og.common.util.ui.Form, constructor,
            INDX = '<INDEX>', CVIP = 'curveInstrumentProvider',
            POST = 'postfix', PREF = 'prefix', DFNM = 'dataFieldName', SPPF = 'spotPrefix',
            NAME = 'name', TARG = 'target', CURR = 'currency',
            type_map = [
                [['0', INDX].join('.'),             Form.type.STR],
                [[CVIP, '0', INDX].join('.'),       Form.type.STR],
                [[CVIP, DFNM].join('.'),            Form.type.STR],
                [[CVIP, POST].join('.'),            Form.type.STR],
                [[CVIP, PREF].join('.'),            Form.type.STR],
                [[CVIP, SPPF].join('.'),            Form.type.STR],
                [NAME,                              Form.type.STR],
                [[TARG, '0', INDX].join('.'),       Form.type.STR],
                [[TARG, CURR].join('.'),            Form.type.STR]
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {});
        constructor = og.views.config_forms['default'].preload({type_map: type_map});
        constructor.type_map = type_map;
        return constructor;
    }
});