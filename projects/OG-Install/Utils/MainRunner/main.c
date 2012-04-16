/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include <strsafe.h>
#include "jvm.h"

static BOOL _main (const char *pszConfigurationFileArg) {
	if (!ReadConfigurationFile (pszConfigurationFileArg)) return FALSE;
	if (!FindJava ()) return FALSE;
	if (!CreateJavaVM ()) return FALSE;
	if (!InvokeMain (NULL)) return FALSE;
	return TRUE;
}

#ifndef _DEBUG
/// Launcher entry point. The parameter must be the INI file defining the launch.
///
/// @param[in] hInstance ignored
/// @param[in] hPrevInstance ignored
/// @param[in] pszCmdLine the command line - containing the path to the INI file
/// @param[in] nCmdShow ignored
/// @return 0 if the launch was okay, 1 if there was a problem
int WINAPI WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance, char *pszCmdLine, int nCmdShow) {
	return _main (pszCmdLine) ? 0 : 1;
}
#else /* ifndef _DEBUG */
/// Diagnostic launcher entry point. The only parameter must be the INI file defining the launch.
///
/// @param[in] argc argument count, must be 2
/// @param[in] argv arguments, element 1 must be the INI file path
void main (int argc, char **argv) {
	exit (_main (argv[1]) ? 0 : 1);
}
#endif /* ifndef _DEBUG */
