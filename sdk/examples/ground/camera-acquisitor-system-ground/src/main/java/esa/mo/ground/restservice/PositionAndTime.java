/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mo.ground.restservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

/**
 *
 * @author Kevin Otto <Kevin@KevinOtto.de>
 */
@JsonIgnoreProperties(value = {"orekitDate", "location"})
public class PositionAndTime
{

  public final AbsoluteDate orekitDate;
  public final String date;

  public final GeodeticPoint location;
  public final double latitude;
  public final double longitude;
  public final double altitude;

  public PositionAndTime(AbsoluteDate orekitDate, GeodeticPoint location)
  {
    this.orekitDate = orekitDate;
    this.date = orekitDate.toString();

    this.location = location;
    latitude = location.getLatitude();
    longitude = location.getLongitude();
    altitude = location.getLongitude();
  }

  public AbsoluteDate getOrekitDate()
  {
    return orekitDate;
  }

  public String getDate()
  {
    return date;
  }

  public GeodeticPoint getLocation()
  {
    return location;
  }

  public double getLatitude()
  {
    return latitude;
  }

  public double getLongitude()
  {
    return longitude;
  }

  public double getAltitude()
  {
    return altitude;
  }

}
