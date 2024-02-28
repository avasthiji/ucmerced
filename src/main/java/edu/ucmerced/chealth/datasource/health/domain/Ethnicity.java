package edu.ucmerced.chealth.datasource.health.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Getter
@Setter
@ToString
@Table(name = "ethnicity")
public class Ethnicity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonProperty(value = "ethnicityName")
    @Column(name = "ethnicity")
    private String ethnicity;
}
