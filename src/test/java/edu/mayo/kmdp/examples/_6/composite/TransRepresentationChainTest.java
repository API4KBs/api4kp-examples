/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.examples._6.composite;

import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.PCV;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.SNOMED_CT;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.fhir.fhir3.FHIR3JsonUtil;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.jena.rdf.model.Model;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeBase;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

public class TransRepresentationChainTest extends CmmnToPlanDefIntegrationTestBase {

  @Override
  protected List<String> getXMLS() {
    return Arrays.asList(
        "/mock/Basic Case Model.cmmn.xml",
        "/mock/Basic Decision Model.dmn.xml"
    );
  }

  @Override
  protected UUID getRootAssetID() {
    return UUID.fromString("14321e7c-cb9a-427f-abf5-1420bf26e03c");
  }

  @Override
  protected String getRootAssetVersion() {
    return "1.0.1";
  }


  @Test
  public void testStep1() {
    // The dependency-based constructor considers the given asset as the root of a tree-based knowledge base,
    // implicitly defined by the query { caseModel dependsOn* decisionModel }
    // In particular, in this case we have
    //    { caseModel dependsOn* decisionModel }
    // The operation then returns a 'structure', which is effectively an 'intensional' manifestation of a new, composite Asset
    Model struct =

        constructor.getKnowledgeBaseStructure(getRootAssetID(), getRootAssetVersion())

            .flatOpt(kc -> kc.as(Model.class))
            .orElseGet(Assertions::fail);

    System.out.println("Structure Graph >>");
    System.out.println(JenaUtil.asString(struct));
  }

  @Test
  public void testStep2() {
    KnowledgeCarrier composite =

        constructor.getKnowledgeBaseStructure(getRootAssetID(), getRootAssetVersion())
            .flatMap(kc -> assembler.assembleCompositeArtifact(kc))

            .orElseGet(Assertions::fail);

    System.out.println("Result >> " + composite.getClass());
    CompositeKnowledgeCarrier ckc = (CompositeKnowledgeCarrier) composite;
    System.out.println("Component # >> " + ckc.getComponent().size());

    System.out.println("Struct Type >> " + ckc.getStruct().getRepresentation().getLanguage());
  }

  @Test
  public void testStep3() {
    KnowledgeCarrier composite =

        constructor.getKnowledgeBaseStructure(getRootAssetID(), getRootAssetVersion())
            .flatMap(kc -> assembler.assembleCompositeArtifact(kc))
            .flatMap(kc -> parser.lift(kc, Abstract_Knowledge_Expression))

            .orElseGet(Assertions::fail);

    CompositeKnowledgeCarrier ckc = (CompositeKnowledgeCarrier) composite;
    ckc.getComponent().forEach(comp -> {
      System.out
          .println("Component : " + comp.getRepresentation().getLanguage() + " " + comp.getLevel());
    });
  }

  @Test
  public void testStep4() {
    KnowledgeCarrier parsedComposite =
        constructor.getKnowledgeBaseStructure(getRootAssetID(), getRootAssetVersion())
            .flatMap(kc -> assembler.assembleCompositeArtifact(kc))
            .flatMap(kc -> parser.lift(kc, Abstract_Knowledge_Expression))
            .orElseGet(Assertions::fail);

    KnowledgeCarrier dictionary =
        loadDictionary();

    KnowledgeCarrier wovenComposite =
        // Init empty KB
        kbManager.initKnowledgeBase()
            // Add composite to KB
            .flatMap(vid ->
                kbManager.populateKnowledgeBase(UUID.fromString(vid.getTag()), vid.getVersionTag(),
                    parsedComposite))
            // IoC : the weaver will retrieve the KB from the manager and apply the dictionary to the KB content
            .flatMap(vid ->
                weaver.weave(vid.getUuid(), vid.getVersionTag(), dictionary))
            // Get the result
            .flatMap(vid ->
                kbManager.getKnowledgeBase(vid.getUuid(), vid.getVersionTag()))
            .map(KnowledgeBase::getManifestation)
            .orElseGet(Assertions::fail);

    KnowledgeCarrier decisionModelComponent
        = ((CompositeKnowledgeCarrier) wovenComposite).getComponent().get(1);
    parser.lower(decisionModelComponent, Concrete_Knowledge_Expression)
        .map(ExpressionCarrier.class::cast)
        .ifPresent(ec -> System.out.println(ec.getSerializedExpression()));
  }


  @Test
  public void testStep5() {
    KnowledgeCarrier parsedComposite =
        constructor.getKnowledgeBaseStructure(getRootAssetID(), getRootAssetVersion())
            .flatMap(kc -> assembler.assembleCompositeArtifact(kc))
            .flatMap(kc -> parser.lift(kc, Abstract_Knowledge_Expression))
            .orElseGet(Assertions::fail);

    Answer<KnowledgeCarrier> wovenComposite =
        kbManager.initKnowledgeBase()
            .flatMap(vid ->
                kbManager.populateKnowledgeBase(vid.getUuid(), vid.getVersionTag(),
                    parsedComposite))
            .flatMap(vid ->
                weaver.weave(vid.getUuid(), vid.getVersionTag(), loadDictionary()))
            .flatMap(vid ->
                kbManager.getKnowledgeBase(vid.getUuid(), vid.getVersionTag()))
            .map(KnowledgeBase::getManifestation);

    KnowledgeCarrier planDefinitionComposite =
        wovenComposite.flatMap(ckc ->
            translator.applyTransrepresentationInto(ckc, rep(FHIR_STU3, SNOMED_CT, PCV)))
            .orElseGet(Assertions::fail);

    CompositeKnowledgeCarrier ckc = (CompositeKnowledgeCarrier) planDefinitionComposite;
    ckc.getComponent().forEach(comp -> {
      System.out
          .println("Component : " + comp.getRepresentation().getLanguage() + " " + comp.getLevel());
    });
  }

  @Test
  public void testStep6() {
    KnowledgeCarrier planDefinitionComposite =
        constructor.getKnowledgeBaseStructure(getRootAssetID(), getRootAssetVersion())
            .flatMap(kc -> assembler.assembleCompositeArtifact(kc))
            .flatMap(kc -> parser.lift(kc, Abstract_Knowledge_Expression))
            .flatMap(parsedComposite ->
                kbManager.initKnowledgeBase()
                    .flatMap(vid ->
                        kbManager
                            .populateKnowledgeBase(vid.getUuid(), vid.getVersionTag(),
                                parsedComposite))
                    .flatMap(vid ->
                        weaver.weave(vid.getUuid(), vid.getVersionTag(),
                            loadDictionary()))
                    .flatMap(vid ->
                        kbManager.getKnowledgeBase(vid.getUuid(), vid.getVersionTag()))
                    .map(KnowledgeBase::getManifestation))
            .flatMap(ckc ->
                translator.applyTransrepresentationInto(ckc, rep(FHIR_STU3, SNOMED_CT, PCV)))
            .orElseGet(Assertions::fail);

    KnowledgeCarrier flatPlanDef = flattener
        .flattenArtifact(planDefinitionComposite, getRootAssetID())
        .orElseGet(Assertions::fail);

    System.out.println("Component : " + flatPlanDef.getRepresentation().getLanguage());
    System.out.println(
        FHIR3JsonUtil.instance.toJsonString(
            flatPlanDef.as(PlanDefinition.class)
                .orElse(null)));
  }


}
