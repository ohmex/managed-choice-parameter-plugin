package com.ohmex.hudson.plugins.managedchoiceparameter;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.Util;
import hudson.cli.CLICommand;
import hudson.model.*;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.*;

public class ManagedChoiceParameterDefinition extends ParameterDefinition {
  private static final Logger LOGGER = Logger.getLogger(ManagedChoiceParameterDefinition.class.getName());
  private String remoteURL;
  private String credentialsId;
  private String pipelinePath;

  @DataBoundConstructor
  public ManagedChoiceParameterDefinition(String name, String description, String remoteURL, String credentialsId, String pipelinePath) {
    super(name, description);
    this.remoteURL = remoteURL;
    this.credentialsId = credentialsId;
    this.pipelinePath = pipelinePath;
  }

  @Override
  public ParameterValue createValue(StaplerRequest staplerRequest, JSONObject jsonObject) {
    Object value = jsonObject.get("value");
    StringBuilder strValue = new StringBuilder();
    if (value instanceof String) {
      strValue.append(value);
    } else if (value instanceof JSONArray) {
      JSONArray jsonValues = (JSONArray) value;
      for (int i = 0; i < jsonValues.size(); i++) {
        strValue.append(jsonValues.getString(i));
        if (i < jsonValues.size() - 1) {
          strValue.append(",");
        }
      }
    }
    return new ManagedChoiceParameterValue(jsonObject.getString("name"), strValue.toString());
  }

  @Override
  public ParameterValue createValue(StaplerRequest req) {
    String value[] = req.getParameterValues(getName());
    if (value == null || value.length == 0 || StringUtils.isBlank(value[0])) {
      return getDefaultParameterValue();
    } else {
      return new ManagedChoiceParameterValue(getName(), value[0]);
    }
  }

  @Override
  public ParameterValue createValue(CLICommand command, String value) {
    if (StringUtils.isNotEmpty(value)) {
      return new ManagedChoiceParameterValue(getName(), value);
    }
    return getDefaultParameterValue();
  }

  // TODO: This essentially gives back empty json to be populated in choices
  @Override
  public ParameterValue getDefaultParameterValue() {
      String defaultValue = "{}";
      return new ManagedChoiceParameterValue(getName(), defaultValue);
  }

  public String getCredentialsId() {
    return credentialsId;
  }

  public String getRemoteURL() {
    return remoteURL;
  }

  public void setRemoteURL(String remoteURL) {
    this.remoteURL = remoteURL;
  }

  public String getPipelinePath() {
    return pipelinePath;
  }

  public void setPipelinePath(String pipelinePath) {
    this.pipelinePath = pipelinePath;
  }

  @Symbol("managedChoice")
  @Extension
  public static class DescriptorImpl extends ParameterDescriptor {
    @Override
    @Nonnull
    public String getDisplayName() {
      return Messages.ManagedChoiceParameterDefinition_DisplayName();
    }

    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item context, @QueryParameter String remote) {
      if (context == null || !context.hasPermission(Item.EXTENDED_READ)) {
        return new StandardListBoxModel();
      }
      return fillCredentialsIdItems(context, remote);
    }

    public ListBoxModel fillCredentialsIdItems(Item context, String remote) {
      List<DomainRequirement> domainRequirements;
      if (remote == null) {
        domainRequirements = Collections.emptyList();
      } else {
        domainRequirements = URIRequirementBuilder.fromUri(remote.trim()).build();
      }

      return new StandardListBoxModel()
        .includeEmptyValue()
        .withMatching(
          CredentialsMatchers.anyOf(
            CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class),
            CredentialsMatchers.instanceOf(StandardCertificateCredentials.class),
            CredentialsMatchers.instanceOf(SSHUserPrivateKey.class)
          ),
          CredentialsProvider.lookupCredentials(StandardCredentials.class,
            context,
            ACL.SYSTEM,
            domainRequirements)
        );
    }

    public FormValidation doCheckRemoteURL(StaplerRequest req, @AncestorInPath Item context, @QueryParameter String value) {
      String url = Util.fixEmptyAndTrim(value);

      if (url == null) {
        return FormValidation.error("Repository URL is required");
      }

      if (url.indexOf('$') != -1) {
        return FormValidation.warning("This repository URL is parameterized, syntax validation skipped");
      }

      try {
        new URIish(value);
      } catch (URISyntaxException e) {
        return FormValidation.error("Repository URL is illegal");
      }
      return FormValidation.ok();
    }
  }
}
