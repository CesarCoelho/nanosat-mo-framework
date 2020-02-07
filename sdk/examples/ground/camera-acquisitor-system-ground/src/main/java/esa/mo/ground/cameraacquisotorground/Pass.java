/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mo.ground.cameraacquisotorground;

import esa.mo.nmf.apps.CameraAcquisitorSystemMCAdapter;
import org.hipparchus.ode.events.Action;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.BooleanDetector;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.time.AbsoluteDate;

/**
 *
 * @author Kevin Otto <Kevin@KevinOtto.de>
 */
class Pass implements EventHandler<BooleanDetector>
{

  private final AbsoluteDate notBeforeDate;
  private final long worstCaseRotationTimeSeconds;

  private AbsoluteDate passStart;
  private AbsoluteDate passEnd;
  private AbsoluteDate optimalTime;
  private boolean timeFound = false;

  private boolean startIsSet = false;

  public Pass(AbsoluteDate notBeforeDate, long worstCaseRotationTimeSeconds)
  {
    this.notBeforeDate = notBeforeDate;
    this.worstCaseRotationTimeSeconds = worstCaseRotationTimeSeconds;
  }

  public AbsoluteDate getPassStart()
  {
    return passStart;
  }

  public AbsoluteDate getPassEnd()
  {
    return passEnd;
  }

  public AbsoluteDate getOptimalTime()
  {
    return optimalTime;
  }

  public boolean isTimeFound()
  {
    return timeFound;
  }

  @Override
  public Action eventOccurred(SpacecraftState s, BooleanDetector detector, boolean increasing)
  {
    if (increasing) {
      if (!startIsSet) {
        this.passStart = s.getDate();
        startIsSet = true;
      }
    } else {
      this.passEnd = s.getDate();
      double elepsedTime = this.passEnd.durationFrom(this.passStart);
      this.optimalTime = this.passStart.shiftedBy(elepsedTime / 2);

      if (this.optimalTime.durationFrom(CameraAcquisitorSystemMCAdapter.getNow()) <= worstCaseRotationTimeSeconds && this.optimalTime.compareTo(
          this.notBeforeDate) > 0) {

        //time to close, try again
        startIsSet = false;
        return Action.CONTINUE;
      }
      // time found
      timeFound = true;
      return Action.STOP;
    }
    return Action.CONTINUE;
  }
}
