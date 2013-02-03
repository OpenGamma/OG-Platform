/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.id', dependencies: [],
    obj: function () {
        var counter = 0;
        return function (prefix) {return (prefix || 'og') + --counter + -new Date;};
    }
});