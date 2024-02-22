package com.project.entity.concretes.business;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.entity.concretes.user.User;
import com.project.entity.enums.Day;
import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Set;

@Entity

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class LessonProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Day day;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "US")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "US")
    private LocalTime stopTime;

    @ManyToMany
    @JoinTable(
            name = "lesson_program_lesson",
            joinColumns = @JoinColumn(name = "lessonprogram_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_id")
    )
    private Set<Lesson> lessons;

    /*
    //CascadeType.PERSIST özelliği ile işaretlenmişse, bir LessonProgram oluşturulduğunda bu ders programı ile ilişkilendirilmiş olan eğitim dönemi nesnesi de otomatik olarak persist edilecektir.

    my note: içinde bulunduğum lesson program kaydedilirken educationTerm bilgisi de kaydedilsin istiyorsam CascadeType.PERSIST kısmını tetikliyoruz. Persist kalıcı hale getiriyor.
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    private EducationTerm educationTerm;

    /*
     - ilişki user tarafta yani lessonsProgramList field'ının bulunduğu yerde setleniyor.
     - FetchType.EAGER ile LessonProgram nesnesi oluşturulduğunda otomatik olarak getirilir (eager loading). Bu, ilgili LessonProgram nesnesinin yüklenmesi sırasında ilişkili kullanıcıları (users) veritabanından çekilir ve bu kullanıcılar users alanına otomatik olarak atanır.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToMany(mappedBy = "lessonsProgramList", fetch = FetchType.EAGER)
    private Set<User> users;

    /*
    Servis katındayız ve bir tane lessonprogram silmek istiyoruz. varsa diye bir kontrol yapıyorum eğer true dönerse silerim. Peki burada kontrol etmem gerekn birşey var mı ? Silmeden önce kontrol etmem gereken userslar var. Lessonprogramın herhangi bir instancesi silinecekse önce @PreRemove methodu tetikle sonra delete methodunu çalıştır. Bu method lesson programı silersem bu lesson programı alan tüm kullanıcıların lesson programlarını sil.
     */
    @PreRemove
    private void removeLessonProgramFromUser(){
        users.forEach(user -> user.getLessonsProgramList().remove(this));
    }
}