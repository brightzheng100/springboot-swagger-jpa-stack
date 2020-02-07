package app.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "STUDENT")
public class Student {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;				//id
	private @NonNull String nid;	//national ID
	private @NonNull String name;	//name
}