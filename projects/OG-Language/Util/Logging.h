/**
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

#define LOGGING(_id_) static ::log4cxx::LoggerPtr _logger (::log4cxx::Logger::getLogger (#_id_))

#if defined(_DEBUG) || defined(FORCE_LOGGING_DEBUG)
#define LOGDEBUG(_expr_) LOG4CXX_DEBUG (_logger, _expr_)
#else /* ifdef _DEBUG */
#define LOGDEBUG(_expr_)
#endif /* ifdef _DEBUG */

#define LOGINFO(_expr_) LOG4CXX_INFO (_logger, _expr_)
#define LOGWARN(_expr_) LOG4CXX_WARN (_logger, _expr_)
#define LOGERROR(_expr_) LOG4CXX_ERROR (_logger, _expr_)
#define LOGFATAL(_expr_) LOG4CXX_FATAL (_logger, _expr_)

#define TODO(_expr_) LOGFATAL (TEXT("TODO: ") << _expr_ << TEXT (" at ") << TEXT (__FUNCTION__))

#include "AbstractSettings.h"

void LoggingInit (const CAbstractSettings *poSettings = NULL);

#else /* ifdef __cplusplus */

// Logging not available unless compiling with C++
#define LOGGING(_id_)
#define LOGDEBUG(_expr_)
#define LOGINFO(_expr_)
#define LOGWARN(_expr_)
#define LOGERROR(_expr_)
#define LOGFATAL(_expr_)
#define TODO(_expr_)

#endif /* ifdef __cplusplus */

#endif /* ifndef __inc_og_language_util_logging_h */
