package com.project.service.user;

import com.project.entity.concretes.user.User;
import com.project.entity.enums.RoleType;
import com.project.exception.ResourceNotFoundException;
import com.project.payload.mappers.UserMapper;
import com.project.payload.messages.ErrorMessages;
import com.project.payload.messages.SuccessMessages;

import com.project.payload.request.user.UserRequest;
import com.project.payload.response.abstracts.BaseUserResponse;
import com.project.payload.response.business.ResponseMessage;
import com.project.payload.response.user.UserResponse;
import com.project.repository.UserRepository;

import com.project.service.helper.PageableHelper;

import com.project.service.validator.UniquePropertyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UniquePropertyValidator uniquePropertyValidator;
    private final UserMapper userMapper;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final PageableHelper pageableHelper;

    public ResponseMessage<UserResponse> saveUser(UserRequest userRequest, String userRole) {

        //!!! Kaydederken neleri kontrol etmeliyim ?
        // username(aynı username kullanan başka kullanıcı olmamalı),
        // email (bir email bir kullanıcıya aittir)
        // phoneNumber(bir phoneNumber bir kullanıcıya aittir)
        // ssn (bir ssn bir kullanıcıya aittir)
        //Not: Bu kontrolleri nasıl yapacağız ?  DB'ye gidilip herbir değişkeni varmı yokmu diye kontrol edeceğim. Normalde exists metodlarını tetikleyerek kontrol etmeliyim. Bunun için satırlarca
        uniquePropertyValidator.checkDuplicate(userRequest.getUsername(), userRequest.getSsn(),
                userRequest.getPhoneNumber(),userRequest.getEmail());
        //!!! DTO --> POJO
        User user = userMapper.mapUserRequestToUser(userRequest);
        //!!! POJO üzerinde Rol bilgisini setliyoruz
        if(userRole.equalsIgnoreCase(RoleType.ADMIN.name())){

            if(Objects.equals(userRequest.getUsername(),"Admin")){
                user.setBuilt_in(true);
            }
            //!!! admin rolu veriliyor
            user.setUserRole(userRoleService.getUserRole(RoleType.ADMIN));
        } else if (userRole.equalsIgnoreCase("Dean")) {
            user.setUserRole(userRoleService.getUserRole(RoleType.MANAGER));
        } else if (userRole.equalsIgnoreCase("ViceDean")) {
            user.setUserRole(userRoleService.getUserRole(RoleType.ASSISTANT_MANAGER));
        } else {
            throw new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_USER_USERROLE_MESSAGE, userRole));
        }
        //!!! password encode
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //!!! isAdvisor degerini False yapiyoruz
        user.setIsAdvisor(Boolean.FALSE);
        User savedUser = userRepository.save(user);

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.USER_CREATED)
                .object(userMapper.mapUserToUserResponse(savedUser))
                .build();
    }

    public Page<UserResponse> getUsersByPage(int page, int size, String sort, String type, String userRole) {
        Pageable pageable=  pageableHelper.getPageableWithProperties(page, size, sort, type);

        return userRepository.findByUserByRole(userRole, pageable)
                .map(userMapper::mapUserToUserResponse);
    }

    public ResponseMessage<BaseUserResponse> getUserById(Long userId) {

        BaseUserResponse baseUserResponse = null;

        User user = userRepository.findById(userId).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_USER_MESSAGE, userId)));

        if(user.getUserRole().getRoleType() == RoleType.STUDENT){
            baseUserResponse = userMapper.mapUserToStudentResponse(user);
        } else if (user.getUserRole().getRoleType() == RoleType.TEACHER) {
            baseUserResponse = userMapper.mapUserToTeacherResponse(user);
        } else {
            baseUserResponse = userMapper.mapUserToUserResponse(user);
        }

        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_FOUND)
                .httpStatus(HttpStatus.OK)
                .object(baseUserResponse)
                .build();
    }
}