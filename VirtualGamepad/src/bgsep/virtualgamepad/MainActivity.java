/*
   Copyright (C) 2013  Patrik Wållgren Victor Olausson

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
package bgsep.virtualgamepad;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.WindowManager;
import bgsep.bluetooth.BluetoothHandler;
import bgsep.bluetooth.SenderImpl;
import bgsep.communication.Communication;
import bgsep.model.Button;

/**
 * This is the starting activity to allow a user to choose a gamepad,
 * get information/help about the app, and (dis)connect to the server  
 * @author Patrik Wållgren
 * @author Victor Olausson
 *
 */
public class MainActivity extends Activity implements Observer {

	private final int NES_CONTROLLER = 45,
					  GC_CONTROLLER  = 46,
					  PS_CONTROLLER  = 47;
	
	private BluetoothHandler bh;
	private ImageView communicationIndicator, communicationButton;
	private ImageView imageNESbutton, imageGCbutton, imagePSbutton;
	private Animation rotate;
	private PopupWindow popupMenu;
	private PopupWindow popupAbout;
	private boolean hapticFeedback;
	private boolean useAccelerometer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		if (bh == null) {
			bh = new BluetoothHandler(this);
		}

		SenderImpl si = new SenderImpl(bh);
		Communication communication = Communication.getInstance();
		communication.setSender(si);
		hapticFeedback = false;
		useAccelerometer = false;		
		init();
	}
	
	private void init() {
		initControllerButtons();
		initConnectionButtons();
		initSettingsMenu();
		initAboutPopup();
	}
	
	private void startBluetooth() {
		bh.startThread();
	}
	
	@Override
	public void update(Observable o, Object obj) {
		if(o instanceof Button) {
			Button button = (Button)o;
			Intent i;
			if(button.isPressed())
				button.getButtonView().setImageResource(button.getPressedDrawableID());			
			else {
				switch(button.getButtonID()) {
				case NES_CONTROLLER:
					i = new Intent(this, NesActivity.class);
					i.putExtra("hapticFeedback", hapticFeedback);
					i.putExtra("useAccelerometer", useAccelerometer);
					startActivity(i);
					break;
				case GC_CONTROLLER:
					i = new Intent(this, GcActivity.class);
					i.putExtra("hapticFeedback", hapticFeedback);
					i.putExtra("useAccelerometer", useAccelerometer);
					startActivity(i);
					break;
				case PS_CONTROLLER:
					i = new Intent(this, PsActivity.class);
					i.putExtra("hapticFeedback", hapticFeedback);
					i.putExtra("useAccelerometer", useAccelerometer);
					startActivity(i);
					break;
				default:
					break;
				}
				button.getButtonView().setImageResource(button.getUnPressedDrawableID());
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(popupMenu.isShowing())
			popupMenu.dismiss();
		if(popupAbout.isShowing())
			popupAbout.dismiss();

		bh.cancelConnectionAttempt();
		bh.disconnect(true, "Disconnected");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(popupMenu.isShowing())
			popupMenu.dismiss();
		if(popupAbout.isShowing())
			popupAbout.dismiss();
	}
	
	/**
	 * Indicate to GUI that the server is not connected. 
	 */
	public void serverDisconnected() {
		if(communicationIndicator.getVisibility() == View.VISIBLE) {
			communicationIndicator.setAnimation(null);
			communicationIndicator.setVisibility(View.INVISIBLE);
		}
		communicationButton.setImageResource(R.drawable.mainpage_red_arrows);
	}
	
	/**
	 * Indicate to GUI that the server is connected.
	 */
	public void serverConnected() {
		communicationIndicator.setAnimation(null);
		communicationIndicator.setVisibility(View.INVISIBLE);
		communicationButton.setImageResource(R.drawable.mainpage_green_arrows);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BluetoothHandler.BLUETOOTH_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				startBluetooth();
			} else {
				bh.cancelConnectionAttempt();
			}
		}
	}	
	
	public void indicateConnecting() {
		communicationButton.setImageResource(R.drawable.mainpage_connect_button);
		communicationIndicator.setVisibility(View.VISIBLE);
		communicationIndicator.startAnimation(rotate);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		bh.autoConnect();
	}
	
	private void initSettingsMenu() {
		LayoutInflater layoutInflater = 
				(LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		final View menuView = layoutInflater.inflate(R.layout.menu_popup, null);
		popupMenu = new PopupWindow(menuView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		final ImageView settingsButton = (ImageView) findViewById(R.id.mainpage_smalldots_button);
		final RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.mainpage_main_layout);
		settingsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ImageView anchor = (ImageView)findViewById(R.id.mainpage_menu_anchor);
				popupMenu.showAsDropDown(anchor, 0, 0);
				TextView txtAbout = (TextView)menuView.findViewById(R.id.menu_about);
				final CheckBox hapticCheckbox = (CheckBox)menuView.findViewById(R.id.menu_chkbox_haptic);
				final CheckBox accCheckbox = (CheckBox)menuView.findViewById(R.id.menu_chkbox_accelerometer);
				
				txtAbout.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						popupAbout.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
						popupMenu.dismiss();
					}
				});
				
				hapticCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(hapticCheckbox.isChecked())
							hapticFeedback = true;
						else
							hapticFeedback = false;
					}
				});
				
				accCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(accCheckbox.isChecked())
							useAccelerometer = true;
						else
							useAccelerometer = false;	
					}
				});
			}
		});
		
		// Dismiss the popupMenu when user presses anywhere on the background
		mainLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(popupMenu.isShowing())
					popupMenu.dismiss();
				if(popupAbout.isShowing())
					popupAbout.dismiss();
			}
		});
	}
	
	private void initAboutPopup() {
		LayoutInflater layoutInflater = 
				(LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		final View aboutView = layoutInflater.inflate(R.layout.about_popup, null);
		popupAbout = new PopupWindow(aboutView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		TextView txtInfo = (TextView)aboutView.findViewById(R.id.about_info);
		
		txtInfo.setMovementMethod(LinkMovementMethod.getInstance());
		
		android.widget.Button closeButton = (android.widget.Button)aboutView.findViewById(R.id.about_close_button);
		
		closeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popupAbout.dismiss();
			}
		});
	}
	
	private void initConnectionButtons() {
		communicationButton = (ImageView) findViewById(R.id.mainpage_connection_button);
		communicationIndicator = (ImageView)findViewById(R.id.mainpage_connection_indicator);
		
		communicationIndicator.setVisibility(View.INVISIBLE);
		rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_view);
		
		communicationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(bh.isConnected()) {
					bh.disconnect(true, "Disconnected");
				} else {
					startBluetooth();
				}
			}
		});
		
		communicationIndicator.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bh.cancelConnectionAttempt();
			}
		});
	}
	
	private void initControllerButtons() {
		imageNESbutton = (ImageView)findViewById(R.id.mainpage_nes);
		imageGCbutton = (ImageView)findViewById(R.id.mainpage_gc);
		imagePSbutton = (ImageView)findViewById(R.id.mainpage_ps);
		
		new Button(imageNESbutton, R.drawable.mainpage_nes, R.drawable.mainpage_nes_pr,
				NES_CONTROLLER, this);
		new Button(imageGCbutton, R.drawable.mainpage_gc, R.drawable.mainpage_gc_pr,
				GC_CONTROLLER, this);
		new Button(imagePSbutton, R.drawable.mainpage_ps, R.drawable.mainpage_ps_pr,
				PS_CONTROLLER, this);
	}
}
