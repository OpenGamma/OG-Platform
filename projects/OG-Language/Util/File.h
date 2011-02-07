/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_file_h
#define __inc_og_language_util_file_h

#ifdef _WIN32
#define PATH_CHAR_STR	"\\"
#define PATH_CHAR		'\\'
#define SEP_CHAR_STR	";"
#else
#define PATH_CHAR_STR	"/"
#define PATH_CHAR		'/'
#define SEP_CHAR_STR	":"
#endif

#endif /* ifndef __inc_og_language_util_file_h */