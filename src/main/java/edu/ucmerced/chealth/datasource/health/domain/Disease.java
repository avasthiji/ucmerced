package edu.ucmerced.chealth.datasource.health.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
public class Disease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String diseaseName;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    private DiseaseCategory category;
}
