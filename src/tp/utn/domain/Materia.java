package tp.utn.domain;

import java.sql.Date;
import java.util.List;

import tp.utn.ann.Column;
import tp.utn.ann.Gui;
import tp.utn.ann.Id;
import tp.utn.ann.Relation;
import tp.utn.ann.Table;

@Table(name="x_materia")
public class Materia
{

	@Id(strategy=Id.IDENTITY)
	@Column(name="id_materia")
	private Integer idMateria;
	
	@Column(name="cant_horas")
	private Integer cantHoras;
	
	@Column(name="descripcion")
	private String descripcion;

	public Integer getIdMateria()
	{
		return idMateria;
	}

	public void setIdMateria(Integer idMateria)
	{
		this.idMateria = idMateria;
	}

	public Integer getCantHoras()
	{
		return cantHoras;
	}

	public void setCantHoras(Integer cantHoras)
	{
		this.cantHoras = cantHoras;
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
