package edu.ucmerced.chealth.datasource.health.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Getter
@Setter
@ToString
@Table(name = "county")
public class County {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonProperty(value = "countyName")
    @Column(name = "county")
    private String county;
    
    @ManyToOne
    @JoinColumn(name = "REGION_ID")
    private Region region;
}
