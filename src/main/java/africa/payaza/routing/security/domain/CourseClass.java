package africa.payaza.routing.security.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "class")
public class CourseClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
