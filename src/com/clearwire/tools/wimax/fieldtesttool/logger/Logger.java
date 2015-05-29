package com.clearwire.tools.wimax.fieldtesttool.logger;

import com.clearwire.tools.wimax.bcs200.event.BCS200Event;
import com.clearwire.tools.wimax.bcs200.event.BCS200EventListener;
import com.clearwire.tools.wimax.typeperf.TypeperfEvent;
import com.clearwire.tools.wimax.typeperf.TypeperfEventListener;
import com.openracesoft.devices.gps.GpsEvent;
import com.openracesoft.devices.gps.GpsEventListener;

public class Logger implements BCS200EventListener, GpsEventListener,
		TypeperfEventListener {

	public void eventOccurred(BCS200Event typeperfEvent) {
		// TODO Auto-generated method stub

	}

	public void eventOccurred(GpsEvent gpsEvent) {
		// TODO Auto-generated method stub

	}

	public void eventOccurred(TypeperfEvent typeperfEvent) {
		// TODO Auto-generated method stub

	}

}
