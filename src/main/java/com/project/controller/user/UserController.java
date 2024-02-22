package com.project.controller.user;

import com.project.payload.request.user.UserRequest;
import com.project.payload.response.abstracts.BaseUserResponse;
import com.project.payload.response.business.ResponseMessage;
import com.project.payload.response.user.UserResponse;
import com.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //!!! Save --> bu saveUser methodu teacher ve student disindakiler icin yani admin,müdür ve müdür yardımcısı için tetiklenecek. Çünkü karşılayacağımız request çeşidi bu userlarda değişiyor.
    @PostMapping("/save/{userRole}") // http://localhost:8080/user/save/Admin + POST + JSON
    @PreAuthorize("hasAnyAuthority('ADMIN')")//sadece admin tetikliyorsa neden hasAnyAuthority yazdık ? Bunun sebebi 1-değişmesi kolay olsun , 2- başka bir senaryoda başka user da ekleyebilelim diye hasAnyAuthority yazarız. Esnek olması için diyebiliriz.
    public ResponseEntity<ResponseMessage<UserResponse>> saveUser(@Valid @RequestBody UserRequest userRequest, @PathVariable String userRole){ //ResponseEntity<ResponseMessage<UserResponse>>iki generik tipi iç içe kullanabilmeyi göstermek için bu şekilde yazıldı.
        return ResponseEntity.ok(userService.saveUser(userRequest,userRole));
    }

    //!!! getall --> Admin,Dean,ViceDean
    @GetMapping("/getAllUserByPage/{userRole}") // http://localhost:8080/user/getAllUserByPage/Admin
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUserByPage(
            @PathVariable String userRole,
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "10") int size,
            @RequestParam(value = "sort",defaultValue = "name") String sort,
            @RequestParam(value = "type",defaultValue = "desc") String type
    ){
        Page<UserResponse> adminsOrDeansOrViceDeans = userService.getUsersByPage(page,size,sort,type,userRole);
        return new ResponseEntity<>(adminsOrDeansOrViceDeans, HttpStatus.OK);
    }

    @GetMapping("/getUserById/{userId}") // http://localhost:8080/user/getUserById/1 + GET
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    public ResponseMessage<BaseUserResponse> getUserById(@PathVariable Long userId){
        return userService.getUserById(userId);
    }

}