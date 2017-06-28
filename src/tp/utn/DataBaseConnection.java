package tp.utn;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import tp.utn.ann.Column;
import tp.utn.ann.Relation;
import tp.utn.ann.Table;
import tp.utn.demo.domain.Direccion;
import tp.utn.main.SingletonConexion;

public class DataBaseConnection extends Xql
{

	Connection connection;


	public DataBaseConnection(Connection connection)
	{
		super();
		this.connection=connection;
	}

	public Connection getConnection()
	{
		return connection;
	}
	
	public static <T> Enhancer setEnhancer(Class<T> dtoClass){
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(dtoClass);
		enhancer.setCallback(new MethodInterceptor() {
		    @SuppressWarnings("unchecked")
			@Override
		    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
		        throws Throwable {    	
		    	if(method.getName().startsWith("get") && proxy.invokeSuper(obj,args)==null){
		    		
			    		Connection con = SingletonConexion.getConnection();
			    		DataBaseConnection connection = new DataBaseConnection(con);
			    	  
			    		String nombreCampo = Xql.stringMinuscula(method.getName().substring(3));
			    		Field campoLazy = dtoClass.getDeclaredField(nombreCampo);
			    		Field[] campos = {campoLazy};
			    		int id = 0;
			    		for(Method metodo : dtoClass.getDeclaredMethods())
			    		{
			    			if(metodo.getName().startsWith("getId"))
			    			{
			    				id = (int)metodo.invoke(obj,null);
			    				break;
			    			}	    		  
			    		}
			    		Object[] args1 = {id};		
			    		String claseStr = dtoClass.getSimpleName();
				    	String idStr = "id" + Xql.stringMayuscula(claseStr);
				    	String xqlWhere = "$" + claseStr + "." + idStr + " = ?";
				    	
			    	if(!method.getReturnType().getSimpleName().equals("Collection"))
			    	{
				    	String nombreAtributoEnTabla = DataBaseConnection.nombreAtributoEnTabla(dtoClass,campoLazy);		    				    	
				  		Query query = new Query(dtoClass.getAnnotation(Table.class).name());
				  		query.generarQuery(campos,dtoClass,false);		        
				        String myQuery = query.generarStringSelect(xqlWhere,dtoClass);
				         
				        List<T> objetosBD = connection.getObjetosDeBD(dtoClass, myQuery, args1, xqlWhere, true, obj, campos);
				        obj = objetosBD.get(0);
			        }
		    		else
		    		{
		    			Class<T> claseDeColeccion = (Class<T>) campoLazy.getAnnotation(Relation.class).type();
						List<T> objetosDeColeccion = (List<T>) Utn.query(con,claseDeColeccion,xqlWhere,id);
						Collection<T> coleccionplz = new ArrayList<T>();
						coleccionplz.addAll(objetosDeColeccion);
						String camposta = Xql.stringMayuscula(nombreCampo);
						for(Method metodo: dtoClass.getDeclaredMethods())	
							if(metodo.getName().startsWith("set") && metodo.getName().substring(3).equals(camposta))
								metodo.invoke(obj,coleccionplz);
		    		}
		    	} 
		      return  proxy.invokeSuper(obj,args);
		    }
		  });
		return enhancer;
	}
	public int update(Connection con, String query, Object[] args, String xql, Class<?> dtoClass)
	{
		PreparedStatement pstm=null;
		int filasAfectadas = 0;
		try
		{
			pstm=this.getConnection().prepareStatement(query);
			agregarCondicion(xql,pstm,args,dtoClass);
			filasAfectadas = pstm.executeUpdate();
		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		finally
		{
			try
			{
				if(pstm!=null) pstm.close();

			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			
		}
		return filasAfectadas;
	}
	public int delete(Connection con, String query, Object[] args, String xql, Class<?> dtoClass)
	{
		PreparedStatement pstm=null;
		int filasAfectadas = 0;
		try
		{
			pstm=this.getConnection().prepareStatement(query);
			agregarCondicion(xql,pstm,args,dtoClass);
			filasAfectadas = pstm.executeUpdate();
		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		finally
		{
			try
			{
				if(pstm!=null) pstm.close();

			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			
		}
		return filasAfectadas;
		
	}
	public int insertOUpdateTable(Connection con, String query) // RETORNA FILAS AFECTADAS - DEBERÍA SER UNA
	{
		PreparedStatement pstm=null;
		int filasAfectadas = 0;
		
		try
		{
			pstm=this.getConnection().prepareStatement(query);
			filasAfectadas = pstm.executeUpdate();
		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		finally
		{
			try
			{
				if(pstm!=null) pstm.close();

			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			
		}
		return filasAfectadas;
	}
	
	public <T> List<T> getObjetosDeBD(Class<?> dtoClass, String query, Object[] args, String xql, boolean chequeoFetchType, Object objetoSetteado, Field[] camposDeQuery)
	{
		PreparedStatement pstm=null;
		ResultSet rs=null;

		try
		{
			pstm=this.getConnection().prepareStatement(query);
			agregarCondicion(xql,pstm,args,dtoClass);
			rs=pstm.executeQuery();
		

			List<T> listaObjetos=new ArrayList<T>();
			while(rs.next())
			{
				if(objetoSetteado == null)
				{
					Object objeto = Reflection.getConstructor(dtoClass).newInstance();
					settearValoresAObjeto(dtoClass,objeto,rs,listaObjetos, false, camposDeQuery);
					listaObjetos.add((T)objeto);
				}
				else
				{
					settearValoresAObjeto(dtoClass,objetoSetteado,rs,listaObjetos, chequeoFetchType, camposDeQuery);
					listaObjetos.add((T)objetoSetteado);
				}
					
			}
			if(listaObjetos.size()==0)
				return null;
			return listaObjetos;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		finally
		{
			try
			{
				if(rs!=null) rs.close();
				if(pstm!=null) pstm.close();

			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}

		}
	}




	public Method buscarSetterHijoAPadre(Object hijo, Object padre) 																										
	{
		Method metodosPadre[]=padre.getClass().getDeclaredMethods();
		ArrayList<Method> settersPadre=Reflection.getGettersSetters(metodosPadre,"set");
		for(Method setter:settersPadre)
		{
			String hijonombre = hijo.getClass().getSimpleName();
			if(setter.getName().substring(3).equals(hijo.getClass().getSimpleName())) return setter;
		}
		return null;
	}

	// SETEA LAS VARIABLES USANDO REFLECTION EN EL RESULT SET PARA TENER LOS
	// GETTERS PRIMITIVOS
	// SI CAMPO A SETTEAR NO ES PRIMITIVO, HACE RECURSIVIDAD SOBRE EL SETTEO DE
	// OBJETOS
	// AL SER UNA CLASE Y UNA TABLA DIFERENTE Y LE SETTEA A LA CLASE PRINCIPAL
	// LA CLASE SECUNDARIA EN SU ATRIBUTO
	public <T> void settearSobreObjeto(ResultSet rs, Field campo, String nombreEnTabla, Method setter, Object objeto, List<T> listaObjetos, Field[] camposDeQuery)
			throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,SQLException
	{
		Class type=campo.getType();
		try
		{
			if(type==int.class||type==Integer.class) setter.invoke(objeto,rs.getInt(nombreEnTabla));

			else
			{
				if(!Reflection.isPrimitiveClass(campo))
				{
					Object objetoCampo = campo.getType().getConstructor(null).newInstance(null);
					settearValoresAObjeto(campo.getType(),objetoCampo,rs,listaObjetos, false, campo.getType().getDeclaredFields());
					
					
					Enhancer enhancer = setEnhancer(campo.getType());
					Object proxy = Reflection.getConstructor(campo.getType()).newInstance();
					proxy = campo.getType().cast(enhancer.create());
					
					Method[] metodos = campo.getType().getDeclaredMethods();
					//Saco todos los getters y setters
					ArrayList<Method> getters = Reflection.getGettersSetters(metodos,"get");
					ArrayList<Method> setters = Reflection.getGettersSetters(metodos,"set");
							
					for(Method unSetter:setters)
					{
						//Busco el getter de cada setter
						Method elGetter = getters.stream()
										.filter(getter -> getter.getName().substring(3).equals(unSetter.getName().substring(3)))
										.findFirst().get();
						//Guardo el resultado del getter y lo uso para settear el proxy
						Object argumentoSetter = elGetter.invoke(objetoCampo,null);
						
						unSetter.invoke(proxy,argumentoSetter);
					}
					Method settearObjeto=buscarSetterHijoAPadre(objetoCampo,objeto);
					settearObjeto.invoke(objeto,proxy);			
				}
				else
				{
					Method[] metodosStatement=rs.getClass().getDeclaredMethods();
					for(Method metodo:metodosStatement)
					{
						String tipoClase;
						String tipoClaseMin;
						if(Reflection.esUnSetterOGetter(metodo,"get"))
						{
							tipoClase=metodo.getName().substring(3);
							tipoClaseMin=stringMinuscula(tipoClase);
							if(type.getSimpleName().equals(tipoClase)||type.getSimpleName().equals(tipoClaseMin))
							{
								Class parametros[]=metodo.getParameterTypes();
								if(parametros.length==1&&parametros[0].getSimpleName().equals(type.getSimpleName())) 
								{
									String parametro = (String)metodo.invoke(rs,nombreEnTabla);
									setter.invoke(objeto,parametro);
								}
							}

						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace(); 
			throw new RuntimeException(ex);
		}
	}

	public static String nombreAtributoEnTabla(Class dtoClass, Field campo)
	{
		return Annotation.getTableName(dtoClass)+"."+Annotation.getAnnotationFieldName(campo);
	}



	

	// OBTIENE LAS VARIABLES DE LA CLASE RELACIONADAS AL WHERE. EN EL CASO DE
	// QUE LA VARIABLE
	// SEA DE OTRA TABLA, SE OBTIENE ESA SEGUNDA TABLA Y SE VERIFICA QUE ESTE EL
	// CAMPO BUSCADO.
	public <T> ArrayList<String> getVariablesDelWhere(Class<T> dtoClass)
	{
		ArrayList<String> variables=new ArrayList<String>();
		for(String atributo:variablesXqlWhere)
		{
			if(stringMayuscula(getClaseDe(atributo)).equals(dtoClass.getSimpleName()))
			{
				atributo=getAtributoSinNombreClase(atributo);
				for(Field campo:dtoClass.getDeclaredFields())
				{
					if(campo.getName().equals(atributo))
					{
						variables.add(campo.getName());
						break;
					}
				}
			}
			else
			{
				int i=0;
				for(Field campito:dtoClass.getDeclaredFields())
				{
					if(!Reflection.isPrimitiveClass(campito)&&stringMayuscula(getClaseDe(atributo)).equals(campito.getType().getSimpleName()))
					{
						atributo=getAtributoSinNombreClase(atributo);
						for(Field campoSegunda:campito.getType().getDeclaredFields())
						{
							if(campoSegunda.getName().equals(atributo))
							{
								variables.add(campoSegunda.getName());
								i=1;
								break;
							}
						}
					}
					if(i==1) break;
				}
			}

		}
		return variables;
	}

	// BUSCA LOS SETTERS DE CADA CAMPO Y LOS INVOCA CON REFLECTION AL ENCONTRAR
	// EL CAMPO
	// PERTINENTE
	public <T> void settearValoresAObjeto(Class dtoClass, Object objeto, ResultSet rs, List<T> listaObjetos, boolean chequeoFetchType, Field[] camposDeQuery)
			throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,SQLException
	{
		ArrayList<Method> setters=Reflection.getGettersSetters(dtoClass.getDeclaredMethods(),"set");
		for(Method setter:setters)
		{
			String atributoSetter=Reflection.getAtributoDelSetterOGetter(setter);
			atributoSetter=stringMinuscula(atributoSetter);
			for(Field campo:dtoClass.getDeclaredFields())
			{

				if(campo.getName().equals(atributoSetter) && Annotation.getAnnotationFieldName(campo) != null	
						&& campoEstaEnColumnasDeQuery(campo, camposDeQuery)) 
				{
					if(campo.getAnnotation(Column.class).fetchType()==2 || chequeoFetchType){
						String nombreEnTabla=nombreAtributoEnTabla(dtoClass,campo);
						settearSobreObjeto(rs,campo,nombreEnTabla,setter,objeto,listaObjetos, camposDeQuery);
					}
				}
			}

		}
	}
	public boolean campoEstaEnColumnasDeQuery(Field campo, Field[] camposDeQuery) // CHEQUEA QUE EL CAMPO ESTE DENTRO
																	// DEL SELECT EJ: SELECT nombre, direccion FROM..
	{
		for(Field campoQuery : camposDeQuery)
		{
			if(campoQuery.equals(campo))
				return true;
		}
		return false;
	}
	// BUSCA EN LOS DISTINTOS ATRIBUTOS DE LA CLASE AQUELLOS QUE NO SON
	// PRIMITIVOS Y REPRESENTAN
	// UNA TABLA, PARA LUEGO BUSCAR EL CAMPO INVOLUCRADO EN EL WHERE EN ESA
	// TABLA Y DEVOLVER EL TIPO DE DATO
	public <T> String obtenerTipoCampoDeOtraClase(Class<T> dtoClass, String campo) throws NoSuchFieldException,SecurityException
	{
		Field[] camposClasePrincipal=dtoClass.getDeclaredFields();
		for(Field campoClaseP:camposClasePrincipal)
		{
			if(!Reflection.isPrimitiveClass(campoClaseP))
			{
				Field campoSec=campoClaseP.getType().getDeclaredField(campo); 
				if(campoSec!=null)
					return campoSec.getType().getSimpleName();
			}
		}
		return null;

	}

	public void settearVariablesALaQuery(ArrayList<String> variablesDelWhere, Class dtoClass, PreparedStatement pstm, Object[] args)
			throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,NoSuchFieldException,SecurityException
	{

		Method[] metodos=pstm.getClass().getDeclaredMethods();
		ArrayList<Method> setters=Reflection.getGettersSetters(metodos,"set");
		
		String tipo=null;
		int i=0;
		boolean esVariableDeLaClase=true;

		for(String variable:variablesDelWhere)
		{
			try
			{
				// VERIFICA SI LA VARIABLE DEL WHERE ES DE LA CLASE O DE UNA
				// CLASE QUE TIENE
				// COMO ATRIBUTO. SI NO ES DE ESTA CLASE, CATCHEA EXCEPCION.
				dtoClass.getDeclaredField(variable);

			}
			catch(NoSuchFieldException ex)
			{
				tipo=obtenerTipoCampoDeOtraClase(dtoClass,variable);
				esVariableDeLaClase=false;
			}
			if(esVariableDeLaClase)
			{
				Field campo=dtoClass.getDeclaredField(variable);
				tipo=campo.getType().getSimpleName();
			}
			for(Method setter:setters)
			{
				String attr=Reflection.getAtributoDelSetterOGetter(setter);
				if(attr.equals(tipo)||attr.equals(stringMayuscula(tipo))||(attr.equals("Int")&&tipo.equals("Integer")))
				{
					// SETEA SEGUN EL SETTER PERTINENTE DEL PSTM
					
					setter.invoke(pstm,i+1,args[i]);
					i++;
					break;
				}
			}

		}
	}

	// OBTIENE LAS VARIABLES IMPLICADAS EN EL WHERE Y LAS SETEA EN LA QUERY A
	// TRAVES DEL PSTM
	public <T> void agregarCondicion(String xql, PreparedStatement pstm, Object[] args, Class<?> dtoClass)
			throws NoSuchFieldException,SecurityException,IllegalAccessException,IllegalArgumentException,InvocationTargetException
	{
		try
		{
			//setVariablesXql(xql);
			settearVariablesALaQuery(getVariablesDelWhere(dtoClass),dtoClass,pstm,args);

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}

	}
}
