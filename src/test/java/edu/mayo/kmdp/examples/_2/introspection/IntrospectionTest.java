package edu.mayo.kmdp.examples._2.introspection;

import static edu.mayo.kmdp.kbase.introspection.dmn.v1_2.DMN12MetadataIntrospector.DMN1_2_EXTRACTOR;
import static edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder.randomArtifactId;
import static edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder.randomAssetId;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.examples.PlatformConfig;
import edu.mayo.kmdp.inference.v4.server.IntrospectionApiInternal;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import java.io.InputStream;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = PlatformConfig.class)
public class IntrospectionTest {

  @Inject
  @KPServer
  IntrospectionApiInternal introspector;

  @KPServer
  @Inject
  DeserializeApiInternal parser;


  @Test
  void testIntrospectDMN() {
    InputStream is = IntrospectionTest.class
        .getResourceAsStream("/mock/Basic Decision Model.dmn.xml");

    KnowledgeCarrier kc = introspector.introspect(
        DMN1_2_EXTRACTOR,
        of(is)
            .withAssetId(randomAssetId())
            .withArtifactId(randomArtifactId())
            .withRepresentation(rep(DMN_1_2, XML_1_1)))
        .orElseGet(Assertions::fail);

    System.out.println("Created >> " + kc.getRepresentation().getLanguage());
    KnowledgeAsset surrogate = kc.as(KnowledgeAsset.class)
        .orElseGet(Assertions::fail);

    parser.lower(kc, Concrete_Knowledge_Expression)
        .flatOpt(AbstractCarrier::asString)
        .ifPresent(System.out::print);

  }


}
