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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.provider.MALInteraction;
import org.ccsds.moims.mo.mal.structures.Duration;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mal.structures.UOctet;
import org.ccsds.moims.mo.mal.structures.UShort;
import org.ccsds.moims.mo.mc.action.structures.ActionDefinitionDetails;
import org.ccsds.moims.mo.mc.action.structures.ActionDefinitionDetailsList;
import org.ccsds.moims.mo.mc.structures.ArgumentDefinitionDetailsList;
import org.ccsds.moims.mo.mc.structures.AttributeValueList;
import org.ccsds.moims.mo.platform.camera.consumer.CameraAdapter;
import org.ccsds.moims.mo.platform.camera.structures.CameraSettings;
import org.ccsds.moims.mo.platform.camera.structures.PictureFormat;
import org.ccsds.moims.mo.platform.camera.structures.PixelResolution;
import org.orekit.bodies.GeodeticPoint;

/**
 * Class for handling taking and saving of Photographs and everything else camera related
 *
 * @author Kevin Otto <Kevin@KevinOtto.de>
 */
public class CameraAcquisitorSystemCameraHandler
{

  private static final Logger LOGGER = Logger.getLogger(
      CameraAcquisitorSystemCameraHandler.class.getName());

  public static final String ACTION_PHOTOGRAPH_NOW = "photographNow";
  public static final int PHOTOGRAPH_NOW_STAGES = 3;
  private final CameraAcquisitorSystemMCAdapter casMCAdapter;

  // Camera settings:
  private final int defaultPictureWidth = 2048;
  private final int defaultPictureHeight = 1944;
  public final PixelResolution defaultCameraResolution;
  private final Duration DEFAULT_CAMERA_EXPOSURE_TIME = new Duration(1.1);
  private final float GAIN_R = 1.0f;
  private final float GAIN_G = 1.0f;
  private final float GAIN_B = 1.0f;

  public CameraAcquisitorSystemCameraHandler(CameraAcquisitorSystemMCAdapter casMCAdapter)
  {
    this.casMCAdapter = casMCAdapter;
    this.defaultCameraResolution =
        new PixelResolution(new UInteger(defaultPictureWidth), new UInteger(
            defaultPictureHeight));
  }

  /**
   * Registers all Camera Actions
   *
   * @param registration
   */
  static void registerActions(MCRegistration registration)
  {
    ActionDefinitionDetailsList actionDefs = new ActionDefinitionDetailsList();
    IdentifierList actionNames = new IdentifierList();

    ActionDefinitionDetails actionDefTakePhotograpNow = new ActionDefinitionDetails(
        "takes a photograph imidietly",
        new UOctet((short) 0), new UShort(PHOTOGRAPH_NOW_STAGES),
        new ArgumentDefinitionDetailsList());

    actionDefs.add(actionDefTakePhotograpNow);
    actionNames.add(new Identifier(ACTION_PHOTOGRAPH_NOW));
    registration.registerActions(actionNames, actionDefs);
  }

  UInteger photographNow(AttributeValueList attributeValues, Long actionInstanceObjId,
      boolean reportProgress, MALInteraction interaction)
  {
    try {
      takePhotograph(actionInstanceObjId, 0, PHOTOGRAPH_NOW_STAGES, "_INSTANT");
    } catch (MALInteractionException | MALException | IOException | NMFException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return new UInteger(0);
    }
    return new UInteger(1);
  }

  /**
   * Inner Class for handling camera events
   */
  private class CameraDataHandler extends CameraAdapter
  {

    private Long actionInstanceObjId;
    private final int stageOffset;
    private final int totalStage;

    private final int STAGE_ACK = 1;
    private final int STAGE_RECIVED = 2;
    private final int STAGE_FIN = 3;

    private final CameraAcquisitorSystemMCAdapter casMCAdapter;
    private final String fileName;

    CameraDataHandler(Long actionInstanceObjId, int stageOffset, int totalStages, String fileName,
        CameraAcquisitorSystemMCAdapter casMCAdapter)
    {
      this.actionInstanceObjId = actionInstanceObjId;
      this.stageOffset = stageOffset;
      this.totalStage = totalStages + PHOTOGRAPH_NOW_STAGES;
      this.fileName = fileName;
      this.casMCAdapter = casMCAdapter;
    }

    @Override
    public void takePictureAckReceived(
        org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader,
        java.util.Map qosProperties)
    {
      try {
        this.casMCAdapter.getConnector().reportActionExecutionProgress(true, 0,
            STAGE_ACK + stageOffset,
            this.totalStage, this.actionInstanceObjId);
      } catch (NMFException ex) {
        Logger.getLogger(CameraAcquisitorSystemCameraHandler.class.getName()).log(Level.SEVERE, null,
            ex);
      }
    }

    @Override
    public void takePictureResponseReceived(
        org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader,
        org.ccsds.moims.mo.platform.camera.structures.Picture picture,
        java.util.Map qosProperties)
    {
      try {
        this.casMCAdapter.getConnector().reportActionExecutionProgress(true, 0,
            STAGE_RECIVED + this.stageOffset,
            this.totalStage, this.actionInstanceObjId);
      } catch (NMFException ex) {
        Logger.getLogger(CameraAcquisitorSystemCameraHandler.class.getName()).log(Level.SEVERE, null,
            ex);
      }

      final String folder = "photos";
      File dir = new File(folder);
      dir.mkdirs();
      Date date = new Date(System.currentTimeMillis());
      Format format = new SimpleDateFormat("yyyyMMdd_HHmmss_");
      final String timeNow = format.format(date);
      GeodeticPoint position = this.casMCAdapter.getGpsHandler().getCurrentPosition();

      String posString = String.valueOf(position.getLatitude()) + "-" + String.valueOf(
          position.getLongitude());
      final String filenamePrefix =
          folder + File.separator + timeNow + "_" + posString + "_" + this.fileName;
      try {
        // Store it in a file!
        if (picture.getSettings().getFormat().equals(PictureFormat.RAW)) {
          FileOutputStream fos = new FileOutputStream(filenamePrefix + ".raw");
          fos.write(picture.getContent().getValue());
          fos.flush();
          fos.close();
        } else if (picture.getSettings().getFormat().equals(PictureFormat.PNG)) {
          FileOutputStream fos = new FileOutputStream(filenamePrefix + ".png");
          fos.write(picture.getContent().getValue());
          fos.flush();
          fos.close();
        } else if (picture.getSettings().getFormat().equals(PictureFormat.BMP)) {
          FileOutputStream fos = new FileOutputStream(filenamePrefix + ".bmp");
          fos.write(picture.getContent().getValue());
          fos.flush();
          fos.close();
        } else if (picture.getSettings().getFormat().equals(PictureFormat.JPG)) {
          FileOutputStream fos = new FileOutputStream(filenamePrefix + ".jpg");
          fos.write(picture.getContent().getValue());
          fos.flush();
          fos.close();
        }
        LOGGER.log(Level.INFO, "Photograph was taken at {0}", posString);
      } catch (IOException | MALException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
      }
      try {
        this.casMCAdapter.getConnector().reportActionExecutionProgress(true, 0,
            STAGE_FIN + this.stageOffset,
            this.totalStage, this.actionInstanceObjId);
      } catch (NMFException ex) {
        LOGGER.log(Level.SEVERE,
            "The action progress could not be reported!", ex);
      }
    }

    @Override
    public void takePictureAckErrorReceived(
        org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader,
        org.ccsds.moims.mo.mal.MALStandardError error,
        java.util.Map qosProperties)
    {
      try {
        this.casMCAdapter.getConnector().reportActionExecutionProgress(false, 1, STAGE_ACK,
            this.totalStage, this.actionInstanceObjId);
        LOGGER.log(Level.WARNING,
            "takePicture ack error received {0}", error.toString());
      } catch (NMFException ex) {
        LOGGER.log(Level.SEVERE,
            "takePicture ack error " + error.toString() + " could not be reported!",
            ex);
      }
    }

    @Override
    public void takePictureResponseErrorReceived(
        org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader,
        org.ccsds.moims.mo.mal.MALStandardError error,
        java.util.Map qosProperties)
    {
      try {
        this.casMCAdapter.getConnector().reportActionExecutionProgress(false, 1, this.STAGE_RECIVED,
            this.totalStage, this.actionInstanceObjId);
        LOGGER.log(Level.WARNING,
            "takePicture response error received {0}", error.toString());
      } catch (NMFException ex) {
        LOGGER.log(Level.SEVERE,
            "takePicture response error " + error.toString() + " could not be reported!",
            ex);
      }
    }
  }

  /**
   * Takes a photograph (instantly)
   *
   * @param actionInstanceObjId the Instance ID of the action that triggers the Photograph
   * @param stageOffset         number of states that where already executed before taking a
   *                            photograph
   * @param totalStages         total number of Stages (including all stages needet for a
   *                            photograph)
   * @param fileName            a text that is added at the end of the filename (before the file
   *                            ending)
   * @throws NMFException
   * @throws IOException
   * @throws MALInteractionException
   * @throws MALException
   */
  public void takePhotograph(long actionInstanceObjId, int stageOffset, int totalStages,
      String fileName) throws
      NMFException,
      IOException,
      MALInteractionException,
      MALException
  {

    float exposureTime = casMCAdapter.getExposureTime();
    if (casMCAdapter.getExposureType() != CameraAcquisitorSystemMCAdapter.ExposureTypeModeEnum.CUSTOM) {
      //TODO
    }
    PixelResolution resolution = new PixelResolution(
        new UInteger(casMCAdapter.getPictureWidth()),
        new UInteger(casMCAdapter.getPictureWidth()));

    LOGGER.log(Level.INFO, "Taking Photograph");
    this.casMCAdapter.getConnector().getPlatformServices().getCameraService().takePicture(
        new CameraSettings(
            resolution,
            casMCAdapter.getPictureType(),
            new Duration(exposureTime),
            casMCAdapter.getGainRed(),
            casMCAdapter.getGainGreen(),
            casMCAdapter.getGainBlue()),
        new CameraDataHandler(actionInstanceObjId, stageOffset, totalStages, fileName,
            this.casMCAdapter));
  }
}
