package com.project.controller.business;

import com.project.payload.request.business.MeetRequest;
import com.project.payload.response.business.MeetResponse;
import com.project.payload.response.business.ResponseMessage;
import com.project.service.business.MeetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
    // Not: ODEV --> geetMeetById    ("/getMeetById/{meetId}")    sadece ADMIN
    // Not :ODEV --> DELETE     ("/delete/{meetId}")  TEACHER ve ADMIN
    // Not: ODEV --> getALLWithPage  ("/getAllMeetByPage")  sadece Admin

}