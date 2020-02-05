/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mo.ground.cameraacquisotorground;

import esa.mo.nmf.sdk.OrekitResources;
import java.util.LinkedList;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataProvidersManager;
import org.orekit.frames.FactoryManagedFrame;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

/**
 *
 * @author Kevin Otto <Kevin@KevinOtto.de>
 */
public class OrbitHandler
{

  private final FactoryManagedFrame earthFrame;
  private final OneAxisEllipsoid earth;
  private TLEPropagator propagator;
  private TLE initialTLE;

  public OrbitHandler(TLE tle)
  {

    earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);//FramesFactory.getEME2000();
    earth = new OneAxisEllipsoid(
        Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
        Constants.WGS84_EARTH_FLATTENING, earthFrame);

    initialTLE = tle;
    propagator = TLEPropagator.selectExtrapolator(tle);
  }

  public void updateTLE(TLE tle)
  {
    initialTLE = tle;
    propagator = TLEPropagator.selectExtrapolator(tle);
  }

  public void reset()
  {
    propagator = TLEPropagator.selectExtrapolator(initialTLE);
  }

  public class PositionAndTime
  {

    public final AbsoluteDate date;
    public final GeodeticPoint location;

    public PositionAndTime(AbsoluteDate date, GeodeticPoint location)
    {
      this.date = date;
      this.location = location;
    }

  }

  public PositionAndTime[] getPositionSeries(AbsoluteDate startDate, AbsoluteDate endDate,
      double timeStepSeconds)
  {

    LinkedList<PositionAndTime> positionSeries = new LinkedList<>();

    // get position at every timestep
    for (AbsoluteDate currentDate = startDate;
        currentDate.compareTo(endDate) < 0;
        currentDate = currentDate.shiftedBy(timeStepSeconds)) {

      SpacecraftState currentState = this.propagator.propagate(startDate, currentDate);

      GeodeticPoint curreLocation = earth.transform(
          currentState.getPVCoordinates(earthFrame).getPosition(), earthFrame, currentDate);
      positionSeries.add(new PositionAndTime(currentDate, curreLocation));
    }
    return positionSeries.toArray(new PositionAndTime[positionSeries.size()]);
  }

  public GeodeticPoint getPosition(AbsoluteDate endDate)
  {
    System.out.println(propagator.getInitialState().getDate());
    SpacecraftState finalState = propagator.propagate(endDate);
    System.out.println(propagator.getInitialState().getDate());
    return earth.transform(
        finalState.getPVCoordinates(earthFrame).getPosition(), earthFrame, finalState.getDate());
  }

}
