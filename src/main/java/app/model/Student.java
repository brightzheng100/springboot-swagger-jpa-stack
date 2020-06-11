package app.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "STUDENT")
@Cache(region = "employeeCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Student {
	@Id @Column(name = "student_id")
	private Long id;						//id
	
	@NonNull @Column(name = "student_nid")
	private String nid;						//national ID
	
	@NonNull @Column(name = "student_name")
	private String name;					//name
}