package com.project.entity.concretes.business;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.entity.concretes.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Meet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "HH:mm",timezone = "US")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "HH:mm",timezone = "US")
    private LocalTime stopTime;

    /*
    Bu classın sadece userlar ile alakası var. Bir rehber öğretmenin birden fazla rehberlik toplantısı olabilir.
     */

    @ManyToOne(cascade = CascadeType.PERSIST)
    private User advisoryTeacher;

    /*
    Bir öğrenci birden fazla rehberlik toplantısına katılabildiği gibi bir toplantıya birden fazla öğrenci katılabilir.
     Bu ksımda özellikle customize edilir çünkü eger student_id diye belirtmezsek user_id olarak alır bu da güzel olmaz */
    @ManyToMany
    @JoinTable(
            name = "meet_student_table",
            joinColumns = @JoinColumn(name = "meet_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<User> studentList;
}