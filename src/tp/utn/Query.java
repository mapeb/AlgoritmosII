
package tp.utn;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Iterator;
import java.util.List;

// import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import tp.utn.ann.Column;
import tp.utn.ann.Id;
import tp.utn.ann.Table;
//import tp.utn.main.SingletonConexion;


public class Query extends Xql
{
	List<String> select;
	String from;
	private static String condicionOrdenada;
	private ArrayList<Field> atributosNoNulosDelObjeto;	
	private ArrayList<Object> contenidoDeAtributosDelObjeto;

	public Query(String from)
	{
		super();
		select=new ArrayList<String>();
		atributosNoNulosDelObjeto = new ArrayList<Field>();
		contenidoDeAtributosDelObjeto = new ArrayList<Object>();
		this.from=from;

	}
	public Query()
	{
		super();
		select=new ArrayList<String>();
		atributosNoNulosDelObjeto = new ArrayList<Field>();
		contenidoDeAtributosDelObjeto = new ArrayList<Object>();
	}
	public ArrayList<Field> getAtributosNoNulosDelObjeto()
	{
		return atributosNoNulosDelObjeto;
	}
	public ArrayList<Object> getContenidoDeAtributosDelObjeto()
	{
		return contenidoDeAtributosDelObjeto;
	}
	public void generarQuery(Field[] campos, Class<?> dtoClass)
	{
		for(Field campo:campos)
		{
			if(campo.getAnnotation(Column.class)!=null&&Reflection.isPrimitiveClass(campo))
				this.addAttr(dtoClass,campo.getAnnotation(Column.class).name());
			else
			{
				if(campo.getType().getAnnotation(Table.class)!=null)
				{
					// this.addJoin(campo, campo.getType());
					this.addJoin(dtoClass,campo);
					generarQuery(campo.getType().getDeclaredFields(),campo.getType());
				}
			}
		}
	}

	
	public String sacarPesos(String frase)
	{
		StringBuffer cadena=new StringBuffer();
		String[] palabras=frase.split("\\$");
		for(String palabra:palabras)
		{
			cadena=cadena.append(palabra);
		}
		return cadena.toString();
	}

public String cambiarAtributoPorNombreEnTabla(Field campo, Class dtoClass, String atributo)
{
	
	String[] modificacion = condicionOrdenada.split(" ");
	String nombreEnTabla = Annotation.getAnnotationFieldName(campo);
	String reemplazo = "$"+stringMinuscula(dtoClass.getSimpleName())+"."+nombreEnTabla;
	String aModificar = null;
	for(String modif : modificacion)
	{
		if(modif.equals("$"+atributo))
		{
		 aModificar = modif;
		break;
		}
	}
	return condicionOrdenada = condicionOrdenada.replace(aModificar,reemplazo);
}
	public String modificarAtributosClaseAFilasTabla(String xql, Class<?> dtoClass)
	{
		setVariablesXqlWhere(xql);
		condicionOrdenada = xql;
		ArrayList<String> variables=new ArrayList<String>();
		String modificacion=xql;
		for(String atributo : variablesXqlWhere)
		{
			
			if(stringMayuscula(getClaseDe(atributo)).equals(dtoClass.getSimpleName()))
			{
				for(Field campo:dtoClass.getDeclaredFields())
				{
					if(campo.getName().equals(getAtributoSinNombreClase(atributo))
							&& !(Annotation.getAnnotationFieldName(campo).equals(getAtributoSinNombreClase(atributo))))
					{
						modificacion = cambiarAtributoPorNombreEnTabla(campo,dtoClass, atributo);
					}
				}
			}
			else
			{
				int i=0;
				for(Field campito:dtoClass.getDeclaredFields())
				{
					
					if((!Reflection.isPrimitiveClass(campito))&&stringMayuscula(getClaseDe(atributo)).equals(campito.getType().getSimpleName()))
					{
						
						for(Field campoSegunda:campito.getType().getDeclaredFields())
						{
							if(campoSegunda.getName().equals(getAtributoSinNombreClase(atributo))
									&& !(Annotation.getAnnotationFieldName(campoSegunda).equals(getAtributoSinNombreClase(atributo))))
							{
								modificacion = cambiarAtributoPorNombreEnTabla(campoSegunda,dtoClass, atributo);
								i=1;
								break;
							}
						}
					}
					if(i==1) break;
				}
			}
		}
		return modificacion;
	}
	public String getAtributosRealesDeTabla(String xql, Class dtoClass)
	{
		String xqlConFilasDeTabla = modificarAtributosClaseAFilasTabla(xql, dtoClass);
		return sacarPesos(xqlConFilasDeTabla);
		
	}
	public String setDeUpdate(String xql, Object obj) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Class dtoClass = obj.getClass();
		settearAtributosNoNulosYContenido(dtoClass,obj);
		String[] division = xql.split("WHERE");
		String set = "";
		int cantAtributos = atributosNoNulosDelObjeto.size();
		for(Field atributoNoNulo : atributosNoNulosDelObjeto)
		{
			for(Object contenido : contenidoDeAtributosDelObjeto)
			{
			if(cantAtributos>1)
			{
			set+=atributoNoNulo+"="+contenidoDeAtributosDelObjeto+", ";
			cantAtributos--;
			}
			if(cantAtributos==1)
			set+=atributoNoNulo+"="+contenidoDeAtributosDelObjeto;
			break;
			}
		}
		for(String cadena : division)
		{
			if(cadena.endsWith("SET "))
				cadena+=set;
				
		}
		StringBuffer buffer = new StringBuffer();
		for(String cadena : division)
		buffer = buffer.append(cadena);
		return buffer.toString();
		
		
	}
	public String generarStringUpdate(String xqlWhere, Class dtoClass) // HORRIBLE TENER QUE HACERLO ASI 
	{
		String xqlFinal = getAtributosRealesDeTabla(xqlWhere, dtoClass);
		return "UPDATE FROM "+from+" SET " +" WHERE "+ xqlFinal;
		
		
	}
	public void settearAtributosNoNulosYContenido(Class dtoClass, Object obj) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		for(Field atributo : dtoClass.getDeclaredFields())
		{
			Method getterAtributo = Reflection.getGetterDeAtributo(dtoClass,atributo);
			Object contenido =  getterAtributo.invoke(obj,null);
			if(atributo.getAnnotation(Column.class)!=null && contenido!=null)
			{
				atributosNoNulosDelObjeto.add(atributo);
				contenidoDeAtributosDelObjeto.add(contenido);
			}
		}
	}
	public String generarStringInsert(Object obj) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Class dtoClass = obj.getClass();
		String query = "INSERT INTO " + Annotation.getTableName(dtoClass)+"(";
		String valores = " VALUES(";
		// obligado a haerlo así 
																		//por la coma. no se me ocurrio otra
																		// que me cagaba todo
									// podria haberlo hecho normal y eliminar una coma si aparecia a lo ultimo
					// pero ya ta, paja
		settearAtributosNoNulosYContenido(dtoClass, obj);
		int cantidadAtributos = atributosNoNulosDelObjeto.size();
		int i=0;
		for(Field atributoNoNulo : atributosNoNulosDelObjeto)
		{	
		
			if(cantidadAtributos>1)
			{
			query+=Annotation.getAnnotationFieldName(atributoNoNulo)+", ";
			if(contenidoDeAtributosDelObjeto.get(i).getClass().equals(String.class))
				valores+="'"+contenidoDeAtributosDelObjeto.get(i).toString()+"', ";
			else
			valores+=contenidoDeAtributosDelObjeto.get(i).toString()+", ";
			cantidadAtributos--;
			i++;
			}
			else
			{
				if(cantidadAtributos==1)
				{
					query+=Annotation.getAnnotationFieldName(atributoNoNulo);
					if(contenidoDeAtributosDelObjeto.get(i).getClass().equals(String.class))
					valores+="'"+contenidoDeAtributosDelObjeto.get(i).toString()+"'";
					else
					valores+=contenidoDeAtributosDelObjeto.get(i).toString();
					i++;
				}
			}
			
		}
		query+=")";
		valores+=")";
		return query+=valores;
		
	}
	public String generarStringDelete(String xql, Class dtoClass)
	{
		String xqlFinal = getAtributosRealesDeTabla(xql, dtoClass);
		return "DELETE FROM "+from+" WHERE " +xqlFinal;
	}
	public String generarStringSelect(String xql, Class dtoClass)
	{
		String xqlFinal="";
		if(!xql.equals(""))
		xqlFinal =getAtributosRealesDeTabla(xql, dtoClass);
		String q="SELECT ";
		for(String attr:this.getSelect())
			q+=attr+",";
		q=q.substring(0,q.length()-1);
		q+=" FROM "+from+" ";
		if(!xqlFinal.equals(""))
			q+= "WHERE " + xqlFinal;
		return q;
	}

	public void addAttr(Class<?> claseContenedora, String atrr)
	{
		String newAtrr=Annotation.getTableName(claseContenedora)+"."+atrr;
		this.getSelect().add(newAtrr);
	}

	public void addJoin(Class<?> claseRaiz, Field campo)
	{
		// Table tabla2 = (Table) clase2.getAnnotation(Table.class);
		from+=" JOIN "+Annotation.getTableName(campo.getType())+joinDeTablas(claseRaiz,campo);
	}

	private String joinDeTablas(Class<?> campoSolicitante, Field campoSolicitado)
	{
		String comparacion=" ON ( "+Annotation.getTableName(campoSolicitante);
		comparacion+="."+campoSolicitado.getAnnotation(Column.class).name()+" = ";
		comparacion+=Annotation.getTableName(campoSolicitado.getType())+".";

		for(Field field:campoSolicitado.getType().getDeclaredFields())
		{
			if(field.getAnnotation(Id.class)!=null) return comparacion+field.getAnnotation(Column.class).name()+" )";
		}
		return ""; // Hola si la funcion no tiene id debo explotar, ya se .
	}

	public List<String> getSelect()
	{
		return select;
	}

	public void setSelect(List<String> select)
	{
		this.select=select;
	}

	public void setFrom(String n)
	{
		this.from=n;
	}


}
