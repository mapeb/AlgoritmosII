package tp.utn.demo.domain;

import tp.utn.ann.Column;
import tp.utn.ann.Id;
import tp.utn.ann.Table;
@Table(name="personaPrueba")
public class PersonaPrueba
{
	@Id(strategy=Id.IDENTITY)
	@Column(name="id_persona")
	private Integer idPersona;
	
	@Column(name="nombre")
	private String nombre;
	
	public PersonaPrueba(Integer unId, String unNombre, Direccion unaDireccion, Ocupacion unaOcupacion)
	{
		this.setIdPersona(unId);
		this.setNombre(unNombre);

		
	}
	public PersonaPrueba()
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

	public String getNombre()
	{
		return nombre;
	}

	public void setNombre(String nombre)
	{
		this.nombre=nombre;
	}



	@Override
	public String toString()
	{
		return "Persona [idPersona="+idPersona+", nombre="+nombre+"]";
	}

}
