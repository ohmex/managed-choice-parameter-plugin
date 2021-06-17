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
import com.google.gson.Gson;
import com.ohmex.hudson.plugins.managedchoiceparameter.model.Configuration;
import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

public class ManagedChoiceParameterDefinition extends ParameterDefinition {
  private static final long serialVersionUID = -5551675218932877726L;
  private static final Logger LOGGER = Logger.getLogger(ManagedChoiceParameterDefinition.class.getName());
  private static final String PARAMETER_NAME = "name";

  @Getter private final String uuid = UUIDGenerator.generateUUID(16);
  @Getter @Setter private String configRepoURL;
  @Getter @Setter private String credentialsId;
  @Getter @Setter private String pipelinePath;
  @Getter private Configuration configuration;

  @DataBoundConstructor
  public ManagedChoiceParameterDefinition(String name, String description, String configRepoURL,
    String credentialsId, String pipelinePath, Configuration configuration)
  {
    super(name, description);
    this.configRepoURL = configRepoURL;
    this.credentialsId = credentialsId;
    this.pipelinePath = pipelinePath;
    this.configuration = configuration;
  }

  @JavaScriptMethod
  public String[] getProjects(Integer curProduct) {
    return configuration.getProjects(curProduct).toArray(new String[0]);
  }

  @JavaScriptMethod
  public String[] getEnvironments(Integer curProduct, Integer curProject) {
    return configuration.getEnvironments(curProduct, curProject).toArray(new String[0]);
  }

  @JavaScriptMethod
  public String getDetails(Integer curProduct, Integer curProject, Integer curEnvironment) {
    List<String> list = new ArrayList<>();
    list.add(configuration.get(curProduct).get(curProject).get(curEnvironment).getExt());
    Map<String, String> map = new HashMap<>();
    map.put("Credentials", "myCred");
    return new Gson().toJson(map);
  }

  private Map<String, String> resolve(Map<String, String> selectedValues) {
    Map<String, String> resolvedValues = new HashMap<>();

    Integer curProduct = 0;
    Integer curProject = 0;
    Integer curEnvironment = 0;
    String key = null;
    String value = null;

    for (Map.Entry<String,String> entry : selectedValues.entrySet()) {
      key = entry.getKey();
      value = entry.getValue();

      switch(key) {
        case "product":
          curProduct = Integer.parseInt(entry.getValue());
          value = configuration.get(curProduct).getName();
          break;
        case "project":
          curProject = Integer.parseInt(entry.getValue());
          value = configuration.get(curProduct).get(curProject).getName();
          break;
        case "environment":
          curEnvironment = Integer.parseInt(entry.getValue());
          value = configuration.get(curProduct).get(curProject).get(curEnvironment).getDesc();
          break;
        default:
      }
      resolvedValues.put("BS_" + key.toUpperCase(), value);
    }

    return resolvedValues;
  }

  public ManagedChoiceParameterValue createValue(Map<String, Object> jsonObject) {
    Map<String, String> selectedValues = new HashMap<>();

    // convert json object to map of strings to integers (values from parameter form)
    jsonObject.forEach((key, value) -> {
      // exclude parameter name
      if (!key.equals(PARAMETER_NAME) && value instanceof String && ((String) value).length() > 0) {
        try {
          // store new key combination in map
          selectedValues.put(key, (String)value);
        } catch (NumberFormatException exception) {
          LOGGER.log(Level.WARNING, "Invalid configuration index value sent from form", exception);
        }
      }
    });

    return new ManagedChoiceParameterValue(getName(), resolve(selectedValues));
  }

  @Override
  public ParameterValue createValue(StaplerRequest staplerRequest, JSONObject jsonObject) {
    return createValue(jsonObject);
  }

  @Override
  public ParameterValue createValue(StaplerRequest staplerRequest) {
    return getDefaultParameterValue();
  }

  @Override
  @Nullable
  public ParameterValue getDefaultParameterValue() {
    return new ManagedChoiceParameterValue(getName(), new HashMap<>());
  }

  @Symbol("managedChoice")
  @Extension
  public static class DescriptorImpl extends ParameterDescriptor {
    @Override
    public ParameterDefinition newInstance(@Nullable StaplerRequest req, @Nonnull JSONObject formData) throws FormException {
      String name;
      String description;
      String configRepoURL;
      String credentialsId;
      String pipelinePath;
      Configuration configuration = null;

      name = formData.getString("name");
      description = formData.getString("description");
      configRepoURL = formData.getString("configRepoURL");
      credentialsId = formData.getString("credentialsId");
      pipelinePath = formData.getString("pipelinePath");

      //TODO: Checkout repo and get configuration json
      Gson gson = new Gson();
      try {
        URL url = new URL("http://localhost/testing/organisation.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        configuration = gson.fromJson(reader, Configuration.class);
      } catch (IOException e) {
        e.printStackTrace();
      }

      return new ManagedChoiceParameterDefinition(name, description,configRepoURL, credentialsId, pipelinePath, configuration);
    }

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
