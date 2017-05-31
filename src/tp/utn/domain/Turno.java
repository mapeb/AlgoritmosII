package tp.utn.domain;

import java.sql.Date;
import java.util.List;

import tp.utn.ann.Column;
import tp.utn.ann.Gui;
import tp.utn.ann.Id;
import tp.utn.ann.Relation;
import tp.utn.ann.Table;

@Table(name="x_turno")
public class Turno
{
	@Id(strategy=Id.IDENTITY)
	@Column(name="id_turno")
	private Integer idTurno;

	@Column(name="descripcion")
	private String descripcion;

	public Integer getIdTurno()
	{
		return idTurno;
	}

	public void setIdTurno(Integer idTurno)
	{
		this.idTurno = idTurno;
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
