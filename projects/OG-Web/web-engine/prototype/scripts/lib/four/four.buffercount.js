/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.Four) window.Four = {};
    /**
     * If Webgl inspector, query the dom and log out the active buffers
     */
    window.Four.buffercount = function () {
        var live_buffer = '.buffer-item.listing-item', deleted_buffer = '.buffer-item.listing-item.buffer-item-deleted';
        if ($(live_buffer).length) console.log($(live_buffer).length - $(deleted_buffer).length);
    };
})();