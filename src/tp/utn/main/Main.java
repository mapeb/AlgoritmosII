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

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import tp.utn.Utn;
import tp.utn.ann.Column;
import tp.utn.ann.Table;
import tp.utn.demo.domain.*;

public class Main
{

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException
	{
			Connection con = SingletonConexion.getConnection();
			List<Persona> lista = (List<Persona>) Utn.query(con,Persona.class,"$direccion.numero = ?",123);
			for(Persona per : lista)
			{
				
				System.out.println("Nombre " + per.nombre);
				System.out.println("Nombre : " + per.getNombre());
				System.out.println("Direccion: " + per.direccion);
				System.out.println("Direccion : " + per.getDireccion());
				System.out.print("Ocupacion : " + per.getOcupacion().getDescripcion());
				System.out.println(" Descripcion : " + per.getOcupacion().getTipoOcupacion().getDescripcion());
			
			}
			int id =6;
			Persona personaFound = Utn.find(con,Persona.class,id);
			
			System.out.println("La persona buscada por el id "+id+ " es " + personaFound.getNombre());
			
			/*Persona p = new Persona();
			p.setNombre("juanitoElLoco");
			p.setIdPersona(77);
			
			System.out.println("Cantidad de filas afectadas " +Utn.insert(con,p));
			
			Direccion d = new Direccion();
			d.setCalle("la nueva calle");
			d.setIdDireccion(66);
			d.setNumero(5);
			
			System.out.println("Cantidad de filas afectadas DIRECCION " +Utn.insert(con,d));	*/
			
			//System.out.println("Cantidad de filas eliminadas " + Utn.delete(con,Persona.class,"$persona.idPersona = ?",66));
			int idEliminar = 77;
			System.out.println("Cantidad de filas eliminadas con id " + idEliminar + " es: " + Utn.delete(con,Persona.class,idEliminar));
	} 

}
