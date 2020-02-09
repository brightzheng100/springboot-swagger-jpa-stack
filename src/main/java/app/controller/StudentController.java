package app.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.model.Student;
import app.repository.StudentRepository;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

	private static final Logger LOGGER = LoggerFactory.getLogger(StudentController.class);
	
	@Autowired
	StudentRepository repository;

	@GetMapping("/{id}")
	public ResponseEntity<?> findStudentById(@PathVariable("id") Long id) {
		LOGGER.info("GET v1/students/{}", id);

		try {
			Optional<Student> student = this.repository.findById(id);
			
			if (student.isPresent()) {
				LOGGER.debug("GET v1/students/{} - OK", id);
				return new ResponseEntity<Student>(student.get(), HttpStatus.OK);
			} else {
				LOGGER.warn("GET v1/students/{} - NOT FOUND", id);
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			
		} catch (Exception e) {
			LOGGER.error("GET v1/students/{} - ERROR: {}", id, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/query")
	public ResponseEntity<?> findStudentByName(@RequestParam("name") String name) {
		LOGGER.info("GET v1/students/query?name={}", name);

		try {
			List<Student> students = this.repository.findByNameContaining(name);
			
			return new ResponseEntity<>(students, HttpStatus.OK);
			
		} catch (Exception e) {
			LOGGER.error("GET v1/students/query?name={} - ERROR: {}", name, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping
	public ResponseEntity<?> createStudent(Student student) {
		LOGGER.info("POST v1/students/");
		
		try {
			LOGGER.debug("student: id={}, nid={}, name={}", 
					student.getId(), student.getNid(), student.getName());
			
			this.repository.save(student);
			
			return new ResponseEntity<>(HttpStatus.OK);
			
		} catch (Exception e) {
			LOGGER.error("POST v1/students/ - ERROR: {}", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping
	public ResponseEntity<?> listAll() {
		LOGGER.info("GET v1/students/");
		
		try {
			Iterable<Student> all = this.repository.findAll();
			
			return new ResponseEntity<>(all, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("GET v1/students/ - ERROR: {}", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}