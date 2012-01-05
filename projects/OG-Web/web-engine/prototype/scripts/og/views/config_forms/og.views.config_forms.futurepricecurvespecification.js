/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.futurepricecurvespecification',
    dependencies: [
        'og.api',
        'og.common.util.ui'
    ],
    obj: function () {
        var module = this, Form = og.common.util.ui.Form,
            INDX = '<INDEX>', CRIP = 'curveInstrumentProvider',
            DATA = 'dataFieldName', FUPX = 'futurePrefix', POFX = 'postfix',
            type_map = [
                ['0',                               Form.type.STR],
                [[CRIP, '0'].join('.'),             Form.type.STR],
                [[CRIP, DATA].join('.'),            Form.type.STR],
                [[CRIP, FUPX].join('.'),            Form.type.STR],
                [[CRIP, POFX].join('.'),            Form.type.STR],
                ['name',                            Form.type.STR],
                [['target', '0', INDX].join('.'),   Form.type.STR],
                [['target', 'currency'].join('.'),  Form.type.STR],
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {}),
            constructor;
        constructor = og.views.config_forms['default'].preload({type_map: type_map});
        constructor.type_map = type_map;
        return constructor;
    }
});