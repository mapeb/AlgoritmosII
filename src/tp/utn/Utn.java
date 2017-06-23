package tp.utn;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import tp.utn.ann.Column;
import tp.utn.ann.Table;
import tp.utn.demo.domain.Direccion;
import tp.utn.demo.domain.Persona;
import tp.utn.main.SingletonConexion;
import tp.utn.Reflection;

public class Utn {
	// Retorna: el SQL correspondiente a la clase dtoClass acotado por xql <- la
	// consulta
	public static <T> String _query(Class<T> dtoClass, String xql) {
		Query query = new Query(dtoClass.getAnnotation(Table.class).name());
		Field[] campos = dtoClass.getDeclaredFields();
		
		query.generarQuery(campos, dtoClass);
		return query.generarStringSelect(xql, dtoClass);
	}

	public static <T> Enhancer setEnhancer(Class<T>  dtoClass){
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(dtoClass);
		enhancer.setCallback(new MethodInterceptor() {
		    @Override
		    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
		        throws Throwable {
		      if(method.getName().startsWith("get") && proxy.invokeSuper(obj,args)==null) {
		      		Connection con = SingletonConexion.getConnection();
		    	  DataBaseConnection connection = new DataBaseConnection(con);
		    	  
		    	  String nombreCampo = Xql.stringMinuscula(method.getName().substring(3));
		    	  Field campoLazy = dtoClass.getDeclaredField(nombreCampo);
		    	  Field[] campos = {campoLazy};
		    	  int id= 0;
		      	  for(Method metodo : dtoClass.getDeclaredMethods())
		    	  {
		    		  if(metodo.getName().startsWith("getId"))
		    		  {
		    		    id = (int)metodo.invoke(obj,null);
		    		    break;
		    		  }
		    		  
		     	  }
		    
		    	  Object[] args1 = {id};		    	  
		    	  String nombreAtributoEnTabla = DataBaseConnection.nombreAtributoEnTabla(dtoClass,campoLazy);
		  		Query query = new Query(dtoClass.getAnnotation(Table.class).name());
		  		 query.generarQuery(campos,dtoClass);
		         String xqlWhere = "$Persona.idPersona = ?";
		         String myQuery = query.generarStringSelect(xqlWhere,dtoClass);
		         
		         List<T> objetosBD = connection.getObjetosDeBD(dtoClass,myQuery, args1,xqlWhere,true, obj, campos);
		         obj = objetosBD.get(0);
		    
		      } 
		      return  proxy.invokeSuper(obj,args);
		    }
		  });
		return enhancer;
	}
	
	
	// Invoca a: _query para obtener el SQL que se debe ejecutar
	// Retorna: una lista de objetos de tipo T
	// EJ: query(con,dtoClass,"$nombre  LIKE 'P%'") Donde $ indica variable de la clase.
	public static <T> List<T> query(Connection con, Class<T> dtoClass, String xqlWhere, Object... args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		DataBaseConnection connection = new DataBaseConnection(con);
		String query = _query(dtoClass, xqlWhere);	
		List<T> objetosBD = connection.getObjetosDeBD(dtoClass,query,args, xqlWhere, false, null, dtoClass.getDeclaredFields());
		
		//Setteo el enhancer con el que voy a crear a mis proxies
		Enhancer enhancer = setEnhancer(dtoClass);
		List<T> proxies = new ArrayList<T>();
		for(Object objeto:objetosBD)
		{
			//Creo una nueva instancia de la clase
			Object proxy = Reflection.getConstructor(dtoClass).newInstance();
			//Le creo un proxy encima
			proxy = objeto.getClass().cast(enhancer.create());
			Method[] metodos = objeto.getClass().getDeclaredMethods();
			//Saco todos los getters y setters
			ArrayList<Method> getters = Reflection.getGettersSetters(metodos,"get");
			ArrayList<Method> setters = Reflection.getGettersSetters(metodos,"set");
					
			for(Method setter:setters)
			{
				//Busco el getter de cada setter
				Method elGetter = getters.stream()
								.filter(getter -> getter.getName().substring(3).equals(setter.getName().substring(3)))
								.findFirst().get();
				//Guardo el resultado del getter y lo uso para settear el proxy
				Object argumentoSetter = elGetter.invoke(objeto,null);
				setter.invoke(proxy,argumentoSetter);
			}
			
			proxies.add((T)proxy);
		}
		
		return proxies;
		
	}

	// Retorna: una fila identificada por id o null si no existe
	// Invoca a: query
	public static <T> T find(Connection con, Class<T> dtoClass, Object id) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException{
		String idClase = Reflection.getIdField(dtoClass);
		String clase = dtoClass.getSimpleName();
		String xqlWhere = "$"+clase+"."+idClase+" = ?";
		List<T> listaObjetos = query(con, dtoClass, xqlWhere, id);
		return listaObjetos.get(0);
		
	}

	// Retorna: una todasa las filas de la tabla representada por dtoClass
	// Invoca a: query
	private static <T> List<T> findAll(Connection con, Class<T> dtoClass) {
		return null;
	}

	// Retorna: el SQL correspondiente a la clase dtoClass acotado por xql
	public static <T> String _update(Class<T> dtoClass, String xql) { // XQL =  
		Query query = new Query(dtoClass.getAnnotation(Table.class).name());
		return query.generarStringUpdate(xql,dtoClass);
	}

	// Invoca a: _update para obtener el SQL que se debe ejecutar
	// Retorna: la cantidad de filas afectadas luego de ejecutar el SQL
	public static int update(Connection con, Class<?> dtoClass, String xql, Object... args) {
		DataBaseConnection connection = new DataBaseConnection(con);
		String myQuery = _update(dtoClass, xql);
		
		
		
		return 0;
	}

	// Invoca a: update
	// Que hace?: actualiza todos los campos de la fila identificada por el id
	// de dto
	// Retorna: Cuantas filas resultaron modificadas (deberia: ser 1 o 0)
	public static int update(Connection con, Object dto) {
		
		return 0;
	}

	// Retorna: el SQL correspondiente a la clase dtoClass acotado por xql
	public static String _delete(Class<?> dtoClass, String xql) {
		Query query = new Query(dtoClass.getAnnotation(Table.class).name());
		return query.generarStringDelete(xql,dtoClass);
		
	}

	// Invoca a: _delete para obtener el SQL que se debe ejecutar
	// Retorna: la cantidad de filas afectadas luego de ejecutar el SQL
	public static int delete(Connection con, Class<?> dtoClass, String xql, Object... args) {
		DataBaseConnection connection = new DataBaseConnection(con);
		String query = _delete(dtoClass, xql);
		return connection.delete(con,query,args,xql,dtoClass);
		
		
	}

	// Retorna la cantidad de filas afectadas al eliminar la fila identificada
	// por id
	// (deberia ser: 1 o 0)
	// Invoca a: delete
	public static int delete(Connection con, Class<?> dtoClass, Object id) {
		String idClase = Reflection.getIdField(dtoClass);
		String clase = dtoClass.getSimpleName();
		String xqlWhere = "$"+clase+"."+idClase+" = ?";
		return delete(con, dtoClass, xqlWhere, id);
		
	}

	// Retorna: el SQL correspondiente a la clase dtoClass
	public static String _insert(Class<?> dtoClass) {
		return null;
	}

	// Invoca a: _insert para obtener el SQL que se debe ejecutar
	// Retorna: la cantidad de filas afectadas luego de ejecutar el SQL
	public static int insert(Connection con, Object dto) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Query query = new Query();
		String miQuery = query.generarStringInsert(dto);
		System.out.println(query);
		DataBaseConnection connection = new DataBaseConnection(con);
		return(connection.insertIntoTable(con,miQuery));
	
	}

	/*public static void main(String[] args) {
		Persona x = new Persona();
		//x.setDireccion(new Direccion());
		System.out.println(Utn._query(x.getClass(), " WHere edad == 2"));
	}*/

}
