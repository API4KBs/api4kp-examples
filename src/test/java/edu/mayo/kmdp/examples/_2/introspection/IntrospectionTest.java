package edu.mayo.kmdp.examples._2.introspection;

import static edu.mayo.kmdp.SurrogateBuilder.assetId;
import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static edu.mayo.kmdp.kbase.introspection.dmn.v1_2.DMN12MetadataIntrospector.DMN1_2_EXTRACTOR;
import static edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries.Extract_Description_Task;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.SurrogateBuilder;
import edu.mayo.kmdp.examples.PlatformConfig;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.inference.v3.server.IntrospectionApiInternal;
import edu.mayo.kmdp.kbase.introspection.dmn.v1_2.DMN12MetadataIntrospector;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.tranx.v3.DeserializeApi;
import edu.mayo.kmdp.tranx.v3.server.DeserializeApiInternal;
import edu.mayo.kmdp.tranx.v3.server.DetectApiInternal;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations.KnowledgeProcessingOperationSeries;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import java.io.InputStream;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPOperation;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KPSupport;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
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
    InputStream is = IntrospectionTest.class.getResourceAsStream("/mock/Basic Decision Model.dmn.xml");

    KnowledgeCarrier kc = introspector.introspect(
        DMN1_2_EXTRACTOR,
        of( is )
            .withAssetId(assetId(UUID.randomUUID(),"0.0.0"))
            .withArtifactId(uri(UUID.randomUUID().toString(),"0.0.0"))
            .withRepresentation(rep(DMN_1_2, XML_1_1)) )
        .orElseGet(Assertions::fail);

    System.out.println("Created >> " + kc.getRepresentation().getLanguage());
    KnowledgeAsset surrogate = kc.as(KnowledgeAsset.class).orElseGet(Assertions::fail);

    parser.lower(kc, Concrete_Knowledge_Expression)
        .map(ExpressionCarrier.class::cast)
        .map(ExpressionCarrier::getSerializedExpression)
        .ifPresent(System.out::print);

  }


}
