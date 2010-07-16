package fcampos.rawengine3D.model;


/*
*****************************************************


 Biblioteca de rotinas auxiliares que inclui fun��es 
 para realizar as seguintes tarefas:
 - Normalizar um vetor;
 - Calcular o produto vetorial entre dois vetores;
 - Calcular um vetor normal a partir de tr�s v�rtices;
 - Rotacionar um v�rtice ao redor de um eixo (x, y ou z);
 - Ler um modelo de objeto 3D de um arquivo no formato 
		OBJ e armazenar em uma estrutura;
 - Desenhar um objeto 3D recebido por par�metro;
 - Calcular o vetor normal de cada face de um objeto 3D;
 - Decodificar e armazenar numa estrutura uma imagem JPG, PNG ou GIF 
		para usar como textura;
 - Armazenar em uma estrutura uma imagem JPG, PNG ou GIF para usar 
		como textura.
*/

import fcampos.rawengine3D.graficos.*;
import fcampos.rawengine3D.resource.*;
import fcampos.rawengine3D.MathUtil.*;

import java.io.IOException;
import java.util.ArrayList;


import java.io.*;
import static org.lwjgl.opengl.GL11.*;

public class ObjLoader {

	private ArrayList<Obj> objects;
	private TextureManager texManager;
	private Obj obj;
	
	float branco[] = { 1.0f, 1.0f, 1.0f, 1.0f };	// constante para cor branca
	public ObjLoader()
	{
		objects = new ArrayList<Obj>();
		texManager = new TextureManager();
		
	}

	/*
	 Cria e carrega um objeto 3D que esteja armazenado em um
	 arquivo no formato OBJ, cujo nome � passado por par�metro.
	 � feita a leitura do arquivo para preencher as estruturas 
	 de v�rtices e faces, que s�o retornadas atrav�s de um OBJ.
	
	 O par�metro mipmap indica se deve-se gerar mipmaps a partir
	 das texturas (se houver)
	*/
	
	public Obj carregaObjeto(String arqName, boolean mipmap, boolean useAnisotropicFilter) throws IOException
	{
		
		int vcont,ncont,fcont,tcont;
		int material, texid;
		BufferedReader reader = null;
		obj = new Obj();
		Texture tex = null;
		Vector3f[] vertices = null;
		Vector3f[] normals = null;
		Vector3f[] texCoords = null;
		int numVert = 0; int numFace = 0; int numNormais = 0; int numTexCoords = 0;
		try {
			reader = openArq(arqName);
		
			
	
			while(true)
			{
				String line = reader.readLine();

				if (line == null)
				{
					reader.close();
					break;
        	
				}

				if (line.startsWith("v ")) // encontramos um v�rtice
					numVert++;
				if (line.startsWith("f ")) // encontramos uma face
					numFace++;
				if (line.startsWith("vn ")) // encontramos uma normal
					numNormais++;
				if (line.startsWith("vt ")) // encontramos uma texcoord
					numTexCoords++;

			}
	
	
			obj.setNumVertices(numVert);
			obj.setNumFaces(numFace);
			obj.setNumNormais(numNormais);
			obj.setNumTexcoords(numTexCoords);
			
			vertices = new Vector3f[numVert];
			for(int i=0; i < vertices.length; i++)
			{
				vertices[i] = new Vector3f();
			}
			
			normals = new Vector3f[numNormais];
			for(int i=0; i < normals.length; i++)
			{
				normals[i] = new Vector3f();
			}
			
			texCoords = new Vector3f[numTexCoords];
			for(int i=0; i < texCoords.length; i++)
			{
				texCoords[i] = new Vector3f();
			}
			
		}catch(IOException e)
		{
			e.getMessage();
		}
	
	/*
		//DEBUG
		System.out.println("Vertices:  " + obj.getNumVertices());
		System.out.println("Faces:     " + obj.getNumFaces());
		System.out.println("Normais:   " + obj.getNumNormais());
		System.out.println("Texcoords: " + obj.getNumTexcoords());
		//fim DEBUG
	 */
		/* Especifica��o do formato .obj para objetos poligonais:
		 * 
		 * # - comment�rio
		 * v x y z - v�rtice
		 * vn x y z - normal
		 * vt x y z - texcoord
		 * f a1 a2 ... - face com apenas �ndices de v�rtices
		 * f a1/b1/c1 a2/b2/c2 ... - face com �ndices de v�rtices, texcoords e normais
		 * f a1//c1 a2//c2 ... - face com �ndices de v�rtices e normais (sem texcoords)
		 * f a1/b1 a2/b2 ... - face com �ndices de v�rtices e texcoords (sem normais)
		 *
		 * Campos ignorados:
		 * g nomegrupo1 nomegrupo2... - especifica que o objeto � parte de um ou mais grupos
		 * s numerogrupo ou off - especifica um grupo de suaviza��o ("smoothing")
		 * o nomeobjeto: define um nome para o objeto
		 *
		 * Biblioteca de materiais e textura:
		 * mtllib - especifica o arquivo contendo a biblioteca de materiais
		 * usemtl - seleciona um material da biblioteca
		 * usemat - especifica o arquivo contendo a textura a ser mapeada nas pr�ximas faces
		 * 
		 */
		
		// A segunda passagem � para ler efetivamente os
		// elementos do arquivo, j� que sabemos quantos
		// tem de cada tipo
		vcont = 0;
		ncont = 0;
		tcont = 0;
		fcont = 0;
		// Material corrente = nenhum
		material = -1;
		// Textura corrente = nenhuma
		texid = -1;

		// Utilizadas para determinar os limites do objeto
		// em x,y e z
		float minx,miny,minz;
		minx = 0; miny = 0; minz = 0;
		float maxx,maxy,maxz;
		maxx = 0; maxy = 0; maxz = 0;
		
		
		reader = openArq(arqName); // abre arquivo texto para leitura
		obj.setName(arqName);
		
		while(true)
		{
			String line = reader.readLine();

            if (line == null)
            {
            	reader.close();
                break;
            }
            
    		if(line.startsWith("#")) 
    			continue;
    		
    		if(line.startsWith("o")) 
    			continue;
    		
    		// Defini��o da biblioteca de materiais ?
    		if(line.startsWith("mtllib"))
    		{
    				// Chama fun��o para ler e interpretar o arquivo
    				// que define os materiais
    				readMaterials(line.substring(7), obj); //modificado
				
    				// Indica que o objeto possui materiais
    				obj.setTemMateriais(true);
    				continue;
    		}
    		
    		// Sele��o de material ?
    		if(line.startsWith("usemtl"))
    		{
    			// Procura pelo nome e salva o �ndice para associar
    			// �s pr�ximas faces
    			material = obj.findMaterial(line.substring(7));
    			
    			texid = -1;
    			continue;
    		}
    		
    		// Sele��o de uma textura (.jpg, .gif, .png)
    		if(line.startsWith("usemat"))
    		{
    			// �s vezes, � especificado o valor (null) como textura
    			// (especialmente em se tratando do m�dulo de exporta��o
    			// do Blender)
    			if(line.substring(7).equalsIgnoreCase("(null)"))
    			{
    				texid = -1;
    				continue;
    			}
    			
    			
    			// Tenta carregar a textura
    			if(line.substring(7).contains("jpg"))
    			{
    				tex = texManager.getFlippedImage(line.substring(7), mipmap, useAnisotropicFilter);
    			}else
    				{
    					tex = texManager.getNormalImage(line.substring(7), mipmap, useAnisotropicFilter);
    				}
    			texid = tex.getTexID();
    			
    			
    		}
    		    		
    		// V�rtice ?
    		if (line.startsWith("v "))
    		{
    			String[] lin = line.substring(2).split(" ");
    			
    			vertices[vcont].setTo(Conversion.convertK3f(lin));
    			//obj.setVertices(Conversion.convertK3f(lin), vcont);
    			
    			
    			if(vcont == 0)
    			{
    				minx = maxx = vertices[vcont].x;
    				miny = maxy = vertices[vcont].y;
    				minz = maxz = vertices[vcont].z;
    			}
    			else
    			{
    				if(vertices[vcont].x < minx) minx = vertices[vcont].x;
    				if(vertices[vcont].y < miny) miny = vertices[vcont].y;
    				if(vertices[vcont].z < minz) minz = vertices[vcont].z;
    				if(vertices[vcont].x > maxx) maxx = vertices[vcont].x;
    				if(vertices[vcont].y > maxy) maxy = vertices[vcont].y;
    				if(vertices[vcont].z > maxz) maxz = vertices[vcont].z;
    			}
    			
    			
    			vcont++;
    			obj.setDimMin(minx, miny, minz);
    			obj.setDimMax(maxx, maxy, maxz);
    			continue;
    		}
    		
    		// Normal ?
    		if (line.startsWith("vn "))
    		{
    			String[] lin = line.substring(3).split(" ");
    			
    			normals[ncont].setTo(Conversion.convertK3f(lin));
    			//obj.setNormais(Conversion.convertK3f(lin), ncont);
    			
    			ncont++;
    			// Registra que o arquivo possui defini��o de normais por
    			// v�rtice
    			//obj.setNormaisPorVertice(true);
    			continue;
    		}
    		
    		// Texcoord ?
    		if (line.startsWith("vt "))
    		{
    			String[] lin = line.substring(3).split(" ");
    			
    			texCoords[tcont].setTo(Conversion.convertK3f(lin));
    			//obj.setTexcoords(Conversion.convertK3(lin), tcont);
    						
    			tcont++;
    			continue;
    		}
    		
    		// Face ?
    		if (line.startsWith("f "))
    		{
    			    			
    			// Associa � face o �ndice do material corrente, ou -1 se
    			// n�o houver
    			
    			obj.getFace(fcont).setIndMat(material);
    			if(material !=  -1)
    			{
    				obj.getFace(fcont).setMaterial(obj.getMaterial(material));
    			}
    			
    			// Associa � face o texid da textura selecionada ou -1 se
    			// n�o houver
    			obj.getFace(fcont).setTexId(texid);
    			    			
    			// Tempor�rios para armazenar os �ndices desta face
    			int vi[] = new int[4];
    			int ti[] = new int[4];
    			int ni[] = new int[4];
    					
    			
    			int nv = 0;
    			boolean tem_t = false;
    			boolean tem_n = false;
    			// Interpreta a descri��o da face
    			
    			String[] temp = null;
    			String[] nova = null;
    			int barra = 0;
    			ArrayList<String> lista = new ArrayList<String>();
    			
    				temp = line.substring(2).split(" ");
   					
    				if(temp[0].contains("//"))
    		    	   {
    		    	    	barra=3;
    		    	   }else 
    		    	    	{    					
    		    	    		for(int j = 0; j < temp[0].length() - 1; j++)
    		    	    			{ 		    	    					
    		    	    				char ch = temp[0].charAt(j);
   		    	    					if(ch == '/')
   		    	    					{
    		    	    					barra++;
    		    	    				}
		    	    				}
		    	 			}
    				nova = new String[temp.length];
    	    		
    	    			
    	    		if(barra == 3)
    	    		{
    	    			for(int j=0; j < temp.length; j++)
    	    			{
    	    				nova = temp[j].split("//");
    	    				
    	    				for(int a = 0; a < nova.length; a++)
    	    				{
    	    				
    	    				lista.add(nova[a]);
    	    				
    	    				}
    	    			}
    	    			for (int b=0; b < lista.size() -1 ; b+=2)
    	    			{
    	    				vi[nv] = Integer.parseInt(lista.get(b));
    	    				ni[nv] = Integer.parseInt(lista.get(b+1));
    						tem_n = true;
    						nv++;

    	    			}
    	    		}
    	    		
    	    		if(barra == 2)
    	    		{
    	    			for(int j=0; j < temp.length; j++)
    	    			{
    	    				nova = temp[j].split("/");
    	    				
    	    				for(int a = 0; a < nova.length; a++)
    	    				{
    	    				
    	    				lista.add(nova[a]);
    	    				
    	    				}
    	    			}
    	    			for (int b=0; b < lista.size() -1 ; b+=3)
    	    			{
    	    				vi[nv] = Integer.parseInt(lista.get(b));
    	    				ti[nv] = Integer.parseInt(lista.get(b+1));
    						ni[nv] = Integer.parseInt(lista.get(b+2));
    	    				tem_t = true;
    						tem_n = true;
    						nv++;

    	    			}
    	    		}
    	    		if(barra == 1)
    	    		{
    	    			for(int j=0; j < temp.length; j++)
    	    			{
    	    				nova = temp[j].split("/");
    	    				
    	    				for(int a = 0; a < nova.length; a++)
    	    				{
    	    				
    	    				lista.add(nova[a]);
    	    				
    	    				}
    	    			}
    	    			for (int b=0; b < lista.size() -1 ; b+=2)
    	    			{
    	    				vi[nv] = Integer.parseInt(lista.get(b));
    	    				ti[nv] = Integer.parseInt(lista.get(b+1));
    						tem_t = true;
    						nv++;

    	    			}
    	    		}
    	    		
    	    		if(barra == 0)
    	    		{
    	    			for(int j=0; j < temp.length; j++)
    	    			{
    	    				    	    				
    	    				lista.add(temp[0]);
    	    				
    	    			}
    	    			
    	    			for (int b=0; b < lista.size() -1 ; b++)
    	    			{
    	    				vi[nv] = Integer.parseInt(lista.get(b));
    	    				nv++;

    	    			}
    	    		}
    	    	 			
    			
    		
    		
    			// Fim da face, aloca mem�ria para estruturas e preenche com
    			// os valores lidos
    			obj.getFace(fcont).setNumVert(nv);
    			    			
    			// S� aloca mem�ria para normais e texcoords se for necess�rio
    			if(tem_n) 
    				{
    					obj.getFace(fcont).setNumNorm(nv);
    					obj.getFace(fcont).setPerVertexNormal(true);
    					
    				}
    				else {
    						obj.getFace(fcont).setNumNorm(0);
    					 	
    					 }
    			if(tem_t) 
    				{
    					obj.getFace(fcont).setNumTex(nv);
    					
    				}
    				else 
    					{
    						obj.getFace(fcont).setNumTex(0);
    						
    					}
    			    			   			
    			// Copia os �ndices dos arrays tempor�rios para a face
    			for(int t=0; t < nv; ++t)
    			{
    				// Subtra�mos -1 dos �ndices porque o formato OBJ come�a
    				// a contar a partir de 1, n�o 0
    				obj.getFace(fcont).setVertices(vertices[vi[t]-1], t);
    				 
    				if(tem_n)
    					{
    						
    						obj.getFace(fcont).setNormal(normals[ni[t]-1], t);
    						
    					}
    				if(tem_t)
    					{
    						obj.getFace(fcont).setTexcoords(texCoords[ti[t]-1], t);
    						 
    					}
    			}
    			// Prepara para pr�xima face
    			fcont++;
    			vi = null;
    			ti = null;
    			ni = null;
    			continue;
    		}

    		
		}  
		
		
		objects.add(obj);
		//System.out.println("Nome " + obj.getName() +  " maxx " + obj.getDimMax().x+ " maxy " + obj.getDimMax().y+  " maxz " + obj.getDimMax().z);
		return obj;
	
	}
	
	

	// Desenha um objeto 3D passado como par�metro.
	//public void desenhaObjeto(Obj obj){}
	
		
			
	// L� um arquivo que define materiais para um objeto 3D no
	// formato .OBJ
	public void readMaterials(String arqName, Obj obj) throws IOException //modificado
	{
		
		Material mat = null;
		
		
		BufferedReader reader = openArq(arqName);
			
		/* Especifica��o do arquivo de materiais (.mtl):
		 * 
		 * #,!,$  - coment�rio
		 * newmtl - nome do novo material(sem espa�os)
		 * Ka	  - Cor ambiente, especificada como 3 floats
		 * Kd	  - Cor difusa, idem
		 * Ks	  - Cor especular, idem
		 * Ns	  - Coeficiente de especularidade, entre 0 and 1000 
		 *          (convertido para a faixa v�lida em OpenGL (0..128) durante a leitura)
		 * d	  - Transpar�ncia - mesmo significado que o alpha em OpenGL: 0.0 a 1.0 (0.0 = transparente)
		 *
		 * Os demais campos s�o ignorados por este leitor:
		 * Tr	  - Transpar�ncia (0 = opaco, 1 = transparente)
		 * map_Kd - Especifica arquivo com a textura a ser mapeada como material difuso
		 * refl	  - Especifica arquivo com um mapa de reflex�o
		 * Ni	  - �ndice de refra��o, 1.0 ou maior, onde 1.0 representa sem refra��o
		 * bump	  - Especifica arquivo para "bump mapping"
		 * illum  - Modelo de ilumina��o (1 = apenas cores, 2 = textura, 3 = reflex�es e raytracing)
		 * map_Ka - Especifica arquivo com a textura a ser mapeada como cor ambiente
		 * map_Ks - Especifica arquivo com a textura a ser mapeada como cor especular
		 * map_Ns - Especifica arquivo com a textura a ser mapeada como reflex�o especular
		 * map_d  - Especifica arquivo cmo a textura a ser mapeada como transpar�ncia (branco � opaco, preto � transparente)
		 *
		 */

		while(true)
		{
			String line = reader.readLine();

            if (line == null)
            {
                reader.close();
                break;
            }
            
    		if(line.startsWith("#")) 
    			continue;
			
			if(line.startsWith("newmtl")) // Novo material ?
			{
				
				// Se material j� existe na lista, pula para o 
				// pr�ximo
				if(findMaterial(line.substring(7)) != -1)
				{
					mat = null;
					continue;
				}
				mat = new Material();
				// Adiciona � lista
				obj.setMaterial(mat); //modificado
				// Copia nome do material
				mat.setName(line.substring(7));
				
				// N�o existe "emission" na defini��o do material
				// mas o valor pode ser setado mais tarde,
				// via SetaEmissaoMaterial(..)
				mat.setKe(new float[]{0f, 0f, 0f, 0f});
			}
			if(line.startsWith("Ka ")) // Ambiente
			{
				if(mat==null)
				{
					continue;
				}
				String[] lin = line.substring(3).split(" ");
				
				mat.setKa(Conversion.convertK4(lin));
				
			}
			if(line.startsWith("Kd ")) // Difuso
			{
				if(mat==null)
				{
					continue;
				}
				String[] lin = line.substring(3).split(" ");
				
				mat.setKd(Conversion.convertK4(lin));
			}
			if(line.startsWith("Ks ")) // Especular
			{
				if(mat==null)
				{
					continue;
				}
				String[] lin = line.substring(3).split(" ");
				
				mat.setKs(Conversion.convertK4(lin));
			}
			if(line.startsWith("Ns ")) // Fator de especularidade
			{
				if(mat==null)
				{
					continue;
				}
				// Converte da faixa lida (0...1000) para o intervalo
				// v�lido em OpenGL (0...128)
				mat.setSpec((Float.parseFloat(line.substring(3))/1000) * 128);
				
				
			}
			if(line.startsWith("d ")) // Alpha
			{
				if(mat==null)
				{
					continue;
				}
				// N�o existe alpha na defini��o de cada componente
				// mas o valor � lido em separado e vale para todos
				float alpha = Float.parseFloat(line.substring(2));
				mat.setAlpha(alpha);
				
			}
		}
		
		
	}
	
	// Desabilita a gera��o de uma display list
	// para o objeto especificado
	public void desabilitaDisplayList(Obj obj)
	{
		if(obj == null) return;
		if(obj.getNumDisplayList() > 0 && obj.getNumDisplayList() < 1000) // j� existe uma dlist ?
		{
				// Libera dlist existente
				glDeleteLists(obj.getNumDisplayList(),1);
		}
		// O valor especial -2 indica que n�o queremos
		// gerar dlist para esse objeto
		obj.setNumDisplayList(-2);
	}

	
	// Fun��o interna para criar uma display list
	// (na verdade, a display list em si � gerada durante a
	// primeira vez em que o objeto � redesenhado)
	private void criaDList(Obj obj)
	{
		// Se o objeto n�o possui display list, gera uma identifica��o nova
		if(obj.getNumDisplayList() == -1) 
			{
				obj.setNumDisplayList(glGenLists(1));
			}
		// Adiciona 1000 ao valor, para informar � rotina de desenho
		// que uma nova display list deve ser compilada
		obj.setNumDisplayList(obj.getNumDisplayList()+1000);
	}

	// Cria uma display list para o objeto informado
	// - se for null, cria display lists para TODOS os objetos
	// (usada na rotina de desenho, se existir)
	public void criaDisplayList(Obj obj)
	{
		if(obj == null)
		{
			for(int i=0;i < objects.size(); ++i)
			{
				obj = objects.get(i);
				// Pula os objetos que n�o devem usar dlists
				if(obj.getNumDisplayList() == -2) 
				{
					continue;
				}
				criaDList(obj);
			}
		}
		else if(obj.getNumDisplayList() != -2)
				{
					criaDList(obj);
				}
	}


	public int findMaterial(String nome) //modificado
	{
		for (int i=0; i < obj.getMaterial().size(); i++) 
		{
			if (nome.equalsIgnoreCase(obj.getMaterial(i).getName()))
			{
				return i;
			}
		}
		return -1;
	}
	
		
	
	
	public BufferedReader openArq(String arqName)
	{
		BufferedReader reader = null;
		File fp = new File(arqName);
		
		if (fp.exists())
		{
			try
			{
			reader = new BufferedReader(new FileReader(fp)); // abre arquivo texto para leitura
			}catch(FileNotFoundException f)
			{
				f.getMessage();
			}
		}
		return reader;
	}
	
	// Seta o filtro de uma textura espec�fica
	// ou de todas na lista (se for passado o argumento -1)
	public void setaFiltroTextura(int tex, int filtromin, int filtromag)
	{
		glEnable(GL_TEXTURE_2D);
		if(tex != -1)
		{
			glBindTexture(GL_TEXTURE_2D,tex);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filtromin);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filtromag);
		}
		else
		for(int i=0;i < texManager.getTexture().size(); i++)
		{
			glBindTexture(GL_TEXTURE_2D, texManager.getTexture(i).getTexID());
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filtromin);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filtromag);
			
		}
		glDisable(GL_TEXTURE_2D);
	}
	
	public ArrayList<Obj> getObj()
	{
		return objects;
	}
	
	public Obj getObj(int index)
	{
		return objects.get(index);
	}
	
	public TextureManager getTexManager()
	{
		return texManager;
	}
	
	public Vector3f maxDist()
	{
		
		
		Vector3f max = objects.get(0).getDimMax();
		for(int i=1; i < objects.size(); i++)
		{
						
			if(objects.get(i).getDimMax().x > max.x) max.x = objects.get(i).getDimMax().x;
			if(objects.get(i).getDimMax().y > max.y) max.y = objects.get(i).getDimMax().y;
			if(objects.get(i).getDimMax().z > max.z) max.z = objects.get(i).getDimMax().z;
		}
		
		return max;
	}
	
}
