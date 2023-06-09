package app.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "STUDENT")
@Cache(region = "studentCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Student {
	@Id @Column(name = "student_id")
	private Long id;						//id
	
	@Column(name = "student_nid")
	private String nid;						//national ID
	
	@Column(name = "student_name")
	private String name;					//name
	
	public Student() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return "{student_id: " + this.getId() + ", student_nid: " + this.getNid() + ", student_name: " + this.getName() + "}";
	}
}