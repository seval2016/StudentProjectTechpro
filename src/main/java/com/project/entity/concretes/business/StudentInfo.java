package com.project.entity.concretes.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.entity.concretes.user.User;
import com.project.entity.enums.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StudentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer absentee; // yoklama

    private Double midtermExam;

    private Double finalExam;

    private Double examAverage;

    private String infoNote;

    @Enumerated(EnumType.STRING)
    private Note letterGrade; // 100 --> AA ,30 --> FF

    /*
    Soru 1 : Student info bilgisini ilk etapta kullanacak roller hangisidir ? Teacher,Student
        O zaman öğretmen ve student'ı buraya ilişkilendirlecek. Ben öğrenci Seval benım matematik student infom olacak
        java student infom olacak vs vs. Benim tarafımdan bakarsam student info yu many görüyorum. Student ınd-fo tarafına
        geçersem oradaki matematik yada java student infosu sadece bana ait.
     */

    @ManyToOne //1 öğretmenin 1 den fazla student info ya atanabilir
    @JsonIgnore
    private User teacher;

    @ManyToOne // 1 öğrenci aldığı ders sayısı kadar student infosu vardır
    @JsonIgnore //user da da studentinfo nesnesi oluşturulabildiği için sonsuz döngüye girmesin diye jsonignore yazarız
    private User student;

    /*
    StudentInfonun Lesson ile ManyToOne ilişkisi var -> Bir lessonun birden fazla studentinfosu olabilir.
    Şöyleki matematik dersi düşünün matematik dersine giren 100 tane öğrenci varsa 100 tane studentInfo olur.
    Not: Lesson tarafına studentInfo eklemediğimiz için ve ilişkiyi sadece buradan setlediğimiz için jsonignore kullanmadık.
     */
    @ManyToOne
    private Lesson lesson;

    /*
    Tek taraflı ilişkilerde jsonignore falan gerek yok
     */
    @OneToOne
    private EducationTerm educationTerm;

}