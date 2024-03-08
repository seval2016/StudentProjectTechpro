package com.project.controller.business;

import com.project.payload.request.business.MeetRequest;
import com.project.payload.response.business.MeetResponse;
import com.project.payload.response.business.ResponseMessage;
import com.project.service.business.MeetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/meet")
@RequiredArgsConstructor
public class MeetController {

    private  final MeetService meetService;

    @PostMapping("/save") // hhtp://localhost:8080/meet/save + JSON + POST
    @PreAuthorize("hasAnyAuthority('TEACHER')")
    public ResponseMessage<MeetResponse> saveMeet(HttpServletRequest httpServletRequest,
                                                  @RequestBody @Valid MeetRequest meetRequest){
        return meetService.saveMeet(httpServletRequest, meetRequest);
    }

    // Not: ODEV  --> getALL   ("/getAll") sadece ADMIN
    @GetMapping("/getAll") //http://localhost:8080/meet/getAll
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public List<MeetResponse> getAll(){
            return meetService.getAll();
    }
    // Not: ODEV --> getMeetById    ("/getMeetById/{meetId}")    sadece ADMIN
    @GetMapping("/getMeetById/{meetId}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseMessage<MeetResponse> getMeetById (@PathVariable Long meetId){
        return meetService.getMeetById(meetId);
    }

    // Not :ODEV --> DELETE("/delete/{meetId}")  TEACHER ve ADMIN
    @DeleteMapping("/delete/{meetId}")//http://localhost:8080/meet/delete/3
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public ResponseMessage delete(@PathVariable Long meetId, HttpServletRequest httpServletRequest){
        return meetService.delete(meetId,httpServletRequest);
    }

    // Not: ODEV --> getALLWithPage  ("/getAllMeetByPage")  sadece Admin
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/getAllMeetByPage") // http://localhost:8080/meet/getAllMeetByPage?page=0&size=1
    public Page<MeetResponse> getAllMeetByPage(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size
    ){
        return meetService.getAllMeetByPage(page,size);
    }
}