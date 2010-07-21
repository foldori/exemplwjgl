package fcampos.rawengine3D.gamecore;



import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;



import java.io.IOException;
import org.lwjgl.Sys;

import fcampos.rawengine3D.fps.FPSCounter;
import fcampos.rawengine3D.graficos.Frustum;
import fcampos.rawengine3D.graficos.ScreenManager;
import fcampos.rawengine3D.input.GameAction;





/*
 * Est� � uma classe abstrata e nela � onde criaremos nossa primeira janela e nosso primeiro
 * loop que ser� usado para fazer anima��es, simula��es, enfim, qualquer atualiza��o de objetos.
 * Os m�todos, fun��es, vari�veis est�o com os nomes em ingl�s devido a facilidade e integra��o
 * na programa��o, pois todo material de pesquisa est� em ingl�s, mas se algu�m tiver dificuldade
 * � s� me avisar.
 */

public abstract class GameCore {

	protected ScreenManager screen; //Cria um objeto do Tipo ScreenManager para gerenciar nossa janela
	protected boolean isRunning;    //Vari�vel que controla o in�cio e o t�rmino do programa
	protected float elapsedTime;	//Vari�vel que controla o varia��o de tempo que o programa ir� iterar
	
    // Cria a��es para interagir com o programa     
	public GameAction pause;
    public GameAction exit; 
    public GameAction fullScreen;
    public static Frustum gFrustum = new Frustum();
    private boolean paused; //Vari�vel para controlar quando a simula��o ir� pausar

    
    
    // M�todo principal: Respons�vel pela inicializa��o e fluxo do programa
	public void run() 
	{
		try
       {
            init();
            gameLoop();
        
       }catch(IOException e)
        {
           e.printStackTrace();
           Sys.alert("Erro", "Falhou: "+e.getMessage());
        }
       
	}
	
    protected void createGameActions()
    {
       	/* Cria dois objetos GameAction passando como par�metro o nome da A��o, o tipo de acionamento
       	 * de tecla, que nesse caso s� detectar� o clique inicial e a tecla que ser� utilizada para a a��o.
       	 */
    	pause = new GameAction("pause", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_P);
      	exit = new GameAction("Exit", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_ESCAPE);
        fullScreen  = new GameAction("FullScreen", GameAction.DETECT_INITIAL_PRESS_ONLY, Keyboard.KEY_F1);
     }
    
    
	
	//Cria a janela e define todos os estados iniciais do OpenGL;
	protected void init() throws IOException
	{
		screen = new ScreenManager(800, 600, 32); // Inicializa o objeto e define a janela com Resolu��o(800X600 e 32bits);
		setFullScreen(false); // Define a janela para n�o abrir em FullScreen e sim no modo Window
		screen.create(); // Cria janela
		
		
		/*
		 * A pr�xima linha habilita o shade suave. Shade suave mistura muito bem as cores e suaviza a ilumina��o
		 */
		glShadeModel(GL_SMOOTH); // Habilita Smooth Shading		
		
		
		/*
		 * As linhas a seguir definem a cor da janela quando ela sofre a opera��o de limpeza. Se voc� n�o sabe como
		 * as cores funcionam no OpenGL, eu explicarei rapidamente. Os valores das cores variam de 0.0f at� 1.0f.
		 * 0.0f sendo o mais escuro e 1.0f o mais claro. O primeiro par�metro depois do glClearColor � a intensidade
		 * de Vermelho, o segundo parametro � o Verde e o terceiro parametro � o Azul, assim formando o formato de cores
		 * RGB(Red-Vermelho, Green-Verde e Blue-Azul). Quanto mais pr�ximo um n�mero chegar de 1.0f, mais brilhante ou mais 
		 * clara ser� aquela cor espec�fica. O �ltimo e quarto n�mero � um valor Alpha.
		 * Quando fazemos opera��o de limpeza da janela, n�o devemos nos preocupar com esse quarto valor. Por agora
		 * deixe ele em 0.0f. Eu explicarei seu uso em outro tutorial.
		 */
		
		/*
		 * Voc� pode criar diversas cores, apenas misturando as 3 cores prim�rias aditivas(Vermelho, Verde e Azul).
		 * Ent�o, se voc� tem glClearColor(0.0f,0.0f,1.0f,0.0f), voc� pode limpar a janela com a cor Azul. Se voc� tem
		 * glClearColor(0.5f,0.0f,0.0f,0.0f) voc� pode limpar a janela com um Vermelho m�dio. Nem claro(1.0f) e nem escuro(0.0f).
		 * Para fazer um fundo de cor branca, voc� pode definir todas as cores para o m�ximo poss�vel(1.0f). Para fazer 
		 * um fundo de cor preta, voc� pode definir todas as cores para o m�nimo poss�vel(0.0f).
		 */
				
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f); //Limpa o fundo com a cor preta
		
		
		/*
		 * As pr�ximas 3 linhas tem haver como o Depth Buffer(Buffer de profundidade). Pense no Depth Buffer como camadas dentro
		 * da janela. O Depth Buffer controla o qu�o profundo os objetos est�o na tela. N�s realmente n�o usaremos depth buffer
		 * neste tutorial, mas todos os programas em OpenGL, que desenham em 3D na tela usar�o depth buffer. Ele classifica qual
		 * objeto ser� desenhado primeiro, assim um quadrado que voc� desenhou atr�s de um c�ruclo n�o aparecer� na frente dele.
		 * O depth buffer � uma parte muito importante do OpenGL.
		 */
		
		glClearDepth(1.0); 			// Define Depth Buffer
		glEnable(GL_DEPTH_TEST); 	// Habilita teste de Profundidade
        glDepthFunc(GL_LEQUAL); 	// Tipo de teste de profundidade que ir� fazer
        
        
        
        /*
         *  As linhas a seguir configuram a janela para ser visualizada em modo de perspectiva. Significa que
         *  quanto maior a dist�ncia do observador, menores os objetos ficar�o. Isto cria uma cena com aspecto muito 
         *  realista.
         *  A perspectiva neste caso � calculada com um �ngulo de visibilidade de 45 graus baseado na largura e altura da
         *  janela.
         *  As dist�ncias 0.1f e 100.0f, s�o respectivamente o ponto inicial e o ponto final, de qu�o distante
         *  podemos desenhar dentro da janela.
         *  
         *  glMatrixMode(GL_PROJECTION)indica que as pr�ximas 2 linhas de c�digo afetar� a matriz de proje��o.
         *  A matriz de proje��o � respons�vel por adicionar perspectiva para nossa cena. glLoadIdentity() � similar
         *  a um "reset", um rein�cio. Ele restaura a matriz selecionada, nesse caso a matriz de proje��o, para
         *  o seu estado inicial. Depois de chamar glLoadIdentity(), n�s configuraremos a perspectiva para nossa cena.
         *  
         *  glMatrixMode(GL_MODELVIEW) indica que qualquer transforma��o(transla��o, rota��o ou combina��o dos dois) 
         *  afetar� a matriz ModelView.
         *  A matriz ModelView � onde as informa��es referente aos objetos s�o armazenadas. Finalmente n�s reiniciamos 
         *  nossa matriz ModelView.
         */
        
               
		glMatrixMode(GL_PROJECTION); // Selecione a Matrix de Proje��o
		glLoadIdentity(); //Reinicia a Matriz de Proje��o		
		
		// Calcula a propor��o da Janela
		gluPerspective(45.0f, screen.getWidth() / screen.getHeight(), 0.1f, 500.0f);
		glMatrixMode(GL_MODELVIEW); // Selecione a Matrix de ModelView
		glLoadIdentity(); //Reinicia a Matriz ModelView		
		
		
		/*
		 * Na linha abaixo n�s diremos a OpenGL que n�s queremos que a melhor corre��o de perspectiva seja feita. Isto causa
		 * uma pequenina queda de performance, mas faz com que a vis�o da perspectiva seja muito melhor.
		 */
		
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // Define para �timos c�lculos de perspectiva		
		
		
		isRunning = true; //Define in�cio do programa para verdadeiro
		
		createGameActions(); // Cria a��es para interagirmos com o programa(Este classe executa duas a��es;
		 // 1- Quando clicamos em F1 o programa troca o estado para FullScreen(Tela cheia) ou modo janela.
		 // 2- Quando clicamos na tecla ESC, o programa termina.
	
     }
	
	//M�todo que finaliza o programa
	protected void stop()
    {
        isRunning = false;
        Display.destroy();
		System.exit(0);
    }
	
	//M�todo para definir FullScreen(Janela cheia) ou n�o.
	protected void setFullScreen(boolean fullscreen)
	{
		 screen.setFullScreen(fullscreen);
		 		
	}
		
	//M�todo que verifica se a Janela est� em FullScreen(Tela cheia)
	protected boolean isFullScreen()
	{
		 return screen.isFullscreen();
	}

	//M�todo que verifica se o programa est� no modo pause 
	protected boolean isPaused()
	{
		 return paused;
	}

	//M�todo para entrar ou sair do modo pause, a fun��o do m�todo depende do estado do programa, pausado ou rodando
	protected void setPaused()
	{
		 paused = !paused;
	}
	
	
	//Looping do programa, depois de iniciado s� terminar� ao chamar o m�todo stop()		
	public void gameLoop() 
	{
	    	long startTime = Sys.getTime(); //vari�vel que recebe o tempo inicial
			long currTime = startTime; //vari�vel que recebe o tempo corrente(inicialmente igual ao tempo inicial)
			
			
		//loop principal	
		while (isRunning) 
		{
			// calcula o quanto passou desde que n�s entramos neste loop
			// e guarda este valor para que as rotinas de update(atualizar)
			// e render(desenhar) saibam quanto tempo tem para atualiza��o de desenho.
						
			elapsedTime = (float)(Sys.getTime() - currTime);
			
			elapsedTime /= 1000; // tempo em  milisegundos
			FPSCounter.update(elapsedTime);
			
			//System.out.println(elapsedTime);
			currTime = Sys.getTime(); //recebe o tempo corrente do sistema
			update(elapsedTime);
			
			
			render(); //chama m�todo desenhar
			 
			
			
			// finalmente dizemos para o Display fazer uma atualiza��o.
			// agora que n�s desenhamos toda nossa cena, n�s apenas atualizamos ela
			// na janela.
			
			screen.update();
			
			
			// Se o usu�rio fechar a janela, apertando CTRL+F4, ou clicando no
			// no bot�o de fechar ent�o n�s precisamos parar o looping e terminar o programa.
			if (screen.isCloseRequested()) 
			{
				stop();
			}
		}
		
	}
	
	//M�todo b�sico que � usado para atualizar estados de objetos. Para uma maior utilidade, deve ser sobreecrito
	//numa classe filha.
    protected void update(float elapsedTime)
    {
       	checkSystemInput();
    	checkGameInput();
    }

    // Verifica se a a��o de sa�da foi acionada
    protected void checkSystemInput()
    {
    	if (pause.isPressed())
        {
            setPaused();
        }
      	if (exit.isPressed())
    	{
    		stop();
    	}
    }
        
    /* Verifica se as teclas utilizadas nos nossos programas forma pressionadas
     * Este m�todo deve ser sobreescrito em classes filhas de acordo com a funcionalidade do programa a ser escrito.
     */
    protected void checkGameInput()
	{
    	if (fullScreen.isPressed())
    	{
    		setFullScreen(!isFullScreen());
    	}
	}

	
	//M�todo abstrato, obrigando a ser implementado numa classe filha. Deve conter a rotina de desenho.
	protected abstract void render();
	

}