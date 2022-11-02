package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;


@SpringBootTest
public class EndToEndAssignmentTest {
	
	public static final String CHROME_DRIVER_FILE_LOCATION = "/Users/keshavgupta/Desktop/chromedriver";
	public static final String URL = "http://localhost:3000";
	public static final int SLEEP_DURATION = 1000; // 1 second.
	//Test values
	public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
	public static final String TEST_ASSIGNMENT_NAME = "Test Assignment";
	public static final String TEST_DUE_DATE = "2021-09-01";
	public static final String TEST_COURSE_ID = "999001";
	
	@Autowired
	AssignmentRepository assignmentRepository;
	
	@Test
	public void addAssignmentTest() throws Exception {
		
		//Delete test assignment if it exists already
		List<Assignment> assignments = assignmentRepository.findNeedGradingByEmail(TEST_INSTRUCTOR_EMAIL);
		for(Assignment a: assignments) {
			if(a.getName().equals(TEST_ASSIGNMENT_NAME)) {
				assignmentRepository.delete(a);
			}
		}
		
		//Start web driver
		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
		WebDriver driver = new ChromeDriver();
		// Puts an Implicit wait for 10 seconds before throwing exception
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.get(URL);
		Thread.sleep(SLEEP_DURATION);
		
		try {
			//Find and click button to add assignment
			driver.findElement(By.xpath("//div/a[2]")).click();
			Thread.sleep(SLEEP_DURATION);
			//Enter test data in the fields
			driver.findElement(By.xpath("//div/*[@name=\"name\"]")).sendKeys(TEST_ASSIGNMENT_NAME);
			driver.findElement(By.xpath("//div/*[@name=\"date\"]")).sendKeys(TEST_DUE_DATE);
			driver.findElement(By.xpath("//div/*[@name=\"courseid\"]")).sendKeys(TEST_COURSE_ID);
			Thread.sleep(SLEEP_DURATION);
			//Find and click button to add test assignment
			driver.findElement(By.xpath("//div/*[@id=\"Add\"]")).click();
			Thread.sleep(SLEEP_DURATION);
			//Check toast message
			String toastMessage = driver.findElement(By.className("Toastify__toast-body")).getText();
			assertEquals(toastMessage, "Assignment successfully added");
			//Verify that assignment has been added to the repository
			boolean found = false;
			assignments = assignmentRepository.findNeedGradingByEmail(TEST_INSTRUCTOR_EMAIL);
			for(Assignment a: assignments) {
				if(a.getName().equals(TEST_ASSIGNMENT_NAME)) {
					found=true;
				}
			}
			assertTrue(found, "Assignment was not found in the database");
			
		} catch (Exception ex) {
			throw ex;
			
		} finally {
			//clean up database so the test is repeatable.
			assignments = assignmentRepository.findNeedGradingByEmail(TEST_INSTRUCTOR_EMAIL);
			for(Assignment a: assignments) {
				if(a.getName().equals(TEST_ASSIGNMENT_NAME)) {
					assignmentRepository.delete(a);
				}
			}

			driver.quit();
		}
	}
}
	
