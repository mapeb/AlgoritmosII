package tp.utn.demo.domain;

import tp.utn.ann.Column;
import tp.utn.ann.Id;
import tp.utn.ann.Table;
@Table(name="personaDos")
public class PersonaDos
{
	@Id(strategy=Id.IDENTITY)
	@Column(name="id_persona")
	private Integer idPersona;
	
	@Column(name="esFeo")
	private boolean esFeo;
	

		
	//LO SAQUE PARA PROBAR FUNCIONAMIENTO. FALTA DESARROLLAR CASO DONDE CAMPO NO PRIMITIVO
	/*@Column(name="id_direccion")    
	public Direccion direccion;
	
	@Column(name="id_ocupacion")
	public Ocupacion ocupacion;*/

	public PersonaDos()
	{
		super();
	}
	public Integer getIdPersona()
	{
		return idPersona;
	}

	public void setIdPersona(Integer idPersona)
	{
		this.idPersona=idPersona;
	}

	public boolean getEsFeo()
	{
		return esFeo;
	}

	public void setNombre(boolean nombre)
	{
		this.esFeo=nombre;
	}

	/*public Direccion getDireccion()
	{
		return direccion;
	}*/

	/*public void setDireccion(Direccion direccion)
	{
		this.direccion=direccion;
	}

	public Ocupacion getOcupacion()
	{
		return ocupacion;
	}

	public void setOcupacion(Ocupacion ocupacion)
	{
		this.ocupacion=ocupacion;
	}*/

	/*@Override
	public String toString()
	{
		return "Persona [idPersona="+idPersona+", nombre="+nombre+", direccion="+direccion+", ocupacion="+ocupacion+"]";
	}
	
	@Override
	public boolean equals(Object o)
	{
		Persona other = (Persona)o;
		boolean ok = true;
		ok = ok && idPersona==other.getIdPersona();
		ok = ok && nombre.equals(other.getNombre());

		if( direccion!=null )
		{
			ok = ok && direccion.getIdDireccion()==other.getDireccion().getIdDireccion();
		}
		else
		{
			ok = ok && other.getDireccion()==null;
		}
		
		if( ocupacion!=null )
		{
			ok = ok && ocupacion.getIdOcupacion()==other.getOcupacion().getIdOcupacion();
		}
		else
		{
			ok = ok && other.getOcupacion()==null;
		}
		
		return ok;
	}*/
	
	
	

	
}
