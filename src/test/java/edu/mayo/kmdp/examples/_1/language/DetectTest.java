package edu.mayo.kmdp.examples._1.language;

import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;

import edu.mayo.kmdp.examples.PlatformConfig;
import edu.mayo.kmdp.tranx.v4.server.DetectApiInternal;
import java.io.InputStream;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = PlatformConfig.class)
public class DetectTest {

  @Inject
  @KPServer
  DetectApiInternal detector;

  @Test
  void testParseCMMN() {
    InputStream is = DetectTest.class.getResourceAsStream("/mock/Basic Case Model.cmmn.xml");

    SyntacticRepresentation rep
        = detector.getDetectedRepresentation(of(is))
        .orElseGet(Assertions::fail);

    System.out.println("Detected Language >> " + rep.getLanguage());
    System.out.println("Detected Serialization >> " + rep.getSerialization());
    System.out.println("Detected Serialization Foramt >> " + rep.getFormat());
  }

  @Test
  void testParseDMN() {
    InputStream is = DetectTest.class.getResourceAsStream("/mock/Basic Decision Model.dmn.xml");

    SyntacticRepresentation rep
        = detector.getDetectedRepresentation(of(is))
        .orElseGet(Assertions::fail);

    System.out.println("Detected Language >> " + rep.getLanguage());
    System.out.println("Detected Serialization >> " + rep.getSerialization());
    System.out.println("Detected Serialization Foramt >> " + rep.getFormat());
  }


  @Test
  void testParseOWL() {
    InputStream is = DetectTest.class.getResourceAsStream("/mock/Basic Ontology.owl");

    SyntacticRepresentation rep
        = detector.getDetectedRepresentation(of(is))
        .orElseGet(Assertions::fail);

    System.out.println("Detected Language >> " + rep.getLanguage());
    System.out.println("Detected Serialization >> " + rep.getSerialization());
    System.out.println("Detected Serialization Foramt >> " + rep.getFormat());
  }


}
