package ajdu_restful_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ajdu_restful_api.model.Schedule;
import ajdu_restful_api.model.CalendarTask;
import ajdu_restful_api.model.TaskStatus;
import ajdu_restful_api.model.User;
import ajdu_restful_api.service.ScheduleService;
import ajdu_restful_api.service.CalendarTaskService;
import ajdu_restful_api.service.UserService;

@RestController
public class ScheduleRestController extends AuthenticatedRestController {

	@Autowired
	ScheduleService scheduleService;
	@Autowired
	UserService userService;
	@Autowired
	CalendarTaskService taskService;
	
	
	@RequestMapping(value="/schedules",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<Schedule>> getAllSchedules() {
		return new ResponseEntity<List<Schedule>>(scheduleService.findSchedules(),HttpStatus.OK);
	}
	
	@RequestMapping(value="/schedules",method=RequestMethod.POST,consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Schedule> addSchedule(@RequestBody Schedule schedule, Authentication auth) {
		if(schedule.getUser() == null || schedule.getUser().getId() == null) {
			return new ResponseEntity<Schedule>(HttpStatus.BAD_REQUEST);
		}
		else {		
			User u = userService.findUser(schedule.getUser().getId());
			if(!hasPermission(auth, u.getLogin()))
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			
			if(u.getSchedule() != null)
				return new ResponseEntity<Schedule>(HttpStatus.CONFLICT);
			else {			
				scheduleService.save(schedule);
				return new ResponseEntity<Schedule>(schedule, HttpStatus.CREATED);
			}
		}
	}
	
	@RequestMapping(value="/schedules/{id}",method=RequestMethod.DELETE)
	public ResponseEntity<Schedule> deleteSchedule(@PathVariable int id, Authentication auth) {
		Schedule s = scheduleService.findSchedule(id);
		if(s != null) {
			User u = userService.findUser(s.getUser().getId());
			if(!hasPermission(auth, u.getLogin()))
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			
			scheduleService.delete(id);
			return new ResponseEntity<Schedule>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<Schedule>(HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping(value="/schedules/{id}",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Schedule> getSchedule(@PathVariable int id, Authentication auth) {
		Schedule s = scheduleService.findSchedule(id);
		if(s != null) {
			User u = userService.findUser(s.getUser().getId());
			if(!hasPermission(auth, u.getLogin()))
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			
			return new ResponseEntity<Schedule>(scheduleService.findSchedule(id),HttpStatus.OK);
		}
		else return new ResponseEntity<Schedule>(HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping(value="/schedules/{id}",method=RequestMethod.PUT,consumes=MediaType.APPLICATION_JSON_UTF8_VALUE )
	public ResponseEntity<Schedule> updateSchedule(@PathVariable int id, @RequestBody Schedule schedule, Authentication auth) {
		Schedule s = scheduleService.findSchedule(id);
		if(s != null) {
			User u = userService.findUser(s.getUser().getId());
			if(!hasPermission(auth, u.getLogin()))
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			
			s.setTasks(schedule.getTasks());
			scheduleService.save(s);
			return new ResponseEntity<Schedule>(s, HttpStatus.OK);
		}
		return new ResponseEntity<Schedule>(HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping(value="/schedules/{id}/tasks",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<CalendarTask>> findAllTasksBySchedule(@PathVariable int id, Authentication auth){
		Schedule s = scheduleService.findSchedule(id);
		if(s != null) {
			User u = userService.findUser(s.getUser().getId());
			if(!hasPermission(auth, u.getLogin()))
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			
			return new ResponseEntity<List<CalendarTask>>(taskService.findAllTaskBySchedule(id), HttpStatus.OK);
		}
		else 
			return new ResponseEntity<List<CalendarTask>>(HttpStatus.NOT_FOUND);
	}
	
	@RequestMapping(value="/schedules/{id}/tasks", method=RequestMethod.POST,consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<CalendarTask> addTaskToSchedule(@RequestBody CalendarTask task, @PathVariable int id, Authentication auth) {
		Schedule s = scheduleService.findSchedule(id);
		if(s != null) {
			User u = userService.findUser(s.getUser().getId());
			if(!hasPermission(auth, u.getLogin()))
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			
			task.setSchedule(s);
			task.setStatus(TaskStatus.TODO);
			taskService.saveTask(task);
			return new ResponseEntity<CalendarTask>(task, HttpStatus.CREATED);
		}
		else return new ResponseEntity<CalendarTask>(HttpStatus.NOT_FOUND);
	}
	
	
}
