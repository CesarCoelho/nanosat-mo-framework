/* ----------------------------------------------------------------------------
 * Copyright (C) 2015      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : ESA NanoSat MO Framework
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ----------------------------------------------------------------------------
 */
package esa.mo.nmf.apps;

import esa.mo.nmf.MCRegistration;
import esa.mo.nmf.NMFException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.provider.MALInteraction;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mc.structures.AttributeValueList;
import org.ccsds.moims.mo.platform.gps.body.GetLastKnownPositionResponse;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

/**
 * Class handling communication with the navigation system (GPS)
 *
 * @author Kevin Otto <Kevin@KevinOtto.de>
 */
public class CameraAcquisitorSystemGPSHandler
{

  private static final Logger LOGGER = Logger.getLogger(
      CameraAcquisitorSystemGPSHandler.class.getName());

  private CameraAcquisitorSystemMCAdapter casMCAdapter;

  public CameraAcquisitorSystemGPSHandler(CameraAcquisitorSystemMCAdapter casMCAdapter)
  {
    this.casMCAdapter = casMCAdapter;
  }

  static void registerActions(MCRegistration registration)
  {

  }

  public UInteger actionArrived(Identifier name, AttributeValueList attributeValues,
      Long actionInstanceObjId, boolean reportProgress, MALInteraction interaction)
  {
    return new UInteger(0); // error code 0 - unknown error
  }

  /**
   * gets the current geographical position of the Satellite (longitude, latitude, altitude)
   *
   * @return
   */
  public GeodeticPoint getCurrentPosition()
  {
    GetLastKnownPositionResponse pos;
    try {
      pos = casMCAdapter.getConnector().getPlatformServices().getGPSService().getLastKnownPosition();

      //Vector3D velocity = new Vector3D(0, 0, 0); TODO
      //get geographical position
      float latitude = pos.getBodyElement0().getLatitude();
      float longitude = pos.getBodyElement0().getLongitude();
      float altitude = pos.getBodyElement0().getAltitude();
      return new GeodeticPoint(latitude, longitude, altitude);

    } catch (NMFException ex) {
      Logger.getLogger(CameraAcquisitorSystemGPSHandler.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(CameraAcquisitorSystemGPSHandler.class.getName()).log(Level.SEVERE, null, ex);
    } catch (MALInteractionException ex) {
      Logger.getLogger(CameraAcquisitorSystemGPSHandler.class.getName()).log(Level.SEVERE, null, ex);
    } catch (MALException ex) {
      Logger.getLogger(CameraAcquisitorSystemGPSHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public Orbit getCurrentOrbit()
  {
    GeodeticPoint geoPosition = getCurrentPosition();
    Vector3D startPos = this.casMCAdapter.earth.transform(geoPosition);

    //TODO calculate velocity
    //best way?
    PVCoordinates pvCoordinates = new PVCoordinates(startPos, startPos); //return new CartesianOrbit(pvCoordinates, frame, AbsoluteDate.GPS_EPOCH, altitude)
    return new CartesianOrbit(pvCoordinates, this.casMCAdapter.earthFrame,
        CameraAcquisitorSystemMCAdapter.getNow(),
        Constants.EIGEN5C_EARTH_MU);
  }

}
