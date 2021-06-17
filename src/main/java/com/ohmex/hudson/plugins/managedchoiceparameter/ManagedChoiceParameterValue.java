package com.ohmex.hudson.plugins.managedchoiceparameter;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.model.Run;
import hudson.util.VariableResolver;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Map;

public class ManagedChoiceParameterValue extends ParameterValue {
  private static final long serialVersionUID = -7857702537901594599L;
  private final Map<String, String> selectedValues;

  @DataBoundConstructor
  public ManagedChoiceParameterValue(String name, Map<String, String> selectedValues) {
    super(name, null);
    this.selectedValues = selectedValues;
  }

  @Override
  public void buildEnvironment(Run<?, ?> build, EnvVars env) {
    for(Map.Entry<String, String> entry : getSelectedValues().entrySet()) {
      env.put(entry.getKey(), entry.getValue());
    }
  }

  public Map<String, String> getSelectedValues() {
    return selectedValues;
  }

  @Override
  public Object getValue() {
    return getSelectedValues();
  }

  @Override
  public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
    // Hide the default single build variable by supplying null as a value
    return s -> null;
  }
}
