package tp.utn.demo.domain;

import java.util.List;

import tp.utn.ann.Column;
import tp.utn.ann.Gui;
import tp.utn.ann.Id;
import tp.utn.ann.Relation;
import tp.utn.ann.Table;

@Table(name="deptT", alias="d")
public class Dept 
{
	@Id(strategy=Id.ASSIGNED)
	@Column(name="deptno")
	@Gui(editable=true)
	private Integer deptno;
	
	@Column(name="dname")
	private String dname;
	
	@Column(name="loc")
	private String loc;
	
	@Relation(type=Emp.class, att="dept") // ONE TO MANY -> TYPE: TIPO DE DATO A TRAER; ATT-> ATRIBUTO
	private List<Emp> emps;                                 // QUE EST� MAPEANDO DEL OTRO LADO (DEL LADO
                                                           //DEL ONE

	// Getters and setters.
	public Integer getDeptno()
	{ 
		return deptno;
	}
	public void setDeptno(Integer deptno)
	{
		this.deptno=deptno;
	}
	public String getDname()
	{
		return dname;
	}
	public void setDname(String dname)
	{
		this.dname=dname;
	}
	public String getLoc()
	{
		return loc;
	}
	public void setLoc(String loc)
	{
		this.loc=loc;
	}
	public List<Emp> getEmps()
	{
		return emps;
	}
	public void setEmps(List<Emp> emps)
	{
		this.emps = emps;
	}	

	public boolean equals(Object o)
	{
		return ((Dept)o).getDeptno()==getDeptno();			
	}
	@Override
	public String toString()
	{
		return getDname();
	}

}

