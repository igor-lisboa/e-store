package br.uff.store.steps.runner;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = {
                "src/test/resources/features/product.feature" }, glue = "br.uff.store.steps.stepDefinition", plugin = {
                                "pretty", "html:target/cucumber" })
public class CucumberProductTest {
}
