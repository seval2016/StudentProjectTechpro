package com.project.entity.concretes.business;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.entity.enums.Term;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Entity

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class EducationTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Education Term must not be empty")
    private Term term; // yaz - guz - YAZ

    @NotNull(message = "Start Date must not be empty")
    @Column(name = "start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End Date must not be empty")
    @Column(name = "end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "Last Registration Date must not be empty")
    @Column(name = "last_registration_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate lastRegistrationDate;

    /*
    Bir educationTerm kaydediliyorsa lessonProgram'larda kaydedilsin eğer siliniyorsa lessonProgram'lar da silinsin demek istiyorsak CascadeType.ALL kullanırız.
     */
    @OneToMany(mappedBy = "educationTerm", cascade = CascadeType.ALL)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) //Bu kısma jsonignore da diyebiliriz. Bu kısımda response tarafına yazılmasını engeller ve bu durum sonsun döngü hatasını almamamızı sağlar.
    private List<LessonProgram> lessonProgram;


}