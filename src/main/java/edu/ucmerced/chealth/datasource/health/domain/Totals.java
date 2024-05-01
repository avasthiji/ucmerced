package edu.ucmerced.chealth.datasource.health.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
public class Totals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "AGE_GROUP_ID")
    private AgeGroup ageGroup;
    @Column(precision=19, scale=2)
    private Double cases;
    @Column(precision=19, scale=2)
    private Double costs;
    @ManyToOne
    @JoinColumn(name = "county")
    private County county;
    @ManyToOne
    @JoinColumn(name = "DISEASE_ID")
    private Disease disease;
    @ManyToOne
    @JoinColumn(name = "ETHNICITY_ID")
    private Ethnicity ethnicity;
    private Long population;
    private Float prevalenceRate;
    private String sex;
    @ManyToOne
    @JoinColumn(name = "REGION_ID")
    private Region region;

    public String getCountyName() {
        return county.getCounty();
    }

    public String getDiseaseName() {
        return disease.getDisease();
    }

    public Long getRegionId() {
        return region.getId();
    }

    public String getRegionName() {
        return region.getRegion();
    }
}
