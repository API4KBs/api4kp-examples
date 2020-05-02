package edu.mayo.kmdp.examples._7.inference;

import static edu.mayo.kmdp.kbase.introspection.dmn.v1_1.DMN11MetadataIntrospector.DMN1_1_EXTRACTOR;
import static edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder.artifactId;
import static edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder.assetId;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static java.util.Collections.emptyMap;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.examples._3.publish.PublicationTest;
import edu.mayo.kmdp.inference.v4.server.InferenceApiInternal._infer;
import edu.mayo.kmdp.kbase.inference.dmn.v1_1.DMNEngineProvider;
import edu.mayo.kmdp.kbase.introspection.dmn.v1_1.DMN11MetadataIntrospector;
import edu.mayo.kmdp.knowledgebase.KnowledgeBaseProvider;
import edu.mayo.kmdp.knowledgebase.v4.server.KnowledgeBaseApiInternal;
import edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.repository.asset.KnowledgeAssetRepositoryService;
import edu.mayo.kmdp.util.FileUtil;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.IntegerType;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier.Encodings;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

public class InferTest {

  UUID modelId = UUID.nameUUIDFromBytes("mockPredictor".getBytes());
  String versionTag = "0.0.0";

  DMN11MetadataIntrospector introspector = new DMN11MetadataIntrospector();

  KnowledgeAssetRepositoryService assetRepo = KnowledgeAssetRepositoryService
      .selfContainedRepository();
  KnowledgeBaseApiInternal kbaseManager = new KnowledgeBaseProvider(assetRepo);

  private void publish() {
    byte[] modelData = FileUtil
        .readBytes(PublicationTest.class.getResourceAsStream("/mock/MockPredictor.dmn"))
        .orElseGet(Assertions::fail);
    KnowledgeCarrier artifactCarrier = of(modelData)
        .withAssetId(assetId(modelId, versionTag))
        .withArtifactId(artifactId(UUID.randomUUID().toString(), versionTag))
        .withRepresentation(rep(DMN_1_1, XML_1_1, Charset.defaultCharset(), Encodings.DEFAULT));

    // introspect
    KnowledgeAsset surrogate =
        introspector.introspect(DMN1_1_EXTRACTOR, artifactCarrier, null)
            .flatOpt(kc -> kc.as(KnowledgeAsset.class))
            .orElseGet(Assertions::fail);

    assetRepo.publish(surrogate, artifactCarrier);
  }

  private _infer initInference() {
    publish();
    return assetRepo
        // get Metadata
        .getKnowledgeAssetVersion(modelId, versionTag)
        // Use metadata to instantiate the appropriate engine
        // and deploy the KB constructed around the asset
        .flatOpt(asset -> new DMNEngineProvider(kbaseManager).apply(asset))
        .orElseGet(Assertions::fail);
  }

  @Test
  public void testInference() {
    _infer infService = initInference();

    Map<String, Type> map = new HashMap<>();
    map.put(MockVocab.Current_Caffeine_User.getTag(),
        new BooleanType().setValue(true));
    map.put(MockVocab.Current_Chronological_Age.getTag(),
        new IntegerType().setValue(37));

    java.util.Map<?, ?> out = infService.infer(modelId, versionTag, map)
        .orElse(emptyMap());

    System.out.println(out);
    Quantity qty = (Quantity) out.get(MockVocab.Survival_Rate.getTag());
    System.out.println("Inferred risk >> " + qty.getValue());
  }


}
