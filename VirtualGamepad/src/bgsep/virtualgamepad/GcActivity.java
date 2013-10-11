/* Copyright (C) <2013>  <Victor Olausson, Patrik Wållgren>

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

import java.util.Observable;
import java.util.Observer;
import bgsep.communication.Communication;
import bgsep.communication.CommunicationNotifier;
import bgsep.model.Button;
import bgsep.model.JoystickHandler;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;

/**
 * description...
 * @author 
 *
 */
public class GcActivity extends Activity implements Observer {

	private boolean isInitialized;
	private JoystickHandler gcJoystick;
	private Button	aButton, bButton, xButton, yButton, startButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gc);
		
		//Dim soft menu keys if present
		if (!ViewConfiguration.get(this).hasPermanentMenuKey())
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		isInitialized = false;
	}
	
	
	
	@Override
	public void onWindowFocusChanged(boolean hasChanged) {
		// Initialization of the joystick must happen when all the views has been drawn.
		// Therefor initialize it when the window has focus and not in onCreate.
		if(!isInitialized) {
			ImageView boundary  = (ImageView)findViewById(R.id.gc_joystickboundary);
			ImageView stick 	= (ImageView)findViewById(R.id.gc_joystick);
			
			Communication comm = Communication.getInstance();
			
			gcJoystick = new JoystickHandler(boundary, stick);
			gcJoystick.setLeftRightJoystickID(5, 6);
			gcJoystick.setUpDownJoystickID(7, 8);
			gcJoystick.addObserver(this);
			gcJoystick.addObserver(comm);
			
			///////
			ImageView aImageView = (ImageView)findViewById(R.id.gc_a_button);
			ImageView bImageView = (ImageView)findViewById(R.id.gc_b_button);
			ImageView xImageView = (ImageView)findViewById(R.id.gc_x_button);
			ImageView yImageView = (ImageView)findViewById(R.id.gc_y_button);
			ImageView imageStart = (ImageView)findViewById(R.id.gc_start_button);
			
			aButton = new Button(aImageView, R.drawable.gc_a_button, R.drawable.gc_a_button_pressed, 
					0, this);
			bButton = new Button(bImageView, R.drawable.gc_b_button, R.drawable.gc_b_button_pressed, 
					1, this);
			xButton = new Button(xImageView, R.drawable.gc_x_button, R.drawable.gc_x_button_pressed, 
					2, this);
			yButton = new Button(yImageView, R.drawable.gc_y_button, R.drawable.gc_y_button_pressed, 
					3, this);
			startButton = new Button(imageStart, R.drawable.gc_start_button, R.drawable.gc_start_button_pressed,
					4, this);
					
			
			
			aButton.addObserver(comm);
			bButton.addObserver(comm);
			xButton.addObserver(comm);
			yButton.addObserver(comm);
			startButton.addObserver(comm);
			
			
			isInitialized = true;
		}
		super.onWindowFocusChanged(hasChanged);
	}



	@Override
	public void update(Observable o, Object data) {
		// Joystick and Button movement handling
		
		if(o instanceof JoystickHandler && !(data instanceof CommunicationNotifier)) {
			JoystickHandler joystick = (JoystickHandler)o;
			ImageView stick = joystick.getStick();
			ImageView boundary = joystick.getBoundary();
			
			stick.setX((joystick.getX() * boundary.getWidth()/2) + boundary.getLeft() + boundary.getWidth()/2-stick.getWidth()/2);
			stick.setY((joystick.getY() * boundary.getHeight()/2 * -1) + boundary.getTop() + boundary.getHeight()/2-stick.getHeight()/2);
		}
		else if(o instanceof Button) {
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
		getMenuInflater().inflate(R.menu.gc, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		Intent i;
	    switch (item.getItemId()) {
        
	        case R.id.action_nes:
	        	i = new Intent(this, NesActivity.class);
	    		startActivity(i);
	            finish();
	            return true;
	        
	        case R.id.action_ps:
	        	i = new Intent(this, PsActivity.class);
	    		startActivity(i);
	            finish();
	            return true;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
