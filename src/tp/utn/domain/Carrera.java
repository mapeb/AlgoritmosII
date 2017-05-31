package tp.utn.domain;

import java.sql.Date;
import java.util.List;

import tp.utn.ann.Column;
import tp.utn.ann.Gui;
import tp.utn.ann.Id;
import tp.utn.ann.Relation;
import tp.utn.ann.Table;

@Table(name="x_carrera")
public class Carrera
{
	@Id(strategy=Id.IDENTITY)
	@Column(name="id_carrera")
	private Integer idCarrera;

	@Column(name="descripcion")
	private String descripcion;
	
	public Integer getIdCarrera()
	{
		return idCarrera;
	}
	public void setIdCarrera(Integer idCarrera)
	{
		this.idCarrera = idCarrera;
	}
	public String getDescripcion()
	{
		return descripcion;
	}
	public void setDescripcion(String descripcion)
	{
		this.descripcion = descripcion;
	}
	
	
}
