

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import tp.utn.ann.Column;
import tp.utn.ann.Id;
import tp.utn.ann.Table;
import tp.utn.main.SingletonConexion;
import tp.utn.main.ValorDiccionario;

public class Query {
	List<String> select;
	String from;

	
	public Query(String from) {
		super();
		select = new ArrayList<String>();
		this.from = from;
	}

	public Query()
	{
		super();
	}
	public void generarQuery(Field[] campos, Class dtoClass) {
		for (Field campo : campos) {
			if (campo.getAnnotation(Column.class) != null && isPrimiteClass(campo))
				this.addAttr(dtoClass,campo.getAnnotation(Column.class).name());
			else {
				if (campo.getType().getAnnotation(Table.class) != null && campo.getAnnotation(Column.class).fetchType() == 2) {
				//	this.addJoin(campo, campo.getType());
					this.addJoin(dtoClass, campo);
					generarQuery(campo.getType().getDeclaredFields(), campo.getType());
				}
			}
		}
	}
	
	public String generarString(String xql) {
		String q = "Select ";
		for(String attr:this.getSelect())
			q+=attr + ",";
		q=q.substring(0,q.length()-1);
		q+=" FROM "+ from + " " + xql;
		return q;
	}

	
	private static boolean isPrimiteClass(Field field) {
		Class type = field.getType();
		if ((type == int.class) || (type == Integer.class) || (type == String.class) || (type == char.class)
				|| (type == double.class) || (type == long.class) || (type == short.class) || (type == boolean.class))
			return true;
		return false;
	}

	public void addAttr(Class claseContenedora ,String atrr){
		String newAtrr = nombreTabla(claseContenedora) + "." +atrr;
		this.getSelect().add(newAtrr);
	}
	public void addJoin(Class claseRaiz,Field campo){ 
	//	Table tabla2 = (Table) clase2.getAnnotation(Table.class);
		from+=" JOIN " + nombreTabla(campo.getType()) + joinDeTablas(claseRaiz,campo);
	}
	
	private String joinDeTablas(Class campoSolicitante,Field campoSolicitado) {
		String comparacion = " ON ( " + nombreTabla(campoSolicitante);
		comparacion+= "." + campoSolicitado.getAnnotation(Column.class).name() + " = ";
		comparacion += nombreTabla(campoSolicitado.getType()) + ".";
		
		for (Field field: campoSolicitado.getType().getDeclaredFields()) {
			if(field.getAnnotation(Id.class) != null)
				return comparacion + field.getAnnotation(Column.class).name() + " )";
		}
		return ""; // Hola si la funcion no tiene id debo explotar, ya se .
	}
	
	private String nombreTabla(Class clase){
		return ((Table)clase.getAnnotation(Table.class)).name();
	}
	
	public List<String> getSelect() {
		return select;
	}

	public void setSelect(List<String> select) {
		this.select = select;
	}
	
	public void setFrom(String n) {
		this.from = n;
	}	
	public ArrayList<Method> obtenerSetters(Method metodos[])
	{
		ArrayList<Method> setters = new ArrayList<Method>();
		for(Method metodo:metodos)
		{
			if(metodo.getName().substring(0,3).equals("set"))
				setters.add(metodo);
		}
		return setters;
	}
	public String stringMinuscula(String palabra)
	{
		return (palabra.substring(0,1).toLowerCase()+ palabra.substring(1));
		
	}
	public void settearSobreObjeto(ResultSet rs, Class type, String nombreEnTabla, Method setter, Object objeto) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException
	{
		try{
		if(type==int.class||type==Integer.class) setter.invoke(objeto,rs.getInt(nombreEnTabla));
		
		else
		{	
		Method[] metodosStatement = rs.getClass().getDeclaredMethods();
		
		for(Method metodo : metodosStatement)
		{
			String tipoClase;
			String tipoClaseMin;
			int valorInt;
			if(metodo.getName().substring(0,3).equals("get"))
			{
				
				tipoClase = metodo.getName().substring(3);
				tipoClaseMin = stringMinuscula(tipoClase);
			
				if(type.getSimpleName().equals(tipoClase) || type.getSimpleName().equals(tipoClaseMin) )
				{
															
					Class parametros[] = metodo.getParameterTypes();
					int cantidadParametros = 0;
					for(Class parametro : parametros)
						cantidadParametros++;
					if(cantidadParametros == 1 && parametros[0].getSimpleName().equals(type.getSimpleName())
		)
					{
				System.out.println(metodo.getName());
						Object valor = metodo.invoke(rs,nombreEnTabla);
						setter.invoke(objeto, valor);
					}
				
				}
					//obtenerObjetosDeBD(dtoClass,query,args)
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
	public String nombreEnTabla(Class dtoClass, Field campo)
	{
		return nombreTabla(dtoClass)+"."+campo.getAnnotation(Column.class).name();
	}
	public <T> List<T> obtenerObjetosDeBD(Class dtoClass, String query,Object[] args)
	{

		Connection con=SingletonConexion.getConnection();
		PreparedStatement pstm=null;
		ResultSet rs=null;
		try
		{
			con=SingletonConexion.getConnection();
			String sql= query;
			pstm=con.prepareStatement(sql);
			pstm.setString(1,(String)args[0]);
			rs=pstm.executeQuery();
			
			Class<T> clazz=(Class<T>)Class.forName(dtoClass.getName());
			Constructor<?> constructor=clazz.getConstructor();
			Object objeto=null;

			Field[] campos=dtoClass.getDeclaredFields();
			Method metodos[]=dtoClass.getDeclaredMethods();
			ArrayList<Method> setters= obtenerSetters(metodos);
			
			
			List<T> objetos=new ArrayList<T>();
			while(rs.next())
			{
				objeto=constructor.newInstance();
				for(Method setter:setters)
				{
					String atributoDelSetter=setter.getName().substring(3);
					atributoDelSetter = stringMinuscula(atributoDelSetter);
					for(Field campo:campos)
					{
						if(campo.getName().equals(atributoDelSetter)&&campo.getAnnotation(Column.class)!=null)
						{

							String nombreEnTabla= nombreEnTabla(dtoClass, campo);
				
							Class type=campo.getType();
							//FALTA CON LOS CASOS QUE NO SEAN PRIMITIVO
							//if(type==String.class) setter.invoke(objeto,rs.getString(nombreEnTabla));
							settearSobreObjeto(rs, type, nombreEnTabla, setter, objeto);

						}
					}
					
				}
				/*Method getterId=objeto.getClass().getMethod("getIdPersona",null);
				System.out.println(getterId.invoke(objeto,null));
				Method getterNombre=objeto.getClass().getMethod("getNombre",null);
				System.out.println(getterNombre.invoke(objeto,null));*/
				objetos.add((T)objeto);
			}
			return objetos;
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
	

}
