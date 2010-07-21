
package fcampos.rawengine3D.input;

import java.util.List;
import java.util.ArrayList;
import org.lwjgl.input.Keyboard;
/**
 *
 * @author Fabio
 */

public class InputManager {

    private static final int NUM_KEY_CODES = 600;

    private GameAction[] KeyActions;

    public InputManager()
    {
        KeyActions = new GameAction[NUM_KEY_CODES];
    }

    public void mapToKey(GameAction gameAction, int keyCode)
    {
        gameAction.setKeyCode(keyCode);
        
        KeyActions[gameAction.getKeyCode()] = gameAction;
        
    }

    public void clearMap(GameAction gameAction)
    {
        for (int i=0; i < KeyActions.length; i++)
        {
            if (KeyActions[i] == gameAction)
            {
                KeyActions[i] = null;
            }
        }
        gameAction.reset();
    }

    public List<String> getMaps(GameAction gameCode)
    {
        ArrayList<String> list = new ArrayList<String>();

        for (int i=0; i<KeyActions.length; i++)
        {
            if (KeyActions[i] == gameCode)
            {
                list.add(gameCode.getKeyName());
            }
        }
        return list;
    }

    public void resetAllGameActions()
    {
        for (int i=0; i<KeyActions.length; i++)
        {
            if (KeyActions[i] != null)
            {
                KeyActions[i].reset();
            }
        }
    }

    public String getKeyName(GameAction gameAction)
    {
        for (int i=0; i < KeyActions.length; i++)
        {
            if (gameAction != null)
            {
                if (KeyActions[i] == gameAction)
                {
                 return KeyActions[i].getKeyName();
                }
            }
        }
        return null;
    }

    public GameAction getKeyAction(int KeyCode)
    {
        if (KeyCode < KeyActions.length)
        {
            return KeyActions[KeyCode];
        } else {
                   return null;
                }

    }

    @SuppressWarnings("unused")
	public void KeyPressed(int KeyCode)
    {
        boolean isPressed = Keyboard.isKeyDown(KeyCode);
        GameAction gameAction = getKeyAction(KeyCode);
        if (isPressed)
        {
            if (gameAction != null)
            {
                gameAction.press();
            }
         else {
                if (gameAction != null)
                    {
                        gameAction.release();

                    }
              }
        }
    }

    public void KeyReleased(int KeyCode)
    {
        KeyPressed(KeyCode);
    }
}


