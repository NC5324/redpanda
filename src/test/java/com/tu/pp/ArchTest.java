package com.tu.pp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.tu.pp");

        noClasses()
            .that()
            .resideInAnyPackage("com.tu.pp.service..")
            .or()
            .resideInAnyPackage("com.tu.pp.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..com.tu.pp.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}
