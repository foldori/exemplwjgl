package fcampos.rawengine3D.gamecore;

import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import java.io.IOException;
import org.lwjgl.Sys;

import fcampos.rawengine3D.graficos.*;



/*
 * Est� � uma classe abstrata e nela � onde criamos nossa primeira janela e nosso primeiro
 * loop que ser� usado para fazer anima��es, simula��es, enfim, qualquer movimento com objetos.
 * Os m�todos, fun��es, vari�veis est�o com os nomes em ingl�s devido a facilidade e integra��o
 * na programa��o, pois todo material de pesquisa est� em ingl�s.
 */

public abstract class GameCore {

	protected ScreenManager screen; //Cria um objeto do Tipo ScreenManager para gerenciar nossa janela
	protected boolean isRunning;    //vari�vel que controla o in�cio e o t�rmino do programa
	protected float elapsedTime;	//vari�vel que controla o dt(delta de tempo) que o programa ir� iterar

     
 // Este � o objeto global Frustum que manter� nosso Frustum durante a execu��o do programa
    public static Frustum gFrustum = new Frustum();
    
    private boolean paused; //vari�vel para controlar quando a simula��o ir� pausar

    
    //M�todo principal: Respons�vel pela inicializa��o e fluxo do programa
	protected void run() 
	{
		try
       {
            init();
            gameLoop();
        
       }catch(IOException e)
        {
           e.printStackTrace();
           Sys.alert("Error", "Failed: "+e.getMessage());
        }
       
	}
	
	//Cria a janela e seta todos os estados iniciais do OpenGL;
	protected void init() throws IOException
	{
		screen = new ScreenManager(800, 600, 32); //Inicializa o objeto e seta a janela com Resolu��o(800X600 e 32bits);
		setFullScreen(false); //Seta a janela para n�o abrir em FullScreen e sim no modo Window
		screen.create(); //Cria janela
		
		glEnable(GL_TEXTURE_2D); // Habilita Texture Mapping
        glShadeModel(GL_SMOOTH); // Habilita Smooth Shading		
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f); //Limpa o fundo com a cor preta
		glClearDepth(1.0); // Seta Depth Buffer
		glEnable(GL_DEPTH_TEST); // Habilita teste de Profundidade
        glDepthFunc(GL_LEQUAL); // Tipo de teste de profundidade que ir� fazer
        
		glMatrixMode(GL_PROJECTION); // Selecione a Matrix de Proje��o
		glLoadIdentity(); //Reseta a Matrix de Proje��o		
		
		// Calcula a propor��o da Janela
		gluPerspective(45.0f, screen.getWidth() / screen.getHeight(), 1.0f, 1000.0f);
		glMatrixMode(GL_MODELVIEW); // Selecione a Matrix de ModelView
		
		// Seta �timos calculos de perspectiva		
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		
		isRunning = true; //Seta in�cio do programa para verdadeiro
	
     }
	
	//M�todo que finaliza o programa
	public void stop()
    {
        isRunning = false;
        Display.destroy();
		System.exit(0);
    }
	
	//M�todo para setar FullScreen(Janela cheia) ou n�o.
	protected void setFullScreen(boolean fullscreen)
	{
		 screen.setFullScreen(fullscreen);
		 		
	}
		
	//M�todo que checa se a Janela est� em FullScreen
	protected boolean isFullScreen()
	{
		 return screen.isFullscreen();
	}

	//M�todo que checa se o programa est� no modo pause 
	protected boolean isPaused()
	{
		 return paused;
	}

	//M�todos para entrar ou sair do modo pause, a fun��o do m�todo depende do estado do programa
	//pausado ou rodando
	protected void setPaused()
	{
		 paused = !paused;
	}
	
	
	//Looping do programa, depois de iniciado s� terminar� ao chamar o m�todo stop()		
	public void gameLoop() 
	{
	    	long startTime = Sys.getTime(); //vari�vel que recebe o tempo inicial
			long currTime = startTime; //vari�vel que recebe o tempo corrente(inicialmente igual o tempo inicial)
			
			
		//loop principal	
		while (isRunning) 
		{
			// calcula o quanto passou desde que n�s entramos neste loop
			// e guarda este valor para que as rotinas de update(atualizar)
			// e render(desenhar) saibam quanto tempo tem para atualiza��o de desenho.
						
			elapsedTime = (float)(Sys.getTime() - currTime);
			currTime += elapsedTime;
			
			elapsedTime /= 1000;
			
			update(elapsedTime);
			
			currTime = Sys.getTime(); //recebe o tempo corrente do sistema
			
			render(); //chama m�todo desenhar
			 
			// finalmente dizemos para o Display fazer uma atualiza��o.
			// agora que n�s desenhamos toda nossa cena, n�s apenas atualizamos ela
			// na janela.
			
			Display.update();
			
			// Se o usu�rio fechar a janela, apertando CTRL+F4, ou clicando no
			// no bot�o de fechar ent�o n�s precisamos para o loop e terminar o programa.
			
			if (Display.isCloseRequested()) {
				stop();
			}
		}
		
	}
	
	//M�todo que pode ser usado para atualizar estados de objetos(N�o implementado nesta classe)
	protected void update(float elapsedtime){}; 
	
	//M�todo abstrato, obrigando a ser implementado numa classe filha. Deve conter a rotina de desenho.
	protected abstract void render();
	
	//M�todo para desenhar o fundo da janela(Programas 2D)	
	protected void drawBackground(Texture background, int x, int y)
	{
		
		screen.enterOrtho(); // Entra no modo Ortogonal(2D)
		glPushMatrix(); // Empilha a Matrix
		
        background.bind(); // Vincula a Textura de fundo

		glTranslatef(x, y, 0); //Seta a posi��o inicial da textura
		glBegin(GL_QUADS); //Desenha um pol�gono de 4 lados
			glTexCoord2f(0,0); // Seta a coordenada de textura(canto superior esquerdo)
			glVertex2i(0,0); // Seta a coordenada de posi��o(canto superior esquerdo)
			glTexCoord2f(0,1);// Seta a coordenada de textura(canto inferior esquerdo)
			glVertex2i(0,screen.getHeight()); // Seta a coordenada de posi��o(canto inferior esquerdo)
			glTexCoord2f(1,1); // Seta a coordenada de textura(canto inferior direito)
			glVertex2i(screen.getWidth(),screen.getHeight()); // Seta a coordenada de posi��o(canto inferior direito)
			glTexCoord2f(1,0); // Seta a coordenada de textura(canto superior direito)
			glVertex2i(screen.getWidth(),0); // Seta a coordenada de posi��o(canto superior direito)
		glEnd(); //fim do pol�gono
		
        glPopMatrix(); //Desempilha a Matrix
		
        screen.leaveOrtho(); //Sai do modo Ortogonal(2D)
	}
	
	/*
 // Calcula e retorna a taxa de quadros por segundo
    protected int CalculaQPS()
    {
    	// Incrementa o contador de quadros
    	numquadro++;

    	// Obt�m o tempo atual
    	float tempo = getElapsedTime();
    	// Verifica se passou mais um segundo
    	if (tempo - tempoAnterior > 1000)
    	{
    		// Calcula a taxa atual
    		ultqps = numquadro*1000.0f/(tempo - tempoAnterior);
    		// Ajusta as vari�veis de tempo e quadro
    	 	tempoAnterior = tempo;
    		numquadro = 0;
    	}
    	// Retorna a taxa atual
    	return (int)ultqps;
    }
*/
}
