package com.project.service.user;

import com.project.entity.concretes.user.UserRole;
import com.project.entity.enums.RoleType;
import com.project.exception.ResourceNotFoundException;
import com.project.payload.messages.ErrorMessages;
import com.project.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    /*
    Bir servis katında dto değil de pojo class döndürülüyorsa bunun 3 sebebi vardır.
        1- İçerisinde güvenlik sorunu oluşturacak bir veri yoksa ve servis clas'ı sadece basit veri nesnelerini döndürüyorsa, DTO kullanmak yerine POJO kullanmak kodu daha basit ve performans açısından daha hafif hale getirebilir.
        2- Bu method başka servisler tarafından çağrılıyor olabilir. (Aşağıdaki metodda olduğu gibi!)
        3- Kötü koddur.
     */

    public UserRole getUserRole(RoleType roleType){
        return userRoleRepository.findByEnumRoleEquals(roleType).orElseThrow(()->
                new ResourceNotFoundException(ErrorMessages.ROLE_NOT_FOUND));
    }
}