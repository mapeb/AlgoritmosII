package tp.utn.main;

import java.sql.DriverManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import tp.utn.Utn;
import tp.utn.ann.Column;
import tp.utn.ann.Table;
import tp.utn.demo.domain.*;

public class Main
{


	public static <T> void main(String[] args) throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{

		 String query = Utn._query(Persona.class,"where nombre = ?");
		 System.out.println(query);
		 
		List<T> lista = (List<T>) Utn.query(SingletonConexion.getConnection(),Persona.class,"where $persona.nombre = ? AND $direccion.numero = ? AND $persona.id_persona = ?","Juani", 567,6);
		
		System.out.println(lista.size());
		int i = 0;
		
		int cantidad = lista.size();
		while(i<cantidad)
		{
			if(lista.get(i).getClass().getSimpleName().equals("Persona"))
			{
		Method metodo = lista.get(i).getClass().getMethod("getNombre",null);
		Object objeto = lista.get(i);
		Method metodo2 = objeto.getClass().getMethod("getIdPersona",null);
		System.out.println("Nombre: " + metodo.invoke(objeto,null) + " , id Persona = " + metodo2.invoke(objeto,null));
		Method metodo3 = objeto.getClass().getMethod("getDireccion",null);
		Direccion direccionJuani = (Direccion) metodo3.invoke(objeto,null);
		System.out.print("Calle: " + direccionJuani.getCalle());
		System.out.println(", Numero: " + direccionJuani.getNumero());
		Method metodoOcupacion = objeto.getClass().getMethod("getOcupacion",null);
		Ocupacion ocupacionJuani = (Ocupacion) metodoOcupacion.invoke(objeto,null);
		System.out.print("Ocupacion: " + ocupacionJuani.getDescripcion());
		Method metodoTipoOcupacion = ocupacionJuani.getClass().getMethod("getTipoOcupacion",null);
		TipoOcupacion tipoJuani = (TipoOcupacion) metodoTipoOcupacion.invoke(ocupacionJuani,null);
		System.out.println(", Descripcion: " + tipoJuani.getDescripcion());
			}
			i++;
		}
		}

		/*Method metodo3 = lista.get(1).getClass().getMethod("getNombre",null);
		Object objeto2 = lista.get(1);
		Method metodo4 = objeto2.getClass().getMethod("getIdPersona",null);
		System.out.println(metodo.invoke(objeto2,null) + " ," + metodo4.invoke(objeto2,null));*/

	}


