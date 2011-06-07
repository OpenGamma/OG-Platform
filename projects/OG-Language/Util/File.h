/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_file_h
#define __inc_og_language_util_file_h

#ifdef _WIN32

// The path character as a string, a backslash on Windows.
#define PATH_CHAR_STR	"\\"

// The path character, a backslash on Window
#define PATH_CHAR		'\\'

// The separator character (e.g. in the PATH environment variable), a semi-colon on Windows
#define SEP_CHAR_STR	";"

#else

// The path character as a string, a forward slash on Posix
#define PATH_CHAR_STR	"/"

// The path character, a forward slash on Posix
#define PATH_CHAR		'/'

// The separator character (e..g in the PATH environment variable), a colon on Posix
#define SEP_CHAR_STR	":"

#endif

#endif /* ifndef __inc_og_language_util_file_h */
