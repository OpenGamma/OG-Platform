/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_forms.multicurvecalculationconfig',
    dependencies: [
        'og.api',
        'og.common.util.ui'
    ],
    obj: function () {
        var Form = og.common.util.ui.Form, constructor,
            INDX = '<INDEX>',
            CAME = 'calculationMethods', CFNM = 'configurationName',
            IDEN = 'ids', CURR = 'currency',
            IECN = 'instrumentExposureCurveName', IEFC = 'instrumentExposuresForCurve',
            CEXP = 'curveExposures', CNME = 'curveName',
            SINT = 'stripInstrumentType', YCNM = 'yieldCurveNames',
            EXCC = 'exogenousCurveConfig', EXCN = 'exogenousCurveName', EXDA = 'exogenousData',
            type_map = [
                ['0',                                                           Form.type.STR],
                [CAME,                                                          Form.type.STR],
                [CFNM,                                                          Form.type.STR],
                [IECN,                                                          Form.type.STR],
                [YCNM,                                                          Form.type.STR],
                [EXDA,                                                          Form.type.STR],
                [[IDEN, '0', INDX].join('.'),                                   Form.type.STR],
                [[IDEN, CURR].join('.'),                                        Form.type.STR],
                [[IECN, INDX].join('.'),                                        Form.type.STR],
                [[IEFC, '0'].join('.'),                                         Form.type.STR],
                [[IEFC, SINT].join('.'),                                        Form.type.STR],
                [[IEFC, CEXP, INDX, CNME].join('.'),                            Form.type.STR],
                [[IEFC, CEXP, INDX, CNME, INDX].join('.'),                      Form.type.STR],
                [[IEFC, CEXP, CNME].join('.'),                                  Form.type.STR],
                [[IEFC, CEXP, CNME, INDX].join('.'),                            Form.type.STR],
                [[IEFC, SINT, INDX].join('.'),                                  Form.type.STR],
                [[IEFC, INDX, '0'].join('.'),                                   Form.type.STR],
                [[IEFC, INDX, CEXP, INDX, CNME].join('.'),                      Form.type.STR],
                [[IEFC, INDX, CEXP, CNME, INDX].join('.'),                      Form.type.STR],
                [[IEFC, INDX, CEXP, INDX, CNME, INDX].join('.'),                Form.type.STR],
                [[IEFC, INDX, SINT].join('.'),                                  Form.type.STR],
                [[IEFC, INDX, SINT, INDX].join('.'),                            Form.type.STR],
                [[YCNM, INDX].join('.'),                                        Form.type.STR],
                [[EXCC, EXCN, INDX].join('.'),                                  Form.type.STR],
                [[EXCC, EXCN].join('.'),                                        Form.type.STR]
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {});
        constructor = og.views.config_forms['default'].preload({type_map: type_map});
        constructor.type_map = type_map;
        return constructor;
    }
});