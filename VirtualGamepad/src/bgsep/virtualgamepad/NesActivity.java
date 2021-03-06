/* Copyright (C) 2013  Patrik Wållgren, Victor Olausson,

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

package bgsep.virtualgamepad;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import bgsep.communication.Communication;
import bgsep.model.Button;
import bgsep.model.Gyro;

/**
 * The activity for the NES controller
 * @author Victor Olausson
 * @author Patrik Wållgren
 *
 */
public class NesActivity extends Activity implements Observer {

	private ImageView imageAbutton, imageBbutton, imageLeftArrow, imageRightArrow,
					  imageUpArrow, imageDownArrow, imageSelect, imageStart;
	
	private Button	aButton, bButton, leftArrowButton,
					rightArrowButton, upArrowButton, downArrowButton,
					selectButton, startButton;
	
	private ArrayList<Button> buttons;
	
	private Communication comm;
	
	private boolean hapticFeedback;
	private boolean useAccelerometer;
	
	private Gyro gyro;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nes);
		
		// Keep screen on
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		//Dim soft menu keys if present
		if (!ViewConfiguration.get(this).hasPermanentMenuKey())
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		Intent i = getIntent();
		hapticFeedback = i.getBooleanExtra("hapticFeedback", false);
		useAccelerometer = i.getBooleanExtra("useAccelerometer", false);
		
		comm = Communication.getInstance();
		
		initImages();
		initButtons();
		if(useAccelerometer)
			initGyro();
	}

	@Override
	public void update(Observable o, Object obj) {
		if(o instanceof Button) {
			Button button = (Button)o;
			
			if(button.isPressed())
				button.getButtonView().setImageResource(button.getPressedDrawableID());
			else
				button.getButtonView().setImageResource(button.getUnPressedDrawableID());
				
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nes, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		Intent i;
	    switch (item.getItemId()) {
        
	        case R.id.action_gc:
	        	i = new Intent(this, GcActivity.class);
	        	i.putExtra("hapticFeedback", hapticFeedback);
	        	i.putExtra("useAccelerometer", useAccelerometer);
	    		startActivity(i);
	            finish();
	            return true;
	        
	        case R.id.action_ps:
	        	i = new Intent(this, PsActivity.class);
	        	i.putExtra("hapticFeedback", hapticFeedback);
	        	i.putExtra("useAccelerometer", useAccelerometer);
	    		startActivity(i);
	            finish();
	            return true;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void initImages() {
		imageAbutton 	= (ImageView) findViewById(R.id.nes_a_button);
		imageBbutton 	= (ImageView) findViewById(R.id.nes_b_button);
		imageLeftArrow 	= (ImageView) findViewById(R.id.nes_left_arrow);
		imageRightArrow = (ImageView) findViewById(R.id.nes_right_arrow);
		imageUpArrow 	= (ImageView) findViewById(R.id.nes_up_arrow);
		imageDownArrow 	= (ImageView) findViewById(R.id.nes_down_arrow);
		imageSelect		= (ImageView) findViewById(R.id.nes_select_button);
		imageStart		= (ImageView) findViewById(R.id.nes_start_button);
	}
	
	private void initButtons() {
		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		leftArrowButton = new Button(imageLeftArrow, R.drawable.nes_left_arrow, R.drawable.nes_left_arrow_pressed,
				0, this, vibrator, hapticFeedback);
		
		rightArrowButton = new Button(imageRightArrow, R.drawable.nes_right_arrow, R.drawable.nes_right_arrow_pressed,
				1, this, vibrator, hapticFeedback);
		
		upArrowButton = new Button(imageUpArrow, R.drawable.nes_up_arrow, R.drawable.nes_up_arrow_pressed,
				2, this, vibrator, hapticFeedback);
		
		downArrowButton = new Button(imageDownArrow, R.drawable.nes_down_arrow, R.drawable.nes_down_arrow_pressed,
				3, this, vibrator, hapticFeedback);
		
		aButton = new Button(imageAbutton, R.drawable.nes_a_button, R.drawable.nes_a_button_pressed,
				4, this, vibrator, hapticFeedback);
		
		bButton = new Button(imageBbutton, R.drawable.nes_b_button, R.drawable.nes_b_button_pressed,
				5, this, vibrator, hapticFeedback);
		
		selectButton = new Button(imageSelect, R.drawable.nes_select_button, R.drawable.nes_select_button_pressed,
				6, this, vibrator, hapticFeedback);
		
		startButton = new Button(imageStart, R.drawable.nes_start_button, R.drawable.nes_start_button_pressed,
				7, this, vibrator, hapticFeedback);
		
		buttons = new ArrayList<Button>();
		
		buttons.add(aButton);
		buttons.add(bButton);
		buttons.add(leftArrowButton);
		buttons.add(rightArrowButton);
		buttons.add(upArrowButton);
		buttons.add(downArrowButton);
		buttons.add(selectButton);
		buttons.add(startButton);
		for(Button b : buttons)
			b.addObserver(comm);		
	}
	
	private void initGyro() {
		SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		gyro = new Gyro(sensorManager);
		gyro.setLeftRightGyroID(8, 9);
		gyro.setEnabled(useAccelerometer);
		gyro.registerListener();
		gyro.addObserver(comm);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		releaseAliciaKeys();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		releaseAliciaKeys();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(useAccelerometer)
			gyro.registerListener();
	}
	
	private void releaseAliciaKeys() {
		if(useAccelerometer)
			gyro.unregisterListener();
		
		unPressAllButtons();
	}
	
	private void unPressAllButtons() {
		for(Button b : buttons)
			b.setPressed(false);
	}
}
