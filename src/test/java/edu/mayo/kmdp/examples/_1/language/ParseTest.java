package edu.mayo.kmdp.examples._1.language;

import static edu.mayo.kmdp.util.FileUtil.readBytes;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Parsed_Knowedge_Expression;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.DMN_1_2_XML_Syntax;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.examples.PlatformConfig;
import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import edu.mayo.kmdp.tranx.v4.server.DetectApiInternal;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.cmmn._20151109.model.TDefinitions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Document;

@SpringBootTest
@ContextConfiguration(classes = PlatformConfig.class)
public class ParseTest {

  @Inject
  @KPServer
  DeserializeApiInternal parser;

  @Inject
  @KPServer
  DetectApiInternal detector;

  @Test
  void testParseCMMNWithImplicitDetection() {
    byte[] data = readBytes(DetectTest.class.getResourceAsStream("/mock/Basic Case Model.cmmn.xml"))
        .orElseGet(Assertions::fail);

    TDefinitions cmmnModel =
        parser.applyLift(of(data)
            .withRepresentation(
                detector.applyDetect(of(data))
                    .map(KnowledgeCarrier::getRepresentation)
                    .orElseGet(Assertions::fail)
            ), Abstract_Knowledge_Expression)
            .flatOpt(kc -> kc.as(TDefinitions.class))
            .orElseGet(Assertions::fail);

    System.out.println("Model Name >>> " + cmmnModel.getName());
  }

  @Test
  void testParseDMNWithAssertedRepresentation() {
    byte[] data = readBytes(
        DetectTest.class.getResourceAsStream("/mock/Basic Decision Model.dmn.xml"))
        .orElseGet(Assertions::fail);

    org.omg.spec.dmn._20180521.model.TDefinitions dmnModel =
        parser.applyLift(of(data)
            .withRepresentation(
                new SyntacticRepresentation()
                    .withLanguage(DMN_1_2)
                    .withSerialization(DMN_1_2_XML_Syntax)
                    .withFormat(XML_1_1)
            ), Abstract_Knowledge_Expression)
            .flatOpt(kc -> kc.as(org.omg.spec.dmn._20180521.model.TDefinitions.class))
            .orElseGet(Assertions::fail);

    System.out.println("Model Name >>> " + dmnModel.getName());
  }

  @Test
  void testParseDMNToDOM() {
    byte[] data = readBytes(
        DetectTest.class.getResourceAsStream("/mock/Basic Decision Model.dmn.xml"))
        .orElseGet(Assertions::fail);

    Document dox =
        parser.applyLift(
            of(data)
                .withRepresentation(rep(DMN_1_2, DMN_1_2_XML_Syntax, XML_1_1)),
            Parsed_Knowedge_Expression)
            .flatOpt(kc -> kc.as(Document.class))
            .orElseGet(Assertions::fail);

    System.out.println("Parsed Model >> " + dox.getClass());
    System.out.println("Model Name >>> " + dox.getDocumentElement().getAttribute("name"));
  }

  @Test
  void testParseDMNToString() {
    byte[] data = readBytes(
        DetectTest.class.getResourceAsStream("/mock/Basic Decision Model.dmn.xml"))
        .orElseGet(Assertions::fail);

    Object dmnStr =
        parser.applyLift(of(data)
                .withRepresentation(rep(DMN_1_2, DMN_1_2_XML_Syntax, XML_1_1)),
            Concrete_Knowledge_Expression)
            .map(KnowledgeCarrier::getExpression)
            .orElseGet(Assertions::fail);

    System.out.println("Parsed Model >> " + dmnStr.getClass());
    System.out.println("Model >>> \n" + dmnStr.toString().substring(0, 300));
  }
}
