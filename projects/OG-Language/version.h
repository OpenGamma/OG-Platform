#ifndef __INC_OPENGAMMA_VERSION_H
#define __INC_OPENGAMMA_VERSION_H

// Major version number
#define VERSION_MAJOR	0

// Minor version number
#define VERSION_MINOR	5

// Revision
#define REVISION	0

// Build system generated build number
#ifndef BUILD_NUMBER
#define BUILD_NUMBER	1
#endif /* ifndef BUILD_NUMBER */

// Textual suffix
#ifndef VERSION_SUFFIX
#ifdef _DEBUG
#define VERSION_SUFFIX "-Debug"
#else /* ifdef _DEBUG */
#define VERSION_SUFFIX ""
#endif /* ifdef _DEBUG */
#endif /* ifndef VERSION_SUFFIX */

// Generated "Version" string
#define QUOTE_(x)	#x
#define QUOTE(x)	QUOTE_(x)
#define VERSION		TEXT(QUOTE(VERSION_MAJOR)) TEXT(".") TEXT(QUOTE(VERSION_MINOR)) TEXT(".") TEXT(QUOTE(REVISION)) TEXT(".") TEXT(QUOTE(BUILD_NUMBER)) TEXT(VERSION_SUFFIX)

#endif /* ifndef __INC_OPENGAMMA_VERSION_H */
