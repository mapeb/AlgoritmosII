package demo.test;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.List;

import org.junit.Assert;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Single;

import tp.utn.Utn;
import tp.utn.demo.domainReal.*;
import tp.utn.main.SingletonConexion;


public class UltimoTest
{
	@org.junit.Test
	public void testFind() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException
	{
		Connection con = SingletonConexion.getConnection();
		
		// verifico el find
		Persona p = Utn.find(con,Persona.class,13);
		Assert.assertEquals(p.getNombre(),"Hernan");
		// ocupacion es LAZY => debe permanecer NULL hasta que haga el get
				Assert.assertNull(p.ocupacion);
		Assert.assertEquals((Integer)p.getOcupacion().getIdOcupacion(),(Integer)4);

		

		// debe traer el objeto
		Ocupacion o = p.getOcupacion();
		Assert.assertNotNull(o);
	
		// verifico que lo haya traido bien
		Assert.assertEquals(o.getDescripcion(),"Ingeniero");
	
		// tipoOcupacion (por default) es EAGER => no debe ser null
		Assert.assertNotNull(o.getTipoOcupacion());
		TipoOcupacion to = o.getTipoOcupacion();
		
		// verifico que venga bien...
		Assert.assertEquals(to.getDescripcion(),"Profesional");
		
		// -- Relation --
		
		// las relaciones son LAZY si o si!
		Assert.assertNull(p.direcciones);
		
		List<PersonaDireccion> dirs = p.getDirecciones();
		Assert.assertNotNull(dirs);
		
		// debe tener 2 elementos
		Assert.assertEquals(dirs.size(),1);
		
		for(PersonaDireccion pd:dirs)
		{
			Persona p1 = pd.getPersona();
			Direccion d = pd.getDireccion();
			
			Assert.assertNotNull(p1);
			Assert.assertNotNull(d);
		
			Assert.assertEquals(p1.getNombre(),p.getNombre());
		}
	}
	
	@org.junit.Test
	public void testXQL() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException
	{
		Connection con = SingletonConexion.getConnection();

		String xql="$ocupacion.tipoOcupacion.descripcion LIKE ?";
		List<Persona> lst = Utn.query(con,Persona.class,xql,"Profesional");

		Assert.assertEquals(lst.size(),5);

		for(Persona p:lst)
		{
			Assert.assertEquals(p.getOcupacion().getTipoOcupacion().getDescripcion(),"Profesional");
		}
	}
}
