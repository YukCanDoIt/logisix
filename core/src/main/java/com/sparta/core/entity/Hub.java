package com.sparta.core.entity;

import com.sparta.core.dto.HubRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "p_hubs")
@Getter
@NoArgsConstructor
public class Hub extends Base {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hubId;

  @Column(name = "hub_name", nullable = false, length = 100)
  private String hubName;

  @Column(name = "latitude", nullable = false, scale = 8)
  private BigDecimal latitude;

  @Column(name = "longitude", nullable = false, scale = 8)
  private BigDecimal longitude;

  @Column(name = "address", nullable = false, length = 100)
  private String address;

  @Column(name = "location", nullable = false)
  private Point location;

  @Column(name = "hub_manager_id")
  private Long hubManagerId;

  public Hub(HubRequest hubRequest, Point location) {
    this.hubName = hubRequest.hubName();
    this.latitude = hubRequest.latitude();
    this.longitude = hubRequest.longitude();
    this.address = hubRequest.address();
    this.location = location;
    this.createBase("tempUser");
  }

  public void update(Hub hub) {
    this.hubName = hub.getHubName();
    this.address = hub.getAddress();
    this.latitude = hub.getLatitude();
    this.longitude = hub.getLongitude();
    this.location = hub.getLocation();
  }
}
