package edu.ucmerced.chealth.datasource.health.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Entity
@Data
@JsonIgnoreProperties
@Table(name = "health_data")
public class HealthTotalData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    
    private String disease;
    
   
    private String region;
    
    private String county;
    

    private String ethnicity;
    
    private String sex;
    
    private Integer age;
    
    private long cases;
    @Column(precision=19, scale=2)
    
    private Double costs;
    
    private Long population;
    
    private Float prevalenceRate;
    
    private Float averageUtility;
    
    private Double averageHealthyUtility;
    
    private Double costPerCase;
    
    private Double totalHCCost; 
    
    private Double utilityLoss;
    
    private Double totalCostOfUtility;
    
    private Double TotalTotalCost;
  
}
