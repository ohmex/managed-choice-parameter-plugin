package com.ohmex.hudson.plugins.managedchoiceparameter.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Project {
  @NonNull @Getter @Setter private String name;
  @NonNull @Getter @Setter private String desc;
  private List<Environment> environments;

  public Environment get(Integer curEnvironment){
    return environments.get(curEnvironment);
  }

  public List<String> getEnvironments() {
    List<String> list= new ArrayList<>();
    for (Environment e : environments) {
      list.add(e.getDesc());
    }
    return list;
  }

  public String getExt(Integer curEnv) {
    return environments.get(curEnv).getExt();
  }
}
