package app.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import app.model.Student;

@Repository
@Profile({"h2", "mysql", "postgres"}) // the db profiles, add whatever RDBMS you're keen
public interface StudentRepository extends CrudRepository<Student, Long> {
}
