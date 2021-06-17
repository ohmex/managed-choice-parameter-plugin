package com.ohmex.hudson.plugins.managedchoiceparameter.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Product {
  @NonNull @Getter @Setter private String name;
  @NonNull @Getter @Setter private String desc;
  private List<Project> projects;

  public Project get(Integer curProject){
    return projects.get(curProject);
  }

  public List<String> getProjects() {
    List<String> list= new ArrayList<>();
    for (Project p : projects) {
      list.add(p.getName() + ": " + p.getDesc());
    }
    return list;
  }

  public List<String> getEnvironments(Integer curProject) {
    return projects.get(curProject).getEnvironments();
  }

  public String getExt(Integer curProject, Integer curEnv) {
    return projects.get(curProject).getExt(curEnv);
  }
}
