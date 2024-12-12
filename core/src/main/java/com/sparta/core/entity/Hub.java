package com.sparta.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

  @Column(name = "latitude", nullable = false)
  private Long latitude;

  @Column(name = "longitude", nullable = false)
  private Long longitude;

  @Column(name = "address", nullable = false, length = 100)
  private String address;

  @Column(name = "location", nullable = false)
  private Point location;

  @Column(name = "hub_manager_id")
  private Long hubManagerId;


  public Hub(String hubName, String address, Long latitude, Long longitude, Point location) {
    this.hubName = hubName;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
    this.location = location;
  }

  public void update(Hub hub) {
    this.hubName = hub.getHubName();
    this.address = hub.getAddress();
    this.latitude = hub.getLatitude();
    this.longitude = hub.getLongitude();
    this.location = hub.getLocation();
    this.hubManagerId = hub.getHubManagerId();
  }
}
