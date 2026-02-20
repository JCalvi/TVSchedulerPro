#pragma once

#include <windows.h>
#include <string>
#include <strsafe.h>

void getAllUserPath(char *buff, int buffLen);
void openLogFile(char *logFile);
void closeLogFile();
int log(char *sz,...);