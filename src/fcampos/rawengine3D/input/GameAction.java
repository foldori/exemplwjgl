package fcampos.rawengine3D.input;


import org.lwjgl.input.Keyboard;


public class GameAction {

/**
	Comportamento "Normal". O m�todo isPressed() retornar� verdadeiro enquanto 
	a tecla estiver pressionada.
*/
public static final int NORMAL = 0;

/**
	Comportamento "S� Detectar o Pressionamento Inicial". O m�todo isPressed() retornar� verdadeiro somente 
	ap�s a tecla ser pressionada pela primeira vez, e n�o novamente at� que a tecla seja solta 
	e pressionada novamente.
*/
public static final int DETECT_INITIAL_PRESS_ONLY = 1;

/**
 * Vari�veis constantes representando os poss�veis estados de uma tecla.
 */
private static final int STATE_RELEASED = 0;
private static final int STATE_PRESSED = 1;
private static final int STATE_WAITING_FOR_RELEASE = 2;


private String name; // Nome da a��o que a tecla representar�

private int behavior; // Comportamento da a��o

private int amount; // Quantidade que poder� ser usada para movimenta��o com mouse
private int state;  // Estado da tecla
private int keyCode; // C�digo da tecla.


// Inicia a a��o com compartamento padr�o(NORMAL)
public GameAction(String name)
{
    this(name, NORMAL);
}


/**
 *  Inicia a a��o com compartamento passado pelo usu�rio e reinicia o estado da tecla e quantidade de quanto ela ficou
 * 	pressionada
 */

public GameAction(String name, int behavior)
{
    this.name = name;
    this.behavior = behavior;
    reset();
}


/**
 * Faz mesmo procedimento do construtor acima e configura a tecla que ser� utilizada para a a��o.
 */
public GameAction(String name, int behavior, int keyCode)
{
	this(name, behavior);
    setKeyCode(keyCode);
}

/**
 *  Retorna o nome da a��o.
 */
public String getName()
{
    return name;
}

/**
 *  Define qual tecla ser� utilizada para a a��o
 */
public void setKeyCode(int keyCode)
{
    this.keyCode = keyCode;
}

/**
 *  Retorna o c�digo da tecla
 */
public int getKeyCode()
{
    return keyCode;
}

/**
 *  Retorna o nome da tecla
 */
public String getKeyName()
{
    return Keyboard.getKeyName(keyCode);
}

/**
 Reinicia o estado da tecla e a quantidade que ela ficou pressionada
 */
public void reset()
{
    state = STATE_RELEASED;
    amount = 0;
}

/**
Este m�todo � como se Apertasse a tecla e soltasse rapidamente. Mesma coisa que chamar press() seguido de release().
*/
public synchronized void tap() 
{
	press();
	release();
}

/**
Muda o estado para tecla pressionada.
*/
public synchronized void press()
{
    press(1);
}


/**
Marca que a tecla foi pressionada � um determinado n�mero de vezes, 
ou que o mouse se moveu � uma determinada dist�ncia.
*/
public synchronized void press(int amount)
{
    if (state != STATE_WAITING_FOR_RELEASE)
    {
        this.amount += amount;
        state = STATE_PRESSED;
    }
}

/**
Muda o estado para tecla solta.
*/
public synchronized void release()
{
   state = STATE_RELEASED;
}

/**
Retorna se a tecla foi pressionada ou n�o
desde a �ltima verifica��o.
*/
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


/**
Para as teclas, este m�todo retorna o n�mero de vezes que a tecla 
foi pressionada desde a �ltima verifica��o.
Para o movimento do mouse, este m�todo retorna a dist�ncia
que ele foi movido.
*/
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

/**
 	M�todo de Debug
 */

@Override
public String toString()
{
	return (name + "- "+ state + "- "+ behavior);
}
}
