#pragma once

#include <windows.h>
#include <string>
#include <strsafe.h>

void openLogFile(char *logFile);
void closeLogFile();
int log(char *sz,...);
int logErrorMessage(DWORD dw);