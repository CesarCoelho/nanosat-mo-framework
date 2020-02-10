/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mo.ground.restservice;

/**
 *
 * @author Kevin Otto <Kevin@KevinOtto.de>
 */
public class GroundTrack
{

  private final long id;
  private final PositionAndTime[] trackPoints;

  public GroundTrack(long id, PositionAndTime[] track)
  {
    this.id = id;
    this.trackPoints = track;
  }

  public long getId()
  {
    return id;
  }

  public PositionAndTime[] getTrack()
  {
    return trackPoints;
  }

}
