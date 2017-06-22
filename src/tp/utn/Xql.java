package tp.utn;

import java.util.ArrayList;

public class Xql
{
	protected static ArrayList<String> variablesXqlWhere=new ArrayList<String>();
	protected static ArrayList<String> variablesXqlUpdate = new ArrayList<String>();
	
	
	public String getClaseDe(String anotacionSQL)// ej: persona.id_persona
	{
		if(anotacionSQL.contains("."))
		{
			String[] division=anotacionSQL.split("\\.");
			return division[0];
		}
		return "";
	}
	public static String stringMayuscula(String palabra)
	{
		return (palabra.substring(0,1).toUpperCase()+palabra.substring(1));
	}
	public static String stringMinuscula(String palabra)
	{
		return (palabra.substring(0,1).toLowerCase()+palabra.substring(1));

	}
	public void setVariablesXqlUpdate(String xql)
	{
		//$nombre=? WHERE ...
		//if(variablesXqlUpdate.size())
	}
	public void setVariablesXqlWhere(String xql)
	{
		if(variablesXqlWhere.size()!=0)
			variablesXqlWhere.clear();    // NO ME QUEDO OTRA YA QUE ES VARIABLE DE CLASE PORQUE 
		String[] palabras=xql.split(" "); // TERMINAMOS DIVIENDO LAS COSAS EN SUBCLASES
		for(String palabra:palabras)
		{
			if(palabra.substring(0,1).equals("$")) variablesXqlWhere.add(palabra.substring(1));
		}
	}
	public String getAtributoSinNombreClase(String anotacionSQL)
	{
		if(anotacionSQL.contains("."))
		{

			String[] division=anotacionSQL.split("\\.");
			return division[1];
		}
		return anotacionSQL;
	}
}
