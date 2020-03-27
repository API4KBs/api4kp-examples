package edu.mayo.kmdp.examples._5.carrier;

import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.examples.PlatformConfig;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.language.detectors.html.HTMLDetector;
import edu.mayo.kmdp.language.parsers.dmn.v1_2.DMN12Parser;
import edu.mayo.kmdp.language.parsers.surrogate.v1.SurrogateParser;
import edu.mayo.kmdp.metadata.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder;
import edu.mayo.kmdp.repository.asset.KnowledgeAssetRepositoryService;
import edu.mayo.kmdp.tranx.v4.server.DeserializeApiInternal;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.id.Pointer;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = PlatformConfig.class)
public class CarrierTest {

  @KPServer
  @Inject
  DeserializeApiInternal parser;

  KnowledgeAssetRepositoryService assetRepo
      = KnowledgeAssetRepositoryService.selfContainedRepository(
      Arrays.asList(new SurrogateParser(), new DMN12Parser()),
      Collections.singletonList(new HTMLDetector()),
      Collections.emptyList(),
      Collections.emptyList()
  );

  @BeforeEach
  void setup() {
    InputStream modelIs = CarrierTest.class
        .getResourceAsStream("/mock/Basic Decision Model.dmn.xml");
    InputStream surrIs = CarrierTest.class
        .getResourceAsStream("/mock/Basic Decision Model.surrogate.xml");
    KnowledgeAsset surrogate = readSurrogate(surrIs);
    assetRepo.publish(surrogate, readArtifact(surrogate, modelIs));

    InputStream modelIs2 = CarrierTest.class.getResourceAsStream("/mock/Basic Decision Model.html");
    assetRepo.publish(surrogate,
        readArtifact(DatatypeHelper.toSemanticIdentifier(surrogate.getAssetId()), modelIs2));
  }

  private KnowledgeAsset readSurrogate(InputStream surrIs) {
    return parser.lift(
        of(surrIs)
            .withRepresentation(rep(Knowledge_Asset_Surrogate, XML_1_1)),
        Abstract_Knowledge_Expression)
        .flatOpt(kc -> kc.as(KnowledgeAsset.class))
        .orElseGet(Assertions::fail);
  }

  private KnowledgeCarrier readArtifact(KnowledgeAsset surrogate, InputStream modelIs) {
    return AbstractCarrier.of(modelIs)
        .withAssetId(DatatypeHelper.toSemanticIdentifier(surrogate.getAssetId()))
        .withRepresentation(rep(
            ((ComputableKnowledgeArtifact) surrogate.getCarriers().get(0)).getRepresentation()))
        .withArtifactId(
            DatatypeHelper.toSemanticIdentifier(surrogate.getCarriers().get(0).getArtifactId()));
  }

  private KnowledgeCarrier readArtifact(ResourceIdentifier assetId, InputStream modelIs) {
    return AbstractCarrier.of(modelIs)
        .withAssetId(assetId)
        .withRepresentation(rep(HTML, TXT))
        .withArtifactId(SurrogateBuilder.randomArtifactId());
  }

  @Test
  void testListCarriers() {
    List<Pointer> pointers = assetRepo.listKnowledgeAssets()
        .orElse(Collections.emptyList());
    assertTrue(pointers.size() > 0);

    ResourceIdentifier assetId = pointers.get(0);
    List<Pointer> artifacts
        = assetRepo
        .getKnowledgeAssetCarriers(assetId.getUuid(), assetId.getVersionTag())
        .orElse(null);

    System.out.println("Found Artifacts >> " + artifacts.size());
  }

  @Test
  void testGetCanonicalCarrier() {
    List<Pointer> pointers = assetRepo.listKnowledgeAssets()
        .orElse(Collections.emptyList());
    assertTrue(pointers.size() > 0);

    ResourceIdentifier assetId = pointers.get(0);
    KnowledgeCarrier kc = assetRepo
        .getCanonicalKnowledgeAssetCarrier(assetId.getUuid(), assetId.getVersionTag())
        .orElse(null);

    System.out.println("Found Carrier w/ Lang >> " + kc.getRepresentation().getLanguage());
  }

  @Test
  void testGetCarrierWithNegotiation() {
    List<Pointer> pointers = assetRepo.listKnowledgeAssets()
        .orElse(Collections.emptyList());
    assertTrue(pointers.size() > 0);

    ResourceIdentifier assetId = pointers.get(0);
    KnowledgeCarrier kc = assetRepo
        .getCanonicalKnowledgeAssetCarrier(assetId.getUuid(), assetId.getVersionTag(),
            "text/html")
        .orElse(null);

    System.out.println("Found Carrier w/ Lang >> " + kc.getRepresentation().getLanguage());
  }


}
