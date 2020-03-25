package edu.mayo.kmdp.examples._3.publish;

import static edu.mayo.kmdp.SurrogateBuilder.assetId;
import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static edu.mayo.kmdp.kbase.introspection.dmn.v1_2.DMN12MetadataIntrospector.DMN1_2_EXTRACTOR;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_2;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.examples.PlatformConfig;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.inference.v3.server.IntrospectionApiInternal;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.repository.asset.KnowledgeAssetRepositoryService;
import edu.mayo.kmdp.repository.asset.v3.server.KnowledgeAssetRepositoryApiInternal;
import edu.mayo.kmdp.tranx.v3.server.DeserializeApiInternal;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import java.io.InputStream;
import java.util.Collections;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = PlatformConfig.class)
public class PublicationTest {

  @KPServer
  @Inject
  DeserializeApiInternal parser;

  KnowledgeAssetRepositoryService assetRepo
      = KnowledgeAssetRepositoryService.selfContainedRepository();

  @Test
  void testIntrospectDMN() {
    InputStream modelIs = PublicationTest.class.getResourceAsStream("/mock/Basic Decision Model.dmn.xml");
    InputStream surrIs = PublicationTest.class.getResourceAsStream("/mock/Basic Decision Model.surrogate.xml");

    KnowledgeAsset surrogate = parser.lift(
        of(surrIs)
            .withRepresentation(rep(Knowledge_Asset_Surrogate, XML_1_1)),
        Abstract_Knowledge_Expression)
        .flatOpt(kc -> kc.as(KnowledgeAsset.class))
        .orElseGet(Assertions::fail);
    byte[] artifact = ((BinaryCarrier) AbstractCarrier.of(modelIs)).getEncodedExpression();

    VersionedIdentifier surrogateId = surrogate.getAssetId();
    VersionedIdentifier artifactId = surrogate.getCarriers().get(0).getArtifactId();

    // publish metadata
    assetRepo.setVersionedKnowledgeAsset(
        Util.toUUID(surrogateId.getTag()),
        surrogateId.getVersion(),
        surrogate);

    // publish artifact
    assetRepo.setKnowledgeAssetCarrierVersion(
        Util.toUUID(surrogateId.getTag()),
        surrogateId.getVersion(),
        Util.toUUID(artifactId.getTag()),
        artifactId.getVersion(),
        artifact);

    int n = assetRepo.listKnowledgeAssets()
        .orElse(Collections.emptyList())
        .size();
    System.out.println("Number of published assets >> " + n);
  }


}
