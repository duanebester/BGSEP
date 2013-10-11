/* Copyright (C) 2013  Patrik Wållgren

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/  */

package bgsep.model;

import java.util.Observable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * description...
 * 
 * @author
 * 
 */
public class Gyro extends Observable implements SensorEventListener {

	private SensorManager sensorManager;
	private Sensor sensorAccelerometer;
	private float indication;
	private boolean enabled;

	/**
	 * Initializes the gyro. Enabled by default.
	 * 
	 * @param manager
	 *            An initialized SensorManager.
	 */
	public Gyro(SensorManager manager) {
		sensorManager = manager;
		sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		registerListener();
		indication = 0;
	}

	public void registerListener() {
		sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void unregisterListener() {
		sensorManager.unregisterListener(this);
	}

	public float getIndication() {
		return indication;
	}

	public float getMaximumRange() {
		return sensorAccelerometer.getMaximumRange();
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (enabled)
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				indication = event.values[1];
				setChanged();
				notifyObservers();
			}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

}
