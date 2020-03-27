package edu.mayo.kmdp.examples;

import edu.mayo.kmdp.kbase.introspection.dmn.DMNMetadataIntrospector;
import edu.mayo.kmdp.language.LanguageDeSerializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
    basePackageClasses = {
        DMNMetadataIntrospector.class,
        LanguageDeSerializer.class})
public class PlatformConfig {

}
