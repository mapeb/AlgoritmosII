package tp.utn;

import java.util.ArrayList;

public class Xql
{
	protected static ArrayList<String> variablesXql=new ArrayList<String>();
	
	
	public String getClaseDe(String anotacionSQL)// ej: persona.id_persona
	{
		if(anotacionSQL.contains("."))
		{
			String[] division=anotacionSQL.split("\\.");
			return division[0];
		}
		return "";
	}
	public String stringMayuscula(String palabra)
	{
		return (palabra.substring(0,1).toUpperCase()+palabra.substring(1));
	}
	public String stringMinuscula(String palabra)
	{
		return (palabra.substring(0,1).toLowerCase()+palabra.substring(1));

	}

	public void setVariablesXql(String xql)
	{
		if(variablesXql.size()!=0)
			variablesXql.clear();    // NO ME QUEDO OTRA YA QUE ES VARIABLE DE CLASE PORQUE 
		String[] palabras=xql.split(" "); // TERMINAMOS DIVIENDO LAS COSAS EN SUBCLASES
		for(String palabra:palabras)
		{
			if(palabra.substring(0,1).equals("$")) variablesXql.add(palabra.substring(1));
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
