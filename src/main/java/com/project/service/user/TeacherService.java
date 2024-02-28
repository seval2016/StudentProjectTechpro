package com.project.service.user;

import com.project.contactmessage.repository.ContactMessageRepository;
import com.project.entity.concretes.user.User;
import com.project.entity.enums.RoleType;
import com.project.exception.ConflictException;
import com.project.payload.mappers.UserMapper;
import com.project.payload.request.business.messages.ErrorMessages;
import com.project.payload.request.business.messages.SuccessMessages;
import com.project.payload.request.user.TeacherRequest;
import com.project.payload.response.business.ResponseMessage;
import com.project.payload.response.user.StudentResponse;
import com.project.payload.response.user.TeacherResponse;
import com.project.payload.response.user.UserResponse;
import com.project.repository.user.UserRepository;
import com.project.repository.user.UserRoleRepository;
import com.project.service.helper.MethodHelper;
import com.project.service.validator.UniquePropertyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final UserRoleRepository userRoleRepository;
    private final UniquePropertyValidator uniquePropertyValidator;
    private final UserMapper userMapper;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final MethodHelper methodHelper;
    private final ContactMessageRepository contactMessageRepository;

    public ResponseMessage<TeacherResponse> saveTeacher(TeacherRequest teacherRequest) {

        //!!! TODO : LessonProgram kontrolu

        //!!! unique kontrolu
        uniquePropertyValidator.checkDuplicate(teacherRequest.getUsername(), teacherRequest.getSsn(),
                teacherRequest.getPhoneNumber(), teacherRequest.getEmail());
        //!!! DTO --> POJO
        User teacher = userMapper.mapTeacherRequestToUser(teacherRequest);

        teacher.setUserRole(userRoleService.getUserRole(RoleType.TEACHER));//getUserRole() methodu argüman olan rol db'de var mı kontrol ediyor varsa getiriyor yoksa haa fırlatıyor. Bu kodda pojo class'ta olması gereken rol bilgisini setlemiş olduk.

        //!!! TODO : LessonProgram setleme islemi :
        teacher.setPassword(passwordEncoder.encode(teacherRequest.getPassword()));

        if(teacherRequest.getIsAdvisorTeacher()){
            teacher.setIsAdvisor(Boolean.TRUE);
        } else teacher.setIsAdvisor(Boolean.FALSE);

        User savedTeacher = userRepository.save(teacher);

        return ResponseMessage.<TeacherResponse>builder()
                .message(SuccessMessages.TEACHER_SAVED)
                .httpStatus(HttpStatus.CREATED)//neden burada gönderildi de user da gönderilmedi ?
                .object(userMapper.mapUserToTeacherResponse(savedTeacher))
                .build();
    }

    public ResponseMessage<TeacherResponse> updateTeacherForManagers(TeacherRequest teacherRequest, Long userId) {

        //!!! id var mı yok mu kontrolu
        User user = methodHelper.isUserExist(userId);

        //!!! parametrede gelen User gercekten Teacher mi kontrolu
        methodHelper.checkRole(user, RoleType.TEACHER);

        //!!! TODO : LessonProgramlar getiriliyor

        //!!! unique kontrolu
        uniquePropertyValidator.checkUniqueProperties(user, teacherRequest);

        //!!! DTO --> POJO
        User updatedTeacher = userMapper.mapTeacherRequestToUpdatedUser(teacherRequest, userId);
        updatedTeacher.setPassword(passwordEncoder.encode(teacherRequest.getPassword()));

        //!!! TODO :  LessonProgramlar setlenecek
        updatedTeacher.setUserRole(userRoleService.getUserRole(RoleType.TEACHER));

        User savedTeacher = userRepository.save(updatedTeacher);

        return ResponseMessage. <TeacherResponse>builder()
                .object(userMapper.mapUserToTeacherResponse(savedTeacher))
                .message(SuccessMessages.TEACHER_UPDATE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public List<StudentResponse> getAllStudentByAdvisorUsername(String userName) {

        //userName bilgisi var mı db de ?
        User teacher = methodHelper.isUserExistByUsername(userName);

        //!!! isAdvisor ??
        methodHelper.checkAdvisor(teacher);

        return userRepository.findByAdvisorTeacherId(teacher.getId())
                .stream()
                .map(userMapper::mapUserToStudentResponse)
                .collect(Collectors.toList());
    }

    public ResponseMessage<UserResponse> saveAdvisorTeacher(Long teacherId) {

        User teacher = methodHelper.isUserExist(teacherId);
        methodHelper.checkRole(teacher, RoleType.TEACHER);
        //!!! id ile gelen teacher zaten Advisor mi konrolu
        if(Boolean.TRUE.equals(teacher.getIsAdvisor())) {
            throw new ConflictException(String.format(ErrorMessages.ALREADY_EXIST_ADVISOR_MESSAGE, teacherId));
        }

        teacher.setIsAdvisor(Boolean.TRUE);
        userRepository.save(teacher);

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.ADVISOR_TEACHER_SAVE)
                .object(userMapper.mapUserToUserResponse(teacher))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public ResponseMessage<UserResponse> deleteAdvisorTeacherById(Long id) {

        User teacher = methodHelper.isUserExist(id);
        methodHelper.checkRole(teacher, RoleType.TEACHER);
        methodHelper.checkAdvisor(teacher);

        teacher.setIsAdvisor(Boolean.FALSE);
        userRepository.save(teacher);

        //!!! silinen rehber ogretmenin ogrencileri carsa bu ilsikiyi koparmamiz gerekiyor
        List<User> allStudents = userRepository.findByAdvisorTeacherId(id);
        if(!allStudents.isEmpty()){
            allStudents.forEach(student -> student.setAdvisorTeacherId(null));
        }

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.ADVISOR_TEACHER_DELETE)
                .object(userMapper.mapUserToUserResponse(teacher))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public List<UserResponse> getAllAdvisorTeacher() {

        return userRepository.findAllByAdvisor(Boolean.TRUE)
                .stream()
                .map(userMapper::mapUserToUserResponse)
                .collect(Collectors.toList());
    }
}