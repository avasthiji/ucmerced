package edu.ucmerced.chealth.datasource.health.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@Table(name = "disease")
public class Disease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonProperty(value = "diseaseName")
    @Column(name = "disease")
    private String disease;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    private DiseaseCategory category;
}
