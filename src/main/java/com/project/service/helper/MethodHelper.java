package com.project.service.helper;

import com.project.entity.concretes.user.User;
import com.project.entity.enums.RoleType;
import com.project.exception.BadRequestException;
import com.project.exception.ResourceNotFoundException;
import com.project.payload.request.business.messages.ErrorMessages;
import com.project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MethodHelper {

    private final UserRepository userRepository;

    //!!! isUserExist
    public User isUserExist(Long userId){
        return userRepository.findById(userId).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_USER_MESSAGE, userId)));
    }

    //!!! builtIn kontrolu
    public void checkBuiltIn(User user){
        if(Boolean.TRUE.equals(user.getBuilt_in())) {
            throw new BadRequestException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
        }
    }

    //!!! Rol kontrolu yapan metod
    public void checkRole(User user, RoleType roleType){
        if(!user.getUserRole().getRoleType().equals(roleType)){
            throw new ResourceNotFoundException(
                    String.format(ErrorMessages.NOT_FOUND_USER_WITH_ROLE_MESSAGE, user.getId(), roleType));
        }
    }

    //!!! username ile kontrol
    public User isUserExistByUsername(String username){
        User user = userRepository.findByUsernameEquals(username);

        if(user.getId() == null){
            throw new ResourceNotFoundException(ErrorMessages.NOT_FOUND_USER_MESSAGE);
        }

        return user;
    }

    //!!! Gelen User , Advisor mi kontrolu
    public void checkAdvisor(User user){
        if(Boolean.FALSE.equals(user.getIsAdvisor())){
            throw new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_ADVISOR_MESSAGE, user.getId()));
        }
    }
}