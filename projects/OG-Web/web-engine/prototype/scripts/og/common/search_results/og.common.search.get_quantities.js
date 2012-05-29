/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 *
 */
$.register_module({
    name: 'og.common.search.get_quantities',
    dependencies: ['og.common.routes'],
    obj: function () {
        return function (input) {
            var obj = {}, str = input ? input.replace(/,/g, '') : '',
                range         = /^\s*(-{0,1}[0-9]+)\s*-\s*(-{0,1}[0-9]+)\s*$/,  // (-)x-(-)x
                less          = /^\s*<\s*(-{0,1}[0-9]+)\s*$/,                   // <(0)x
                more          = /^\s*>\s*(-{0,1}[0-9]+)\s*$/,                   // >(0)x
                less_or_equal = /^\s*<\s*=\s*(-{0,1}[0-9]+)\s*$/,               // <=(0)x
                more_or_equal = /^\s*>\s*=\s*(-{0,1}[0-9]+)\s*$/,               // >=(0)x
                exact         = /^\s*(-{0,1}[0-9]+)\s*$/;                       // (-)x
            switch (true) {
                case less.test(str): obj.max_quantity = +str.replace(less, '$1') - 1; break;
                case less_or_equal.test(str): obj.max_quantity = str.replace(less_or_equal, '$1'); break;
                case more.test(str): obj.min_quantity = +str.replace(more, '$1') + 1; break;
                case more_or_equal.test(str): obj.min_quantity = str.replace(more_or_equal, '$1'); break;
                case exact.test(str): obj.min_quantity = obj.max_quantity = str.replace(exact, '$1'); break;
                case range.test(str):
                    obj.min_quantity = str.replace(range, '$1'), obj.max_quantity = str.replace(range, '$2'); break;
            }
            return obj;
        };
    }
});