package fcampos.rawengine3D.input;


import org.lwjgl.input.Keyboard;

/**
 *
 * @author Fabio
 */
	public class GameAction {

    /**
Normal behavior. The isPressed() method returns true
as long as the key is held down.
*/
    public static final int NORMAL = 0;

    /**
Initial press behavior. The isPressed() method returns
true only after the key is first pressed, and not again
until the key is released and pressed again.
*/
    public static final int DETECT_INITIAL_PRESS_ONLY = 1;

    private static final int STATE_RELEASED = 0;
    private static final int STATE_PRESSED = 1;
    private static final int STATE_WAITING_FOR_RELEASE = 2;

    private String name;
    private int behavior;
    private int amount;
    private int state;
    private int keyCode;

    public GameAction(String name)
    {
        this(name, NORMAL);
    }

    public GameAction(String name, int behavior)
    {
        this.name = name;
        this.behavior = behavior;
        reset();
    }

    public String getName()
    {
        return name;
    }

    public void setKeyCode(int keyCode)
    {
        this.keyCode = keyCode;
    }
    
    public int getKeyCode()
    {
        return keyCode;
    }

    public String getKeyName()
    {
        return Keyboard.getKeyName(keyCode);
    }
    
    public void reset()
    {
        state = STATE_RELEASED;
        amount = 0;
    }

    public synchronized void tap()
    {
        press();
        release();
    }

    public synchronized void press()
    {
        press(1);
    }

    public synchronized void press(int amount)
    {
        if (state != STATE_WAITING_FOR_RELEASE)
        {
            this.amount += amount;
            state = STATE_PRESSED;
        }
    }

    public synchronized void release()
    {
       state = STATE_RELEASED;
    }

    public synchronized boolean isPressed()
    {
        if (Keyboard.isKeyDown(keyCode))
        {
        	press();
            if (getAmount() != 0)
            {
               	return true;
            }else{
                return false;
                }
        }else {
            release();
        }
        return false;
    }
    public synchronized int getAmount()
    {
        int retVal = amount;
        if (retVal != 0)
        {
            if (state == STATE_RELEASED)
            {
                amount = 0;
            } else if (behavior == DETECT_INITIAL_PRESS_ONLY)
            {
                state = STATE_WAITING_FOR_RELEASE;
                amount = 0;
            }
        }
        return retVal;
    }
 
    @Override
    public String toString()
    {
    	return (name + "- "+ state + "- "+ behavior);
   }
}
