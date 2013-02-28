/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#ifndef SERVICE_CONTROL
# define SERVICE_CONTROL    "/sbin/service"
#endif /* ifndef SERVICE_CONTROL */
#ifndef SERVICE_NAME
# define SERVICE_NAME		"og-language"
#endif /* ifndef SERVICE_NAME */
#ifndef SERVICE_COMMAND
# error "Must specify service command"
#endif /* ifndef SERVICE_COMMAND */

/// Changes to the privileged user and sends the command to the service. Note that this does not
/// use any command line parameters or environment variables to avoid introducing a security
/// loophole from the setuid mechanism.
int main () {
	setuid (0);
	execl (SERVICE_CONTROL, SERVICE_CONTROL, SERVICE_NAME, SERVICE_COMMAND, (char*)NULL);
}
