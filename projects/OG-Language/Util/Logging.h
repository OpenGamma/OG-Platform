/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_logging_h
#define __inc_og_language_util_logging_h

// Logging wrapper for log4cxx to simplify it

#ifdef __cplusplus

#include "Unicode.h"

#include <log4cxx/logger.h>

#ifdef _UNICODE
#define TCharMessageBuffer WideMessageBuffer
#else /* ifdef _UNICODE */
#define TCharMessageBuffer CharMessageBuffer
#endif /* ifdef _UNICODE */

/// Defines a static logger for use within the module, referenced by LOGDEBUG, LOGINFO, LOGWARN, LOGERROR and LOGFATAL
#define LOGGING(_id_) static ::log4cxx::LoggerPtr _logger (::log4cxx::Logger::getLogger (#_id_))

#if defined(_DEBUG) || defined(FORCE_LOGGING_DEBUG)

/// Log a message at DEBUG level
#define LOGDEBUG(_expr_) LOG4CXX_DEBUG (_logger, _expr_)

#else /* ifdef _DEBUG */

/// Log a message at DEBUG level (no code emitted during a debug build)
#define LOGDEBUG(_expr_)

#endif /* ifdef _DEBUG */

/// Log a message at INFO level
#define LOGINFO(_expr_) LOG4CXX_INFO (_logger, _expr_)

/// Log a message at WARN level
#define LOGWARN(_expr_) LOG4CXX_WARN (_logger, _expr_)

/// Log a message at ERROR level
#define LOGERROR(_expr_) LOG4CXX_ERROR (_logger, _expr_)

/// Log a message at FATAL level
#define LOGFATAL(_expr_) LOG4CXX_FATAL (_logger, _expr_)

/// Log a TODO message at FATAL level
#define TODO(_expr_) LOGFATAL (TEXT("TODO: ") << _expr_ << TEXT (" at ") << TEXT (__FUNCTION__))

#include "AbstractSettings.h"

void LoggingInit (const CAbstractSettings *poSettings = NULL);

#else /* ifdef __cplusplus */

// Logging not available unless compiling with C++

/// Suppress logging when not using C++
#define LOGGING(_id_)

/// Suppress logging when not using C++
#define LOGDEBUG(_expr_)

/// Suppress logging when not using C++
#define LOGINFO(_expr_)

/// Suppress logging when not using C++
#define LOGWARN(_expr_)

/// Suppress logging when not using C++
#define LOGERROR(_expr_)

/// Suppress logging when not using C++
#define LOGFATAL(_expr_)

/// Suppress logging when not using C++
#define TODO(_expr_)

#endif /* ifdef __cplusplus */

#endif /* ifndef __inc_og_language_util_logging_h */
