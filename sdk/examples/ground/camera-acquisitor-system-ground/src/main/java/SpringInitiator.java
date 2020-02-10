
import esa.mo.ground.cameraacquisotorground.CameraAcquisitorGround;
import java.util.Collections;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Kevin Otto <Kevin@KevinOtto.de>
 */
@SpringBootApplication
public class SpringInitiator
{

  /**
   * Main command line entry point.
   *
   * @param args the command line arguments
   * @throws java.lang.Exception If there is an error
   */
  public static void main(final String args[])
  {
    SpringApplication app = new SpringApplication(CameraAcquisitorGround.class);

    app.setDefaultProperties(Collections.singletonMap("server.port", "8083"));
    app.run(args);
  }

}
