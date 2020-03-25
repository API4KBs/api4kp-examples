package edu.mayo.kmdp.examples._7.inference;


import static edu.mayo.kmdp.id.helper.DatatypeHelper.resolveTerm;

import edu.mayo.kmdp.id.Identifier;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum MockVocab implements Term {


  Survival_Rate(
      "b7cdb1b3-8e96-419a-ac08-01d9b38de3e8",
      "hodgkinLymphoma5YearSurvivalRate",
      "Hodgkin's Lymphoma Survival Rate (5 Year)"
  ),

  Current_Caffeine_User(
      "currentCaffeineUser",
      "Current Caffeine User"
  ),

  Current_Chronological_Age(
      "01aaaf22-ca7f-42d0-a1a2-f027fbf81fa6",
      "currentChronologicalAge",
      "Current Chronological Age"
  );


  public static final Map<UUID,MockVocab> INDEX = Arrays.stream(MockVocab.values())
      .collect(Collectors.toConcurrentMap(Term::getConceptUUID, Function.identity()));

  private UUID uuid;
  private String tag;
  private String label;

  MockVocab(final String code,
      final String displayName) {
    this(Util.uuid(code).toString(),code,displayName);
  }

  MockVocab(final String conceptUUID,
      final String code,
      final String displayName) {
    this.uuid = UUID.fromString(conceptUUID);
    this.tag = code;
    this.label = displayName;
  }


  public static Optional<MockVocab> resolve(final String tag) {
    return resolveTag(tag);
  }

  public static Optional<MockVocab> resolveTag(final String tag) {
    return resolveTerm(tag, MockVocab.values(), Term::getTag);
  }

  public static Optional<MockVocab> resolveUUID(final UUID conceptId) {
    return Optional.ofNullable(INDEX.get(conceptId));
  }



  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String getTag() {
    return tag;
  }

  @Override
  public URI getRef() {
    return null;
  }

  @Override
  public URI getConceptId() {
    return null;
  }

  @Override
  public Identifier getNamespace() {
    return null;
  }

}
