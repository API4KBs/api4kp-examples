package edu.mayo.kmdp.examples._4.discover;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.deRef;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.examples.PlatformConfig;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.metadata.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.repository.asset.KnowledgeAssetRepositoryService;
import edu.mayo.kmdp.tranx.v3.server.DeserializeApiInternal;
import edu.mayo.kmdp.util.Util;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = PlatformConfig.class)
public class DiscoverTest {

  @KPServer
  @Inject
  DeserializeApiInternal parser;

  KnowledgeAssetRepositoryService assetRepo
      = KnowledgeAssetRepositoryService.selfContainedRepository();

  @BeforeEach
  void setup() {
    InputStream modelIs = DiscoverTest.class.getResourceAsStream("/mock/Basic Decision Model.dmn.xml");
    InputStream surrIs = DiscoverTest.class.getResourceAsStream("/mock/Basic Decision Model.surrogate.xml");
    publish(modelIs,surrIs);

    InputStream modelIs2 = DiscoverTest.class.getResourceAsStream("/mock/Basic Case Model.cmmn.xml");
    InputStream surrIs2 = DiscoverTest.class.getResourceAsStream("/mock/Basic Case Model.surrogate.xml");
    publish(modelIs2,surrIs2);
  }

  private void publish(InputStream modelIs, InputStream surrIs) {
    KnowledgeAsset surrogate = parser.lift(
        of(surrIs)
            .withRepresentation(rep(Knowledge_Asset_Surrogate, XML_1_1)),
        Abstract_Knowledge_Expression)
        .flatOpt(kc -> kc.as(KnowledgeAsset.class))
        .orElseGet(Assertions::fail);
    KnowledgeCarrier artifact = AbstractCarrier.of(modelIs)
        .withAssetId(surrogate.getAssetId())
        .withArtifactId(surrogate.getCarriers().get(0).getArtifactId());

    assetRepo.publish(surrogate,artifact);
  }

  @Test
  void testListAssets() {
    List<Pointer> pointers = assetRepo.listKnowledgeAssets()
        .orElse(Collections.emptyList());

    System.out.println("Number of published assets >> " + pointers.size());
  }

  @Test
  void testGetAsset() {
    List<Pointer> pointers = assetRepo.listKnowledgeAssets()
        .orElse(Collections.emptyList());
    assertTrue(pointers.size() > 0);

    VersionedIdentifier vid = deRef(pointers.get(0));
    KnowledgeAsset asset
        = assetRepo.getVersionedKnowledgeAsset(UUID.fromString(vid.getTag()), vid.getVersion())
        .orElse(null);

    System.out.println("Asset name >> " + asset.getName());
  }


}
