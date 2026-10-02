package com.fivesense.api;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureFitnessTests {
    private final JavaClasses classes=new ClassFileImporter().importPackages("com.fivesense.api");

    @Test
    void capabilitiesHaveNoDependencyCycles(){
        SlicesRuleDefinition.slices().matching("com.fivesense.api.(*)..").should().beFreeOfCycles().check(classes);
    }

    @Test
    void controllersDoNotAccessPersistenceAdapters(){
        noClasses().that().resideInAPackage("..controller..").should().dependOnClassesThat().resideInAPackage("..infra..").check(classes);
    }

    @Test
    void domainDoesNotDependOnOuterLayers(){
        noClasses().that().resideInAPackage("..domain..").should().dependOnClassesThat().resideInAnyPackage("..controller..","..dto..","..app..","..infra..").check(classes);
    }

    @Test
    void typesFollowTheirLayerNamingConvention(){
        classes().that().haveSimpleNameEndingWith("Controller").should().resideInAPackage("..controller..").check(classes);
        classes().that().haveSimpleNameEndingWith("Repository").should().resideInAPackage("..infra..").check(classes);
        classes().that().haveSimpleNameEndingWith("Mapper").should().resideInAPackage("..app..").check(classes);
    }
}
