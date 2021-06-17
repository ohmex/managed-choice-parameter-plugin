package com.ohmex.hudson.plugins.managedchoiceparameter.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Configuration {
  @NonNull @Getter @Setter private String cred;
  @NonNull @Getter @Setter private String url;
  @NonNull @Getter @Setter private String desc;
  private List<Product> products;

  public Product get(Integer curProduct){
    return products.get(curProduct);
  }

  public List<String> getProducts() {
    List<String> list= new ArrayList<>();
    for (Product p : products) {
      list.add(p.getName() + ": " + p.getDesc());
    }
    return list;
  }

  public List<String> getProjects(Integer curProduct) {
    return products.get(curProduct).getProjects();
  }

  public List<String> getEnvironments(Integer curProduct, Integer curProject) {
    return products.get(curProduct).getEnvironments(curProject);
  }

  public String getExt(Integer curProduct, Integer curProject, Integer curEnv) {
    return products.get(curProduct).getExt(curProject, curEnv);
  }
}
