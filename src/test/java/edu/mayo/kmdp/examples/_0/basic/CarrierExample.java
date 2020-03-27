package edu.mayo.kmdp.examples._0.basic;

import static edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder.artifactId;
import static edu.mayo.kmdp.metadata.v2.surrogate.SurrogateBuilder.assetId;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.XHTML;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.VERSION_ZERO;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

public class CarrierExample {

  @Test
  /**
   * KnowledgeCarriers ({@link KnowledgeCarrier}) wrap a Knowledge Artifact,
   * whether in byte[], String, Stream, Document or Object format,
   * providing minimal metadata necessary at computation time:
   * - the Knowledge Asset Id
   * - the Knowledge Artifact Id
   * - the Representation Information (language, profile, serialization, format, etc)
   * Carriers can also include a URL, in case the actual Artifact is not serializable
   *
   * The interface {@link AbstractCarrier} provides utility constructor methods
   */
  void testCarrier() {

    KnowledgeCarrier kc = AbstractCarrier.of(
        "<html xmlns=\"http://www.w3.org/1999/xhtml\"> "
            + "<head> "
            + "  <title>Title of document</title> "
            + "</head> "
            + "<body> "
            + "  some content "
            + "</body> "
            + "</html> ")
        .withRepresentation(rep(XHTML, XML_1_1))
        .withAssetId(assetId("asset000", VERSION_ZERO))
        .withArtifactId(artifactId("artifact123", "0.0.1"))
        .withHref(URI.create("http://www.foo.bar/home"));

    assertNotNull(kc.getAssetId());
    assertNotNull(kc.getArtifactId());
    assertSame(XHTML, kc.getRepresentation().getLanguage());
  }


}
