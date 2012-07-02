/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.tablesorter.addParser({
    id: 'numeric_string',
    is: function(s) {return /[0-9]+,[0-9]+/.test(s);},
    format: function(s) {return s.replace(/,/g,'');},
    type: 'numeric'
});
$.tablesorter.addParser({
    id: 'currency_string',
    is: function(s) {return /[0-9]+\s[a-zA-Z]+/.test(s);},
    format: function(s) {return s.replace(/[a-zA-Z()\s]+/g,'');},
    type: 'numeric'
});