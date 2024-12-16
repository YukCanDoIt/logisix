package com.sparta.core.entity;

import com.sparta.core.dto.CompanyRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "p_companies")
@Getter
@NoArgsConstructor
public class Company extends Base {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID companyId;

  @Column(name = "company_name", nullable = false, length = 100)
  private String companyName;

  @Column(name = "company_type", nullable = false)
  @Enumerated(value = EnumType.STRING)
  private CompanyTypeEnum type;

  @Column(name = "company_manager_id")
  private Long companyManagerId;

  @Column(name = "hub_id")
  private UUID hubId;

  @Column(name = "address", nullable = false, length = 100)
  private String address;

  @Column(name = "latitude", nullable = false, scale = 8)
  private BigDecimal latitude;

  @Column(name = "longitude", nullable = false, scale = 8)
  private BigDecimal longitude;

  @Column(name = "location", nullable = false)
  private Point location;

  public Company(CompanyRequest companyRequest, Point location) {
    this.companyName = companyRequest.companyName();
    this.address = companyRequest.address();
    this.latitude = companyRequest.latitude();
    this.longitude = companyRequest.longitude();
    this.hubId = companyRequest.hubId();
    this.type = companyRequest.companyType();
    this.location = location;
    this.createBase("tempUser");
  }

  public void update(Company company) {
    this.companyName = company.getCompanyName();
    this.address = company.getAddress();
    this.latitude = company.getLatitude();
    this.longitude = company.getLongitude();
    this.type = company.getType();
    this.location = company.getLocation();
    this.companyManagerId = company.getCompanyManagerId();
    this.hubId = company.getHubId();
  }
}
