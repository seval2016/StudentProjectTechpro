package com.project.service.business;

import com.project.entity.concretes.business.Meet;
import com.project.entity.concretes.user.User;
import com.project.entity.enums.RoleType;
import com.project.exception.BadRequestException;
import com.project.exception.ConflictException;
import com.project.exception.ResourceNotFoundException;
import com.project.payload.mappers.MeetMapper;
import com.project.payload.messages.ErrorMessages;
import com.project.payload.messages.SuccessMessages;
import com.project.payload.request.business.MeetRequest;
import com.project.payload.response.business.MeetResponse;
import com.project.payload.response.business.ResponseMessage;
import com.project.repository.business.MeetRepository;
import com.project.service.helper.MethodHelper;
import com.project.service.helper.PageableHelper;
import com.project.service.user.UserService;
import com.project.service.validator.DateTimeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetService {

    private final MeetRepository meetRepository;
    private final MethodHelper methodHelper;
    private final DateTimeValidator dateTimeValidator;
    private final UserService userService;
    private final MeetMapper meetMapper;
    private final PageableHelper pageableHelper;

    public ResponseMessage<MeetResponse> saveMeet(HttpServletRequest httpServletRequest,
                                                  MeetRequest meetRequest) {
        String username = (String) httpServletRequest.getAttribute("username");
        User advisorTeacher =  methodHelper.isUserExistByUsername(username);
        methodHelper.checkAdvisor(advisorTeacher);

        // !!! Yeni Meet saatlerınde cakısma var mı kontrolu
        dateTimeValidator.checkTimeWithException(meetRequest.getStartTime(), meetRequest.getStopTime());

        //!!! AdvTeacher'in mevcut meetleri ile cakisma var mi
        checkMeetConflict(advisorTeacher.getId(), meetRequest.getDate(),
                meetRequest.getStartTime(), meetRequest.getStopTime());

        //!!! Meete e katilacak ogrenciler getiriliyor
        List<User> students = userService.getStudentById(meetRequest.getStudentIds());
        //!!! DTO --> POJO
        Meet meet = meetMapper.mapMeetRequestToMeet(meetRequest);
        meet.setStudentList(students);
        meet.setAdvisoryTeacher(advisorTeacher);

        Meet savedMeet =  meetRepository.save(meet);

        return ResponseMessage.<MeetResponse>builder()
                .message(SuccessMessages.MEET_SAVED)
                .object(meetMapper.mapMeetToMeetResponse(savedMeet))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    private void checkMeetConflict(Long userId, LocalDate date, LocalTime startTime, LocalTime stoptime) {

        List<Meet> meets ;

        //!!! Student veya Teachera ait olan mevcut meet ler getiriliyor
        if(Boolean.TRUE.equals(userService.getUserByUserId(userId).getIsAdvisor())){
            meets = meetRepository.getByAdvisoryTeacher_IdEquals(userId);
        } else meets = meetRepository.findByStudentList_IdEquals(userId);

        //!!! cakisma kontrolu
        for(Meet meet: meets){
            LocalTime existingStartTime =  meet.getStartTime();
            LocalTime existingStopTime =  meet.getStopTime();

            if(meet.getDate().equals(date) &&
                    (
                            (startTime.isAfter(existingStartTime) && startTime.isBefore(existingStopTime)) ||
                                    (stoptime.isAfter(existingStartTime) && stoptime.isBefore(existingStopTime)) ||
                                    (startTime.isBefore(existingStartTime) && stoptime.isAfter(existingStopTime)) ||
                                    (startTime.equals(existingStartTime) || stoptime.equals(existingStopTime))

                    )

            ){
                throw new ConflictException(ErrorMessages.MEET_HOURS_CONFLICT);
            }
        }

    }

    public List<MeetResponse> getAll() {
        return meetRepository.findAll()
                .stream()
                .map(meetMapper::mapMeetToMeetResponse)
                .collect(Collectors.toList());
    }

    public ResponseMessage<MeetResponse> getMeetById(Long meetId) {
        return ResponseMessage.<MeetResponse>builder()
                .message(SuccessMessages.MEET_FOUND)
                .httpStatus(HttpStatus.OK)
                .object(meetMapper.mapMeetToMeetResponse(isMeetExistById(meetId)))
                .build();
    }

    private Meet isMeetExistById(Long meetId){
        return meetRepository
                .findById(meetId).orElseThrow(
                        ()->new ResourceNotFoundException(String.format(ErrorMessages.MEET_NOT_FOUND_MESSAGE,meetId)));
    }

    public ResponseMessage delete(Long meetId, HttpServletRequest httpServletRequest) {
        Meet meet = isMeetExistById(meetId);
        //!!! Teacher ise sadece kendi Meet lerini silebilsin

        String userName = (String) httpServletRequest.getAttribute("username");
        User teacher = methodHelper.isUserExistByUsername(userName);
        if(teacher.getUserRole().getRoleType().equals(RoleType.TEACHER)){
            isTeacherControl(meet, teacher);
        }

        meetRepository.deleteById(meetId);
        //TODO : ogrencilerden meet silinecek
        return ResponseMessage.builder()
                .message(SuccessMessages.MEET_DELETE)
                .httpStatus(HttpStatus.OK)
                .build();

    }

    private void isTeacherControl(Meet meet, User teacher){
        //!!! Teacher ise sadece kendi Meet lerini silebilsin
        if(
                (teacher.getUserRole().getRoleType().equals(RoleType.TEACHER)) && // metodu tetikleyenin Role bilgisi TEACHER ise
                        !(meet.getAdvisoryTeacher().getId().equals(teacher.getId())) // Teacher, baskasinin Meet ini silmeye calisiyorsa
        )
        {
            throw new BadRequestException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
        }
    }

    public Page<MeetResponse> getAllMeetByPage(int page, int size) {
        Pageable pageable = pageableHelper.getPageableWithProperties(page, size);
        return meetRepository.findAll(pageable).map(meetMapper::mapMeetToMeetResponse);
    }
}