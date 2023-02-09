package edu.ucmerced.chealth.datasource.health.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
public class County {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String countyName;
    @ManyToOne
    @JoinColumn(name = "REGION_ID")
    private Region region;
}
