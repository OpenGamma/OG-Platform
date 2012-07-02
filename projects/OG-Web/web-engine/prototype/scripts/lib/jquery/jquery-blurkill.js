/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

(function ($) {
    var namespace = 'blurkill', elements = [];
    $(function () {
        $(document).on('mousedown.' + namespace, function () {
            elements = elements.filter(function (close) {return close(), false;});
        });
    });
    $.fn[namespace] = function (close) {
        this.on('mousedown', function (event) {
            event.preventDefault();
            event.stopPropagation();
        });
        return elements.push(close || (function (element) {return function () {element.remove();};})(this)), this;
    };
})(jQuery);