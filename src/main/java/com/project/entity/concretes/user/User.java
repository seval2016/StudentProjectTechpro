package com.project.entity.concretes.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.entity.concretes.business.LessonProgram;
import com.project.entity.concretes.business.Meet;
import com.project.entity.concretes.business.StudentInfo;
import com.project.entity.enums.Gender;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "t_user")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String ssn;

    private String name;

    private String surname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDay;

    private String birthPlace;

    /*
    //JSON verisinden nesneye donusturulurken
    //kullanilsin, tersi olmasin, Örneğin, bir kullanıcı nesnesi bir REST API üzerinden istemciye
    //gönderildiğinde, şifre alanı JSON içinde yer almayacak ve bu sayede şifre korunmuş olacaktır.
    //Ancak, bir kullanıcı yeni bir hesap oluştururken veya şifresini güncellerken, bu değer JSON
    //içinde gönderilebilir ve Java nesnesine dönüştürülebilir.

    // my note: Json verisinden nesneye dönüşüm yapıldığı zaman yani client'dan veri geldiği zaman elbette password verisi olacak fakat tam tersi olursa yani kullanıcı nesnesini bir restapi üzerinden client tarafına gönderildiğinde şifre alanı json içerisinde yer almamalı ki  bu sayede şifre korunmuş olsun. Bunun için @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) kodunu yazıyoruz.
    */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    /*
    my note: Sistemde müdahale edilemeyen veri demektir. Yani sistemde üç tane admin olduğunu düşünürsek admin diğer iki admini sildikten sonra kendini de silerse bu durumda sisteme müdahale eddebilecek kimse kalmayacak ki böyle bir durumda sistem değiştirilemez olur. Bu bir yazılım hatasıdır. Bu yüzden bazı user'lara built in özelliği vereceğiz ki bu tarz bir hata ile karşılaşmayalım.

    */
    private Boolean built_in;

    private String motherName;

    private String fatherName;

    private int studentNumber;

    private boolean isActive;

    private Boolean isAdvisor;

    private Long advisorTeacherId;// bu Ogrenciler icin lazim, kendi rehber ogretmeninin
    // id si buraya yazilacak

    @Enumerated(EnumType.STRING)
    private Gender gender; // Erkek , erkek , Erkek , Bay , bay , BAY , ERKEK

   /*
    // UserRole -> Herbir kullanıcının bir user rolü olmalıdır.
    // StudentInfo   - > Bu öğrenci ise StudentInfo bilgileri olmalı
    // LessonProgram - > Student yada Teacher ise Lesson program bilgileri olmalı
    // Sistem designında hiçbir user lesson ile bağlantısı yok herşey lesson program üzerinden design edilmiş.
    // Meet - > Eğer user rehber öğretmen de olsa student da olsa  meet ile ilişkisi olması lazım. Benim rehber toplantılarımı getir dediğinde ileriki tarihlerde olan toplantılarını da görmesi gerekli

    */

    /*
    mappedBy = "teacher" da student de diyebiliriz sorun olmaz. Teachers yada student isimli değişken neredeyse setlemeler oradan yapılsın.
    CascadeType.REMOVE -> Mesela bir öğrenciyi silersek bu öğrencinin studentInfo'larının da silinmesi gerekli.Bu işlem bu kod ile silinebilir. Yani o user silinirken o user'a ait infolar da silinir.
     */
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.REMOVE)
    private List<StudentInfo> studentInfos;

    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "user_lessonprogram",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_program_id")
    )
    private Set<LessonProgram> lessonsProgramList;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "meet_student_table",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "meet_id")
    )
    private List<Meet> meetList;

    @OneToOne 
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UserRole userRole;
}