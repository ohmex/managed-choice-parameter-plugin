package com.ohmex.hudson.plugins.managedchoiceparameter;

import hudson.model.StringParameterValue;
import org.kohsuke.stapler.DataBoundConstructor;

public class ManagedChoiceParameterValue extends StringParameterValue {
  @DataBoundConstructor
  public ManagedChoiceParameterValue(String name, String value) {
    super(name, value);
  }
}
