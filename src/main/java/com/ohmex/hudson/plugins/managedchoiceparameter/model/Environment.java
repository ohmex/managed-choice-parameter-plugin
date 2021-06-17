package com.ohmex.hudson.plugins.managedchoiceparameter.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class Environment {
  @NonNull @Getter @Setter private String ext;
  @NonNull @Getter @Setter private String desc;
}
