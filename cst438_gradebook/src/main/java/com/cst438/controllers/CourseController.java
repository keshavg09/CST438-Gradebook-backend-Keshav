package com.cst438.controllers;

import java.sql.Date;  
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.GradebookDTO;

@RestController
public class CourseController {
	
	@Autowired
	AssignmentRepository assignmentRepository;
	
	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	AssignmentGradeRepository assignmentGradeRepository;
	
	@PostMapping("/addAssignment")
	@Transactional
	public AssignmentListDTO.AssignmentDTO addAssignment(@RequestBody AssignmentListDTO.AssignmentDTO assignment) {
		System.out.print("Begin");
		
		String email = "dwisneski@csumb.edu";
		List<Assignment> assignments = assignmentRepository.findNeedGradingByEmail(email);
		for(Assignment a: assignments) {
			System.out.print("IN LOOP");
			AssignmentListDTO.AssignmentDTO as = makeAssignmentDTO(a);
			if(assignment.equals(as)) {
				throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Tried to add assignment that already exists." );
			}
		}
		Date dueDate = Date.valueOf(assignment.dueDate);
		Course c = courseRepository.findById(assignment.courseId).orElse(null);
		
		Assignment assign = new Assignment();
		
		assign.setId(assignment.assignmentId);
		assign.setName(assignment.assignmentName);
		assign.setDueDate(dueDate);
		assign.setCourse(c);
		assign.setNeedsGrading(1);
		assignmentRepository.save(assign);
		return assignment;
	}
	
	@PostMapping("/renameAssignment")
	@Transactional
	public AssignmentListDTO.AssignmentDTO renameAssignment(@RequestBody AssignmentListDTO.AssignmentDTO assignment) {
		
		String email = "dwisneski@csumb.edu";
		List<Assignment> assignments = assignmentRepository.findNeedGradingByEmail(email);
		for(Assignment a: assignments) {
			AssignmentListDTO.AssignmentDTO as = makeAssignmentDTO(a);
			if(assignment.assignmentId == as.assignmentId) {
				a.setName(assignment.assignmentName);
				assignmentRepository.save(a);
				return makeAssignmentDTO(a);
			}
		}
		throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Tried to rename an assignment that does not exist." );
	}
	
	@PostMapping("/deleteAssignment")
	@Transactional
	public void deleteAssignment(@RequestBody AssignmentListDTO.AssignmentDTO assignment) {
		Date dueDate = Date.valueOf(assignment.dueDate);
		Course c = courseRepository.findById(assignment.courseId).orElse(null);
		
		Assignment assign = new Assignment();
		
		assign.setId(assignment.assignmentId);
		assign.setName(assignment.assignmentName);
		assign.setDueDate(dueDate);
		assign.setCourse(c);
		assign.setNeedsGrading(1);
		
		String email = "dwisneski@csumb.edu";
		
		GradebookDTO gradebook = new GradebookDTO();
		gradebook.assignmentId= assign.getId();
		gradebook.assignmentName = assign.getName();
		for (Enrollment e : assign.getCourse().getEnrollments()) {
			GradebookDTO.Grade grade = new GradebookDTO.Grade();
			grade.name = e.getStudentName();
			grade.email = e.getStudentEmail();
			AssignmentGrade ag = assignmentGradeRepository.findByAssignmentIdAndStudentEmail(assign.getId(),  grade.email);
			if (ag != null && !ag.getScore().equals("")) {
				throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Cannot delete assignment if there are grades for it." );
			}
		}
		
		assignmentRepository.delete(assign);
	}
	
	public AssignmentListDTO.AssignmentDTO makeAssignmentDTO(Assignment a) {
		AssignmentListDTO.AssignmentDTO assign = new AssignmentListDTO.AssignmentDTO(a.getId(), a.getCourse().getCourse_id(), a.getName(), a.getDueDate().toString() , a.getCourse().getTitle());
		return assign;
	}
}

