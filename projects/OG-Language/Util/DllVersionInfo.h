/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_dllversioninfo_h
#define __inc_og_language_util_dllversioninfo_h

#include <Util/Version.h>

#ifndef DllVersion_Comments
#define DllVersion_Comments			""
#endif
#ifndef DllVersion_CompanyName
#define DllVersion_CompanyName		"OpenGamma Ltd"
#endif
#ifndef DllVersion_FileDescription
#error "Must define DllVersion_FileDescription"
#endif
#ifndef DllVersion_FileVersion
#ifdef VERSION
#define DllVersion_FileVersion		VERSION
#else
#error "Must define either DllVersion_FileVersion or VERSION"
#endif
#endif
#ifndef DllVersion_InternalName
#define DllVersion_InternalName		DllVersion_OriginalFilename
#endif
#ifndef DllVersion_LegalCopyright
#define DllVersion_LegalCopyright	"Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies. Please see distribution for license."
#endif
#ifndef DllVersion_OriginalFilename
#error "Must define DllVersion_OriginalFilename"
#endif
#ifndef DllVersion_ProductName
#define DllVersion_ProductName		"Language Integration"
#endif
#ifndef DllVersion_ProductVersion
#ifdef VERSION
#define DllVersion_ProductVersion	VERSION
#else
#error "Must define either DllVersion_ProductVersion or VERSION"
#endif
#endif
#ifndef DllVersion_PrivateBuild
#define DllVersion_PrivateBuild		""
#endif
#ifndef DllVersion_SpecialBuild
#define DllVersion_SpecialBuild		""
#endif

#endif /* ifndef __inc_og_language_util_dllversioninfo_h */
