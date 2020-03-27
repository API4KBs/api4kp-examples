package edu.mayo.kmdp.examples._0.basic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.repository.asset.v4.KnowledgeAssetCatalogApi;
import edu.mayo.kmdp.repository.asset.v4.client.ApiClientFactory;
import edu.mayo.kmdp.repository.asset.v4.server.KnowledgeAssetCatalogApiDelegate;
import edu.mayo.kmdp.repository.asset.v4.server.KnowledgeAssetCatalogApiInternal;
import edu.mayo.kmdp.repository.asset.v4.server.KnowledgeAssetCatalogApiInternalAdapter;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.id.Pointer;
import org.omg.spec.api4kp._1_0.services.repository.KnowledgeAssetCatalog;

/**
 * This example uses the KnowledgeAssetRepositoryAPI to demonstrate how to implement a server, and
 * connect to it using a client
 */
public class APIArchitectureExample {


  /**
   * This test demonstrates various ways for a client to obtain an instance of an API4KP server As
   * various forms of proxying and/or delegation (including web-based services) are provided through
   * code-generation, the goal is to offer a single, transparent interface.
   */
  public void testLocalClientInitialization() {

    // The client instantiates the service provider directly
    // FIXME: using the 'internal', server-oriented interface - should use KnowledgeAssetCatalogApi instead
    // FIXME:   KnowledgeAssetCatalogApi server = KnowledgeAssetCatalogApi.newInstance(newServerImplementation());
    KnowledgeAssetCatalogApiInternal server = newServerImplementation();
    assertTrue(server.getAssetCatalog().isSuccess());

    // The client can also use a delegate
    KnowledgeAssetCatalogApiDelegate delegate = new KnowledgeAssetCatalogApiInternalAdapter();
    KnowledgeAssetCatalogApi delegateClient = KnowledgeAssetCatalogApi.newInstance(delegate);
    assertTrue(delegateClient.getAssetCatalog().isSuccess());
  }

  public void testRemoteClientInitialization() {

    // Otherwise, the client can connect to a web service
    KnowledgeAssetCatalogApi restClient = KnowledgeAssetCatalogApi
        .newInstance(new ApiClientFactory("http://localhost:8080"));
    assertTrue(restClient.getAssetCatalog().isSuccess());

  }


  /**
   * A mock factory method that returns a server implementing an API4KP interface All the
   * operations, except for 'getAssetCatalog' are unsupported.
   *
   * @return A mock implementation of the KnowledgeAssetCatalog API
   */
  KnowledgeAssetCatalogApiInternal newServerImplementation() {
    return new KnowledgeAssetCatalogApi() {
      @Override
      public Answer<KnowledgeAssetCatalog> getAssetCatalog() {
        return Answer.of(new KnowledgeAssetCatalog().withName("Mock Server"));
      }

      @Override
      public Answer<KnowledgeAsset> getKnowledgeAsset(UUID assetId, String xAccept) {
        return Answer.unsupported();
      }

      @Override
      public Answer<List<Pointer>> getKnowledgeAssetVersions(UUID assetId, Integer offset,
          Integer limit, String beforeTag, String afterTag, String sort) {
        return Answer.unsupported();
      }

      @Override
      public Answer<KnowledgeAsset> getVersionedKnowledgeAsset(UUID assetId, String versionTag) {
        return Answer.unsupported();
      }

      @Override
      public Answer<UUID> initKnowledgeAsset() {
        return Answer.unsupported();
      }

      @Override
      public Answer<List<Pointer>> listKnowledgeAssets(String assetType, String assetAnnotation,
          Integer offset, Integer limit) {
        return Answer.unsupported();
      }

      @Override
      public Answer<Void> setVersionedKnowledgeAsset(UUID assetId, String versionTag,
          KnowledgeAsset assetSurrogate) {
        return Answer.unsupported();
      }
    };
  }
}
