#pragma once
#include "types.h"

void navInit();

// Goi lien tuc trong loop(), sau sensorsUpdate()
void navUpdate();

// Chon che do: AUTO (tu di) / MANUAL (dung yen cho lenh dieu khien) / IDLE (dung yen)
void navStartAuto();
void navStartManual();
void navStop();

// Lenh dieu khien tay - chi co tac dung khi dang o MODE_MANUAL. Tra ve false neu bi bo qua.
bool navManual(ManualCmd cmd);

RobotMode navGetMode();
NavState navGetState();
ManualCmd navGetManualCmd();
const char* navStateName(NavState s);
const char* navModeName(RobotMode m);
const char* navManualName(ManualCmd c);
