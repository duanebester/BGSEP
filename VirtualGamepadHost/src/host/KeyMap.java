package host;

import java.util.ArrayList;

public final class KeyMap {
	private static ArrayList<Integer> keyCodes;
	
	private static final int KEYS_PER_CLIENT = 25;
	
	private static final int MY_COMPUTER = 182;
	private static final int MY_CALCULATOR = 183;
	private static final int NUM_LOCK = 144;
	private static final int SCROLL_LOCK = 145;

	static {
		keyCodes = new ArrayList<Integer>();
		for (int i = 1; i <= 7; i++) {
			keyCodes.add(i);
		}
		for (int i = 21; i <= 26; i++) {
			keyCodes.add(i);
		}
		for (int i = 124; i <= 249; i++) {
			if (i != MY_COMPUTER && i != MY_CALCULATOR && i != NUM_LOCK && i != SCROLL_LOCK) { 
				keyCodes.add(i);
			}
		}
	}
	
	public static int getKeyCode(int clientID, int buttonID) {
		int index = clientID * KEYS_PER_CLIENT + buttonID;
		if (index < keyCodes.size()) {
			return keyCodes.get(clientID * KEYS_PER_CLIENT + buttonID);
		} else {
			throw new IllegalArgumentException("clientID " + clientID + " is too big!");
		}
		
	}
}